package net.onrc.onos.ofcontroller.flowprogrammer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IOFSwitch;
import net.onrc.onos.graph.GraphDBOperation;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IFlowEntry;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.ISwitchObject;
import net.onrc.onos.ofcontroller.util.Dpid;
import net.onrc.onos.ofcontroller.util.FlowEntryId;

/**
 * FlowSynchronizer is an implementation of FlowSyncService.
 * In addition to IFlowSyncService, FlowSynchronizer periodically reads flow
 * tables from switches and compare them with GraphDB to drop unnecessary
 * flows and/or to install missing flows.
 * @author Brian
 *
 */
public class FlowSynchronizer implements IFlowSyncService {

    private static Logger log = LoggerFactory.getLogger(FlowSynchronizer.class);

    private GraphDBOperation dbHandler;
    protected IFlowPusherService pusher;
    private Map<IOFSwitch, Thread> switchThreads; 

    public FlowSynchronizer() {
	dbHandler = new GraphDBOperation("");
	switchThreads = new HashMap<IOFSwitch, Thread>();
    }

    @Override
    public void synchronize(IOFSwitch sw) {
	Synchronizer sync = new Synchronizer(sw);
	Thread t = new Thread(sync);
	switchThreads.put(sw, t);
	t.start();
    }
    
    @Override
    public void interrupt(IOFSwitch sw) {
	Thread t = switchThreads.remove(sw);
	if(t != null) {
	    t.interrupt();
	}	
    }

    /**
     * Initialize Synchronizer.
     * @param pusherService FlowPusherService used for sending messages.
     */
    public void init(IFlowPusherService pusherService) {
	pusher = pusherService;
    }

    /**
     * Synchronizer represents main thread of synchronization.
     * @author Brian
     *
     */
	protected class Synchronizer implements Runnable {
	IOFSwitch sw;
	ISwitchObject swObj;

	public Synchronizer(IOFSwitch sw) {
	    this.sw = sw;
	    Dpid dpid = new Dpid(sw.getId());
	    this.swObj = dbHandler.searchSwitch(dpid.toString());
	}

	@Override
	public void run() {
	    // TODO: stop adding other flow entries while synchronizing
	    //pusher.suspend(sw);
	    Set<FlowEntryWrapper> graphEntries = getFlowEntriesFromGraph();
	    Set<FlowEntryWrapper> switchEntries = getFlowEntriesFromSwitch();
	    compare(graphEntries, switchEntries);
	    //pusher.resume(sw);
	}

	/**
	 * Compare flows entries in GraphDB and switch to pick up necessary
	 * messages.
	 * After picking up, picked messages are added to FlowPusher.
	 * @param graphEntries Flow entries in GraphDB.
	 * @param switchEntries Flow entries in switch.
	 */
	private void compare(Set<FlowEntryWrapper> graphEntries, Set<FlowEntryWrapper> switchEntries) {
	    int added = 0, removed = 0, skipped = 0;
	    for(FlowEntryWrapper entry : switchEntries) {
		if(graphEntries.contains(entry)) {
		    graphEntries.remove(entry);
		    skipped++;
		}
		else {
		    // remove flow entry from the switch
		    entry.removeFromSwitch(sw);
		    removed++;
		}
	    }
	    for(FlowEntryWrapper entry : graphEntries) {
		// add flow entry to switch
		entry.addToSwitch(sw);
		added++;
	    }	  
	    log.debug("Flow entries added "+ added + ", " +
		      "Flow entries removed "+ removed + ", " +
		      "Flow entries skipped " + skipped);
	}

	/**
	 * Read GraphDB to get FlowEntries associated with a switch.
	 * @return set of FlowEntries
	 */
	private Set<FlowEntryWrapper> getFlowEntriesFromGraph() {
	    Set<FlowEntryWrapper> entries = new HashSet<FlowEntryWrapper>();
	    for(IFlowEntry entry : swObj.getFlowEntries()) {
		FlowEntryWrapper fe = new FlowEntryWrapper(entry);
		entries.add(fe);
	    }
	    return entries;	    
	}

	/**
	 * Read flow table from switch and derive FlowEntries from table.
	 * @return set of FlowEntries
	 */
	private Set<FlowEntryWrapper> getFlowEntriesFromSwitch() {

	    int lengthU = 0;
	    OFMatch match = new OFMatch();
	    match.setWildcards(OFMatch.OFPFW_ALL);

	    OFFlowStatisticsRequest stat = new OFFlowStatisticsRequest();
	    stat.setOutPort((short) 0xffff); //TODO: OFPort.OFPP_NONE
	    stat.setTableId((byte) 0xff); // TODO: fix this with enum (ALL TABLES)
	    stat.setMatch(match);
	    List<OFStatistics> stats = new ArrayList<OFStatistics>();
	    stats.add(stat);
	    lengthU += stat.getLength();

	    OFStatisticsRequest req = new OFStatisticsRequest();
	    req.setStatisticType(OFStatisticsType.FLOW);
	    req.setStatistics(stats);
	    lengthU += req.getLengthU();
	    req.setLengthU(lengthU);

	    List<OFStatistics> entries = null;
	    try {
		Future<List<OFStatistics>> dfuture = sw.getStatistics(req);
		entries = dfuture.get();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    Set<FlowEntryWrapper> results = new HashSet<FlowEntryWrapper>();
	    for(OFStatistics result : entries){
		OFFlowStatisticsReply entry = (OFFlowStatisticsReply) result;
		FlowEntryWrapper fe = new FlowEntryWrapper(entry);
		results.add(fe);
	    }
	    return results;
	}

    }

    /**
     * FlowEntryWrapper represents abstract FlowEntry which is embodied
     * by FlowEntryId (from GraphDB) or OFFlowStatisticsReply (from switch).
     * @author Brian
     *
     */
    class FlowEntryWrapper {
	FlowEntryId flowEntryId;
	OFFlowStatisticsReply statisticsReply;

	public FlowEntryWrapper(IFlowEntry entry) {
	    flowEntryId = new FlowEntryId(entry.getFlowEntryId());
	}

	public FlowEntryWrapper(OFFlowStatisticsReply entry) {
	    flowEntryId = new FlowEntryId(entry.getCookie());
	    statisticsReply = entry;
	}

	/**
	 * Install this FlowEntry to a switch via FlowPusher.
	 * @param sw Switch to which flow will be installed.
	 */
	public void addToSwitch(IOFSwitch sw) {
	    if (statisticsReply != null) {
		log.error("Error adding existing flow entry {} to sw {}", 
			  statisticsReply.getCookie(), sw.getId());
		return;
	    }

	    // Get the Flow Entry state from the Network Graph
	    IFlowEntry iFlowEntry = null;
	    try {
		iFlowEntry = dbHandler.searchFlowEntry(flowEntryId);
	    } catch (Exception e) {
		log.error("Error finding flow entry {} in Network Graph",
			  flowEntryId);
		return;
	    }
	    if (iFlowEntry == null) {
		log.error("Cannot add flow entry {} to sw {} : flow entry not found",
			  flowEntryId, sw.getId());
		return;
	    }

	    pusher.add(sw, iFlowEntry.getFlow(), iFlowEntry);
	}
	
	/**
	 * Remove this FlowEntry from a switch via FlowPusher.
	 * @param sw Switch from which flow will be removed.
	 */
	public void removeFromSwitch(IOFSwitch sw) {
	    if (statisticsReply == null) {
		log.error("Error removing non-existent flow entry {} from sw {}", 
			  flowEntryId, sw.getId());
		return;
	    }

	    // Convert Statistics Reply to Flow Mod, then write it
	    OFFlowMod fm = new OFFlowMod();
	    fm.setCookie(statisticsReply.getCookie());
	    fm.setCommand(OFFlowMod.OFPFC_DELETE_STRICT);
	    fm.setLengthU(OFFlowMod.MINIMUM_LENGTH);
	    fm.setMatch(statisticsReply.getMatch());
	    fm.setPriority(statisticsReply.getPriority());
	    fm.setOutPort(OFPort.OFPP_NONE);

	    pusher.add(sw, fm);
	}

	/**
	 * Return the hash code of the Flow Entry ID
	 */
	@Override
	public int hashCode() {
	    return flowEntryId.hashCode();
	}

	/**
	 * Returns true of the object is another Flow Entry ID with 
	 * the same value; otherwise, returns false.
	 * 
	 * @param Object to compare
	 * @return true if the object has the same Flow Entry ID.
	 */
	@Override
	public boolean equals(Object obj){
	    if(obj.getClass() == this.getClass()) {
		FlowEntryWrapper entry = (FlowEntryWrapper) obj;
		// TODO: we need to actually compare the match + actions
		return this.flowEntryId.equals(entry.flowEntryId);
	    }
	    return false;
	}

	@Override
	public String toString() {
	    return flowEntryId.toString();
	}
    }
}
