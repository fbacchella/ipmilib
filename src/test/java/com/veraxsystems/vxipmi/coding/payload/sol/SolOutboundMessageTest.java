/*
 * SolRequestTest.java
 * Created on 17.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload.sol;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SolOutboundMessageTest {

    private SolOutboundMessage request;
    private byte[] rawRequestData;
    private SolOutboundOperationField operationField;

    @Before
    public void setUp() throws Exception {
        operationField = new SolOutboundOperationField(SolAckState.ACK, new HashSet<SolOperation>());
        rawRequestData = new byte[] {(byte) 1, (byte) 1, (byte) 1, operationField.convertToByte()};

        this.request = new SolOutboundMessage(rawRequestData[0], rawRequestData[1], rawRequestData[2], operationField);
    }

    @Test
    public void getPayloadLengthWhenNoData() throws Exception {
        assertEquals(4, request.getPayloadLength());
    }

    @Test
    public void getPayloadLengthWhenSomeData() throws Exception {
        byte[] data = new byte[] {1, 2, 3, 4, 5, 6};
        request.setData(data);

        int expectedLength = 4 + data.length;

        assertEquals(expectedLength, request.getPayloadLength());
    }

    @Test
    public void getPayloadDataWhenNoData() throws Exception {
        assertArrayEquals(rawRequestData, request.getPayloadData());
        assertArrayEquals(new byte[0], request.getData());
    }

    @Test
    public void getPayloadDataWhenSomeData() throws Exception {
        byte[] data = new byte[12];
        request.setData(data);

        byte[] expectedPayloadData = new byte[rawRequestData.length + data.length];
        System.arraycopy(rawRequestData, 0, expectedPayloadData, 0, rawRequestData.length);
        System.arraycopy(data, 0, expectedPayloadData, rawRequestData.length, data.length);

        assertArrayEquals(expectedPayloadData, request.getPayloadData());
        assertArrayEquals(data, request.getData());
    }

    @Test
    public void getIpmiCommandDataWhenNoData() throws Exception {
        assertArrayEquals(request.getData(), request.getIpmiCommandData());
    }

    @Test
    public void getIpmiCommandDataWhenSomeData() throws Exception {
        byte[] data = new byte[] {15, 18, 10, 33, 34, 111, 90};
        request.setData(data);

        assertArrayEquals(request.getData(), request.getIpmiCommandData());
    }

    @Test
    public void getOperationFieldReturnsPassedObject() throws Exception {
        assertEquals(operationField, request.getOperationField());
    }

    @Test
    public void getSequenceNumberWhenAckOnly() throws Exception {
        request = new SolOutboundMessage((byte) 0, (byte) 10, (byte) 20, operationField);

        assertEquals(0, request.getSequenceNumber());
    }

    @Test
    public void getSequenceNumberWhenLessThanMax() throws Exception {
        byte sequenceNumber = SolMessage.MAX_SEQUENCE_NUMBER - 12;
        request = new SolOutboundMessage(sequenceNumber, (byte) 0, (byte) 0, operationField);

        assertEquals(sequenceNumber, request.getSequenceNumber());
    }

    @Test
    public void getSequenceNumberWhenEquelsMax() throws Exception {
        request = new SolOutboundMessage(SolMessage.MAX_SEQUENCE_NUMBER, (byte) 0, (byte) 0, operationField);

        assertEquals(SolMessage.MAX_SEQUENCE_NUMBER, request.getSequenceNumber());
    }

    @Test
    public void getSequenceNumberWhenGreaterThanMax() throws Exception {
        request = new SolOutboundMessage((byte) (SolMessage.MAX_SEQUENCE_NUMBER + 13), (byte) 0, (byte) 0, operationField);

        assertEquals(12, request.getSequenceNumber());
    }

    @Test
    public void getAckNackSequenceNumberWhenLessThanMax() throws Exception {
        byte ackNackSequenceNumber = SolMessage.MAX_SEQUENCE_NUMBER - 7;
        request = new SolOutboundMessage((byte) 0, ackNackSequenceNumber, (byte) 0, operationField);

        assertEquals(ackNackSequenceNumber, request.getAckNackSequenceNumber());
    }

    @Test
    public void getAckNackSequenceNumberWhenEquelsMax() throws Exception {
        request = new SolOutboundMessage((byte) 0, SolMessage.MAX_SEQUENCE_NUMBER, (byte) 0, operationField);

        assertEquals(SolMessage.MAX_SEQUENCE_NUMBER, request.getAckNackSequenceNumber());
    }

    @Test
    public void getAckNackSequenceNumberWhenGreaterThanMax() throws Exception {
        request = new SolOutboundMessage((byte) 0, (byte) (SolMessage.MAX_SEQUENCE_NUMBER + 22), (byte) 0, operationField);

        assertEquals(5, request.getAckNackSequenceNumber());
    }
}