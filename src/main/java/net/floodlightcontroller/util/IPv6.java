package net.floodlightcontroller.util;

import org.openflow.util.HexString;
import net.floodlightcontroller.util.serializers.IPv6Serializer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The class representing an IPv6 address.
 */
@JsonSerialize(using=IPv6Serializer.class)
public class IPv6 {
    private long valueHigh;	// The higher (more significant) 64 bits
    private long valueLow;	// The lower (less significant) 64 bits

    /**
     * Default constructor.
     */
    public IPv6() {
	this.valueHigh = 0;
	this.valueLow = 0;
    }

    /**
     * Constructor from integer values.
     *
     * @param valueHigh the higher (more significant) 64 bits of the address.
     * @param valueLow the lower (less significant) 64 bits of the address.
     */
    public IPv6(long valueHigh, long valueLow) {
	this.valueHigh = valueHigh;
	this.valueLow = valueLow;
    }

    /**
     * Get the value of the higher (more significant) 64 bits of the address.
     *
     * @return the value of the higher (more significant) 64 bits of the
     * address.
     */
    public long valueHigh() { return valueHigh; }

    /**
     * Get the value of the lower (less significant) 64 bits of the address.
     *
     * @return the value of the lower (less significant) 64 bits of the
     * address.
     */
    public long valueLow() { return valueLow; }

    /**
     * Set the value of the IPv6 address.
     *
     * @param valueHigh the higher (more significant) 64 bits of the address.
     * @param valueLow the lower (less significant) 64 bits of the address.
     */
    public void setValue(long valueHigh, long valueLow) {
	this.valueHigh = valueHigh;
	this.valueLow = valueLow;
    }

    /**
     * Convert the IPv6 value to a ':' separated string.
     *
     * @return the IPv6 value as a ':' separated string.
     */
    @Override
    public String toString() {
	return HexString.toHexString(this.valueHigh) + ":" +
	    HexString.toHexString(this.valueLow);
    }
}
