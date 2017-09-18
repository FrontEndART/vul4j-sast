/*
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.consensus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.lang3.tuple.Pair;
import org.semux.Config;
import org.semux.consensus.SemuxBFT.Event.Type;
import org.semux.core.Account;
import org.semux.core.Block;
import org.semux.core.BlockHeader;
import org.semux.core.Blockchain;
import org.semux.core.Consensus;
import org.semux.core.Delegate;
import org.semux.core.PendingManager;
import org.semux.core.Sync;
import org.semux.core.Transaction;
import org.semux.core.TransactionExecutor;
import org.semux.core.TransactionResult;
import org.semux.core.state.AccountState;
import org.semux.core.state.DelegateState;
import org.semux.crypto.EdDSA;
import org.semux.crypto.EdDSA.Signature;
import org.semux.crypto.Hash;
import org.semux.crypto.Hex;
import org.semux.net.Channel;
import org.semux.net.ChannelManager;
import org.semux.net.msg.Message;
import org.semux.net.msg.MessageCode;
import org.semux.net.msg.ReasonCode;
import org.semux.net.msg.consensus.BFTNewHeightMessage;
import org.semux.net.msg.consensus.BFTNewViewMessage;
import org.semux.net.msg.consensus.BFTProposalMessage;
import org.semux.net.msg.consensus.BFTVoteMessage;
import org.semux.utils.ArrayUtil;
import org.semux.utils.Bytes;
import org.semux.utils.MerkleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemuxBFT implements Consensus {
    static final Logger logger = LoggerFactory.getLogger(SemuxBFT.class);

    private Blockchain chain;
    private ChannelManager channelMgr;
    private PendingManager pendingMgr;
    private Sync sync;

    private EdDSA coinbase;

    private AccountState accountState;
    private DelegateState delegateState;

    private Timer timer;
    private Broadcaster broadcaster;
    private BlockingQueue<Event> events = new LinkedBlockingQueue<>();

    private volatile Status status;
    private volatile State state;

    private Block prevBlock;

    private long height;
    private int view;
    private Proof proof;
    private Proposal proposal;
    private boolean finalized;

    private volatile List<String> validators;
    private volatile List<Channel> activeValidators;

    private VoteSet validateVotes;
    private VoteSet precommitVotes;
    private VoteSet commitVotes;

    private static SemuxBFT instance;

    /**
     * Get the singleton instance of consensus.
     * 
     * @return
     */
    public static synchronized SemuxBFT getInstance() {
        if (instance == null) {
            instance = new SemuxBFT();
        }

        return instance;
    }

    private SemuxBFT() {
    }

    @Override
    public void init(Blockchain chain, ChannelManager channelMgr, PendingManager pendingMgr, EdDSA coinbase) {
        this.chain = chain;
        this.channelMgr = channelMgr;
        this.pendingMgr = pendingMgr;
        this.sync = SemuxSync.getInstance();
        this.coinbase = coinbase;

        this.accountState = chain.getAccountState();
        this.delegateState = chain.getDeleteState();

        this.timer = new Timer();
        this.broadcaster = new Broadcaster();

        this.status = Status.STOPPED;
        this.state = State.NEW_HEIGHT;
    }

    /**
     * Pause the consensus, and do synchronization.
     * 
     */
    private void sync(long target) {
        if (status == Status.RUNNING) {
            // reset votes, timer, and events
            resetVotes();
            resetTimerAndEvents();

            // change status
            status = Status.SYNCING;

            // start syncing
            sync.start(target);

            // restore status if not stopped
            if (status != Status.STOPPED) {
                status = Status.RUNNING;

                // enter new height
                enterNewHeight();
            }
        }
    }

    /**
     * Main loop that processes all the BFT events.
     */
    private void eventLoop() {
        while (!Thread.currentThread().isInterrupted() && status != Status.STOPPED) {
            try {
                Event ev = events.take();
                if (status != Status.RUNNING) {
                    continue;
                }

                switch (ev.getType()) {
                case STOP:
                    return;
                case TIMEOUT:
                    onTimeout();
                    break;
                case NEW_HEIGHT:
                    onNewHeight(ev.getData());
                    break;
                case NEW_VIEW:
                    onNewView(ev.getData());
                    break;
                case PROPOSAL:
                    onProposal(ev.getData());
                    break;
                case VOTE:
                    onVote(ev.getData());
                    break;
                default:
                    break;
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                logger.warn("Unexpected exception in event loop", e);
            }
        }
    }

    @Override
    public void start() {
        if (status == Status.STOPPED) {
            status = Status.RUNNING;
            timer.start();
            broadcaster.start();
            logger.info("Consensus started");

            enterNewHeight();
            eventLoop();

            logger.info("Consensus stopped");
        }
    }

    @Override
    public void stop() {
        if (status != Status.STOPPED) {
            // interrupt sync
            if (status == Status.SYNCING) {
                sync.stop();
            }

            timer.stop();
            broadcaster.stop();

            status = Status.STOPPED;
            events.offer(new Event(Event.Type.STOP));
        }
    }

    @Override
    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    /**
     * Enter the NEW_HEIGHT state
     */
    protected void enterNewHeight() {
        state = State.NEW_HEIGHT;

        // update previous block
        prevBlock = chain.getLatestBlock();

        // update view state
        height = prevBlock.getNumber() + 1;
        view = 0;
        proof = new Proof(height, view);
        proposal = null;
        finalized = false;

        // update validators
        List<Delegate> list = delegateState.getValidators();
        List<String> peerIds = new ArrayList<>();
        list.forEach(d -> {
            peerIds.add(Hex.encode(d.getAddress()));
        });
        validators = peerIds;
        activeValidators = channelMgr.getActiveChannels(validators);

        // reset votes and events
        resetVotes();
        resetTimerAndEvents();

        logger.info("Entered new_height: height = {}, # validators = {}", height, validators.size());
        if (isValidator()) {
            timer.timeout(Config.BFT_NEW_HEIGHT_TIMEOUT);
        }

        // Broadcast NEW_HEIGHT messages to all peers.
        BFTNewHeightMessage msg = new BFTNewHeightMessage(height);
        for (Channel c : channelMgr.getActiveChannels()) {
            c.getMessageQueue().sendMessage(msg);
        }
    }

    /**
     * Enter the PROPOSE state
     */
    protected void enterPropose() {
        state = State.PROPOSE;
        timer.timeout(Config.BFT_PROPOSE_TIMEOUT);

        // update active peers
        activeValidators = channelMgr.getActiveChannels(validators);

        if (validateVotes.isRejected()) {
            view++;
            proof = new Proof(height, view, validateVotes.getRejections());

            proposal = null;
            resetVotes();

            // Broadcast NEW_VIEW messages to all active validators.
            BFTNewViewMessage msg = new BFTNewViewMessage(proof);
            for (Channel c : activeValidators) {
                c.getMessageQueue().sendMessage(msg);
            }
        }

        logger.info("Entered propose: height = {}, view = {}, primary = {}, # connected validators = 1 + {}", height,
                view, isPrimary(), activeValidators.size());

        if (isPrimary()) {
            if (proposal == null) {
                Block block = proposeBlock();
                proposal = new Proposal(proof, block);
                proposal.sign(coinbase);
            }

            logger.debug("Proposing: {}", proposal);
            broadcaster.broadcast(new BFTProposalMessage(proposal));
        }
    }

    /**
     * Enter the VALIDATE state
     */
    protected void enterValidate() {
        state = State.VALIDATE;
        timer.timeout(Config.BFT_VALIDATE_TIMEOUT);
        logger.info("Entered validate: proposal ready = {}", proposal != null);

        boolean valid = false;
        if (proposal != null) {
            if (proposal.isBlockValid() != null) {
                valid = proposal.isBlockValid();
            } else {
                valid = validateBlock(proposal.getBlock());
                proposal.setBlockValid(valid);
            }
        }
        Vote vote = valid ? Vote.newApprove(VoteType.VALIDATE, height, view, proposal.getBlock().getHash())
                : Vote.newReject(VoteType.VALIDATE, height, view);
        vote.sign(coinbase);

        // always broadcast vote directly.
        validateVotes.addVote(vote);
        broadcaster.broadcast(new BFTVoteMessage(vote));
    }

    /**
     * Enter the PRE_COMMIT state
     */
    protected void enterPreCommit() {
        state = State.PRE_COMMIT;
        timer.timeout(Config.BFT_PRE_COMMIT_TIMEOUT);
        logger.info("Entered pre_commit: validate votes = {}", validateVotes);

        Vote vote = null;
        if (proposal != null && validateVotes.isApproved()) {
            // vote APPROVE if everything ok
            vote = Vote.newApprove(VoteType.PRECOMMIT, height, view, proposal.getBlock().getHash());
            vote.sign(coinbase);

            // always broadcast vote directly.
            precommitVotes.addVote(vote);
            broadcaster.broadcast(new BFTVoteMessage(vote));
        }
    }

    /**
     * Enter the COMMIT state
     */
    protected void enterCommit() {
        state = State.COMMIT;
        timer.timeout(Config.BFT_COMMIT_TIMEOUT);
        logger.info("Entered commit: pre_commit votes = {}", precommitVotes);

        if (proposal != null) {
            // create a COMMIT vote
            Vote vote = Vote.newApprove(VoteType.COMMIT, height, view, proposal.getBlock().getHash());
            vote.sign(coinbase);

            // always broadcast vote directly.
            commitVotes.addVote(vote);
            broadcaster.broadcast(new BFTVoteMessage(vote));
        }
    }

    /**
     * Enter the FINALIZE state
     */
    protected void enterFinalize() {
        state = State.FINALIZE;
        timer.timeout(Config.BFT_FINALIZE_TIMEOUT);
        logger.info("Entered finalize: pre_commit votes = {}, commit votes = {}, block ready = {}", precommitVotes,
                commitVotes, proposal != null);

        finalized = true;

        if (proposal != null && precommitVotes.isApproved()) {
            // [1] create a block
            Block block = proposal.getBlock();

            // [2] update view and votes
            List<Signature> votes = new ArrayList<>();
            for (Vote vote : precommitVotes.getApprovals()) {
                votes.add(vote.getSignature());
            }
            block.setView(view);
            block.setVotes(votes);

            // [3] add the block to chain
            logger.info(block.toString());
            applyBlock(block);
        } else {
            sync(height + 1);
        }
    }

    protected void onNewHeight(long h) {
        logger.trace("On new_height: {}", h);

        if (h > height && activeValidators != null) {
            long lastestBlockNum = chain.getLatestBlockNumber();

            int count = 0;
            for (Channel c : activeValidators) {
                if (c.isActive()) {
                    count += c.getRemotePeer().getLatestBlockNumber() > lastestBlockNum ? 1 : 0;
                }
            }

            if (count >= (int) Math.ceil(activeValidators.size() * 2.0 / 3.0)) {
                sync(h);
            }
        }
    }

    protected void onNewView(Proof p) {
        logger.trace("On new_view: {}", p);

        if (p.getHeight() == height && p.getView() > view) {

            // check proof-of-unlock
            VoteSet vs = new VoteSet(p.getHeight(), p.getView() - 1, validators);
            vs.addVotes(p.getVotes());
            if (!vs.isRejected()) {
                return;
            }

            // switch view
            view = p.getView();
            proof = p;
            proposal = null;
            resetVotes();
            resetTimerAndEvents();

            // jump into propose
            enterPropose();
        }
    }

    protected void onProposal(Proposal p) {
        logger.trace("On proposal: {}", p);

        if (p.getHeight() == height //
                && (p.getView() > view || p.getView() == view && proposal == null) //
                && isFromValidator(p.getSignature()) //
                && isPrimary(p.getView(), p.getSignature().getPublicKey())) {

            // check proof-of-unlock
            if (p.getView() != 0) {
                VoteSet vs = new VoteSet(p.getHeight(), p.getView() - 1, validators);
                vs.addVotes(p.getProof().getVotes());
                if (!vs.isRejected()) {
                    return;
                }
            } else if (!p.getProof().getVotes().isEmpty()) {
                return;
            }
            logger.trace("Proposal accepted: height = {}, view = {}", p.getHeight(), p.getView());

            // forward proposal
            BFTProposalMessage msg = new BFTProposalMessage(p);
            broadcaster.broadcast(msg);

            if (view == p.getView() && state == State.PROPOSE) {
                proposal = p;
            } else {
                // change height and view
                height = p.getHeight();
                view = p.getView();
                proof = p.getProof();
                proposal = p;
                resetVotes();
                resetTimerAndEvents();

                // jump into propose
                enterPropose();
            }
        }
    }

    protected void onVote(Vote v) {
        logger.trace("On vote: {}", v);

        if (v.getHeight() == height //
                && v.getView() == view //
                && isFromValidator(v.getSignature()) //
                && v.validate()) {
            boolean added = false;

            switch (v.getType()) {
            case VALIDATE:
                added = validateVotes.addVote(v);
                break;
            case PRECOMMIT:
                added = precommitVotes.addVote(v);
                break;
            case COMMIT:
                added = commitVotes.addVote(v);
                if (commitVotes.isApproved()) {
                    // skip COMMIT state time out if +2/3 commit votes
                    if (!finalized) {
                        enterFinalize();
                    }
                }
                break;
            }

            if (added) {
                BFTVoteMessage msg = new BFTVoteMessage(v);
                broadcaster.broadcast(msg);
            }
        }
    }

    /**
     * Timeout handler
     */
    protected void onTimeout() {
        switch (state) {
        case NEW_HEIGHT:
            enterPropose();
            break;
        case PROPOSE:
            enterValidate();
            break;
        case VALIDATE:
            enterPreCommit();
            break;
        case PRE_COMMIT:
            if (precommitVotes.isApproved()) {
                enterCommit();
            } else {
                enterPropose();
            }
            break;
        case COMMIT:
            if (!finalized) {
                enterFinalize();
            }
            break;
        case FINALIZE:
            enterNewHeight();
            break;
        }
    }

    @Override
    public boolean onMessage(Channel channel, Message msg) {
        // only process BFT_NEW_HEIGHT message when not running
        if (!isRunning() && msg.getCode() != MessageCode.BFT_NEW_HEIGHT) {
            return false;
        }

        switch (msg.getCode()) {
        case BFT_NEW_HEIGHT: {
            BFTNewHeightMessage m = (BFTNewHeightMessage) msg;

            // update peer height state
            channel.getRemotePeer().setLatestBlockNumber(m.getHeight() - 1);

            if (m.getHeight() > height) {
                events.add(new Event(Event.Type.NEW_HEIGHT, m.getHeight()));
            }
            return true;
        }
        case BFT_NEW_VIEW: {
            BFTNewViewMessage m = (BFTNewViewMessage) msg;

            // update peer height state
            channel.getRemotePeer().setLatestBlockNumber(m.getHeight() - 1);

            if (m.getHeight() > height) {
                events.add(new Event(Event.Type.NEW_HEIGHT, m.getHeight()));
            } else if (m.getHeight() == height) {
                events.add(new Event(Event.Type.NEW_VIEW, m.getProof()));
            }
            return true;
        }
        case BFT_PROPOSAL: {
            BFTProposalMessage m = (BFTProposalMessage) msg;
            Proposal proposal = m.getProposal();

            if (proposal.getHeight() == height) {
                if (proposal.validate()) {
                    events.add(new Event(Event.Type.PROPOSAL, m.getProposal()));
                } else {
                    logger.debug("Invalid proposal from {}", channel.getRemotePeer().getPeerId());
                    channel.getMessageQueue().disconnect(ReasonCode.CONSENSUS_ERROR);
                }
            }
            return true;
        }
        case BFT_VOTE: {
            BFTVoteMessage m = (BFTVoteMessage) msg;
            Vote vote = m.getVote();

            if (vote.getHeight() == height) {
                if (vote.validate()) {
                    events.add(new Event(Event.Type.VOTE, vote));
                } else {
                    logger.debug("Invalid vote from {}", channel.getRemotePeer().getPeerId());
                    channel.getMessageQueue().disconnect(ReasonCode.CONSENSUS_ERROR);
                }
            }
            return true;
        }
        default:
            return false;
        }
    }

    /**
     * Check if this node is a validator.
     * 
     * @return
     */
    protected boolean isValidator() {
        return validators.contains(coinbase.toAddressString());
    }

    /**
     * Check if this node is the primary validator for this view.
     * 
     * @return
     */
    protected boolean isPrimary() {
        return isPrimary(view, coinbase.getPublicKey());
    }

    /**
     * Check if a node is the primary for the specified view.
     * 
     * @param view
     *            a specific view
     * @param pubKey
     *            public key
     * @return
     */
    protected boolean isPrimary(int view, byte[] pubKey) {
        String peerId = Hex.encode(Hash.h160(pubKey));

        int p = Bytes.toInt(prevBlock.getHash()) + view;
        p = p % validators.size();
        p = (p >= 0) ? p : p + validators.size();

        return validators.get(p).equals(peerId);
    }

    /**
     * Check if the signature is from one of the validators.
     * 
     * @param sig
     * @return
     */
    protected boolean isFromValidator(Signature sig) {
        return validators.contains(Hex.encode(Hash.h160(sig.getPublicKey())));
    }

    /**
     * Reset all vote sets. This should be invoked whenever height or view changes.
     */
    protected void resetVotes() {
        validateVotes = new VoteSet(height, view, validators);
        precommitVotes = new VoteSet(height, view, validators);
        commitVotes = new VoteSet(height, view, validators);
    }

    /**
     * Reset timer and events.
     */
    protected void resetTimerAndEvents() {
        timer.reset();
    }

    /**
     * Create a block for BFT proposal.
     * 
     * @return the proposed block
     */
    protected Block proposeBlock() {
        long t1 = System.currentTimeMillis();

        // fetch pending transactions
        Pair<List<Transaction>, List<TransactionResult>> pending = pendingMgr
                .getTransactionsAndResults(Config.MAX_BLOCK_SIZE);

        // compute roots
        byte[] transactionsRoot = MerkleUtil.computeTransactionsRoot(pending.getLeft());
        byte[] resultsRoot = MerkleUtil.computeResultsRoot(pending.getRight());
        byte[] stateRoot = Hash.EMPTY_H256;

        // construct block
        long number = height;
        byte[] prevHash = chain.getBlockHash(height - 1);
        long timestamp = System.currentTimeMillis();
        byte[] data = {};
        BlockHeader header = new BlockHeader(number, coinbase.toAddress(), prevHash, timestamp, transactionsRoot,
                resultsRoot, stateRoot, data);
        Block block = new Block(header.sign(coinbase), pending.getLeft(), pending.getRight());

        long t2 = System.currentTimeMillis();
        logger.debug("Block creation: # txs = {}, time = {} ms", pending.getLeft().size(), t2 - t1);

        return block;
    }

    /**
     * Check if a block is valid.
     * 
     * NOTOE: this method assume the block data and signature is valid. If not, use
     * {@link Block#validate()} to check.
     * 
     * @param block
     * @return
     */
    protected boolean validateBlock(Block block) {
        long t1 = System.currentTimeMillis();
        // [1] check block integrity and signature
        if (!block.validate()) {
            logger.debug("Invalid block/transaction format");
            return false;
        }

        // [2] check number and prevHash
        Block latest = chain.getLatestBlock();
        if (block.getNumber() != latest.getNumber() + 1 || !Arrays.equals(block.getPrevHash(), latest.getHash())) {
            logger.debug("Invalid block number or prevHash");
            return false;
        }

        AccountState as = accountState.track();
        DelegateState ds = delegateState.track();
        TransactionExecutor exec = new TransactionExecutor();

        // [3] check transactions
        List<Transaction> txs = block.getTransactions();
        List<TransactionResult> results = exec.execute(txs, as, ds);
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).isValid()) {
                logger.debug("Invalid transaction #{}", i);
                return false;
            }
        }

        long t2 = System.currentTimeMillis();
        logger.debug("Block validation: # txs = {}, time = {} ms", txs.size(), t2 - t1);

        return true;
    }

    /**
     * Apply a block to the chain.
     * 
     * @param block
     */
    protected void applyBlock(Block block) {
        if (block.getNumber() != chain.getLatestBlockNumber() + 1) {
            throw new RuntimeException("Applying wrong block: number = " + block.getNumber());
        }

        AccountState as = chain.getAccountState().track();
        DelegateState ds = chain.getDeleteState().track();

        // [1] execute all transactions
        TransactionExecutor exec = new TransactionExecutor();
        List<TransactionResult> results = exec.execute(block.getTransactions(), as, ds);
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).isValid()) {
                Transaction tx = block.getTransactions().get(i);
                logger.debug("Invalid transaction: type = {}, hash = {}", tx.getType(), Hex.encode(tx.getHash()));
                return;
            }
        }

        // [2] apply block reward and tx fees
        long reward = Config.getBlockReward(block.getNumber());
        for (Transaction tx : block.getTransactions()) {
            reward += tx.getFee();
        }
        if (reward > 0) {
            Account acc = as.getAccount(block.getCoinbase());
            acc.setBalance(acc.getBalance() + reward);
        }

        // [3] commit state change
        as.commit();
        ds.commit();

        WriteLock lock = Config.STATE_LOCK.writeLock();
        lock.lock();
        try {
            // [4] flush state to disk
            chain.getAccountState().commit();
            chain.getDeleteState().commit();

            // [5] add block to chain
            chain.addBlock(block);
        } finally {
            lock.unlock();
        }
    }

    public enum State {
        NEW_HEIGHT, PROPOSE, VALIDATE, PRE_COMMIT, COMMIT, FINALIZE
    }

    public class Timer implements Runnable {
        private long timeout;

        private Thread t;

        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    if (timeout != 0 && timeout < System.currentTimeMillis()) {
                        events.add(new Event(Type.TIMEOUT));
                        timeout = 0;
                        continue;
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        public synchronized void start() {
            if (t == null) {
                t = new Thread(this, "cons-timer");
                t.start();
            }
        }

        public synchronized void stop() {
            if (t != null) {
                try {
                    t.interrupt();
                    t.join(10000);
                } catch (InterruptedException e) {
                    logger.warn("Failed to stop consensus timer");
                }
                t = null;
            }
        }

        public synchronized void timeout(long miliseconds) {
            if (miliseconds < 0) {
                throw new IllegalArgumentException("Timeout can not be negative");
            }
            timeout = System.currentTimeMillis() + miliseconds;
        }

        public synchronized void reset() {
            timeout = 0;
        }
    }

    public class Broadcaster implements Runnable {
        private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

        private Thread t;

        @Override
        public void run() {
            while (true) {
                try {
                    Message msg = queue.take();

                    // thread-safety via volatile
                    List<Channel> channels = activeValidators;
                    if (channels != null) {
                        int[] indexes = ArrayUtil.permutation(channels.size());
                        for (int i = 0; i < indexes.length && i < Config.NET_RELAY_REDUNDANCY; i++) {
                            Channel c = channels.get(indexes[i]);
                            if (c.isActive()) {
                                c.getMessageQueue().sendMessage(msg);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public synchronized void start() {
            if (t == null) {
                t = new Thread(this, "cons-broadcaster");
                t.start();
            }
        }

        public synchronized void stop() {
            if (t != null) {
                try {
                    t.interrupt();
                    t.join();
                } catch (InterruptedException e) {
                    logger.error("Failed to stop consensus broadcaster");
                }
                t = null;
            }
        }

        public void broadcast(Message msg) {
            queue.offer(msg);
        }
    }

    public static class Event {
        public enum Type {
            /**
             * Stop signal
             */
            STOP,

            /**
             * Received a timeout signal.
             */
            TIMEOUT,

            /**
             * Received a new height message.
             */
            NEW_HEIGHT,

            /**
             * Received a new view message.
             */
            NEW_VIEW,

            /**
             * Received a proposal message.
             */
            PROPOSAL,

            /**
             * Received a vote message.
             */
            VOTE
        }

        private Type type;
        private Object data;

        public Event(Type type) {
            this(type, null);
        }

        public Event(Type type, Object data) {
            this.type = type;
            this.data = data;
        }

        public Type getType() {
            return type;
        }

        @SuppressWarnings("unchecked")
        public <T> T getData() {
            return (T) data;
        }

        @Override
        public String toString() {
            return "Event [type=" + type + ", data=" + data + "]";
        }
    }

    public enum Status {
        STOPPED, RUNNING, SYNCING
    }
}