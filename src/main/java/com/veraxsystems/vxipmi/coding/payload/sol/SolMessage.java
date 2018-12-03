/*
 * SolMessage.java
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

import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.common.MessageComposer;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Implementation of {@link IpmiPayload} for SOL (Serial over LAN) messages.
 */
public abstract class SolMessage extends IpmiPayload {

    public static final byte MIN_SEQUENCE_NUMBER = 1;
    public static final byte MAX_SEQUENCE_NUMBER = 15;

    private static final int PACKET_SEQUENCE_NUMBER_LENGTH = 1;
    private static final int PACKET_ACK_SEQUENCE_NUMBER_LENGTH = 1;
    private static final int ACCEPTED_CHARACTERS_LENGTH = 1;
    private static final int OPERATION_STATUS_LENGTH = 1;

    public static final int PAYLOAD_HEADER_LENGTH =
            PACKET_SEQUENCE_NUMBER_LENGTH +
            PACKET_ACK_SEQUENCE_NUMBER_LENGTH +
            ACCEPTED_CHARACTERS_LENGTH +
            OPERATION_STATUS_LENGTH;

    /**
     * Sequence number of this packet.
     */
    private final byte sequenceNumber;

    /**
     * Sequence number of packet being ACKd/NACKd by this packet.
     */
    private final byte ackNackSequenceNumber;

    /**
     * Number of characters being accepted from ACKd/NACKd packet.
     */
    private final byte acceptedCharacterCount;

    /**
     * Operation to invoke or status of previously send packet.
     */
    private final byte operationStatus;

    protected SolMessage(byte sequenceNumber, byte ackNackSequenceNumber, byte acceptedCharacterCount, byte operationStatus) {
        this.sequenceNumber = trimSequenceNumber(sequenceNumber);
        this.ackNackSequenceNumber = trimSequenceNumber(ackNackSequenceNumber);
        this.acceptedCharacterCount = acceptedCharacterCount;
        this.operationStatus = operationStatus;
    }

    /**
     * Trims given sequence number to max allowed value for sequence numbers, applying MAX_SEQUENCE_NUMBER mask on it.
     *
     * @param sequenceNumber
     *          Sequence number before trim
     * @return trimmed sequence number
     */
    private byte trimSequenceNumber(byte sequenceNumber) {
        return TypeConverter.intToByte(sequenceNumber & MAX_SEQUENCE_NUMBER);
    }

    @Override
    public byte[] getPayloadData() {
        return MessageComposer.get(getPayloadLength())
            .appendField(sequenceNumber)
            .appendField(ackNackSequenceNumber)
            .appendField(acceptedCharacterCount)
            .appendField(operationStatus)
            .appendField(getData())
            .getMessage();
    }

    @Override
    public int getPayloadLength() {
        return PAYLOAD_HEADER_LENGTH + getData().length;
    }

    @Override
    public byte[] getIpmiCommandData() {
        return getData();
    }

    @Override
    public byte[] getData() {
        byte[] data = super.getData();

        if (data == null) {
            return new byte[0];
        }

        return data;
    }

    public byte getSequenceNumber() {
        return sequenceNumber;
    }

    public byte getAckNackSequenceNumber() {
        return ackNackSequenceNumber;
    }

    public byte getAcceptedCharacterCount() {
        return acceptedCharacterCount;
    }

    /**
     * Checks if given message carries some data (or operation/status).
     */
    public boolean isDataCarrier() {
        return sequenceNumber != 0 || getData().length > 0;
    }

    /**
     * Checks if given {@link SolMessage} is an ACK/NACK for some other message.
     */
    public boolean isAcknowledgeMessage() {
        return ackNackSequenceNumber != 0;
    }
}
