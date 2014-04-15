package net.onrc.onos.core.intent;

import net.floodlightcontroller.util.MACAddress;
import net.onrc.onos.core.util.Dpid;

/**
 * @author Toshio Koide (t-koide@onlab.us)
 */
public class ShortestPathIntent extends Intent {
    protected long srcSwitchDpid;
    protected long srcPortNumber;
    protected long srcMacAddress;
    protected long dstSwitchDpid;
    protected long dstPortNumber;
    protected long dstMacAddress;
    protected String pathIntentId = null;

    /**
     * Default constructor for Kryo deserialization.
     */
    protected ShortestPathIntent() {
    }

    public ShortestPathIntent(String id,
                              long srcSwitch, long srcPort, long srcMac,
                              long dstSwitch, long dstPort, long dstMac) {
        super(id);
        srcSwitchDpid = srcSwitch;
        srcPortNumber = srcPort;
        srcMacAddress = srcMac;
        dstSwitchDpid = dstSwitch;
        dstPortNumber = dstPort;
        dstMacAddress = dstMac;
    }

    public long getSrcSwitchDpid() {
        return srcSwitchDpid;
    }

    public long getSrcPortNumber() {
        return srcPortNumber;
    }

    public long getSrcMac() {
        return srcMacAddress;
    }

    public long getDstSwitchDpid() {
        return dstSwitchDpid;
    }

    public long getDstPortNumber() {
        return dstPortNumber;
    }

    public long getDstMac() {
        return dstMacAddress;
    }

    public void setPathIntent(PathIntent pathIntent) {
        pathIntentId = pathIntent.getId();
    }

    public String getPathIntentId() {
        return pathIntentId;
    }

    @Override
    public int hashCode() {
        // TODO: Is this the intended behavior?
        return (super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        // TODO: Is this the intended behavior?
        return (super.equals(obj));
    }

    @Override
    public String toString() {
        return String.format("id:%s, state:%s, srcDpid:%s, srcPort:%d, srcMac:%s, dstDpid:%s, dstPort:%d, dstMac:%s",
                getId(), getState(),
                new Dpid(srcSwitchDpid), srcPortNumber, MACAddress.valueOf(srcMacAddress),
                new Dpid(dstSwitchDpid), dstPortNumber, MACAddress.valueOf(dstMacAddress));
    }
}
