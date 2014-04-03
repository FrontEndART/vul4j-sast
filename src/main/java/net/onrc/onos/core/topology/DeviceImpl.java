package net.onrc.onos.core.topology;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.floodlightcontroller.util.MACAddress;

/**
 * @author Toshio Koide (t-koide@onlab.us)
 */
public class DeviceImpl extends NetworkGraphObject implements Device {

    private final MACAddress macAddr;
    protected LinkedList<Port> attachmentPoints;
    protected Set<InetAddress> ipAddresses;
    private long lastSeenTime;

    public DeviceImpl(NetworkGraph graph, MACAddress mac) {
		super(graph);
		this.macAddr = mac;
		this.attachmentPoints = new LinkedList<>();
		this.ipAddresses = new HashSet<>();
    }

    @Override
    public MACAddress getMacAddress() {
    	return this.macAddr;
    }

    @Override
    public Collection<InetAddress> getIpAddress() {
    	return Collections.unmodifiableSet(ipAddresses);
    }

    @Override
    public Iterable<Port> getAttachmentPoints() {
    	return Collections.unmodifiableList(this.attachmentPoints);
    }

    @Override
    public long getLastSeenTime() {
    	return lastSeenTime;
    }

    @Override
    public String toString() {
    	return macAddr.toString();
    }
    
    void setLastSeenTime(long lastSeenTime) {
    	this.lastSeenTime = lastSeenTime;
    }

    /**
     * Only {@link TopologyManager} should use this method
     * @param p
     */
    void addAttachmentPoint(Port p) {
    	this.attachmentPoints.remove(p);
    	this.attachmentPoints.addFirst(p);
    }

    /**
     * Only {@link TopologyManager} should use this method
     * @param p
     */
    boolean removeAttachmentPoint(Port p) {
    	return this.attachmentPoints.remove(p);
    }

    /**
     * Only {@link TopologyManager} should use this method
     * @param p
     */
    boolean addIpAddress(InetAddress addr) {
    	return this.ipAddresses.add(addr);
    }

    /**
     * Only {@link TopologyManager} should use this method
     * @param p
     */
    boolean removeIpAddress(InetAddress addr) {
    	return this.ipAddresses.remove(addr);
    }
}
