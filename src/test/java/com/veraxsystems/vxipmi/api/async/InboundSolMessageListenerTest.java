/*
 * InboundSolMessageListenerTest.java
 * Created on 17.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.api.async;

import com.veraxsystems.vxipmi.api.sol.SolEventListener;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.payload.sol.SolAckState;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundStatusField;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;
import com.veraxsystems.vxipmi.coding.sol.SolCoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InboundSolMessageListenerTest {

    @Mock
    private IpmiConnector connector;

    @Mock
    private ConnectionHandle connectionHandle;

    private List<SolEventListener> eventListeners;

    private InboundSolMessageListener solMessageListener;

    @Before
    public void setUp() throws Exception {
        eventListeners = new LinkedList<SolEventListener>();
        solMessageListener = new InboundSolMessageListener(connector, connectionHandle, eventListeners);
    }

    @Test
    public void isPayloadSupportedForSolReturnsTrue() throws Exception {
        IpmiPayload payload = mock(SolInboundMessage.class);
        assertTrue("SOL payload should be supported;", solMessageListener.isPayloadSupported(payload));
    }

    @Test
    public void isPayloadSupportedForLanReturnsFalse() throws Exception {
        IpmiPayload payload = mock(IpmiLanResponse.class);
        assertFalse("Lan Response payload should not be supported", solMessageListener.isPayloadSupported(payload));
    }

    @Test
    public void notifyWhenShortMessageCame() throws Exception {
        byte[] characterData = new byte[] {2, 2, 2, 3, 3, 4};
        byte payloadSequenceNumber = 1;

        SolInboundMessage payload = prepareMockPayload(characterData, payloadSequenceNumber);

        solMessageListener.notify(payload);

        SolCoder expectedAckCoder = new SolCoder(payloadSequenceNumber, (byte) characterData.length, SolAckState.ACK, connectionHandle.getCipherSuite());
        verify(connector).sendOneWayMessage(eq(connectionHandle), eq(expectedAckCoder));
    }

    @Test
    public void notifyWhenTooLongMessageCame() throws Exception {
        byte[] characterData = new byte[3000];
        byte payloadSequenceNumber = 4;

        SolInboundMessage payload = prepareMockPayload(characterData, payloadSequenceNumber);

        solMessageListener.notify(payload);

        SolCoder expectedNackCoder = new SolCoder(payloadSequenceNumber, (byte) 0, SolAckState.NACK, connectionHandle.getCipherSuite());
        verify(connector).sendOneWayMessage(eq(connectionHandle), eq(expectedNackCoder));
    }

    @Test
    public void notifyWhenMessageCameAndBufferIsFull() throws Exception {
        SolInboundMessage payload = prepareMockPayload(new byte[InboundSolMessageListener.BUFFER_CAPACITY - 100], (byte) 11);
        solMessageListener.notify(payload);

        byte[] characterData = new byte[120];
        byte payloadSequenceNumber = 12;

        payload = prepareMockPayload(characterData, payloadSequenceNumber);

        solMessageListener.notify(payload);

        SolCoder expectedNackCoder = new SolCoder(payloadSequenceNumber, (byte) 0, SolAckState.NACK, connectionHandle.getCipherSuite());
        verify(connector).sendOneWayMessage(eq(connectionHandle), eq(expectedNackCoder));
    }

    @Test
    public void shouldNotifyEventListenersWhenMessageWithStatusesCame() throws Exception {
        byte payloadSequenceNumber = 1;
        Set<SolStatus> statuses = new HashSet<SolStatus>() {{
            add(SolStatus.Break);
            add(SolStatus.TransmitOverrun);
        }};

        SolEventListener eventListener = mock(SolEventListener.class);
        eventListeners.add(eventListener);

        SolInboundMessage payload = prepareMockPayload(new byte[0], payloadSequenceNumber, statuses);

        solMessageListener.notify(payload);

        verify(eventListener).processRequestEvent(statuses);
    }

    @Test
    public void shouldNotNotifyEventListenersWhenMessageWithoutStatusesCame() throws Exception {
        byte[] characterData = new byte[5];
        byte payloadSequenceNumber = 1;

        SolEventListener eventListener = mock(SolEventListener.class);
        eventListeners.add(eventListener);

        SolInboundMessage payload = prepareMockPayload(characterData, payloadSequenceNumber);

        solMessageListener.notify(payload);

        SolCoder expectedAckCoder = new SolCoder(payloadSequenceNumber, (byte) characterData.length, SolAckState.ACK, connectionHandle.getCipherSuite());
        verify(connector).sendOneWayMessage(eq(connectionHandle), eq(expectedAckCoder));
        verify(eventListener, never()).processRequestEvent(any(Set.class));
    }

    @Test
    public void shouldSendAckJustOnceWhenMessageCameWithBothStatusesAndCharacterData() throws Exception {
        byte[] characterData = new byte[9];
        byte payloadSequenceNumber = 1;
        Set<SolStatus> statuses = new HashSet<SolStatus>() {{
            add(SolStatus.CharacterTransferUnavailable);
        }};

        SolEventListener eventListener = mock(SolEventListener.class);
        eventListeners.add(eventListener);

        SolInboundMessage payload = prepareMockPayload(characterData, payloadSequenceNumber);

        solMessageListener.notify(payload);

        SolCoder expectedAckCoder = new SolCoder(payloadSequenceNumber, (byte) characterData.length, SolAckState.ACK, connectionHandle.getCipherSuite());
        verify(connector).sendOneWayMessage(eq(connectionHandle), eq(expectedAckCoder));

        SolCoder notExpectedAckCoder = new SolCoder(payloadSequenceNumber, (byte) 0, SolAckState.ACK, connectionHandle.getCipherSuite());
        verify(connector, never()).sendOneWayMessage(eq(connectionHandle), eq(notExpectedAckCoder));
        verify(eventListener, never()).processRequestEvent(any(Set.class));
    }

    @Test
    public void readBytesWhenNoMessages() throws Exception {
        byte[] bytesRead = solMessageListener.readBytes(12);
        assertEquals(0, bytesRead.length);
    }

    @Test
    public void readBytesAtOnceWhenMessageCame() throws Exception {
        byte[] characterData = new byte[] {7, 7, 7};

        SolInboundMessage payload = prepareMockPayload(characterData, (byte) 1);
        solMessageListener.notify(payload);

        byte[] bytesRead = solMessageListener.readBytes(characterData.length);
        assertArrayEquals(characterData, bytesRead);
    }

    @Test
    public void readBytesDividedWhenMessageCame() throws Exception {
        byte[] characterData = new byte[] {1, 2, 3, 4, 5, 6};

        SolInboundMessage payload = prepareMockPayload(characterData, (byte) 1);
        solMessageListener.notify(payload);

        byte[] bytesRead = solMessageListener.readBytes(2);
        assertArrayEquals(new byte[] {1, 2}, bytesRead);

        bytesRead = solMessageListener.readBytes(3);
        assertArrayEquals(new byte[] {3, 4, 5}, bytesRead);
    }

    @Test
    public void readBytesSendsResumeAckWhenBufferFull() throws Exception {
        SolInboundMessage payload = prepareMockPayload(new byte[InboundSolMessageListener.BUFFER_CAPACITY - 5], (byte) 1);
        solMessageListener.notify(payload);

        byte[] characterData = new byte[8];
        byte sequenceNumber = 2;

        payload = prepareMockPayload(characterData, sequenceNumber);
        solMessageListener.notify(payload);

        solMessageListener.readBytes(10);

        SolCoder expectedResumeAckCoder = new SolCoder(sequenceNumber, (byte) 0, SolAckState.ACK, connectionHandle.getCipherSuite());
        verify(connector).sendOneWayMessage(eq(connectionHandle), eq(expectedResumeAckCoder));
    }

    @Test
    public void readBytesDoesntSendResumeAckWhenNotEnoughSpaceAfterRead() throws Exception {
        SolInboundMessage payload = prepareMockPayload(new byte[InboundSolMessageListener.BUFFER_CAPACITY - 4], (byte) 1);
        solMessageListener.notify(payload);

        byte[] characterData = new byte[12];
        byte sequenceNumber = 2;

        payload = prepareMockPayload(characterData, sequenceNumber);
        solMessageListener.notify(payload);

        solMessageListener.readBytes(7);

        SolCoder expectedResumeAckCoder = new SolCoder(sequenceNumber, (byte) 0, SolAckState.ACK, connectionHandle.getCipherSuite());
        verify(connector, never()).sendOneWayMessage(eq(connectionHandle), eq(expectedResumeAckCoder));
    }

    @Test
    public void shouldReturn0WhenGetAvailableByteCountAndEmptyBuffer() throws Exception {
        assertEquals(0, solMessageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReturnProperValueWhenGetAvailableByteCountAndMessageCame() throws Exception {
        byte[] characterData = new byte[] {2, 8, 14, 33};

        SolInboundMessage payload = prepareMockPayload(characterData, (byte) 1);
        solMessageListener.notify(payload);

        assertEquals(characterData.length, solMessageListener.getAvailableBytesCount());
    }

    private SolInboundMessage prepareMockPayload(byte[] characterData, byte payloadSequenceNumber) {
        return prepareMockPayload(characterData, payloadSequenceNumber, new HashSet<SolStatus>());
    }

    private SolInboundMessage prepareMockPayload(byte[] characterData, byte payloadSequenceNumber, Set<SolStatus> statuses) {
        SolInboundMessage payload = mock(SolInboundMessage.class);
        when(payload.getData()).thenReturn(characterData);
        when(payload.getSequenceNumber()).thenReturn(payloadSequenceNumber);
        when(payload.getStatusField()).thenReturn(new SolInboundStatusField(statuses));

        return payload;
    }
}