/*
 * SolInboundStatusField.java
 * Created on 22.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload.sol;

import com.veraxsystems.vxipmi.common.TypeConverter;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link SolInboundStatusField} is a transfer object for status sent by remote system to this application in {@link SolInboundMessage}.
 */
public class SolInboundStatusField {

    /**
     * Acknowledge state of {@link SolMessage} that this message is response for.
     */
    private final SolAckState ackState;

    /**
     * Set of statuses indicated by BMC in this message.
     */
    private final Set<SolStatus> statuses;

    /**
     * Creates new instance if {@link SolInboundStatusField} without Acknowledge data.
     *
     * @param statuses
     *          Set of SOL specific statuses for inbound message
     */
    public SolInboundStatusField(Set<SolStatus> statuses) {
        this.ackState = SolAckState.ACK;
        this.statuses = statuses;
    }

    /**
     * Creates new instance of {@link SolInboundStatusField} filled with given data.
     *
     * @param ackState
     *          Acknowledge state carried by this object
     * @param statuses
     *          Set of SOL specific statuses for inbound message
     */
    public SolInboundStatusField(SolAckState ackState, Set<SolStatus> statuses) {
        this.ackState = ackState;
        this.statuses = statuses;
    }

    /**
     * Creates new instance of {@link SolInboundStatusField} from raw byte.
     *
     * @param raw
     *          byte carrying information about SOL status
     */
    public SolInboundStatusField(byte raw) {
        this.ackState = SolAckState.extractFromByte(raw);
        this.statuses = extractStatusesFromByte(raw);
    }

    private Set<SolStatus> extractStatusesFromByte(byte raw) {
        Set<SolStatus> result = new HashSet<SolStatus>();

        for (SolStatus status : SolStatus.values()) {
            if (TypeConverter.isBitSetOnPosition(status.getStatusNumber(), raw)) {
                result.add(status);
            }
        }

        return result;
    }

    public Set<SolStatus> getStatuses() {
        return statuses;
    }

    public SolAckState getAckState() {
        return ackState;
    }

    /**
     * Convert this object to it's raw, byte representation.
     */
    public byte convertToByte() {
        byte value = (byte) 0;

        value = ackState.encodeInByte(value);

        for (SolStatus status : statuses) {
            value = TypeConverter.setBitOnPosition(status.getStatusNumber(), value);
        }

        return value;
    }
}
