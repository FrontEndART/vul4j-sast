package net.onrc.onos.ofcontroller.flowmanager;

import java.util.ArrayList;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.onrc.onos.ofcontroller.topology.Topology;
import net.onrc.onos.ofcontroller.util.CallerId;
import net.onrc.onos.ofcontroller.util.DataPathEndpoints;
import net.onrc.onos.ofcontroller.util.FlowEntryId;
import net.onrc.onos.ofcontroller.util.FlowId;
import net.onrc.onos.ofcontroller.util.FlowPath;

/**
 * Interface for providing Flow Service to other modules.
 */
public interface IFlowService extends IFloodlightService {
    /**
     * Add a flow.
     *
     * Internally, ONOS will automatically register the installer for
     * receiving Flow Path Notifications for that path.
     *
     * @param flowPath the Flow Path to install.
     * @param flowId the return-by-reference Flow ID as assigned internally.
     * @return true on success, otherwise false.
     */
    boolean addFlow(FlowPath flowPath, FlowId flowId);

    /**
     * Delete all previously added flows.
     *
     * @return true on success, otherwise false.
     */
    boolean deleteAllFlows();

    /**
     * Delete a previously added flow.
     *
     * @param flowId the Flow ID of the flow to delete.
     * @return true on success, otherwise false.
     */
    boolean deleteFlow(FlowId flowId);

    /**
     * Get a previously added flow.
     *
     * @param flowId the Flow ID of the flow to get.
     * @return the Flow Path if found, otherwise null.
     */
    FlowPath getFlow(FlowId flowId);

    /**
     * Get all installed flows by all installers.
     *
     * @return the Flow Paths if found, otherwise null.
     */
    ArrayList<FlowPath> getAllFlows();

    /**
     * Get all previously added flows by a specific installer for a given
     * data path endpoints.
     *
     * @param installerId the Caller ID of the installer of the flow to get.
     * @param dataPathEndpoints the data path endpoints of the flow to get.
     * @return the Flow Paths if found, otherwise null.
     */
    ArrayList<FlowPath> getAllFlows(CallerId installerId,
				 DataPathEndpoints dataPathEndpoints);

    /**
     * Get all installed flows by all installers for given data path endpoints.
     *
     * @param dataPathEndpoints the data path endpoints of the flows to get.
     * @return the Flow Paths if found, otherwise null.
     */
    ArrayList<FlowPath> getAllFlows(DataPathEndpoints dataPathEndpoints);

    /**
     * Get summary of all installed flows by all installers.
     *
     * @param flowId starting flow Id of the range
     * @param maxFlows number of flows to return
     * @return the Flow Paths if found, otherwise null.
     */
    ArrayList<FlowPath> getAllFlowsSummary(FlowId flowId, int maxFlows);
    
    /**
     * Add and maintain a shortest-path flow.
     *
     * NOTE: The Flow Path argument does NOT contain all flow entries.
     * Instead, it contains a single dummy flow entry that is used to
     * store the matching condition(s).
     * That entry is replaced by the appropriate entries from the
     * internally performed shortest-path computation.
     *
     * @param flowPath the Flow Path with the endpoints and the match
     * conditions to install.
     * @return the added shortest-path flow on success, otherwise null.
     */
    FlowPath addAndMaintainShortestPathFlow(FlowPath flowPath);

    /**
     * Get the network topology.
     *
     * @return the network topology.
     */
    Topology getTopology();
    
    /**
     * Get a globally unique flow ID from the flow service.
     * NOTE: Not currently guaranteed to be globally unique.
     * 
     * @return unique flow ID
     */
    public long getNextFlowEntryId();

    /**
     * Inform the Flow Manager that a Flow Entry on switch expired.
     *
     * @param sw the switch the Flow Entry expired on.
     * @param flowEntryId the Flow Entry ID of the expired Flow Entry.
     */
    public void flowEntryOnSwitchExpired(IOFSwitch sw, FlowEntryId flowEntryId);
}
