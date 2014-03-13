package net.onrc.onos.ofcontroller.flowmanager;

import com.tinkerpop.blueprints.Direction;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.util.MACAddress;
import net.onrc.onos.graph.DBOperation;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IBaseObject;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IFlowEntry;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IFlowPath;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.IPortObject;
import net.onrc.onos.ofcontroller.core.INetMapTopologyObjects.ISwitchObject;
import net.onrc.onos.ofcontroller.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.impls.ramcloud.PerfMon;
import com.tinkerpop.blueprints.impls.ramcloud.RamCloudVertex;

/**
 * Class for performing Flow-related operations on the Database.
 */
public class FlowDatabaseOperation {
    private final static Logger log = LoggerFactory.getLogger(FlowDatabaseOperation.class);
    private static final boolean measureONOSFlowTimeProp = Long.valueOf(System.getProperty("benchmark.measureONOSFlow", "0")) != 0;
    private static final boolean measureONOSFlowEntryTimeProp = Long.valueOf(System.getProperty("benchmark.measureONOSFlowEntry", "0")) != 0;

    /**
     * Add a flow by batching all flow path properties and flow entries together.
     * This is done for performance reasons.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowPath the Flow Path to install.
     * @return true on success, otherwise false.
     */
    static boolean addFlowFast(DBOperation dbHandler, FlowPath flowPath) {
	IFlowPath flowPathObj;
	FlowPathProperty flowProp = new FlowPathProperty();
        FlowEntity flowPathEntity = new FlowEntity();
        boolean flowPathUpdate = false;
	
	flowPathObj = dbHandler.searchFlowPath(flowPath.flowId()); // getVertices("flow_id")
	if (flowPathObj == null) {
	    try {
                flowPathEntity.operationBegin(DBOperationType.ADD.toString());
		flowPathObj = dbHandler.newFlowPath();
	    } catch (Exception e) {
		flowPathObj = null;
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		log.error(":addFlow FlowId:{} failed: {}", flowPath.flowId(), sw.toString());
	    }
            flowPathEntity.setProperty("user_state", "FP_USER_ADD");
	    flowProp.setFlowPathUserState("FP_USER_ADD");
	} else {
            flowPathUpdate = true;
	    // Remove the old Flow Entries (this is special for RAMCloud)
	    for (IFlowEntry flowEntryObj : flowPathObj.getFlowEntries()) { // get.@Adjacency("flow", IN)
                flowPathEntity.operationBegin(DBOperationType.REMOVE.toString());
		dbHandler.removeFlowEntry(flowEntryObj); // removeVertex()
                flowPathEntity.operationEnd(DBOperationType.REMOVE.toString());
	    }
            flowPathEntity.operationBegin(DBOperationType.UPDATE.toString());
            flowPathEntity.setProperty("user_state", "FP_USER_ADD");
	    flowProp.setFlowPathUserState("FP_USER_MODIFY");
	}
	if (flowPathObj == null) {
	    log.error(":addFlow FlowId:{} failed: Flow object not created", flowPath.flowId());
	    dbHandler.rollback();
	    return false;
	}

        flowPathEntity.setProperty("flow_id", flowPath.flowId().toString());
	// Set the Flow key
	flowProp.setFlowId(flowPath.flowId().toString());

	// Set the Flow attributes
        flowPathEntity.setProperty("installer_id", flowPath.installerId().toString());
	flowProp.setInstallerId(flowPath.installerId().toString());

        flowPathEntity.setProperty("flow_path_type", flowPath.flowPathType().toString());
	flowProp.setFlowPathType(flowPath.flowPathType().toString());

        flowPathEntity.setProperty("user_state", flowPath.flowPathUserState().toString());
	flowProp.setFlowPathUserState(flowPath.flowPathUserState().toString());


        flowPathEntity.setProperty("flow_path_flags", flowPath.flowPathFlags().flags());
	flowProp.setFlowPathFlags(flowPath.flowPathFlags().flags());

        flowPathEntity.setProperty("idle_timeout", flowPath.idleTimeout());
	flowProp.setIdleTimeout(flowPath.idleTimeout());

        flowPathEntity.setProperty("hard_timeout", flowPath.hardTimeout());
	flowProp.setHardTimeout(flowPath.hardTimeout());

        flowPathEntity.setProperty("priority", flowPath.priority());
	flowProp.setPriority(flowPath.priority());

        flowPathEntity.setProperty("src_switch", flowPath.dataPath().srcPort().dpid().toString());
	flowProp.setSrcSwitch(flowPath.dataPath().srcPort().dpid().toString());

        flowPathEntity.setProperty("src_port", flowPath.dataPath().srcPort().port().value());
	flowProp.setSrcPort(flowPath.dataPath().srcPort().port().value());

        flowPathEntity.setProperty("dst_switch", flowPath.dataPath().dstPort().dpid().toString());
	flowProp.setDstSwitch(flowPath.dataPath().dstPort().dpid().toString());

        flowPathEntity.setProperty("dst_port", flowPath.dataPath().dstPort().port().value());
	flowProp.setDstPort(flowPath.dataPath().dstPort().port().value());

	if (flowPath.flowEntryMatch().matchSrcMac()) {
            flowPathEntity.setProperty("matchSrcMac",flowPath.flowEntryMatch().srcMac().toString());
	    flowProp.setMatchSrcMac(flowPath.flowEntryMatch().srcMac().toString());
	}
	if (flowPath.flowEntryMatch().matchDstMac()) {
            flowPathEntity.setProperty("matchDstMac", flowPath.flowEntryMatch().dstMac().toString());
	    flowProp.setMatchDstMac(flowPath.flowEntryMatch().dstMac().toString());
	}
	if (flowPath.flowEntryMatch().matchEthernetFrameType()) {
            flowPathEntity.setProperty("matchEthernetFrameType", flowPath.flowEntryMatch().ethernetFrameType());
	    flowProp.setMatchEthernetFrameType(flowPath.flowEntryMatch().ethernetFrameType());
	}
	if (flowPath.flowEntryMatch().matchVlanId()) {
            flowPathEntity.setProperty("matchVlanId", flowPath.flowEntryMatch().vlanId());
	    flowProp.setMatchVlanId(flowPath.flowEntryMatch().vlanId());
	}
	if (flowPath.flowEntryMatch().matchVlanPriority()) {
            flowPathEntity.setProperty("matchVlanPriority", flowPath.flowEntryMatch().vlanPriority());
	    flowProp.setMatchVlanPriority(flowPath.flowEntryMatch().vlanPriority());
	}
	if (flowPath.flowEntryMatch().matchSrcIPv4Net()) {
            flowPathEntity.setProperty("matchSrcIPv4Net", flowPath.flowEntryMatch().srcIPv4Net().toString());
	    flowProp.setMatchSrcIPv4Net(flowPath.flowEntryMatch().srcIPv4Net().toString());
	}
	if (flowPath.flowEntryMatch().matchDstIPv4Net()) {
            flowPathEntity.setProperty("matchDstIPv4Net", flowPath.flowEntryMatch().dstIPv4Net().toString());
	    flowProp.setMatchDstIPv4Net(flowPath.flowEntryMatch().dstIPv4Net().toString());
	}
	if (flowPath.flowEntryMatch().matchIpProto()) {
            flowPathEntity.setProperty("matchIpProto", flowPath.flowEntryMatch().ipProto());
	    flowProp.setMatchIpProto(flowPath.flowEntryMatch().ipProto());
	}
	if (flowPath.flowEntryMatch().matchIpToS()) {
            flowPathEntity.setProperty("matchIpToS", flowPath.flowEntryMatch().ipToS());
	    flowProp.setMatchIpToS(flowPath.flowEntryMatch().ipToS());
	}
	if (flowPath.flowEntryMatch().matchSrcTcpUdpPort()) {
            flowPathEntity.setProperty("matchSrcTcpUdpPort", flowPath.flowEntryMatch().srcTcpUdpPort());
	    flowProp.setMatchSrcTcpUdpPort(flowPath.flowEntryMatch().srcTcpUdpPort());
	}
	if (flowPath.flowEntryMatch().matchDstTcpUdpPort()) {
            flowPathEntity.setProperty("matchDstTcpUdpPort", flowPath.flowEntryMatch().dstTcpUdpPort());
	    flowProp.setMatchDstTcpUdpPort(flowPath.flowEntryMatch().dstTcpUdpPort());
	}
	if (! flowPath.flowEntryActions().actions().isEmpty()) {
            flowPathEntity.setProperty("actions", flowPath.flowEntryActions().toString());
	    flowProp.setActions(flowPath.flowEntryActions().toString());
	}

	flowProp.commitProperties(dbHandler, flowPathObj);

	//
	// Flow Entries:
	// flowPath.dataPath().flowEntries()
	//
	for (FlowEntry flowEntry : flowPath.dataPath().flowEntries()) {
	    if (flowEntry.flowEntryUserState() == FlowEntryUserState.FE_USER_DELETE)
		continue;	// Skip: all Flow Entries were deleted earlier

	    IFlowEntry iFlowEntry;
	    FlowEntryProperty flowEntryProp = new FlowEntryProperty();
            FlowEntity flowEntryEntity = new FlowEntity();
            boolean updateFlowEntry = false;
	    
	    try {
		iFlowEntry = dbHandler.searchFlowEntry(flowEntry.flowEntryId()); // getVertices()
		if (iFlowEntry != null) {
                    updateFlowEntry = true;
                    flowEntryEntity.operationBegin(DBOperationType.UPDATE.toString());
                    flowEntryEntity.setProperty("user_state", "FE_USER_MODIFY");
		    flowEntryProp.setUserState("FE_USER_MODIFY");
		} else {
                    flowEntryEntity.operationBegin(DBOperationType.ADD.toString());
                    flowEntryEntity.setProperty("user_state", "FE_USER_ADD");
		    flowEntryProp.setUserState("FE_USER_ADD");
                    flowEntryEntity.addEdge(flowPathObj, Direction.OUT, "flow");
		}
	    } catch (Exception e) {
                // TODO do we really need to catch this exception.
	    }
	    
            flowEntryEntity.setProperty("flow_id", flowEntry.flowEntryId().toString());
	    // Set the Flow Entry key
           flowEntryEntity.setProperty("flow_entry_id", flowEntry.flowEntryId().toString());

            flowEntryEntity.setProperty("type", "flow_entry");

	    // Set the Flow Entry Edges
	    ISwitchObject sw = dbHandler.searchSwitch(flowEntry.dpid().toString()); // toshi memo: getVertices()

            flowEntryEntity.setProperty("idle_timeout", flowEntry.idleTimeout());

            flowEntryEntity.setProperty("hard_timeout", flowEntry.hardTimeout());

            flowEntryEntity.setProperty("priority", flowEntry.priority());

            flowEntryEntity.setProperty("switch_dpid", flowEntry.dpid().toString());

            flowEntryEntity.addEdge(sw, Direction.OUT, "switch");
	    if (flowEntry.flowEntryMatch().matchInPort()) {
		IPortObject inport = dbHandler.searchPort(flowEntry.dpid().toString(), flowEntry.flowEntryMatch().inPort().value()); // toshi memo: getVertices()

                flowEntryEntity.setProperty("matchInPort", flowEntry.flowEntryMatch().inPort().value());
                flowEntryEntity.addEdge(inport, Direction.OUT, "inport");
	    }

	    // Set the Flow Entry attributes
	    if (flowEntry.flowEntryMatch().matchSrcMac()) {
                flowEntryEntity.setProperty("matchSrcMac", flowEntry.flowEntryMatch().srcMac().toString());
	    }
	    if (flowEntry.flowEntryMatch().matchDstMac()) {
                flowEntryEntity.setProperty("matchDstMac", flowEntry.flowEntryMatch().dstMac().toString());
	    }
	    if (flowEntry.flowEntryMatch().matchEthernetFrameType()) {
                flowEntryEntity.setProperty("matchEthernetFrameType", flowEntry.flowEntryMatch().ethernetFrameType());
	    }
	    if (flowEntry.flowEntryMatch().matchVlanId()) {
                flowEntryEntity.setProperty("matchVlanId", flowEntry.flowEntryMatch().vlanId());
	    }
	    if (flowEntry.flowEntryMatch().matchVlanPriority()) {
                flowEntryEntity.setProperty("matchVlanPriority", flowEntry.flowEntryMatch().vlanPriority());
	    }
	    if (flowEntry.flowEntryMatch().matchSrcIPv4Net()) {
                flowEntryEntity.setProperty("matchSrcIPv4Net", flowEntry.flowEntryMatch().srcIPv4Net().toString());
	    }
	    if (flowEntry.flowEntryMatch().matchDstIPv4Net()) {
                flowEntryEntity.setProperty("matchDstIPv4Net", flowEntry.flowEntryMatch().dstIPv4Net().toString());
	    }
	    if (flowEntry.flowEntryMatch().matchIpProto()) {
                flowEntryEntity.setProperty("matchIpProto", flowEntry.flowEntryMatch().ipProto());
	    }
	    if (flowEntry.flowEntryMatch().matchIpToS()) {
                flowEntryEntity.setProperty("matchIpToS", flowEntry.flowEntryMatch().ipToS());
	    }
	    if (flowEntry.flowEntryMatch().matchSrcTcpUdpPort()) {
                flowEntryEntity.setProperty("matchSrcTcpUdpPort", flowEntry.flowEntryMatch().srcTcpUdpPort());
	    }
	    if (flowEntry.flowEntryMatch().matchDstTcpUdpPort()) {
                flowEntryEntity.setProperty("matchDstTcpUdpPort", flowEntry.flowEntryMatch().dstTcpUdpPort());
	    }

	    for (FlowEntryAction fa : flowEntry.flowEntryActions().actions()) {
		if (fa.actionOutput() != null) {
		    IPortObject outport = dbHandler.searchPort(flowEntry.dpid().toString(), fa.actionOutput().port().value()); // toshi memo: getVertices()
                    flowEntryEntity.setProperty("actionOutputPort", fa.actionOutput().port().value());
                    flowEntryEntity.addEdge(outport, Direction.OUT, "outport");
		}
	    }
	    if (! flowEntry.flowEntryActions().isEmpty()) {
                flowEntryEntity.setProperty("actions", flowEntry.flowEntryActions().toString());
	    }

            flowEntryEntity.setProperty("switch_state", flowEntry.flowEntrySwitchState().toString());
            if (updateFlowEntry) {
               flowEntryEntity.operationEnd(DBOperationType.UPDATE.toString());
            } else {
               flowEntryEntity.operationEnd(DBOperationType.ADD.toString());
            }
            flowPathEntity.append(flowEntryEntity);
	}
	
        if (flowPathUpdate) {
            flowPathEntity.operationEnd(DBOperationType.UPDATE.toString());
        } else {
            flowPathEntity.operationEnd(DBOperationType.ADD.toString());
        }
        flowPathEntity.persist(dbHandler);
	return true;
    }
    
    /**
     * Add a flow.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowPath the Flow Path to install.
     * @return true on success, otherwise false.
     */
    static boolean addFlow(DBOperation dbHandler, FlowPath flowPath) {
        PerfMon pm = PerfMon.getInstance();
        pm.addflowpath_start();
        boolean retValue = addFlowFast(dbHandler, flowPath);
        pm.addflowpath_end();
        return retValue;
    }

    /**
     * Add a flow entry to the Network MAP.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowObj the corresponding Flow Path object for the Flow Entry.
     * @param flowEntry the Flow Entry to install.
     * @return the added Flow Entry object on success, otherwise null.
     */
    static IFlowEntry addFlowEntry(DBOperation dbHandler,
				   IFlowPath flowObj,
				   FlowEntry flowEntry) {
	// Flow edges
	//   HeadFE (TODO)
	long startAddFlowEntry = 0;
	long endAddFlowEntry = 0;

	long endSearchFlowEntry = 0;

	long startCreateNewFlowEntry = 0;
	long endCreateNewFlowEntry = 0;

	long startSetProperties = 0;
	long endSetProperties = 0;
	int numProperties = 0;

	long startSearchSwitch = 0;
	long endSearchSwitch = 0;

	long startAddEdgeToSwitch =0;
	long endAddEdgeToSwitch =0;

	long startSearchInPort = 0;
	long endSearchInPort = 0;

	long startAddEdgeToInPort =0;
	long endAddEdgeToInPort =0;

	long startSearchOutPort = 0;
	long endSearchOutPort = 0;

	long startAddEdgeToOutPort =0;
	long endAddEdgeToOutPort =0;

	long startAddEdgeBetweenFlowPath = 0;
	long endAddEdgeBetweenFlowPath = 0;

	if (measureONOSFlowEntryTimeProp) {
		startAddFlowEntry = System.nanoTime();
	}

	IFlowEntry flowEntryObj = null;
	boolean found = false;
	try {
	    flowEntryObj = dbHandler.searchFlowEntry(flowEntry.flowEntryId());
	    if (measureONOSFlowEntryTimeProp) {
		endSearchFlowEntry = System.nanoTime();
	    }
	    if (flowEntryObj != null) {
		found = true;
	    } else {
		if (measureONOSFlowEntryTimeProp) {
		    startCreateNewFlowEntry = System.nanoTime();
		}
		flowEntryObj = dbHandler.newFlowEntry();
		if (measureONOSFlowEntryTimeProp) {
		    endCreateNewFlowEntry = System.nanoTime();
		}
	    }
	} catch (Exception e) {
	    log.error(":addFlow FlowEntryId:{} failed",
		      flowEntry.flowEntryId());
	    return null;
	}
	if (flowEntryObj == null) {
	    log.error(":addFlow FlowEntryId:{} failed: FlowEntry object not created",
		      flowEntry.flowEntryId());
	    return null;
	}

	if (measureONOSFlowEntryTimeProp) {
	    startSetProperties = System.nanoTime();
	}

	FlowEntryProperty flowProp = new FlowEntryProperty();

	//
	// Set the Flow Entry key:
	// - flowEntry.flowEntryId()
	//
	flowProp.setFlowEntryId(flowEntry.flowEntryId().toString());
	flowProp.setType("flow_entry");
	if (measureONOSFlowEntryTimeProp) {
	    numProperties += 2;
	}

	//
	// Set the Flow Entry Edges and attributes:
	// - Switch edge
	// - InPort edge
	// - OutPort edge
	//
	// - flowEntry.idleTimeout()
	// - flowEntry.hardTimeout()
	// - flowEntry.priority()
	// - flowEntry.dpid()
	// - flowEntry.flowEntryUserState()
	// - flowEntry.flowEntrySwitchState()
	// - flowEntry.flowEntryErrorState()
	// - flowEntry.matchInPort()
	// - flowEntry.matchSrcMac()
	// - flowEntry.matchDstMac()
	// - flowEntry.matchEthernetFrameType()
	// - flowEntry.matchVlanId()
	// - flowEntry.matchVlanPriority()
	// - flowEntry.matchSrcIPv4Net()
	// - flowEntry.matchDstIPv4Net()
	// - flowEntry.matchIpProto()
	// - flowEntry.matchIpToS()
	// - flowEntry.matchSrcTcpUdpPort()
	// - flowEntry.matchDstTcpUdpPort()
	// - flowEntry.actionOutputPort()
	// - flowEntry.actions()
	//
	if (measureONOSFlowEntryTimeProp) {
	    startSearchSwitch = System.nanoTime();
	}
	ISwitchObject sw = dbHandler.searchSwitch(flowEntry.dpid().toString());
	if (measureONOSFlowEntryTimeProp) {
	    endSearchSwitch = System.nanoTime();
	}

	flowProp.setIdleTimeout(flowEntry.idleTimeout());
	flowProp.setHardTimeout(flowEntry.hardTimeout());
	flowProp.setPriority(flowEntry.priority());
	flowProp.setSwitchDpid(flowEntry.dpid().toString());
	if (measureONOSFlowEntryTimeProp) {
	    numProperties += 3;
	}

	if (measureONOSFlowEntryTimeProp) {
	    startAddEdgeToSwitch = System.nanoTime();
	}
	flowEntryObj.setSwitch(sw);
	if (measureONOSFlowEntryTimeProp) {
	    endAddEdgeToSwitch = System.nanoTime();
	}
	if (flowEntry.flowEntryMatch().matchInPort()) {
	    if (measureONOSFlowEntryTimeProp) {
		startSearchInPort = System.nanoTime();
	    }
	    IPortObject inport =
		    dbHandler.searchPort(flowEntry.dpid().toString(),
			    flowEntry.flowEntryMatch().inPort().value());
	    if (measureONOSFlowEntryTimeProp) {
		endSearchInPort = System.nanoTime();
	    }

	    flowProp.setMatchInPort(flowEntry.flowEntryMatch().inPort().value());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }

	    if (measureONOSFlowEntryTimeProp) {
		startAddEdgeToInPort = System.nanoTime();
	    }
	    flowEntryObj.setInPort(inport);
	    if (measureONOSFlowEntryTimeProp) {
		endAddEdgeToInPort = System.nanoTime();
	    }
	}
	if (flowEntry.flowEntryMatch().matchSrcMac()) {
		flowProp.setMatchSrcMac(flowEntry.flowEntryMatch().srcMac().toString());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchDstMac()) {
		flowProp.setMatchDstMac(flowEntry.flowEntryMatch().dstMac().toString());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchEthernetFrameType()) {
		flowProp.setMatchEthernetFrameType(flowEntry.flowEntryMatch().ethernetFrameType());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchVlanId()) {
		flowProp.setMatchVlanId(flowEntry.flowEntryMatch().vlanId());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchVlanPriority()) {
		flowProp.setMatchVlanPriority(flowEntry.flowEntryMatch().vlanPriority());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchSrcIPv4Net()) {
		flowProp.setMatchSrcIPv4Net(flowEntry.flowEntryMatch().srcIPv4Net().toString());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchDstIPv4Net()) {
		flowProp.setMatchDstIPv4Net(flowEntry.flowEntryMatch().dstIPv4Net().toString());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchIpProto()) {
		flowProp.setMatchIpProto(flowEntry.flowEntryMatch().ipProto());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchIpToS()) {
		flowProp.setMatchIpToS(flowEntry.flowEntryMatch().ipToS());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchSrcTcpUdpPort()) {
		flowProp.setMatchSrcTcpUdpPort(flowEntry.flowEntryMatch().srcTcpUdpPort());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}
	if (flowEntry.flowEntryMatch().matchDstTcpUdpPort()) {
		flowProp.setMatchDstTcpUdpPort(flowEntry.flowEntryMatch().dstTcpUdpPort());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}

	for (FlowEntryAction fa : flowEntry.flowEntryActions().actions()) {
	    if (fa.actionOutput() != null) {
		if (measureONOSFlowEntryTimeProp) {
		    if ( startSearchOutPort != 0 ) log.error("Performance addFlowEntry(_,{},{}) -- Multiple output port action unexpected.", flowEntry.flowId(), flowEntry.flowEntryId());
		    startSearchOutPort = System.nanoTime();
		}
		IPortObject outport =
			dbHandler.searchPort(flowEntry.dpid().toString(),
				fa.actionOutput().port().value());
		if (measureONOSFlowEntryTimeProp) {
		    endSearchOutPort = System.nanoTime();
		}

		flowProp.setActionOutputPort(fa.actionOutput().port().value());
		if (measureONOSFlowEntryTimeProp) {
		    ++numProperties;
		}

		if (measureONOSFlowEntryTimeProp) {
		    startAddEdgeToOutPort = System.nanoTime();
		}
		flowEntryObj.setOutPort(outport);
		if (measureONOSFlowEntryTimeProp) {
		    endAddEdgeToOutPort = System.nanoTime();
		}
	    }
	}
	if (! flowEntry.flowEntryActions().isEmpty()) {
		flowProp.setActions(flowEntry.flowEntryActions().toString());
	    if (measureONOSFlowEntryTimeProp) {
		++numProperties;
	    }
	}

	flowProp.setUserState(flowEntry.flowEntryUserState().toString());
	flowProp.setSwitchState(flowEntry.flowEntrySwitchState().toString());
	if (measureONOSFlowEntryTimeProp) {
	    numProperties += 2;
	}
	flowProp.commitProperties(dbHandler, flowEntryObj);
	//
	// TODO: Take care of the FlowEntryErrorState.
	//
	if (measureONOSFlowEntryTimeProp) {
	    endSetProperties = System.nanoTime();
	}

	// Flow Entries edges:
	//   Flow
	//   NextFE (TODO)
	if (! found) {
	    if (measureONOSFlowEntryTimeProp) {
		startAddEdgeBetweenFlowPath = System.nanoTime();
	    }
	    //flowObj.addFlowEntry(flowEntryObj);
	    flowEntryObj.setFlow(flowObj);
	    if (measureONOSFlowEntryTimeProp) {
		endAddEdgeBetweenFlowPath = System.nanoTime();
	    }
	}
	if (measureONOSFlowEntryTimeProp) {
	    endAddFlowEntry = System.nanoTime();

	    log.error("Performance addFlowEntry(_,{},{}) -- "
		    + "GrandTotal: {} "
		    + "SearchExistingFE: {} "
		    + "CreateNewFE: {} "
		    + "SetProp+Edge: {} #Props: {} "
		    + "SearchSwitch: {} "
		    + "AddEdgeToSwitch: {} "
		    + "SearchInPort: {} "
		    + "AddEdgeToInPort: {} "
		    + "SearchOutPort: {} "
		    + "AddEdgeToOutPort: {} "
		    + "AddEdgeBetweenFlowPath: {} "
		    , flowEntry.flowId(), flowEntry.flowEntryId()
		    , endAddFlowEntry - startAddFlowEntry
		    , endSearchFlowEntry - startAddFlowEntry
		    , endCreateNewFlowEntry - startCreateNewFlowEntry
		    , endSetProperties - startSetProperties, numProperties
		    , endSearchSwitch - startSearchSwitch
		    , endAddEdgeToSwitch - startAddEdgeToSwitch
		    , endSearchInPort - startSearchInPort
		    , endAddEdgeToInPort - startAddEdgeToInPort
		    , endSearchOutPort - startSearchOutPort
		    , endAddEdgeToOutPort - startAddEdgeToOutPort
		    , endAddEdgeBetweenFlowPath - startAddEdgeBetweenFlowPath
		    );
	}

	return flowEntryObj;
    }

    /**
     * Delete a flow entry from the Network MAP.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowObj the corresponding Flow Path object for the Flow Entry.
     * @param flowEntry the Flow Entry to delete.
     * @return true on success, otherwise false.
     */
    static boolean deleteFlowEntry(DBOperation dbHandler,
				   IFlowPath flowObj,
				   FlowEntry flowEntry) {
	IFlowEntry flowEntryObj = null;
	try {
	    flowEntryObj = dbHandler.searchFlowEntry(flowEntry.flowEntryId());
	} catch (Exception e) {
	    log.error(":deleteFlowEntry FlowEntryId:{} failed",
		      flowEntry.flowEntryId());
	    return false;
	}
	//
	// TODO: Don't print an error for now, because multiple controller
	// instances might be deleting the same flow entry.
	//
	/*
	if (flowEntryObj == null) {
	    log.error(":deleteFlowEntry FlowEntryId:{} failed: FlowEntry object not found",
		      flowEntry.flowEntryId());
	    return false;
	}
	*/
	if (flowEntryObj == null)
	    return true;

	flowObj.removeFlowEntry(flowEntryObj);
	dbHandler.removeFlowEntry(flowEntryObj);
	return true;
    }

    /**
     * Delete all previously added flows.
     *
     * @param dbHandler the Graph Database handler to use.
     * @return true on success, otherwise false.
     */
    static boolean deleteAllFlows(DBOperation dbHandler) {
	try {
	    // Get all Flow IDs
	    Iterable<IFlowPath> allFlowPaths = dbHandler.getAllFlowPaths();
	    for (IFlowPath flowPathObj : allFlowPaths) {
		if (flowPathObj == null)
		    continue;

		deleteIFlowPath(dbHandler, flowPathObj);
	    }
	    dbHandler.commit();
	} catch (Exception e) {
	    log.error("Exception deleting all Flow Paths from Network MAP: ", e);
	    return false;
	}

	return true;
    }

    /**
     * Delete a previously added flow.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowId the Flow ID of the flow to delete.
     * @return true on success, otherwise false.
     */
    static boolean deleteFlow(DBOperation dbHandler, FlowId flowId) {
	IFlowPath flowObj = null;
	try {
	    flowObj = dbHandler.searchFlowPath(flowId);
	} catch (Exception e) {
	    // TODO: handle exceptions
	    dbHandler.rollback();
	    log.error(":deleteFlow FlowId:{} failed", flowId);
	    return false;
	}
	if (flowObj == null) {
	    dbHandler.commit();
	    return true;		// OK: No such flow
	}

	deleteIFlowPath(dbHandler, flowObj);
	dbHandler.commit();
	return true;
    }

    /**
     * Delete a previously added flow.
     * @note You need to call commit after calling this method.
     * @param dbHandler the Graph Database handler to use.
     * @param flowObj IFlowPath object to delete.
     */
    private static void deleteIFlowPath(DBOperation dbHandler, IFlowPath flowObj) {
	//
	// Remove all Flow Entries
	//
	Iterable<IFlowEntry> flowEntries = flowObj.getFlowEntries();
	for (IFlowEntry flowEntryObj : flowEntries) {
	    flowObj.removeFlowEntry(flowEntryObj);
	    dbHandler.removeFlowEntry(flowEntryObj);
	}
	// Remove the Flow itself
	dbHandler.removeFlowPath(flowObj);
    }

    /**
     * Get a previously added flow.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowId the Flow ID of the flow to get.
     * @return the Flow Path if found, otherwise null.
     */
    static FlowPath getFlow(DBOperation dbHandler, FlowId flowId) {
	IFlowPath flowObj = null;
	try {
	    flowObj = dbHandler.searchFlowPath(flowId);
	} catch (Exception e) {
	    // TODO: handle exceptions
	    dbHandler.rollback();
	    log.error(":getFlow FlowId:{} failed", flowId);
	    return null;
	}
	if (flowObj == null) {
	    dbHandler.commit();
	    return null;		// Flow not found
	}

	//
	// Extract the Flow state
	//
	FlowPath flowPath = extractFlowPath(flowObj);
	dbHandler.commit();

	return flowPath;
    }

    /**
     * Get a previously added flow entry.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowEntryId the Flow Entry ID of the flow entry to get.
     * @return the Flow Entry if found, otherwise null.
     */
    static FlowEntry getFlowEntry(DBOperation dbHandler,
				  FlowEntryId flowEntryId) {
	IFlowEntry flowEntryObj = null;
	try {
	    flowEntryObj = dbHandler.searchFlowEntry(flowEntryId);
	} catch (Exception e) {
	    // TODO: handle exceptions
	    dbHandler.rollback();
	    log.error(":getFlowEntry FlowEntryId:{} failed", flowEntryId);
	    return null;
	}
	if (flowEntryObj == null) {
	    dbHandler.commit();
	    return null;		// Flow not found
	}

	//
	// Extract the Flow Entry state
	//
	FlowEntry flowEntry = extractFlowEntry(flowEntryObj);
	dbHandler.commit();

	return flowEntry;
    }

    /**
     * Get the source switch DPID of a previously added flow.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowId the Flow ID of the flow to get.
     * @return the source switch DPID if found, otherwise null.
     */
    static Dpid getFlowSourceDpid(DBOperation dbHandler, FlowId flowId) {
	IFlowPath flowObj = null;
	try {
	    flowObj = dbHandler.searchFlowPath(flowId);
	} catch (Exception e) {
	    // TODO: handle exceptions
	    dbHandler.rollback();
	    log.error(":getFlowSourceDpid FlowId:{} failed", flowId);
	    return null;
	}
	if (flowObj == null) {
	    dbHandler.commit();
	    return null;		// Flow not found
	}

	//
	// Extract the Flow Source DPID
	//
	String srcSwitchStr = flowObj.getSrcSwitch();
	if (srcSwitchStr == null) {
	    // TODO: A work-around, becauuse of some bogus database objects
	    dbHandler.commit();
	    return null;
	}

	Dpid dpid = new Dpid(srcSwitchStr);

	dbHandler.commit();

	return dpid;
    }

    /**
     * Get all installed flows by all installers.
     *
     * @param dbHandler the Graph Database handler to use.
     * @return the Flow Paths if found, otherwise null.
     */
    static ArrayList<FlowPath> getAllFlows(DBOperation dbHandler) {
	Iterable<IFlowPath> flowPathsObj = null;
	ArrayList<FlowPath> flowPaths = new ArrayList<FlowPath>();

	try {
	    flowPathsObj = dbHandler.getAllFlowPaths();
	} catch (Exception e) {
	    // TODO: handle exceptions
	    dbHandler.rollback();
	    log.error(":getAllFlowPaths failed");
	    return flowPaths;
	}
	if ((flowPathsObj == null) || (flowPathsObj.iterator().hasNext() == false)) {
	    dbHandler.commit();
	    return flowPaths;	// No Flows found
	}

	for (IFlowPath flowObj : flowPathsObj) {
	    //
	    // Extract the Flow state
	    //
	    FlowPath flowPath = extractFlowPath(flowObj);
	    if (flowPath != null)
		flowPaths.add(flowPath);
	}

	dbHandler.commit();

	return flowPaths;
    }

    /**
     * Get all installed flows whose Source Switch is controlled by this
     * instance.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param mySwitches the collection of the switches controlled by this
     * instance.
     * @return the Flow Paths if found, otherwise null.
     */
    static ArrayList<FlowPath> getAllMyFlows(DBOperation dbHandler,
					     Map<Long, IOFSwitch> mySwitches) {
	Iterable<IFlowPath> flowPathsObj = null;
	ArrayList<FlowPath> flowPaths = new ArrayList<FlowPath>();

	try {
	    flowPathsObj = dbHandler.getAllFlowPaths();
	} catch (Exception e) {
	    // TODO: handle exceptions
	    dbHandler.rollback();
	    log.error(":getAllMyFlowPaths failed");
	    return flowPaths;
	}
	if ((flowPathsObj == null) || (flowPathsObj.iterator().hasNext() == false)) {
	    dbHandler.commit();
	    return flowPaths;	// No Flows found
	}

	for (IFlowPath flowObj : flowPathsObj) {
	    //
	    // Extract the Source Switch DPID and ignore if the switch
	    // is not controlled by this instance.
	    //
	    String srcSwitchStr = flowObj.getSrcSwitch();
	    if (srcSwitchStr == null) {
		// TODO: A work-around, becauuse of some bogus database objects
		continue;
	    }
	    Dpid dpid = new Dpid(srcSwitchStr);
	    if (mySwitches.get(dpid.value()) == null)
		continue;

	    //
	    // Extract the Flow state
	    //
	    FlowPath flowPath = extractFlowPath(flowObj);
	    if (flowPath != null)
		flowPaths.add(flowPath);
	}

	dbHandler.commit();

	return flowPaths;
    }

    /**
     * Get a subset of installed flows.
     *
     * @param dbHandler the Graph Database handler to use.
     * @param flowIds the collection of Flow IDs to get.
     * @return the Flow Paths if found, otherwise null.
     */
    static ArrayList<FlowPath> getFlows(DBOperation dbHandler,
					Collection<FlowId> flowIds) {
	ArrayList<FlowPath> flowPaths = new ArrayList<FlowPath>();

	// TODO: This implementation should use threads
	for (FlowId flowId : flowIds) {
	    FlowPath flowPath = getFlow(dbHandler, flowId);
	    if (flowPath != null)
		flowPaths.add(flowPath);
	}
	// dbHandler.commit();

	return flowPaths;
    }

    /**
     * Extract Flow Path State from a Titan Database Object @ref IFlowPath.
     *
     * @param flowObj the object to extract the Flow Path State from.
     * @return the extracted Flow Path State.
     */
    static FlowPath extractFlowPath(IFlowPath flowObj) {
	//
	// Extract the Flow state
	//
    log.info("extractFlowPath: start");
	String flowIdStr;
	String installerIdStr;
	String flowPathType;
	String flowPathUserState;
	Long flowPathFlags;
	Integer idleTimeout;
	Integer hardTimeout;
	Integer priority;
	String srcSwitchStr;
	Short srcPortShort;
	String dstSwitchStr;
	Short dstPortShort;

	if ( flowObj.asVertex() instanceof RamCloudVertex ) {
	    RamCloudVertex v = (RamCloudVertex)flowObj.asVertex();
	    Map<String,Object> propMap = v.getProperties();

	    flowIdStr = (String) propMap.get("flow_id");
	    installerIdStr = (String) propMap.get("installer_id");
	    flowPathType = (String) propMap.get("flow_path_type");
	    flowPathUserState = (String) propMap.get("user_state");
	    flowPathFlags = (Long)propMap.get("flow_path_flags");
	    idleTimeout = (Integer) propMap.get("idle_timeout");
	    hardTimeout = (Integer) propMap.get("hard_timeout");
	    priority = (Integer) propMap.get("priority");
	    srcSwitchStr = (String) propMap.get("src_switch");
	    srcPortShort = (Short)propMap.get("src_port");
	    dstSwitchStr = (String) propMap.get("dst_switch");
	    dstPortShort = (Short)propMap.get("dst_port");
	} else {
	    flowIdStr = flowObj.getFlowId();
	    installerIdStr = flowObj.getInstallerId();
	    flowPathType = flowObj.getFlowPathType();
	    flowPathUserState = flowObj.getFlowPathUserState();
	    flowPathFlags = flowObj.getFlowPathFlags();
	    idleTimeout = flowObj.getIdleTimeout();
	    hardTimeout = flowObj.getHardTimeout();
	    priority = flowObj.getPriority();
	    srcSwitchStr = flowObj.getSrcSwitch();
	    srcPortShort = flowObj.getSrcPort();
	    dstSwitchStr = flowObj.getDstSwitch();
	    dstPortShort = flowObj.getDstPort();
	}

	if ((flowIdStr == null) ||
	    (installerIdStr == null) ||
	    (flowPathType == null) ||
	    (flowPathUserState == null) ||
	    (flowPathFlags == null) ||
	    (idleTimeout == null) ||
	    (hardTimeout == null) ||
	    (priority == null) ||
	    (srcSwitchStr == null) ||
	    (srcPortShort == null) ||
	    (dstSwitchStr == null) ||
	    (dstPortShort == null)) {
	    // TODO: A work-around, because of some bogus database objects
	    log.error("extractFlowPath: wrong properties");
	    return null;
	}

	FlowPath flowPath = new FlowPath();
	flowPath.setFlowId(new FlowId(flowIdStr));
	flowPath.setInstallerId(new CallerId(installerIdStr));
	flowPath.setFlowPathType(FlowPathType.valueOf(flowPathType));
	flowPath.setFlowPathUserState(FlowPathUserState.valueOf(flowPathUserState));
	flowPath.setFlowPathFlags(new FlowPathFlags(flowPathFlags));
	flowPath.setIdleTimeout(idleTimeout);
	flowPath.setHardTimeout(hardTimeout);
	flowPath.setPriority(priority);
	flowPath.dataPath().srcPort().setDpid(new Dpid(srcSwitchStr));
	flowPath.dataPath().srcPort().setPort(new Port(srcPortShort));
	flowPath.dataPath().dstPort().setDpid(new Dpid(dstSwitchStr));
	flowPath.dataPath().dstPort().setPort(new Port(dstPortShort));
	//
	// Extract the match conditions common for all Flow Entries
	//
	{
	    FlowEntryMatch match = extractMatch(flowObj);

	    flowPath.setFlowEntryMatch(match);
	}
	//
	// Extract the actions for the first Flow Entry
	//
	{
	    String actionsStr = flowObj.getActions();
	    if (actionsStr != null) {
		FlowEntryActions flowEntryActions = new FlowEntryActions(actionsStr);
		flowPath.setFlowEntryActions(flowEntryActions);
	    }
	}

	//
	// Extract all Flow Entries
	//
	Iterable<IFlowEntry> flowEntries = flowObj.getFlowEntries();
	for (IFlowEntry flowEntryObj : flowEntries) {
	    FlowEntry flowEntry = extractFlowEntry(flowEntryObj);
	    if (flowEntry == null)
		continue;
	    flowPath.dataPath().flowEntries().add(flowEntry);
	}

    log.info("extractFlowPath: end");
	return flowPath;
    }

    /**
     * Extract Flow Entry State from a Titan Database Object @ref IFlowEntry.
     *
     * @param flowEntryObj the object to extract the Flow Entry State from.
     * @return the extracted Flow Entry State.
     */
    public static FlowEntry extractFlowEntry(IFlowEntry flowEntryObj) {
	log.info("extractFlowEntry: start");
	IFlowPath flowObj = flowEntryObj.getFlow();
	if (flowObj == null) {
		log.error("extractFlowEntry: no flowPath exists");
	    return null;
	}

	String flowIdStr = flowObj.getFlowId();
	//
	String flowEntryIdStr;
	Integer idleTimeout;
	Integer hardTimeout;
	Integer priority;
	String switchDpidStr;
	String userState;
	String switchState;
	if ( flowEntryObj.asVertex() instanceof RamCloudVertex ) {
	    RamCloudVertex v = (RamCloudVertex)flowEntryObj.asVertex();
	    Map<String,Object> propMap = v.getProperties();

	    flowEntryIdStr = (String) propMap.get("flow_entry_id");
	    idleTimeout = (Integer) propMap.get("idle_timeout");
	    hardTimeout = (Integer) propMap.get("hard_timeout");
	    priority = (Integer) propMap.get("priority");
	    switchDpidStr = (String) propMap.get("switch_dpid");
	    userState = (String) propMap.get("user_state");
	    switchState = (String) propMap.get("switch_state");
	} else {
	    flowEntryIdStr = flowEntryObj.getFlowEntryId();
	    idleTimeout = flowEntryObj.getIdleTimeout();
	    hardTimeout = flowEntryObj.getHardTimeout();
	    priority = flowEntryObj.getPriority();
	    switchDpidStr = flowEntryObj.getSwitchDpid();
	    userState = flowEntryObj.getUserState();
	    switchState = flowEntryObj.getSwitchState();
	}

	if ((flowIdStr == null) ||
	    (flowEntryIdStr == null) ||
	    (idleTimeout == null) ||
	    (hardTimeout == null) ||
	    (priority == null) ||
	    (switchDpidStr == null) ||
	    (userState == null) ||
	    (switchState == null)) {
	    // TODO: A work-around, because of some bogus database objects
		log.error("extractFlowEntry: wrong properties");
	    return null;
	}

	FlowEntry flowEntry = new FlowEntry();
	flowEntry.setFlowEntryId(new FlowEntryId(flowEntryIdStr));
	flowEntry.setFlowId(new FlowId(flowIdStr));
	flowEntry.setDpid(new Dpid(switchDpidStr));
	flowEntry.setIdleTimeout(idleTimeout);
	flowEntry.setHardTimeout(hardTimeout);
	flowEntry.setPriority(priority);

	//
	// Extract the match conditions
	//
	FlowEntryMatch match = extractMatch(flowEntryObj);
	flowEntry.setFlowEntryMatch(match);

	//
	// Extract the actions
	//
	FlowEntryActions actions = new FlowEntryActions();
	String actionsStr = flowEntryObj.getActions();
	if (actionsStr != null)
	    actions = new FlowEntryActions(actionsStr);
	flowEntry.setFlowEntryActions(actions);
	flowEntry.setFlowEntryUserState(FlowEntryUserState.valueOf(userState));
	flowEntry.setFlowEntrySwitchState(FlowEntrySwitchState.valueOf(switchState));
	//
	// TODO: Take care of FlowEntryErrorState.
	//
	log.info("extractFlowEntry: end");
	return flowEntry;
    }

    /**
     * Extract FlowEntryMatch from IFlowPath or IFlowEntry
     * @param flowObj : either IFlowPath or IFlowEntry
     * @return extracted Match info
     */
    private static FlowEntryMatch extractMatch(IBaseObject flowObj) {
	FlowEntryMatch match = new FlowEntryMatch();

	Short matchInPort = null; // Only for IFlowEntry
	String matchSrcMac = null;
	String matchDstMac = null;
	Short matchEthernetFrameType = null;
	Short matchVlanId = null;
	Byte matchVlanPriority = null;
	String matchSrcIPv4Net = null;
	String matchDstIPv4Net = null;
	Byte matchIpProto = null;
	Byte matchIpToS = null;
	Short matchSrcTcpUdpPort = null;
	Short matchDstTcpUdpPort = null;

	if ( flowObj.asVertex() instanceof RamCloudVertex ) {
	    RamCloudVertex v = (RamCloudVertex)flowObj.asVertex();
	    Map<String,Object> propMap = v.getProperties();
	    matchInPort = (Short) propMap.get("matchInPort");
	    matchSrcMac = (String) propMap.get("matchSrcMac");
	    matchDstMac = (String) propMap.get("matchDstMac");
	    matchEthernetFrameType = (Short) propMap.get("matchEthernetFrameType");
	    matchVlanId = (Short) propMap.get("matchVlanId");
	    matchVlanPriority = (Byte) propMap.get("matchVlanPriority");
	    matchSrcIPv4Net = (String) propMap.get("matchSrcIPv4Net");
	    matchDstIPv4Net = (String) propMap.get("matchDstIPv4Net");
	    matchIpProto = (Byte) propMap.get("matchIpProto");
	    matchIpToS = (Byte) propMap.get("matchIpToS");
	    matchSrcTcpUdpPort = (Short) propMap.get("matchSrcTcpUdpPort");
	    matchDstTcpUdpPort = (Short) propMap.get("matchDstTcpUdpPort");
	} else {
	    if (flowObj instanceof IFlowEntry ){
		IFlowEntry flowEntry = (IFlowEntry) flowObj;
		matchInPort = flowEntry.getMatchInPort();
		matchSrcMac = flowEntry.getMatchSrcMac();
		matchDstMac = flowEntry.getMatchDstMac();
		matchEthernetFrameType = flowEntry.getMatchEthernetFrameType();
		matchVlanId = flowEntry.getMatchVlanId();
		matchVlanPriority = flowEntry.getMatchVlanPriority();
		matchSrcIPv4Net = flowEntry.getMatchSrcIPv4Net();
		matchDstIPv4Net = flowEntry.getMatchDstIPv4Net();
		matchIpProto = flowEntry.getMatchIpProto();
		matchIpToS = flowEntry.getMatchIpToS();
		matchSrcTcpUdpPort = flowEntry.getMatchSrcTcpUdpPort();
		matchDstTcpUdpPort = flowEntry.getMatchDstTcpUdpPort();
	    } else if(flowObj instanceof IFlowPath) {
		IFlowPath flowPath = (IFlowPath) flowObj;
		matchSrcMac = flowPath.getMatchSrcMac();
		matchDstMac = flowPath.getMatchDstMac();
		matchEthernetFrameType = flowPath.getMatchEthernetFrameType();
		matchVlanId = flowPath.getMatchVlanId();
		matchVlanPriority = flowPath.getMatchVlanPriority();
		matchSrcIPv4Net = flowPath.getMatchSrcIPv4Net();
		matchDstIPv4Net = flowPath.getMatchDstIPv4Net();
		matchIpProto = flowPath.getMatchIpProto();
		matchIpToS = flowPath.getMatchIpToS();
		matchSrcTcpUdpPort = flowPath.getMatchSrcTcpUdpPort();
		matchDstTcpUdpPort = flowPath.getMatchDstTcpUdpPort();
	    }
	}

	if (matchInPort != null)
	    match.enableInPort(new Port(matchInPort));
	if (matchSrcMac != null)
	    match.enableSrcMac(MACAddress.valueOf(matchSrcMac));
	if (matchDstMac != null)
	    match.enableDstMac(MACAddress.valueOf(matchDstMac));
	if (matchEthernetFrameType != null)
	    match.enableEthernetFrameType(matchEthernetFrameType);
	if (matchVlanId != null)
	    match.enableVlanId(matchVlanId);
	if (matchVlanPriority != null)
	    match.enableVlanPriority(matchVlanPriority);
	if (matchSrcIPv4Net != null)
	    match.enableSrcIPv4Net(new IPv4Net(matchSrcIPv4Net));
	if (matchDstIPv4Net != null)
	    match.enableDstIPv4Net(new IPv4Net(matchDstIPv4Net));
	if (matchIpProto != null)
	    match.enableIpProto(matchIpProto);
	if (matchIpToS != null)
	    match.enableIpToS(matchIpToS);
	if (matchSrcTcpUdpPort != null)
	    match.enableSrcTcpUdpPort(matchSrcTcpUdpPort);
	if (matchDstTcpUdpPort != null)
	    match.enableDstTcpUdpPort(matchDstTcpUdpPort);
	return match;
    }
}
