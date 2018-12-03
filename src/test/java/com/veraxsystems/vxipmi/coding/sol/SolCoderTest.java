/*
 * SolCoderTest.java
 * Created on 17.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.sol;

import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.sol.SolAckState;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundStatusField;
import com.veraxsystems.vxipmi.coding.payload.sol.SolMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolOperation;
import com.veraxsystems.vxipmi.coding.payload.sol.SolOutboundMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityNone;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SolCoderTest {

    private CipherSuite cipherSuite;

    @Before
    public void setUp() throws Exception {
        this.cipherSuite = CipherSuite.getEmpty();
    }

    @Test
    public void getSupportedPayloadReturnsSol() throws Exception {
        SolCoder solCoder = new SolCoder(new byte[0], cipherSuite);

        assertEquals(PayloadType.Sol, solCoder.getSupportedPayloadType());
    }

    @Test
    public void preparePayloadWhenCharacterDataOnly() throws Exception {
        byte[] characterData = new byte[] {17, 18, 20, 25};
        byte sequenceNumber = 1;

        SolCoder solCoder = new SolCoder(characterData, cipherSuite);
        IpmiPayload payload = solCoder.preparePayload(1);

        assertThat(payload, instanceOf(SolOutboundMessage.class));

        SolOutboundMessage solPayload = (SolOutboundMessage) payload;
        assertEquals(sequenceNumber, solPayload.getSequenceNumber());
        assertEquals(0, solPayload.getAckNackSequenceNumber());
        assertArrayEquals(characterData, solPayload.getData());
        assertFalse("Message is data-carrier only and should not be considered ACK message", solPayload.isAcknowledgeMessage());
        assertTrue("Message is data-carrier", solPayload.isDataCarrier());
    }

    @Test
    public void preparePayloadWhenAckOnly() throws Exception {
        byte ackSequenceNumber = 7;
        byte acceptedCharactersCount = 20;
        SolAckState solAckState = SolAckState.ACK;

        SolCoder solCoder = new SolCoder(ackSequenceNumber, acceptedCharactersCount, solAckState, cipherSuite);
        IpmiPayload payload = solCoder.preparePayload((byte) 7);

        assertThat(payload, instanceOf(SolOutboundMessage.class));

        SolOutboundMessage solPayload = (SolOutboundMessage) payload;
        assertEquals(0, solPayload.getSequenceNumber());
        assertEquals(ackSequenceNumber, solPayload.getAckNackSequenceNumber());
        assertEquals(0, solPayload.getData().length);
        assertEquals(solAckState, solPayload.getOperationField().getAckState());
        assertFalse("Message is ACK only and should not be considered data-carrier", solPayload.isDataCarrier());
        assertTrue("Message is Acknowledge message", solPayload.isAcknowledgeMessage());
    }

    @Test
    public void preparePayloadWhenOperationsOnly() throws Exception {
        Set<SolOperation> operations = new HashSet<SolOperation>() {{
            add(SolOperation.Break);
            add(SolOperation.DCD_DSR);
        }};

        byte sequenceNumber = 8;

        SolCoder solCoder = new SolCoder(operations, cipherSuite);
        IpmiPayload payload = solCoder.preparePayload(sequenceNumber);

        assertThat(payload, instanceOf(SolOutboundMessage.class));

        SolOutboundMessage solPayload = (SolOutboundMessage) payload;
        assertEquals(sequenceNumber, solPayload.getSequenceNumber());
        assertEquals(0, solPayload.getAckNackSequenceNumber());
        assertEquals(1, solPayload.getData().length);
        assertThat(solPayload.getOperationField().getOperations(), containsInAnyOrder(operations.toArray()));
        assertFalse("Message is data-carrier only and should not be considered ACK message", solPayload.isAcknowledgeMessage());
        assertTrue("Message is data-carrier", solPayload.isDataCarrier());
    }

    @Test
    public void preparePayloadWhenBothDataCarrierAndAck() throws Exception {
        byte[] characterData = new byte[] {1, 2, 3, 4};
        Set<SolOperation> operations = new HashSet<SolOperation>(){{
            add(SolOperation.RingWOR);
            add(SolOperation.FlushInbound);
        }};

        SolAckState ackState = SolAckState.NACK;
        byte sequenceNumber = 3;
        byte nackSequenceNumber = 1;

        SolCoder solCoder = new SolCoder(characterData, nackSequenceNumber, (byte) 0, ackState, operations, cipherSuite);
        IpmiPayload payload = solCoder.preparePayload(sequenceNumber);

        assertThat(payload, instanceOf(SolOutboundMessage.class));

        SolOutboundMessage solPayload = (SolOutboundMessage) payload;
        assertEquals(sequenceNumber, solPayload.getSequenceNumber());
        assertEquals(nackSequenceNumber, solPayload.getAckNackSequenceNumber());
        assertArrayEquals(characterData, solPayload.getData());
        assertEquals(ackState, solPayload.getOperationField().getAckState());
        assertThat(solPayload.getOperationField().getOperations(), containsInAnyOrder(operations.toArray()));
        assertTrue("Message is Acknowledge message", solPayload.isAcknowledgeMessage());
        assertTrue("Message is data-carrier", solPayload.isDataCarrier());
    }

    @Test
    public void preparePayloadWhenSequenceNumberIs0() throws Exception {
        SolCoder solCoder = new SolCoder(new byte[4], cipherSuite);
        IpmiPayload payload = solCoder.preparePayload(0);

        assertThat(payload, instanceOf(SolOutboundMessage.class));
        assertEquals(0, ((SolOutboundMessage) payload).getSequenceNumber());
    }

    @Test
    public void preparePayloadWhenSequenceNumberIsGreaterThanMax() throws Exception {
        SolCoder solCoder = new SolCoder(new byte[4], cipherSuite);

        int sequenceNumber = SolMessage.MAX_SEQUENCE_NUMBER + 3;
        IpmiPayload payload = solCoder.preparePayload(sequenceNumber);

        assertThat(payload, instanceOf(SolOutboundMessage.class));
        assertEquals(2, ((SolOutboundMessage) payload).getSequenceNumber());
    }

    @Test
    public void getResponseDataWhenMessageGiven() throws Exception {
        Set<SolStatus> statuses = new HashSet<SolStatus>() {{
            add(SolStatus.SolDeactivated);
        }};
        byte acceptedCharacters = 3;
        SolInboundStatusField solStatusField = new SolInboundStatusField(SolAckState.ACK, statuses);
        SolInboundMessage solResponsePayload = new SolInboundMessage(new byte[] {1, 2, acceptedCharacters, solStatusField.convertToByte()});

        IpmiMessage message = new Ipmiv20Message(new ConfidentialityNone());
        message.setPayload(solResponsePayload);

        SolCoder solCoder = new SolCoder(new byte[0], cipherSuite);
        ResponseData responseData = solCoder.getResponseData(message);

        assertNotNull(responseData);
        assertThat(responseData, instanceOf(SolResponseData.class));

        SolResponseData solResponseData = (SolResponseData) responseData;
        assertEquals(solStatusField.getAckState(), solResponseData.getAcknowledgeState());
        assertEquals(statuses, solResponseData.getStatuses());
        assertEquals(acceptedCharacters, solResponseData.getAcceptedCharactersNumber());
    }
}