package net.onrc.onos.intent.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.onrc.onos.intent.FlowEntry;
import net.onrc.onos.ofcontroller.flowprogrammer.IFlowPusherService;
import net.onrc.onos.ofcontroller.networkgraph.NetworkGraph;
import net.onrc.onos.ofcontroller.util.Pair;

/**
 * 
 * @author Brian O'Connor <bocon@onlab.us>
 *
 */

public class PathInstallRuntime {
    NetworkGraph graph;
    IFlowPusherService pusher;
    IFloodlightProviderService provider;
    protected List<Set<FlowEntry>> plan;

    public PathInstallRuntime(NetworkGraph graph) {
	this.graph = graph;
    }

    public void installPlan(List<Set<FlowEntry>> plan) {
	this.plan = plan;
	Map<Long,IOFSwitch> switches = provider.getSwitches();
	for(Set<FlowEntry> phase : plan) {
	    Set<Pair<IOFSwitch, net.onrc.onos.ofcontroller.util.FlowEntry>> entries 
	    = new HashSet<>();
	    // convert flow entries and create pairs
	    for(FlowEntry entry : phase) {
		entries.add(new Pair<>(switches.get(entry.getSwitch().getDpid()), 
			entry.getFlowEntry()));
	    }
	    // push flow entries to switches
	    pusher.pushFlowEntries(entries);
	    // TODO: wait for confirmation messages before proceeding
	}
    }

}
