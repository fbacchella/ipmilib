/*
 * SolRequest.java
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
 * Implementation of {@link SolMessage} for Remote Console -&gt; BMC message.
 */
public class SolOutboundMessage extends SolMessage {

    /**
     * Operation field in {@link SolMessage} Remote Console -&gt; BMC payload.
     */
    private final SolOutboundOperationField operationField;

    public SolOutboundMessage(byte sequenceNumber, byte ackNackSequenceNumber, byte acceptedCharacterCount, SolOutboundOperationField operationField) {
        super(sequenceNumber, ackNackSequenceNumber, acceptedCharacterCount, operationField.convertToByte());

        this.operationField = operationField;
    }

    public SolOutboundOperationField getOperationField() {
        return operationField;
    }

}
