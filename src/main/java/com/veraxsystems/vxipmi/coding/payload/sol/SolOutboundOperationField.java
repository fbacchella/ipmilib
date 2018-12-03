/*
 * SolOutboundOperationField.java
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
 * {@link SolOutboundOperationField} is a transfer object for operation sent by this application to remote system in {@link SolOutboundMessage}.
 */
public class SolOutboundOperationField {

    /**
     * Acknowledge state of {@link SolMessage} that this message is response for.
     */
    private final SolAckState ackState;

    /**
     * Set of operations to invoke on BMC.
     */
    private final Set<SolOperation> operations;

    /**
     * Creates new instance of {@link SolOutboundOperationField} filled with given data.
     *
     * @param ackState
     *          Acknowledge state carried by this object
     * @param operations
     *          Set of SOL specific operations for outbound message
     */
    public SolOutboundOperationField(SolAckState ackState, Set<SolOperation> operations) {
        this.ackState = ackState;
        this.operations = operations;
    }

    /**
     * Creates new instance of {@link SolOutboundOperationField} from raw byte.
     *
     * @param raw
     *          byte carrying information about SOL operations
     */
    public SolOutboundOperationField(byte raw) {
        this.ackState = SolAckState.extractFromByte(raw);
        this.operations = extractOperationsFromByte(raw);
    }

    protected Set<SolOperation> extractOperationsFromByte(byte raw) {
        Set<SolOperation> result = new HashSet<SolOperation>();

        for (SolOperation operation : SolOperation.values()) {
            if (TypeConverter.isBitSetOnPosition(operation.getOperationNumber(), raw)) {
                result.add(operation);
            }
        }

        return result;
    }

    public Set<SolOperation> getOperations() {
        return operations;
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

        for (SolOperation operation : operations) {
            value = TypeConverter.setBitOnPosition(operation.getOperationNumber(), value);
        }

        return value;
    }

}
