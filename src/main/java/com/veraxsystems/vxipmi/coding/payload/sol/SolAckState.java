/*
 * SolAckState.java
 * Created on 19.05.2017
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

/**
 * Enumeration of possible acknowledge states for SOL messages.
 */
public enum SolAckState {

    /**
     * Message was acknowledged.
     */
    ACK,

    /**
     * Message was not acknowledged.
     */
    NACK;

    private static final int ACK_BIT_NUMBER = 6;

    /**
     * Extracts {@link SolAckState} from given byte.
     *
     * @param value
     *          byte with encoded {@link SolAckState}
     * @return {@link SolAckState} extracted from byte
     */
    public static SolAckState extractFromByte(byte value) {
        return TypeConverter.isBitSetOnPosition(ACK_BIT_NUMBER, value) ? SolAckState.NACK : SolAckState.ACK;
    }

    /**
     * Encode information about this {@link SolAckState} in given byte, returning updated byte.
     *
     * @param value
     *          current byte value
     * @return updated byte value
     */
    public byte encodeInByte(final byte value) {
        byte updatedValue = value;

        if (this == SolAckState.NACK) {
            updatedValue = TypeConverter.setBitOnPosition(ACK_BIT_NUMBER, value);
        }

        return updatedValue;
    }
}
