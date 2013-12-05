package net.onrc.onos.ofcontroller.flowmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.onrc.onos.datagrid.IDatagridService;
import net.onrc.onos.graph.GraphDBOperation;
import net.onrc.onos.ofcontroller.core.INetMapStorage;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IFlowEntry;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IFlowPath;
import net.onrc.onos.ofcontroller.floodlightlistener.INetworkGraphService;
import net.onrc.onos.ofcontroller.flowmanager.web.FlowWebRoutable;
import net.onrc.onos.ofcontroller.flowprogrammer.IFlowPusherService;
import net.onrc.onos.ofcontroller.topology.Topology;
import net.onrc.onos.ofcontroller.util.*;

import org.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flow Manager class for handling the network flows.
 */
public class FlowManager implements IFloodlightModule, IFlowService, INetMapStorage {
    protected GraphDBOperation dbHandlerApi;
    protected GraphDBOperation dbHandlerInner;

    protected volatile IFloodlightProviderService floodlightProvider;
    protected volatile IDatagridService datagridService;
    protected IRestApiService restApi;
    protected FloodlightModuleContext context;
    protected FlowEventHandler flowEventHandler;

    protected IFlowPusherService pusher;
    
    // Flow Entry ID generation state
    private static Random randomGenerator = new Random();
    private static int nextFlowEntryIdPrefix = 0;
    private static int nextFlowEntryIdSuffix = 0;

    /** The logger. */
    private final static Logger log = LoggerFactory.getLogger(FlowManager.class);

    // The queue to write Flow Entries to the database
    private BlockingQueue<FlowPath> flowPathsToDatabaseQueue =
	new LinkedBlockingQueue<FlowPath>();
    FlowDatabaseWriter flowDatabaseWriter;

    /**
     * Initialize the Flow Manager.
     *
     * @param conf the Graph Database configuration string.
     */
    @Override
    public void init(String conf) {
    	dbHandlerApi = new GraphDBOperation(conf);
    	dbHandlerInner = new GraphDBOperation(conf);
    }

    /**
     * Shutdown the Flow Manager operation.
     */
    public void finalize() {
    	close();
    }

    /**
     * Shutdown the Flow Manager operation.
     */
    @Override
    public void close() {
	datagridService.deregisterFlowEventHandlerService(flowEventHandler);
    	dbHandlerApi.close();
    	dbHandlerInner.close();
    }

    /**
     * Get the collection of offered module services.
     *
     * @return the collection of offered module services.
     */
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = 
            new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFlowService.class);
        return l;
    }

    /**
     * Get the collection of implemented services.
     *
     * @return the collection of implemented services.
     */
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> 
			       getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
	    IFloodlightService> m =
	    new HashMap<Class<? extends IFloodlightService>,
	    IFloodlightService>();
        m.put(IFlowService.class, this);
        return m;
    }

    /**
     * Get the collection of modules this module depends on.
     *
     * @return the collection of modules this module depends on.
     */
    @Override
    public Collection<Class<? extends IFloodlightService>> 
				      getModuleDependencies() {
	Collection<Class<? extends IFloodlightService>> l =
	    new ArrayList<Class<? extends IFloodlightService>>();
	l.add(IFloodlightProviderService.class);
	l.add(INetworkGraphService.class);
	l.add(IDatagridService.class);
	l.add(IRestApiService.class);
        return l;
    }

    /**
     * Initialize the module.
     *
     * @param context the module context to use for the initialization.
     */
    @Override
    public void init(FloodlightModuleContext context)
	throws FloodlightModuleException {
	this.context = context;
	floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	datagridService = context.getServiceImpl(IDatagridService.class);
	restApi = context.getServiceImpl(IRestApiService.class);
	pusher = context.getServiceImpl(IFlowPusherService.class);

	this.init("");
    }

    /**
     * Get the next Flow Entry ID to use.
     *
     * @return the next Flow Entry ID to use.
     */
    @Override
    public synchronized long getNextFlowEntryId() {
	//
	// Generate the next Flow Entry ID.
	// NOTE: For now, the higher 32 bits are random, and
	// the lower 32 bits are sequential.
	// In the future, we need a better allocation mechanism.
	//
	if ((nextFlowEntryIdSuffix & 0xffffffffL) == 0xffffffffL) {
	    nextFlowEntryIdPrefix = randomGenerator.nextInt();
	    nextFlowEntryIdSuffix = 0;
	} else {
	    nextFlowEntryIdSuffix++;
	}
	long result = (long)nextFlowEntryIdPrefix << 32;
	result = result | (0xffffffffL & nextFlowEntryIdSuffix);
	return result;
    }

    /**
     * Startup module operation.
     *
     * @param context the module context to use for the startup.
     */
    @Override
    public void startUp(FloodlightModuleContext context) {
	restApi.addRestletRoutable(new FlowWebRoutable());

	// Initialize the Flow Entry ID generator
	nextFlowEntryIdPrefix = randomGenerator.nextInt();

	//
	// The thread to write to the database
	//
	flowDatabaseWriter = new FlowDatabaseWriter(this,
						flowPathsToDatabaseQueue);
	flowDatabaseWriter.start();

	//
	// The Flow Event Handler thread:
	//  - create
	//  - register with the Datagrid Service
	//  - startup
	//
	flowEventHandler = new FlowEventHandler(this, datagridService);
	datagridService.registerFlowEventHandlerService(flowEventHandler);
	flowEventHandler.start();
    }

    /**
     * Add a flow.
     *
     * @param flowPath the Flow Path to install.
     * @param flowId the return-by-reference Flow ID as assigned internally.
     * @return true on success, otherwise false.
     */
    @Override
    public boolean addFlow(FlowPath flowPath, FlowId flowId) {
	//
	// NOTE: We need to explicitly initialize some of the state,
	// in case the application didn't do it.
	//
	for (FlowEntry flowEntry : flowPath.flowEntries()) {
	    if (flowEntry.flowEntrySwitchState() ==
		FlowEntrySwitchState.FE_SWITCH_UNKNOWN) {
		flowEntry.setFlowEntrySwitchState(FlowEntrySwitchState.FE_SWITCH_NOT_UPDATED);
	    }
	    if (! flowEntry.isValidFlowId())
		flowEntry.setFlowId(new FlowId(flowPath.flowId().value()));
	}

	if (FlowDatabaseOperation.addFlow(dbHandlerApi, flowPath, flowId)) {
	    datagridService.notificationSendFlowAdded(flowPath);
	    return true;
	}
	return false;
    }

    /**
     * Delete all previously added flows.
     *
     * @return true on success, otherwise false.
     */
    @Override
    public boolean deleteAllFlows() {
	if (FlowDatabaseOperation.deleteAllFlows(dbHandlerApi)) {
	    datagridService.notificationSendAllFlowsRemoved();
	    return true;
	}
	return false;
    }

    /**
     * Delete a previously added flow.
     *
     * @param flowId the Flow ID of the flow to delete.
     * @return true on success, otherwise false.
     */
    @Override
    public boolean deleteFlow(FlowId flowId) {
	if (FlowDatabaseOperation.deleteFlow(dbHandlerApi, flowId)) {
	    datagridService.notificationSendFlowRemoved(flowId);
	    return true;
	}
	return false;
    }

    /**
     * Get a previously added flow.
     *
     * @param flowId the Flow ID of the flow to get.
     * @return the Flow Path if found, otherwise null.
     */
    @Override
    public FlowPath getFlow(FlowId flowId) {
	return FlowDatabaseOperation.getFlow(dbHandlerApi, flowId);
    }

    /**
     * Get all installed flows by all installers.
     *
     * @return the Flow Paths if found, otherwise null.
     */
    @Override
    public ArrayList<FlowPath> getAllFlows() {
	return FlowDatabaseOperation.getAllFlows(dbHandlerApi);
    }

    /**
     * Get summary of all installed flows by all installers in a given range.
     *
     * @param flowId the Flow ID of the first flow in the flow range to get.
     * @param maxFlows the maximum number of flows to be returned.
     * @return the Flow Paths if found, otherwise null.
     */
    @Override
    public ArrayList<FlowPath> getAllFlowsSummary(FlowId flowId,
						  int maxFlows) {
	return FlowDatabaseOperation.getAllFlowsSummary(dbHandlerApi, flowId,
							maxFlows);
    }
    
    /**
     * Add and maintain a shortest-path flow.
     *
     * NOTE: The Flow Path argument does NOT contain flow entries.
     *
     * @param flowPath the Flow Path with the endpoints and the match
     * conditions to install.
     * @return the added shortest-path flow on success, otherwise null.
     */
    @Override
    public FlowPath addAndMaintainShortestPathFlow(FlowPath flowPath) {
	//
	// Don't do the shortest path computation here.
	// Instead, let the Flow reconciliation thread take care of it.
	//

	FlowId flowId = new FlowId();
	if (! addFlow(flowPath, flowId))
	    return null;

	return (flowPath);
    }

    /**
     * Get the collection of my switches.
     *
     * @return the collection of my switches.
     */
    public Map<Long, IOFSwitch> getMySwitches() {
	return floodlightProvider.getSwitches();
    }

    /**
     * Get the network topology.
     *
     * @return the network topology.
     */
    public Topology getTopology() {
	return flowEventHandler.getTopology();
    }

    /**
     * Inform the Flow Manager that a Flow Entry on switch expired.
     *
     * @param sw the switch the Flow Entry expired on.
     * @param flowEntryId the Flow Entry ID of the expired Flow Entry.
     */
    public void flowEntryOnSwitchExpired(IOFSwitch sw, FlowEntryId flowEntryId) {
	// TODO: Not implemented yet
    }

    /**
     * Inform the Flow Manager that a collection of Flow Entries have been
     * pushed to a switch.
     *
     * @param entries the collection of <IOFSwitch, FlowEntry> pairs
     * that have been pushed.
     */
    public void flowEntriesPushedToSwitch(
		Collection<Pair<IOFSwitch, FlowEntry>> entries) {

	//
	// Process all entries
	//
	for (Pair<IOFSwitch, FlowEntry> entry : entries) {
	    IOFSwitch sw = entry.first;
	    FlowEntry flowEntry = entry.second;

	    //
	    // Mark the Flow Entry that it has been pushed to the switch
	    //
	    flowEntry.setFlowEntrySwitchState(FlowEntrySwitchState.FE_SWITCH_UPDATED);

	    //
	    // Write the Flow Entry to the Datagrid
	    //
	    switch (flowEntry.flowEntryUserState()) {
	    case FE_USER_ADD:
		datagridService.notificationSendFlowEntryAdded(flowEntry);
		break;
	    case FE_USER_MODIFY:
		datagridService.notificationSendFlowEntryUpdated(flowEntry);
		break;
	    case FE_USER_DELETE:
		datagridService.notificationSendFlowEntryRemoved(flowEntry.flowEntryId());
		break;
	    }
	}
    }

    /**
     * Push modified Flow-related state as appropriate.
     *
     * @param modifiedFlowPaths the collection of modified Flow Paths.
     * @param modifiedFlowEntries the collection of modified Flow Entries.
     */
    void pushModifiedFlowState(Collection<FlowPath> modifiedFlowPaths,
			       Collection<FlowEntry> modifiedFlowEntries) {
	//
	// Push the modified Flow state:
	//  - Flow Entries to switches and the datagrid
	//  - Flow Paths to the database
	//
	pushModifiedFlowEntriesToSwitches(modifiedFlowEntries);
	pushModifiedFlowPathsToDatabase(modifiedFlowPaths);
	cleanupDeletedFlowEntriesFromDatagrid(modifiedFlowEntries);
    }

    /**
     * Push modified Flow Entries to switches.
     *
     * NOTE: Only the Flow Entries to switches controlled by this instance
     * are pushed.
     *
     * @param modifiedFlowEntries the collection of modified Flow Entries.
     */
    private void pushModifiedFlowEntriesToSwitches(
			Collection<FlowEntry> modifiedFlowEntries) {
	if (modifiedFlowEntries.isEmpty())
	    return;

	List<Pair<IOFSwitch, FlowEntry>> entries =
	    new LinkedList<Pair<IOFSwitch, FlowEntry>>();

	Map<Long, IOFSwitch> mySwitches = getMySwitches();

	//
	// Create a collection of my Flow Entries to push
	//
	for (FlowEntry flowEntry : modifiedFlowEntries) {
	    IOFSwitch mySwitch = mySwitches.get(flowEntry.dpid().value());
	    if (mySwitch == null)
		continue;

	    //
	    // Assign Flow Entry IDs if missing.
	    //
	    // NOTE: This is an additional safeguard, in case the
	    // mySwitches set has changed (after the Flow Entry IDs
	    // assignments by the caller).
	    //
	    if (! flowEntry.isValidFlowEntryId()) {
		long id = getNextFlowEntryId();
		flowEntry.setFlowEntryId(new FlowEntryId(id));
	    }

	    log.debug("Pushing Flow Entry To Switch: {}", flowEntry.toString());
	    entries.add(new Pair<IOFSwitch, FlowEntry>(mySwitch, flowEntry));
	}

	pusher.pushFlowEntries(entries);
    }

    /**
     * Cleanup deleted Flow Entries from the datagrid.
     *
     * NOTE: We cleanup only the Flow Entries that are not for our switches.
     * This is needed to handle the case a switch going down:
     * It has no Master controller instance, hence no controller instance
     * will cleanup its flow entries.
     * This is sub-optimal: we need to elect a controller instance to handle
     * the cleanup of such orphaned flow entries.
     *
     * @param modifiedFlowEntries the collection of modified Flow Entries.
     */
    private void cleanupDeletedFlowEntriesFromDatagrid(
			Collection<FlowEntry> modifiedFlowEntries) {
	if (modifiedFlowEntries.isEmpty())
	    return;

	Map<Long, IOFSwitch> mySwitches = getMySwitches();

	for (FlowEntry flowEntry : modifiedFlowEntries) {
	    //
	    // Process only Flow Entries that should be deleted and have
	    // a valid Flow Entry ID.
	    //
	    if (! flowEntry.isValidFlowEntryId())
		continue;
	    if (flowEntry.flowEntryUserState() !=
		FlowEntryUserState.FE_USER_DELETE) {
		continue;
	    }

	    //
	    // NOTE: The deletion of Flow Entries for my switches is handled
	    // elsewhere.
	    //
	    IOFSwitch mySwitch = mySwitches.get(flowEntry.dpid().value());
	    if (mySwitch != null)
		continue;

	    log.debug("Pushing cleanup of Flow Entry To Datagrid: {}", flowEntry.toString());

	    //
	    // Write the Flow Entry to the Datagrid
	    //
	    datagridService.notificationSendFlowEntryRemoved(flowEntry.flowEntryId());
	}
    }

    /**
     * Class to implement writing to the database in a separate thread.
     */
    class FlowDatabaseWriter extends Thread {
	private FlowManager flowManager;
	private BlockingQueue<FlowPath> blockingQueue;

	/**
	 * Constructor.
	 *
	 * @param flowManager the Flow Manager to use.
	 * @param blockingQueue the blocking queue to use.
	 */
	FlowDatabaseWriter(FlowManager flowManager,
			   BlockingQueue<FlowPath> blockingQueue) {
	    this.flowManager = flowManager;
	    this.blockingQueue = blockingQueue;
	}

	/**
	 * Run the thread.
	 */
	@Override
	public void run() {
	    //
	    // The main loop
	    //
	    Collection<FlowPath> collection = new LinkedList<FlowPath>();
	    try {
		while (true) {
		    FlowPath flowPath = blockingQueue.take();
		    collection.add(flowPath);
		    blockingQueue.drainTo(collection);
		    flowManager.writeModifiedFlowPathsToDatabase(collection);
		    collection.clear();
		}
	    } catch (Exception exception) {
		log.debug("Exception writing to the Database: ", exception);
	    }
	}
    }

    /**
     * Push Flow Paths to the Network MAP.
     *
     * NOTE: The complete Flow Paths are pushed only on the instance
     * responsible for the first switch. This is to avoid database errors
     * when multiple instances are writing Flow Entries for the same Flow Path.
     *
     * @param modifiedFlowPaths the collection of Flow Paths to push.
     */
    private void pushModifiedFlowPathsToDatabase(
		Collection<FlowPath> modifiedFlowPaths) {
	//
	// We only add the Flow Paths to the Database Queue.
	// The FlowDatabaseWriter thread is responsible for the actual writing.
	//
	flowPathsToDatabaseQueue.addAll(modifiedFlowPaths);
    }

    /**
     * Write Flow Paths to the Network MAP.
     *
     * NOTE: The complete Flow Paths are pushed only on the instance
     * responsible for the first switch. This is to avoid database errors
     * when multiple instances are writing Flow Entries for the same Flow Path.
     *
     * @param modifiedFlowPaths the collection of Flow Paths to write.
     */
    private void writeModifiedFlowPathsToDatabase(
		Collection<FlowPath> modifiedFlowPaths) {
	if (modifiedFlowPaths.isEmpty())
	    return;

	FlowId dummyFlowId = new FlowId();

	Map<Long, IOFSwitch> mySwitches = getMySwitches();

	for (FlowPath flowPath : modifiedFlowPaths) {
	    //
	    // Don't push Flow Paths that are deleted by the user.
	    // Those will be deleted at the ONOS instance that received the
	    // API call to delete the flow.
	    //
	    if (flowPath.flowPathUserState() ==
		FlowPathUserState.FP_USER_DELETE) {
		continue;
	    }

	    //
	    // Push the changes only on the instance responsible for the
	    // first switch.
	    //
	    Dpid srcDpid = flowPath.dataPath().srcPort().dpid();
	    IOFSwitch mySrcSwitch = mySwitches.get(srcDpid.value());
	    if (mySrcSwitch == null)
		continue;

	    //
	    // Test whether all Flow Entries are valid
	    //
	    boolean allValid = true;
	    for (FlowEntry flowEntry : flowPath.flowEntries()) {
		if (flowEntry.flowEntryUserState() ==
		    FlowEntryUserState.FE_USER_DELETE) {
		    continue;
		}
		if (! flowEntry.isValidFlowEntryId()) {
		    allValid = false;
		    break;
		}
	    }
	    if (! allValid)
		continue;

	    log.debug("Pushing Flow Path To Database: {}", flowPath.toString());

	    //
	    // Write the Flow Path to the Network Map
	    //
	    try {
		if (! FlowDatabaseOperation.addFlow(dbHandlerInner, flowPath,
						    dummyFlowId)) {
		    String logMsg = "Cannot write to Network Map Flow Path " +
			flowPath.flowId();
		    log.error(logMsg);
		}
	    } catch (Exception e) {
		log.error("Exception writing Flow Path to Network MAP: ", e);
	    }
	}
    }
}
