package net.onrc.onos.core.topology;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Port Object stored in In-memory Topology.
 *
 * TODO REMOVE following design memo: This object itself may hold the DBObject,
 * but this Object itself will not issue any read/write to the DataStore.
 */
public class PortImpl extends NetworkGraphObject implements Port {

	private Switch sw;

	private Long number;
	private String description;

	protected Link outgoingLink;
	protected Link incomingLink;
	// These needs to be ConcurrentCollecton if allowing Graph to be accessed Concurrently
	protected Set<Device> devices;

	public PortImpl(NetworkGraph graph, Switch parentSwitch, Long number) {
		super(graph);
		this.sw = parentSwitch;
		this.number = number;
		this.devices = new HashSet<>();
	}

	@Override
	public Long getDpid() {
	    return sw.getDpid();
	}

	@Override
	public Long getNumber() {
		return number;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Long getHardwareAddress() {
		// TODO Auto-generated method stub
		return 0L;
	}

	@Override
	public Switch getSwitch() {
		return sw;
	}

	@Override
	public Link getOutgoingLink() {
		return outgoingLink;
	}

	@Override
	public Link getIncomingLink() {
		return incomingLink;
	}

	@Override
	public Iterable<Device> getDevices() {
	    return Collections.unmodifiableSet(this.devices);
	}

	public void setOutgoingLink(Link link) {
		outgoingLink = link;
	}

	public void setIncomingLink(Link link) {
		incomingLink = link;
	}

	/**
	 *
	 * @param d
	 * @return true if successfully added
	 */
	public boolean addDevice(Device d) {
	    return this.devices.add(d);
	}

	/**
	 *
	 * @param d
	 * @return true if device existed and was removed
	 */
	public boolean removeDevice(Device d) {
	    return this.devices.remove(d);
	}

	public void removeAllDevice() {
	    this.devices.clear();
	}

	@Override
	public String toString() {
		return String.format("%d:%d",
				getSwitch().getDpid(),
				getNumber());
	}
}
