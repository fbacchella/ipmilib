/*
 * SolResponse.java
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

/**
 * Implementation of {@link SolMessage} for BMC -&gt; Remote Console message.
 */
public class SolInboundMessage extends SolMessage {

    /**
     * Status field in {@link SolMessage} BMC -&gt; Remote Console payload.
     */
    private final SolInboundStatusField statusField;

    public SolInboundMessage(byte sequenceNumber, byte ackNackSequenceNumber, byte acceptedCharacterCount, SolInboundStatusField statusField) {
        super(sequenceNumber, ackNackSequenceNumber, acceptedCharacterCount, statusField.convertToByte());
        this.statusField = statusField;
    }

    public SolInboundMessage(byte[] rawData) {
        super(rawData[0], rawData[1], rawData[2], rawData[3]);

        if (rawData.length > PAYLOAD_HEADER_LENGTH) {
            byte[] characterData = new byte[rawData.length - PAYLOAD_HEADER_LENGTH];
            System.arraycopy(rawData, PAYLOAD_HEADER_LENGTH, characterData, 0, characterData.length);
            setData(characterData);
        }

        this.statusField = new SolInboundStatusField(rawData[3]);
    }

    public SolInboundStatusField getStatusField() {
        return statusField;
    }
}
