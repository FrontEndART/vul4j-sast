package net.floodlightcontroller.util;

import net.floodlightcontroller.util.SwitchPort;
import net.floodlightcontroller.util.serializers.DataPathEndpointsSerializer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The class representing the Data Path Endpoints.
 */
@JsonSerialize(using=DataPathEndpointsSerializer.class)
public class DataPathEndpoints {
    private SwitchPort srcPort;		// The source port
    private SwitchPort dstPort;		// The destination port

    /**
     * Default constructor.
     */
    public DataPathEndpoints() {
    }

    /**
     * Constructor for given source and destination ports.
     *
     * @param srcPort the source port to use.
     * @param dstPort the destination port to use.
     */
    public DataPathEndpoints(SwitchPort srcPort, SwitchPort dstPort) {
	this.srcPort = srcPort;
	this.dstPort = dstPort;
    }

    /**
     * Get the data path source port.
     *
     * @return the data path source port.
     */
    public SwitchPort srcPort() { return srcPort; }

    /**
     * Set the data path source port.
     *
     * @param srcPort the data path source port to set.
     */
    public void setSrcPort(SwitchPort srcPort) {
	this.srcPort = srcPort;
    }

    /**
     * Get the data path destination port.
     *
     * @return the data path destination port.
     */
    public SwitchPort dstPort() { return dstPort; }

    /**
     * Set the data path destination port.
     *
     * @param dstPort the data path destination port to set.
     */
    public void setDstPort(SwitchPort dstPort) {
	this.dstPort = dstPort;
    }

    /**
     * Convert the data path endpoints to a string.
     *
     * The string has the following form:
     * [src=01:01:01:01:01:01:01:01/1111 dst=02:02:02:02:02:02:02:02/2222]
     *
     * @return the data path endpoints as a string.
     */
    @Override
    public String toString() {
	String ret = "[src=" + this.srcPort.toString() +
	    " dst=" + this.dstPort.toString() + "]";
	return ret;
    }
}
