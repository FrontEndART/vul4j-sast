package net.onrc.onos.ofcontroller.topology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.onrc.onos.datagrid.IDatagridService;
import net.onrc.onos.graph.DBOperation;
import net.onrc.onos.graph.GraphDBManager;
import net.onrc.onos.graph.GraphDBOperation;
import net.onrc.onos.ofcontroller.floodlightlistener.INetworkGraphService;
import net.onrc.onos.ofcontroller.topology.web.OnosTopologyWebRoutable;
import net.onrc.onos.ofcontroller.util.DataPath;
import net.onrc.onos.ofcontroller.util.FlowEntry;
import net.onrc.onos.ofcontroller.util.FlowPath;
import net.onrc.onos.ofcontroller.util.Port;
import net.onrc.onos.ofcontroller.util.SwitchPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for obtaining Topology Snapshot
 * and PathComputation.
 *
 * TODO: PathComputation part should be refactored out to separate class.
 */
public class TopologyManager implements IFloodlightModule,
					ITopologyNetService {
    private final static Logger log = LoggerFactory.getLogger(TopologyManager.class);
    protected IFloodlightProviderService floodlightProvider;

    protected static final String DBConfigFile = "dbconf";
    protected static final String GraphDBStore = "graph_db_store";

    protected DBOperation dbHandler;
    protected IRestApiService restApi;


    /**
     * Default constructor.
     */
    public TopologyManager() {
    }

    /**
     * Constructor for given database configuration file.
     *
     * @param config the database configuration file to use for
     * the initialization.
     */
    public TopologyManager(FloodlightModuleContext context) {
	Map<String, String> configMap = context.getConfigParams(this);
	String conf = configMap.get(DBConfigFile);
        String dbStore = configMap.get(GraphDBStore);
	this.init(dbStore,conf);
    }

    /**
     * Constructor for a given database operation handler.
     *
     * @param dbHandler the database operation handler to use for the
     * initialization.
     */
    public TopologyManager(DBOperation dbHandler) {
	this.dbHandler = dbHandler;
    }

    /**
     * Init the module.
     * @param 
     * @param config the database configuration file to use for
     * the initialization.
     */
    public void init(final String dbStore, String config) {
	try {
	    dbHandler = GraphDBManager.getDBOperation("ramcloud", "/tmp/ramcloudconf");
	    //dbHandler = GraphDBManager.getDBOperation(dbStore, config);
	} catch (Exception e) {
	    log.error(e.getMessage());
	}
    }

    /**
     * Shutdown the Topology Manager operation.
     */
    public void finalize() {
	close();
    }

    /**
     * Close the service. It will close the corresponding database connection.
     */
    public void close() {
	dbHandler.close();
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
        l.add(ITopologyNetService.class);
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
        m.put(ITopologyNetService.class, this);
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
	floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	restApi = context.getServiceImpl(IRestApiService.class);
	Map<String, String> configMap = context.getConfigParams(this);
	String conf = configMap.get(DBConfigFile);
        String dbStore = configMap.get(GraphDBStore);
	this.init(dbStore, conf);
    }

    /**
     * Startup module operation.
     *
     * @param context the module context to use for the startup.
     */
    @Override
    public void startUp(FloodlightModuleContext context) {
    	restApi.addRestletRoutable(new OnosTopologyWebRoutable());

    }

    /**
     * Fetch the Switch and Ports info from the Titan Graph
     * and return it for fast access during the shortest path
     * computation.
     *
     * After fetching the state, method @ref getTopologyShortestPath()
     * can be used for fast shortest path computation.
     *
     * Note: There is certain cost to fetch the state, hence it should
     * be used only when there is a large number of shortest path
     * computations that need to be done on the same topology.
     * Typically, a single call to @ref newDatabaseTopology()
     * should be followed by a large number of calls to
     * method @ref getTopologyShortestPath().
     * After the last @ref getTopologyShortestPath() call,
     * method @ref dropTopology() should be used to release
     * the internal state that is not needed anymore:
     *
     *       Topology topology = topologyManager.newDatabaseTopology();
     *       for (int i = 0; i < 10000; i++) {
     *           dataPath = topologyManager.getTopologyShortestPath(topology, ...);
     *           ...
     *        }
     *        topologyManager.dropTopology(shortestPathTopo);
     *
     * @return the allocated topology handler.
     */
    public Topology newDatabaseTopology() {
	Topology topology = new Topology();
	topology.readFromDatabase(dbHandler);

	return topology;
    }

    /**
     * Release the topology that was populated by
     * method @ref newDatabaseTopology().
     *
     * See the documentation for method @ref newDatabaseTopology()
     * for additional information and usage.
     *
     * @param topology the topology to release.
     */
    public void dropTopology(Topology topology) {
	topology = null;
    }

    /**
     * Compute the network path for a Flow.
     *
     * @param topology the topology handler to use.
     * @param flowPath the Flow to compute the network path for.
     * @return the data path with the computed path if found, otherwise null.
     */
    public static DataPath computeNetworkPath(Topology topology,
					      FlowPath flowPath) {
	//
	// Compute the network path based on the desired Flow Path type
	//
	switch (flowPath.flowPathType()) {
	case FP_TYPE_SHORTEST_PATH: {
	    SwitchPort src = flowPath.dataPath().srcPort();
	    SwitchPort dest = flowPath.dataPath().dstPort();
	    return ShortestPath.getTopologyShortestPath(topology, src, dest);
	}

	case FP_TYPE_EXPLICIT_PATH:
	    return flowPath.dataPath();

	case FP_TYPE_UNKNOWN:
	    return null;
	}

	return null;
    }

    /**
     * Test whether two Flow Entries represent same points in a data path.
     *
     * NOTE: Two Flow Entries represent same points in a data path if
     * the Switch DPID, incoming port and outgoing port are same.
     *
     * NOTE: This method is specialized for shortest-path unicast paths,
     * and probably should be moved somewhere else.
     *
     * @param oldFlowEntry the first Flow Entry to compare.
     * @param newFlowEntry the second Flow Entry to compare.
     * @return true if the two Flow Entries represent same points in a
     * data path, otherwise false.
     */
    public static boolean isSameFlowEntryDataPath(FlowEntry oldFlowEntry,
						  FlowEntry newFlowEntry) {
	// Test the DPID
	if (oldFlowEntry.dpid().value() != newFlowEntry.dpid().value())
	    return false;

	// Test the inPort
	do {
	    Port oldPort = oldFlowEntry.inPort();
	    Port newPort = newFlowEntry.inPort();
	    if ((oldPort != null) && (newPort != null) &&
		(oldPort.value() == newPort.value())) {
		break;
	    }
	    if ((oldPort == null) && (newPort == null))
		break;
	    return false;		// inPort is different
	} while (false);

	// Test the outPort
	do {
	    Port oldPort = oldFlowEntry.outPort();
	    Port newPort = newFlowEntry.outPort();
	    if ((oldPort != null) && (newPort != null) &&
		(oldPort.value() == newPort.value())) {
		break;
	    }
	    if ((oldPort == null) && (newPort == null))
		break;
	    return false;		// outPort is different
	} while (false);

	return true;
    }

    /**
     * Get the shortest path from a source to a destination by
     * using the pre-populated local topology state prepared
     * by method @ref newDatabaseTopology().
     *
     * See the documentation for method @ref newDatabaseTopology()
     * for additional information and usage.
     *
     * @param topology the topology handler to use.
     * @param src the source in the shortest path computation.
     * @param dest the destination in the shortest path computation.
     * @return the data path with the computed shortest path if
     * found, otherwise null.
     */
    public DataPath getTopologyShortestPath(Topology topology,
					    SwitchPort src, SwitchPort dest) {
	return ShortestPath.getTopologyShortestPath(topology, src, dest);
    }

    /**
     * Get the shortest path from a source to a destination by using
     * the underlying database.
     *
     * @param src the source in the shortest path computation.
     * @param dest the destination in the shortest path computation.
     * @return the data path with the computed shortest path if
     * found, otherwise null.
     */
    @Override
    public DataPath getDatabaseShortestPath(SwitchPort src, SwitchPort dest) {
	return ShortestPath.getDatabaseShortestPath(dbHandler, src, dest);
    }

    /**
     * Test whether a route exists from a source to a destination.
     *
     * @param src the source node for the test.
     * @param dest the destination node for the test.
     * @return true if a route exists, otherwise false.
     */
    @Override
    public Boolean routeExists(SwitchPort src, SwitchPort dest) {
	DataPath dataPath = getDatabaseShortestPath(src, dest);
	return (dataPath != null);
    }
}
