package net.floodlightcontroller.core;

import java.util.List;

import net.floodlightcontroller.core.INetMapTopologyObjects.ISwitchObject;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.topology.NodePortTuple;

public interface INetMapTopologyService extends INetMapService {

	public interface ITopoSwitchService {
		Iterable<ISwitchObject> GetActiveSwitches();
		Iterable<ISwitchObject> GetAllSwitches();
		Iterable<ISwitchObject> GetInactiveSwitches();
		List<String> GetPortsOnSwitch(String dpid);
	}
	
	public interface ITopoLinkService {
		List<Link> GetActiveLinks();
		List<Link> GetLinksOnSwitch(String dpid);
	}
	public interface ITopoDeviceService {
		List<Link> GetActiveDevices();
		List<Link> GetDevicesOnSwitch(String dpid);
	}
	
	public interface ITopoRouteService {
		List<NodePortTuple> GetShortestPath(NodePortTuple src, NodePortTuple dest);
		Boolean RouteExists(NodePortTuple src, NodePortTuple dest);
	}
	
	public interface ITopoFlowService {
		Boolean FlowExists(NodePortTuple src, NodePortTuple dest);
		List<NodePortTuple> GetShortestFlowPath(NodePortTuple src, NodePortTuple dest);
		
	}
}
