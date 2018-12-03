/*
 * SolMessageHandlerTest.java
 * Created on 25.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.connection;

import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.sol.SolAckState;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundStatusField;
import com.veraxsystems.vxipmi.coding.payload.sol.SolMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.sol.SolCoder;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.events.Sendv20Message;
import com.veraxsystems.vxipmi.sm.states.CiphersWaiting;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolMessageHandlerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Connection connection;

    private int sessionId;
    private CipherSuite cipherSuite;
    private SolMessageHandler messageHandler;

    @Before
    public void setUp() throws Exception {
        this.messageHandler = new SolMessageHandler(connection, 10000);
        sessionId = 1;
        cipherSuite = CipherSuite.getEmpty();

        when(connection.getNextSessionSequenceNumber()).thenReturn(11);
    }

    @After
    public void tearDown() throws Exception {
        messageHandler.tearDown();
    }

    @Test
    public void sendMessageWhenSessionValid() throws Exception {
        PayloadCoder coder = mock(SolCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));

        int sequenceNumber = messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        assertEquals(1, sequenceNumber);
        verify(stateMachine).doTransition(eq(new Sendv20Message(coder, sessionId, sequenceNumber, connection.getNextSessionSequenceNumber())));
    }

    @Test
    public void sendMessageWhenSomeMessagesAlreadySent() throws Exception {
        messageHandler.setTimeout(-1);
        PayloadCoder coder = mock(SolCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));

        for (int i = 0; i < 7; ++i) {
            messageHandler.sendMessage(coder, stateMachine, sessionId, false);
        }

        int sequenceNumber = messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        assertEquals(8, sequenceNumber);
        verify(stateMachine).doTransition(eq(new Sendv20Message(coder, sessionId, sequenceNumber, connection.getNextSessionSequenceNumber())));
    }

    @Test
    public void sendMessageWhen15MessagesAlreadySent() throws Exception {
        messageHandler.setTimeout(-1);
        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));

        for (int i = 0; i < SolMessage.MAX_SEQUENCE_NUMBER; ++i) {
            messageHandler.sendMessage(coder, stateMachine, sessionId, false);
        }

        int sequenceNumber = messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        assertEquals(SolMessage.MIN_SEQUENCE_NUMBER, sequenceNumber);
        verify(stateMachine, times(2))
                .doTransition(eq(new Sendv20Message(coder, sessionId, SolMessage.MIN_SEQUENCE_NUMBER, connection.getNextSessionSequenceNumber())));
    }

    @Test
    public void shouldNotSendAnyMessageWhenRetryAndNoMessageWithSuchTag() throws Exception {
        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));

        int retriedTag = messageHandler.retryMessage(2, stateMachine, sessionId);

        assertEquals(-1, retriedTag);
        verify(stateMachine, never()).doTransition(any(Sendv20Message.class));
    }

    @Test
    public void shouldSendTheSameMessageWhenRetry() throws Exception {
        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));

        int tag = messageHandler.sendMessage(coder, stateMachine, sessionId, false);
        int retriedTag = messageHandler.retryMessage(tag, stateMachine, sessionId);

        assertEquals(tag, retriedTag);
        verify(stateMachine, times(2))
                .doTransition(eq(new Sendv20Message(coder, sessionId, SolMessage.MIN_SEQUENCE_NUMBER, connection.getNextSessionSequenceNumber())));
    }

    @Test
    public void shouldThrowExceptionWhenRetryAndSessionNotValid() throws Exception {
        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new CiphersWaiting(1, 1));

        expectedException.expect(ConnectionException.class);

        messageHandler.sendMessage(coder, stateMachine, sessionId, false);
    }

    @Test
    public void handleIncomingMessageWhenAcknowledge() throws Exception {
        byte sequenceNumber = 1;

        ResponseData responseData = mock(ResponseData.class);

        SolInboundStatusField payloadStatusField = new SolInboundStatusField(SolAckState.ACK, Collections.<SolStatus>emptySet());

        SolInboundMessage payload = mock(SolInboundMessage.class);
        when(payload.getAckNackSequenceNumber()).thenReturn(sequenceNumber);
        when(payload.isAcknowledgeMessage()).thenReturn(true);
        when(payload.getStatusField()).thenReturn(payloadStatusField);

        Ipmiv20Message message = mock(Ipmiv20Message.class);
        when(message.getSessionID()).thenReturn(sessionId);
        when(message.getPayload()).thenReturn(payload);

        PayloadCoder coder = mock(SolCoder.class);
        when(coder.getResponseData(message)).thenReturn(responseData);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));
        messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        messageHandler.handleIncomingMessage(message);

        verify(coder).getResponseData(message);
        verify(connection).notifyResponseListeners(eq(connection.getHandle()), eq(sequenceNumber), eq(responseData), isNull(Exception.class));
    }

    @Test
    public void handleIncomingMessageWhenDataCarrier() throws Exception {
        byte sequenceNumber = 1;

        SolInboundMessage payload = mock(SolInboundMessage.class);
        when(payload.getSequenceNumber()).thenReturn(sequenceNumber);
        when(payload.isDataCarrier()).thenReturn(true);

        Ipmiv20Message message = mock(Ipmiv20Message.class);
        when(message.getSessionID()).thenReturn(sessionId);
        when(message.getPayload()).thenReturn(payload);

        messageHandler.handleIncomingMessage(message);

        verify(connection).notifyRequestListeners(eq(payload));
    }
}