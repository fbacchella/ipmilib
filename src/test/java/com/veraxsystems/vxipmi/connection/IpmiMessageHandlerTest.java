/*
 * IpmiMessageHandlerTest.java
 * Created on 2011-09-06
 *
 * Copyright (c) Verax Systems 2011.
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
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanMessage;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.events.Sendv20Message;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import com.veraxsystems.vxipmi.sm.states.Uninitialized;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IpmiMessageHandlerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Connection connection;

    private int sessionId;
    private CipherSuite cipherSuite;
    private IpmiMessageHandler messageHandler;

    @Before
    public void setUp() throws Exception {
        this.messageHandler = new IpmiMessageHandler(connection, 10000);
        sessionId = 1;
        cipherSuite = CipherSuite.getEmpty();

        when(connection.getNextSessionSequenceNumber()).thenReturn(15);
    }

    @After
    public void tearDown() throws Exception {
        messageHandler.tearDown();
    }

    @Test
    public void sendMessageWhenInvalidState() throws Exception {
        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new Uninitialized());

        expectedException.expect(ConnectionException.class);

        messageHandler.sendMessage(coder, stateMachine, sessionId, false);
    }

    @Test
    public void sendMessageWhenSessionValid() throws Exception {
        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));

        int sequenceNumber = messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        assertEquals(1, sequenceNumber);
        verify(stateMachine).doTransition(eq(new Sendv20Message(coder, sessionId, sequenceNumber, connection.getNextSessionSequenceNumber())));
    }

    @Test
    public void sendMessageWhenSomeMessagesAlreadySent() throws Exception {
        messageHandler.setTimeout(-1);
        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));

        for (int i = 0; i < 10; ++i) {
            messageHandler.sendMessage(coder, stateMachine, sessionId, false);
        }

        int sequenceNumber = messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        assertEquals(11, sequenceNumber);
        verify(stateMachine).doTransition(eq(new Sendv20Message(coder, sessionId, sequenceNumber, connection.getNextSessionSequenceNumber())));
    }

    @Test
    public void sendMessageWhenMaxSequenceReached() throws Exception {
        messageHandler.setTimeout(-1);
        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));

        for (int i = 0; i < IpmiLanMessage.MAX_SEQUENCE_NUMBER; ++i) {
            messageHandler.sendMessage(coder, stateMachine, sessionId, false);
        }

        int sequenceNumber = messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        assertEquals(IpmiLanMessage.MIN_SEQUENCE_NUMBER, sequenceNumber);
        verify(stateMachine, times(2))
                .doTransition(eq(new Sendv20Message(coder, sessionId, IpmiLanMessage.MIN_SEQUENCE_NUMBER, connection.getNextSessionSequenceNumber())));
    }

    @Test
    public void handleIncomingMessageWhenPayloadIsLan() throws Exception {
        byte sequenceNumber = 1;

        ResponseData responseData = mock(ResponseData.class);

        IpmiLanResponse payload = mock(IpmiLanResponse.class);
        when(payload.getSequenceNumber()).thenReturn(sequenceNumber);

        Ipmiv20Message message = mock(Ipmiv20Message.class);
        when(message.getSessionID()).thenReturn(sessionId);
        when(message.getPayload()).thenReturn(payload);

        PayloadCoder coder = mock(IpmiCommandCoder.class);
        when(coder.getResponseData(message)).thenReturn(responseData);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));
        messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        messageHandler.handleIncomingMessage(message);

        verify(coder).getResponseData(message);
        verify(connection).notifyResponseListeners(eq(connection.getHandle()), eq(sequenceNumber), eq(responseData), isNull(Exception.class));
    }

    @Test
    public void handleIncomingMessageWhenPayloadIsPlain() throws Exception {
        Ipmiv20Message message = mock(Ipmiv20Message.class);
        when(message.getSessionID()).thenReturn(sessionId);

        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));
        messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        messageHandler.handleIncomingMessage(message);

        verify(coder, never()).getResponseData(message);
        verify(connection, never()).notifyResponseListeners(anyInt(), anyInt(), any(ResponseData.class), any(Exception.class));
    }

    @Test
    public void handleIncomingMessageWhenSessionIdOutOfWindow() throws Exception {
        IpmiLanResponse payload = mock(IpmiLanResponse.class);
        when(payload.getSequenceNumber()).thenReturn((byte) 1);

        Ipmiv20Message message = mock(Ipmiv20Message.class);
        when(message.getSessionID()).thenReturn(sessionId);
        when(message.getPayload()).thenReturn(payload);
        when(message.getSessionSequenceNumber()).thenReturn(20);

        PayloadCoder coder = mock(IpmiCommandCoder.class);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));
        messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        messageHandler.handleIncomingMessage(message);

        verify(coder, never()).getResponseData(message);
        verify(connection, never()).notifyResponseListeners(anyInt(), anyInt(), any(ResponseData.class), any(Exception.class));
    }

    @Test
    public void handleIncomingMessageWhenExceptionOccured() throws Exception {
        int sequenceNumber = 1;

        ResponseData responseData = mock(ResponseData.class);

        IpmiLanResponse payload = mock(IpmiLanResponse.class);
        when(payload.getSequenceNumber()).thenReturn((byte) sequenceNumber);

        Ipmiv20Message message = mock(Ipmiv20Message.class);
        when(message.getSessionID()).thenReturn(sessionId);
        when(message.getPayload()).thenReturn(payload);

        PayloadCoder coder = mock(IpmiCommandCoder.class);
        when(coder.getResponseData(message)).thenReturn(responseData);

        StateMachine stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrent()).thenReturn(new SessionValid(cipherSuite, sessionId));
        messageHandler.sendMessage(coder, stateMachine, sessionId, false);

        Exception testException = new IllegalArgumentException("Test exception");

        doThrow(testException)
                .when(connection).notifyResponseListeners(anyInt(), anyInt(), eq(responseData), isNull(Exception.class));

        messageHandler.handleIncomingMessage(message);

        verify(coder).getResponseData(message);
        verify(connection).notifyResponseListeners(anyInt(), eq(sequenceNumber), eq(responseData), isNull(Exception.class));
        verify(connection).notifyResponseListeners(anyInt(), eq(sequenceNumber), isNull(ResponseData.class), eq(testException));
    }
}