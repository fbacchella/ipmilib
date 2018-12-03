/*
 * SolResponseTest.java
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
import static org.junit.Assert.assertThat;

public class SolInboundMessageTest {

    private SolInboundMessage response;
    private byte[] rawResponseData;
    private SolInboundStatusField statusField;

    @Before
    public void setUp() throws Exception {
        statusField = new SolInboundStatusField(SolAckState.ACK, new HashSet<SolStatus>());
        rawResponseData = new byte[] {(byte) 1, (byte) 1, (byte) 1, statusField.convertToByte()};

        this.response = new SolInboundMessage(rawResponseData);
    }

    @Test
    public void getStatusFieldReturnsPassedObject() throws Exception {
        SolInboundStatusField actualStatusField = response.getStatusField();

        assertEquals(this.statusField.getAckState(), actualStatusField.getAckState());
        assertEquals(this.statusField.getStatuses(), actualStatusField.getStatuses());
    }

}