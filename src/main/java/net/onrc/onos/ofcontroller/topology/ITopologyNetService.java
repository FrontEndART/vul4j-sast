package net.onrc.onos.ofcontroller.topology;

import java.util.Map;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.onrc.onos.ofcontroller.util.DataPath;
import net.onrc.onos.ofcontroller.util.SwitchPort;

/**
 * Interface for providing Topology Network Service to other modules.
 */
public interface ITopologyNetService extends IFloodlightService {
    /**
     * Get the shortest path from a source to a destination.
     *
     * @param src the source in the shortest path computation.
     * @param dest the destination in the shortest path computation.
     * @return the data path with the computed shortest path if
     * found, otherwise null.
     */
    DataPath getShortestPath(SwitchPort src, SwitchPort dest);

    /**
     * Fetch the Switch and Ports info from the Titan Graph
     * and return it for fast access during the shortest path
     * computation.
     *
     * After fetching the state, method @ref getTopoShortestPath()
     * can be used for fast shortest path computation.
     *
     * Note: There is certain cost to fetch the state, hence it should
     * be used only when there is a large number of shortest path
     * computations that need to be done on the same topology.
     * Typically, a single call to @ref prepareShortestPathTopo()
     * should be followed by a large number of calls to
     * method @ref getTopoShortestPath().
     * After the last @ref getTopoShortestPath() call,
     * method @ref dropShortestPathTopo() should be used to release
     * the internal state that is not needed anymore:
     *
     *       Map<Long, ?> shortestPathTopo;
     *       shortestPathTopo = prepareShortestPathTopo();
     *       for (int i = 0; i < 10000; i++) {
     *           dataPath = getTopoShortestPath(shortestPathTopo, ...);
     *           ...
     *        }
     *        dropShortestPathTopo(shortestPathTopo);
     *
     * @return the Shortest Path info handler stored in a map.
     */
    Map<Long, ?> prepareShortestPathTopo();

    /**
     * Release the state that was populated by
     * method @ref prepareShortestPathTopo().
     *
     * See the documentation for method @ref prepareShortestPathTopo()
     * for additional information and usage.
     *
     * @param shortestPathTopo the Shortest Path info handler to release.
     */
    void dropShortestPathTopo(Map<Long, ?> shortestPathTopo);

    /**
     * Get the shortest path from a source to a destination by
     * using the pre-populated local topology state prepared
     * by method @ref prepareShortestPathTopo().
     *
     * See the documentation for method @ref prepareShortestPathTopo()
     * for additional information and usage.
     *
     * @param shortestPathTopo the Shortest Path info handler
     * to use.
     * @param src the source in the shortest path computation.
     * @param dest the destination in the shortest path computation.
     * @return the data path with the computed shortest path if
     * found, otherwise null.
     */
    DataPath getTopoShortestPath(Map<Long, ?> shortestPathTopo,
				 SwitchPort src, SwitchPort dest);

    /**
     * Test whether a route exists from a source to a destination.
     *
     * @param src the source node for the test.
     * @param dest the destination node for the test.
     * @return true if a route exists, otherwise false.
     */
    Boolean routeExists(SwitchPort src, SwitchPort dest);
}
