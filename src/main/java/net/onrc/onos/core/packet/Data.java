/**
 *    Copyright 2011, Big Switch Networks, Inc.
 *    Originally created by David Erickson, Stanford University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.onrc.onos.core.packet;

import java.util.Arrays;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class Data extends BasePacket {
    protected byte[] data;

    /**
     *
     */
    public Data() {
    }

    /**
     * @param data
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
                        justification = "TODO: Store a copy of the object?")
    public Data(byte[] data) {
        this.data = data;
    }

    /**
     * @return the data
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP",
                        justification = "TODO: Return a copy of the object?")
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
                        justification = "TODO: Store a copy of the object?")
    public Data setData(byte[] data) {
        this.data = data;
        return this;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP",
                        justification = "TODO: Return a copy of the object?")
    public byte[] serialize() {
        return this.data;
    }

    @Override
    public IPacket deserialize(byte[] data, int offset, int length) {
        this.data = Arrays.copyOfRange(data, offset, data.length);
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 1571;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(data);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Data)) {
            return false;
        }
        Data other = (Data) obj;
        if (!Arrays.equals(data, other.data)) {
            return false;
        }
        return true;
    }
}