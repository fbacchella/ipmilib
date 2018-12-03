/*
 * SerialOverLanTest.java
 * Created on 02-06-2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.api.sol;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.async.InboundSolMessageListener;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.payload.ActivateSolPayload;
import com.veraxsystems.vxipmi.coding.commands.payload.ActivateSolPayloadResponseData;
import com.veraxsystems.vxipmi.coding.commands.payload.DeactivatePayload;
import com.veraxsystems.vxipmi.coding.commands.payload.GetPayloadActivationStatus;
import com.veraxsystems.vxipmi.coding.commands.payload.GetPayloadActivationStatusResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.SetSessionPrivilegeLevel;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.payload.sol.SolAckState;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundStatusField;
import com.veraxsystems.vxipmi.coding.payload.sol.SolMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolOperation;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.sol.SolCoder;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.connection.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SerialOverLanTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private CipherSuite cipherSuite = CipherSuite.getEmpty();

    @Mock
    private IpmiConnector connector;

    @Mock
    private ConnectionHandle connectionHandle;

    @Mock
    private Session session;

    private SerialOverLan serialOverLan;
    private int payloadSize = 50;

    private String remoteHost = "1.2.3.4";
    private int remotePort = 555;
    private String user = "user";
    private String password = "password";

    private GetPayloadActivationStatus getPayloadActivationStatus;
    private GetPayloadActivationStatusResponseData getPayloadActivationStatusResponse;
    private ActivateSolPayload activateSolPayload;
    private ActivateSolPayloadResponseData activateSolPayloadResponse;

    @Before
    public void setUp() throws Exception {
        setupConnectionHandleMock();
        setupSessionMock();
        setupConnectorMock();

        serialOverLan = new SerialOverLan(connector, session);
    }

    private void setupConnectionHandleMock() throws UnknownHostException {
        when(connectionHandle.getCipherSuite()).thenReturn(cipherSuite);
        when(connectionHandle.getRemoteAddress()).thenReturn(InetAddress.getByName(remoteHost));
        when(connectionHandle.getRemotePort()).thenReturn(remotePort);
        when(connectionHandle.getUser()).thenReturn(user);
        when(connectionHandle.getPassword()).thenReturn(password);
    }

    private void setupSessionMock() {
        when(session.getConnectionHandle()).thenReturn(connectionHandle);
    }

    private void setupConnectorMock() throws Exception {
        getPayloadActivationStatus = new GetPayloadActivationStatus(cipherSuite, PayloadType.Sol);

        getPayloadActivationStatusResponse = mock(GetPayloadActivationStatusResponseData.class);
        when(getPayloadActivationStatusResponse.getInstanceCapacity()).thenReturn((byte) 1);
        when(getPayloadActivationStatusResponse.getAvailableInstances()).thenReturn(Collections.singletonList((byte) 1));

        when(connector.sendMessage(any(ConnectionHandle.class), eq(getPayloadActivationStatus))).thenReturn(getPayloadActivationStatusResponse);

        activateSolPayload = new ActivateSolPayload(cipherSuite, 1);

        activateSolPayloadResponse = mock(ActivateSolPayloadResponseData.class);
        when(activateSolPayloadResponse.getInboundPayloadSize()).thenReturn(payloadSize);
        when(activateSolPayloadResponse.getOutboundPayloadSize()).thenReturn(payloadSize);
        when(activateSolPayloadResponse.getPayloadUdpPortNumber()).thenReturn(remotePort);
        when(activateSolPayloadResponse.getPayloadVlanNumber()).thenReturn(0);

        when(connector.sendMessage(any(ConnectionHandle.class), eq(activateSolPayload))).thenReturn(activateSolPayloadResponse);

        when(connector.getRetries()).thenReturn(3);
    }

    @Test
    public void shouldThrowExceptionWhenNoInstancesForActivationAvailable() throws Exception {
        when(getPayloadActivationStatusResponse.getInstanceCapacity()).thenReturn((byte) 0);

        expectedException.expect(SOLException.class);

        new SerialOverLan(connector, session);
    }

    @Test
    public void shouldThrowExceptionWhenAllSolInstancesActivated() throws Exception {
        when(getPayloadActivationStatusResponse.getInstanceCapacity()).thenReturn((byte) 3);
        when(getPayloadActivationStatusResponse.getAvailableInstances()).thenReturn(new LinkedList<Byte>());

        expectedException.expect(SOLException.class);

        new SerialOverLan(connector, session);
    }

    @Test
    public void shouldSuccessfullySendByteArrayWhenWriteBytes() throws Exception {
        byte[] bytesToWrite =  new byte[] {12, 13, 14, 15};

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) bytesToWrite.length);
        PayloadCoder payloadCoder = preparePayloadCoder(bytesToWrite, responseMessage);

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertTrue("Writing byte array should be successful", success);
    }

    @Test
    public void shouldFailSendingByteArrayWhenExceptionOccurs() throws Exception {
        byte[] bytesToWrite = new byte[] {1, 5, 7, 9};

        doThrow(new IllegalStateException("EXPECTED test exception")).when(connector).sendMessage(any(ConnectionHandle.class), any(PayloadCoder.class));

        PayloadCoder payloadCoder = preparePayloadCoder(bytesToWrite, null);

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Writing byte array after exception is thrown should fail", success);
    }

    @Test
    public void shouldFailSendingByteArrayWhenNackForWholePacket() throws Exception {
        byte[] bytesToWrite = new byte[] {2, 3};
        byte requestSequenceNumber = 1;

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, requestSequenceNumber, (byte) 0);
        PayloadCoder payloadCoder = preparePayloadCoder(bytesToWrite, responseMessage);

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Writing byte array should fail when packet was NACKed", success);
    }

    @Test
    public void shouldRetrySendingByteArrayWhenNackForWholePacket() throws Exception {
        byte[] bytesToWrite = new byte[] {7, 8, 9, 10};
        byte requestSequenceNumber = 1;

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, requestSequenceNumber, (byte) 0);
        preparePayloadCoder(bytesToWrite, responseMessage);

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector, times(connector.getRetries())).retryMessage(eq(connectionHandle), eq(requestSequenceNumber), eq(PayloadType.Sol));
        assertFalse("Writing byte array should fail when packet was NACKed and retries also failed", success);
    }

    @Test
    public void shouldSucceedSendingByteArrayWhenNackFirstAndAckRetryForWholePacket() throws Exception {
        byte[] bytesToWrite = new byte[] {2, 2, 2};
        byte requestSequenceNumber = 1;

        IpmiMessage nackResponseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, requestSequenceNumber, (byte) 0);
        PayloadCoder firstPayloadCoder = new SolCoder(bytesToWrite, cipherSuite);
        doReturn(firstPayloadCoder.getResponseData(nackResponseMessage))
                .when(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));

        IpmiMessage ackResponseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, requestSequenceNumber, (byte) bytesToWrite.length);
        PayloadCoder secondPayloadCoder = new SolCoder(bytesToWrite, cipherSuite);
        doReturn(secondPayloadCoder.getResponseData(ackResponseMessage))
                .when(connector).retryMessage(eq(connectionHandle), eq(requestSequenceNumber), eq(PayloadType.Sol));

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));
        verify(connector).retryMessage(eq(connectionHandle), eq(requestSequenceNumber), eq(PayloadType.Sol));
        assertTrue("Writing byte array should succeed when packet was ACKed after retry", success);
    }

    @Test
    public void shouldFailSendingByteArrayWhenNackForPartialPacket() throws Exception {
        byte[] bytesToWrite = new byte[] {7, 8, 9};
        byte acceptedCharacters = 2;
        byte[] remainingBytesToWrite = Arrays.copyOfRange(bytesToWrite, acceptedCharacters, bytesToWrite.length);

        IpmiMessage firstResponseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, (byte) 1, acceptedCharacters);
        PayloadCoder firstPayloadCoder = new SolCoder(bytesToWrite, cipherSuite);
        doReturn(firstPayloadCoder.getResponseData(firstResponseMessage))
                .when(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));

        IpmiMessage secondResponseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, (byte) 1, (byte) 0);
        PayloadCoder secondPayloadCoder = new SolCoder(remainingBytesToWrite, cipherSuite);
        doReturn(secondPayloadCoder.getResponseData(secondResponseMessage))
                .when(connector).sendMessage(eq(connectionHandle), eq(secondPayloadCoder));

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));
        verify(connector).sendMessage(eq(connectionHandle), eq(secondPayloadCoder));
        assertFalse("Writing byte array should fail when packet was NACKed", success);
    }

    @Test
    public void shouldSendNewPacketWithJustNackedCharactersWhenNackForPartialPacket() throws Exception {
        byte[] bytesToWrite = new byte[] {1, 2, 3, 4, 5};
        byte numberOfAcceptedCharacters = 3;
        byte[] bytesToResend = Arrays.copyOfRange(bytesToWrite, numberOfAcceptedCharacters, bytesToWrite.length);

        IpmiMessage firstResponseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, (byte) 1, numberOfAcceptedCharacters);
        PayloadCoder fullPayloadCoder = new SolCoder(bytesToWrite, cipherSuite);
        doReturn(fullPayloadCoder.getResponseData(firstResponseMessage))
                .when(connector).sendMessage(eq(connectionHandle), eq(fullPayloadCoder));

        IpmiMessage secondResponseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 2,
                (byte) (bytesToWrite.length - numberOfAcceptedCharacters));
        PayloadCoder remainingPayloadCoder = new SolCoder(bytesToResend, cipherSuite);
        doReturn(remainingPayloadCoder.getResponseData(secondResponseMessage))
                .when(connector).sendMessage(eq(connectionHandle), eq(remainingPayloadCoder));

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(fullPayloadCoder));
        verify(connector).sendMessage(eq(connectionHandle), eq(remainingPayloadCoder));
        verify(connector, never()).retryMessage(any(ConnectionHandle.class), anyByte(), any(PayloadType.class));
        assertTrue("Writing byte array should success when part of the packet was NACKed but, after resend, it was ACKed", success);
    }

    @Test
    public void shouldSendNewPacketWithJustNackedCharactersWhenNackForParialPacketTwice() throws Exception {
        byte[] bytesToWrite = new byte[] {1, 2, 3, 4, 5};
        byte numberOfAcceptedCharactersFirstTime = 2;
        byte[] bytesToResendFirstTime = Arrays.copyOfRange(bytesToWrite, numberOfAcceptedCharactersFirstTime, bytesToWrite.length);
        byte numberOfAcceptedCharactersSecondTime = 2;
        byte[] bytesToResendSecondTime = Arrays.copyOfRange(bytesToResendFirstTime, numberOfAcceptedCharactersSecondTime, bytesToResendFirstTime.length);

        IpmiMessage firstResponseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, (byte) 1, numberOfAcceptedCharactersFirstTime);
        PayloadCoder firstPayloadCoder = new SolCoder(bytesToWrite, cipherSuite);
        doReturn(firstPayloadCoder.getResponseData(firstResponseMessage))
                .when(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));

        IpmiMessage secondResponseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, (byte) 2, numberOfAcceptedCharactersSecondTime);
        PayloadCoder secondPayloadCoder = new SolCoder(bytesToResendFirstTime, cipherSuite);
        doReturn(secondPayloadCoder.getResponseData(secondResponseMessage))
                .when(connector).sendMessage(eq(connectionHandle), eq(secondPayloadCoder));

        IpmiMessage thirdResponseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 3,
                (byte) (bytesToResendFirstTime.length - numberOfAcceptedCharactersSecondTime));
        PayloadCoder thirdPayloadCoder = new SolCoder(bytesToResendSecondTime, cipherSuite);
        doReturn(thirdPayloadCoder.getResponseData(thirdResponseMessage))
                .when(connector).sendMessage(eq(connectionHandle), eq(thirdPayloadCoder));

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));
        verify(connector).sendMessage(eq(connectionHandle), eq(secondPayloadCoder));
        verify(connector).sendMessage(eq(connectionHandle), eq(thirdPayloadCoder));
        verify(connector, never()).retryMessage(any(ConnectionHandle.class), anyByte(), any(PayloadType.class));
        assertTrue("Writing byte array should success when part of the packet was NACKed but, after resend, it was ACKed", success);
    }

    @Test
    public void shouldSendMessageInTwoTurnsWhenTooBig() throws Exception {
        byte maxBufferSize = (byte) (payloadSize - SolMessage.PAYLOAD_HEADER_LENGTH);
        int overflow = 17;
        byte[] bytesToWrite = new byte[maxBufferSize + overflow];

        IpmiMessage firstResponseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, maxBufferSize);
        PayloadCoder firstPayloadCoder = preparePayloadCoder(Arrays.copyOf(bytesToWrite, maxBufferSize),
                firstResponseMessage);

        IpmiMessage secondResponseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 2, (byte) overflow);
        PayloadCoder secondPayloadCoder = preparePayloadCoder(Arrays.copyOfRange(bytesToWrite, maxBufferSize, bytesToWrite.length),
                secondResponseMessage);

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));
        verify(connector).sendMessage(eq(connectionHandle), eq(secondPayloadCoder));
        assertTrue("Should successfully send whole big message", success);
    }

    @Test
    public void shouldFailImmediatellyWhenSendingTooBigMessageAndExceptionAfterFirstPart() throws Exception {
        byte maxBufferSize = (byte) (payloadSize - SolMessage.PAYLOAD_HEADER_LENGTH);
        int overflow = 2;
        byte[] bytesToWrite = new byte[maxBufferSize + overflow];

        PayloadCoder firstPayloadCoder = preparePayloadCoder(Arrays.copyOf(bytesToWrite, maxBufferSize), null);

        doThrow(IllegalStateException.class).when(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));
        assertFalse("Should fail immediatelly when sending first part of message", success);
    }

    @Test
    public void shouldFailImmediatellyWhenSendingTooBigMessageAndFirstPartNacked() throws Exception {
        byte maxBufferSize = (byte) (payloadSize - SolMessage.PAYLOAD_HEADER_LENGTH);
        int overflow = 22;
        byte[] bytesToWrite = new byte[maxBufferSize + overflow];

        IpmiMessage firstResponseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, (byte) 1, (byte) 0);
        PayloadCoder firstPayloadCoder = preparePayloadCoder(Arrays.copyOf(bytesToWrite, maxBufferSize),
                firstResponseMessage);

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));
        assertFalse("Should fail immediatelly when sending first part of message", success);
    }

    @Test
    public void shouldFailWhenSendingTooBigMessageAndLastPartNacked() throws Exception {
        byte maxBufferSize = (byte) (payloadSize - SolMessage.PAYLOAD_HEADER_LENGTH);
        int overflow = 10;
        byte[] bytesToWrite = new byte[maxBufferSize + overflow];

        IpmiMessage firstResponseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, maxBufferSize);
        PayloadCoder firstPayloadCoder = preparePayloadCoder(Arrays.copyOf(bytesToWrite, maxBufferSize),
                firstResponseMessage);

        IpmiMessage secondResponseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, (byte) 2, (byte) 0);
        PayloadCoder secondPayloadCoder = preparePayloadCoder(Arrays.copyOfRange(bytesToWrite, maxBufferSize, bytesToWrite.length),
                secondResponseMessage);

        boolean success = serialOverLan.writeBytes(bytesToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(firstPayloadCoder));
        verify(connector).sendMessage(eq(connectionHandle), eq(secondPayloadCoder));
        assertFalse("Should fail when sending big message when last part failed", success);
    }

    @Test
    public void shouldSuccessfullySendSingleByteWhenWriteByte() throws Exception {
        byte byteToWrite =  7;

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) 1);
        PayloadCoder payloadCoder = preparePayloadCoder(new byte[] {byteToWrite}, responseMessage);

        boolean success = serialOverLan.writeByte(byteToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertTrue("Writing byte array should be successful", success);
    }

    @Test
    public void shouldFailSendingSingleByteWhenExceptionOccurs() throws Exception {
        byte byteToWrite =  16;

        doThrow(new IllegalStateException("EXPECTED test exception")).when(connector).sendMessage(any(ConnectionHandle.class), any(PayloadCoder.class));

        PayloadCoder payloadCoder = preparePayloadCoder(new byte[] {byteToWrite}, null);

        boolean success = serialOverLan.writeByte(byteToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Writing byte array after exception is thrown should fail", success);
    }

    @Test
    public void shouldSuccessfullySendSingleIntWhenWriteInt() throws Exception {
        int intToWrite =  214;

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) 1);
        PayloadCoder payloadCoder = preparePayloadCoder(new byte[] {TypeConverter.intToByte(intToWrite)}, responseMessage);

        boolean success = serialOverLan.writeInt(intToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertTrue("Writing byte array should be successful", success);
    }

    @Test
    public void shouldFailSendingSingleIntWhenExceptionOccurs() throws Exception {
        int intToWrite =  6;

        doThrow(new IllegalStateException("EXPECTED test exception")).when(connector).sendMessage(any(ConnectionHandle.class), any(PayloadCoder.class));

        PayloadCoder payloadCoder = preparePayloadCoder(new byte[] {TypeConverter.intToByte(intToWrite)}, null);

        boolean success = serialOverLan.writeInt(intToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Writing byte array after exception is thrown should fail", success);
    }

    @Test
    public void shouldThrowExceptionWhenWriteIntLessThan0() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        serialOverLan.writeInt(-1);
    }

    @Test
    public void shouldThrowExceptionWhenWriteIntGreaterThan255() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        serialOverLan.writeInt(256);
    }

    @Test
    public void shouldSuccessfullySendIntArrayWhenWriteIntArray() throws Exception {
        int[] intsToWrite =  new int[] {1, 2, 12, 17};
        byte[] intsConvertedToBytes = new byte[intsToWrite.length];

        for (int i = 0; i < intsToWrite.length; i++) {
            intsConvertedToBytes[i] = TypeConverter.intToByte(intsToWrite[i]);
        }

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) intsToWrite.length);
        PayloadCoder payloadCoder = preparePayloadCoder(intsConvertedToBytes, responseMessage);

        boolean success = serialOverLan.writeIntArray(intsToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertTrue("Writing byte array should be successful", success);
    }

    @Test
    public void shouldFailSendingIntArrayWhenExceptionOccurs() throws Exception {
        int[] intsToWrite =  new int[] {3, 19, 20};
        byte[] intsConvertedToBytes = new byte[intsToWrite.length];

        for (int i = 0; i < intsToWrite.length; i++) {
            intsConvertedToBytes[i] = TypeConverter.intToByte(intsToWrite[i]);
        }

        doThrow(new IllegalStateException("EXPECTED test exception")).when(connector).sendMessage(any(ConnectionHandle.class), any(PayloadCoder.class));

        PayloadCoder payloadCoder = preparePayloadCoder(intsConvertedToBytes, null);

        boolean success = serialOverLan.writeIntArray(intsToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Writing byte array after exception is thrown should fail", success);
    }

    @Test
    public void shouldThrowExceptionWhenWriteIntArrayWithIntLessThan0() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        serialOverLan.writeIntArray(new int[] {2, 7, -1, 4, 6});
    }

    @Test
    public void shouldThrowExceptionWhenWriteIntArrayWithIntGreaterThan255() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        serialOverLan.writeIntArray(new int[] {1, 17, 99, 256});
    }

    @Test
    public void shouldSuccessfullySendStringWhenWriteString() throws Exception {
        String textToWrite = "Ala ma kota";

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) textToWrite.length());
        PayloadCoder payloadCoder = preparePayloadCoder(textToWrite.getBytes(), responseMessage);

        boolean success = serialOverLan.writeString(textToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertTrue("Writing byte array should be successful", success);
    }

    @Test
    public void shouldFailSendingStringWhenExceptionOccurs() throws Exception {
        String textToWrite = "Kot ma Ale";

        doThrow(new IllegalStateException("EXPECTED test exception")).when(connector).sendMessage(any(ConnectionHandle.class), any(PayloadCoder.class));

        PayloadCoder payloadCoder = preparePayloadCoder(textToWrite.getBytes(), null);

        boolean success = serialOverLan.writeString(textToWrite);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Writing byte array after exception is thrown should fail", success);
    }

    @Test
    public void shouldSuccessfullySendStringWithCharsetWhenWriteString() throws Exception {
        String textToWrite = "Miękka mątwa";
        Charset charset = Charset.forName("UTF-8");

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) textToWrite.length());
        PayloadCoder payloadCoder = preparePayloadCoder(textToWrite.getBytes(charset), responseMessage);

        boolean success = serialOverLan.writeString(textToWrite, charset);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertTrue("Writing byte array should be successful", success);
    }

    @Test
    public void shouldFailSendingStringWithCharsetWhenExceptionOccurs() throws Exception {
        String textToWrite = "Gęźla jaźń";
        Charset charset = Charset.forName("UTF-8");

        doThrow(new IllegalStateException("EXPECTED test exception")).when(connector).sendMessage(any(ConnectionHandle.class), any(PayloadCoder.class));

        PayloadCoder payloadCoder = preparePayloadCoder(textToWrite.getBytes(charset), null);

        boolean success = serialOverLan.writeString(textToWrite, charset);

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Writing byte array after exception is thrown should fail", success);
    }

    @Test
    public void shouldReadEmptyByteArrayWhenNoBytesAvailable() throws Exception {
        verify(connector).registerIncomingMessageListener(any(InboundSolMessageListener.class));

        byte[] bytesRead = serialOverLan.readBytes();

        assertEquals(0, bytesRead.length);
    }

    @Test
    public void shouldReadAllAvailableBytesWhenMessageReceived() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        byte[] receivedBytes = new byte[] {1, 2, 3, 4, 5};
        receiveMessage(messageListener, (byte) 1, receivedBytes);

        byte[] bytesRead = serialOverLan.readBytes();

        assertArrayEquals(receivedBytes, bytesRead);
        assertEquals(0, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadEmptyByteArrayWhenNoBytesAvailableAndReadSpecificNumber() throws Exception {
        verify(connector).registerIncomingMessageListener(any(InboundSolMessageListener.class));

        byte[] bytesRead = serialOverLan.readBytes(15);

        assertEquals(0, bytesRead.length);
    }

    @Test
    public void shouldReadSpecificNumberOfBytesWhenMessageReceived() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        byte[] receivedBytes = new byte[] {1, 2, 3, 4, 5};
        receiveMessage(messageListener, (byte) 1, receivedBytes);

        int bytesToReadCount = 3;
        byte[] bytesRead = serialOverLan.readBytes(bytesToReadCount);

        assertArrayEquals(Arrays.copyOf(receivedBytes, bytesToReadCount), bytesRead);
        assertEquals(receivedBytes.length - bytesToReadCount, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadSpecificNumberOfBytesWhenMessageArrivedBeforeTimeout() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        byte[] receivedBytes = new byte[] {12, 22, 32, 42, 52};
        receiveMessage(messageListener, (byte) 1, receivedBytes);

        int bytesToReadCount = 2;
        byte[] bytesRead = serialOverLan.readBytes(bytesToReadCount, 2);

        assertArrayEquals(Arrays.copyOf(receivedBytes, bytesToReadCount), bytesRead);
        assertEquals(receivedBytes.length - bytesToReadCount, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadEmptyIntArrayWhenNoDataAvailable() throws Exception {
        verify(connector).registerIncomingMessageListener(any(InboundSolMessageListener.class));

        int[] intsRead = serialOverLan.readIntArray();

        assertEquals(0, intsRead.length);
    }

    @Test
    public void shouldReadAllAvailableIntegersWhenMessageReceived() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        byte[] receivedBytes = new byte[] {5, 6, 7, 8};
        receiveMessage(messageListener, (byte) 1, receivedBytes);

        int[] expectedIntArray = new int[receivedBytes.length];

        for (int i = 0; i < receivedBytes.length; i++) {
            expectedIntArray[i] = TypeConverter.byteToInt(receivedBytes[i]);
        }

        int[] integersRead = serialOverLan.readIntArray();

        assertArrayEquals(expectedIntArray, integersRead);
        assertEquals(0, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadEmptyIntArrayWhenNoDataAvailableAndReadSpecificNumber() throws Exception {
        verify(connector).registerIncomingMessageListener(any(InboundSolMessageListener.class));

        int[] intsRead = serialOverLan.readIntArray(1);

        assertEquals(0, intsRead.length);
    }

    @Test
    public void shouldReadSpecificNumberOfIntegersWhenMessageReceived() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        byte[] receivedBytes = new byte[] {7, 7, 7};
        receiveMessage(messageListener, (byte) 1, receivedBytes);

        int[] expectedIntArray = new int[receivedBytes.length];

        for (int i = 0; i < receivedBytes.length; i++) {
            expectedIntArray[i] = TypeConverter.byteToInt(receivedBytes[i]);
        }

        int bytesToReadCount = 2;
        int[] integersRead = serialOverLan.readIntArray(bytesToReadCount);

        assertArrayEquals(Arrays.copyOf(expectedIntArray, bytesToReadCount), integersRead);
        assertEquals(expectedIntArray.length - bytesToReadCount, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadSpecificNumberOfIntegersWhenMessageArrivedBeforeTimeout() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        byte[] receivedBytes = new byte[] {9, 10, 23, 12, 3};
        receiveMessage(messageListener, (byte) 1, receivedBytes);

        int[] expectedIntArray = new int[receivedBytes.length];

        for (int i = 0; i < receivedBytes.length; i++) {
            expectedIntArray[i] = TypeConverter.byteToInt(receivedBytes[i]);
        }

        int bytesToReadCount = 2;
        int[] integersRead = serialOverLan.readIntArray(bytesToReadCount, 2);

        assertArrayEquals(Arrays.copyOf(expectedIntArray, bytesToReadCount), integersRead);
        assertEquals(expectedIntArray.length - bytesToReadCount, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadEmptyStringWhenNoDataAvailable() throws Exception {
        verify(connector).registerIncomingMessageListener(any(InboundSolMessageListener.class));

        String stringRead = serialOverLan.readString();

        assertEquals(0, stringRead.length());
    }

    @Test
    public void shouldReadWholeAvailableStringWhenMessageReceived() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        String receivedString = "This is the whole message";
        receiveMessage(messageListener, (byte) 1, receivedString.getBytes());

        String stringRead = serialOverLan.readString();

        assertEquals(receivedString, stringRead);
        assertEquals(0, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadEmptyStringWhenNoDataAvailableAndReadSpecificNumber() throws Exception {
        verify(connector).registerIncomingMessageListener(any(InboundSolMessageListener.class));

        String stringRead = serialOverLan.readString(15);

        assertEquals(0, stringRead.length());
    }

    @Test
    public void shouldReadLimitedStringWhenMessageReceived() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        String receivedString = "The message that was received is longer than we want to read";
        receiveMessage(messageListener, (byte) 1, receivedString.getBytes());

        int bytesToReadCount = 29;
        String stringRead = serialOverLan.readString(bytesToReadCount);

        assertEquals(receivedString.substring(0, bytesToReadCount), stringRead);
        assertEquals(receivedString.length() - bytesToReadCount, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadLimitedStringWhenMessageArrivedBeforeTimeout() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        String receivedString = "Very short message";
        receiveMessage(messageListener, (byte) 1, receivedString.getBytes());

        int bytesToReadCount = 4;
        String stringRead = serialOverLan.readString(bytesToReadCount, 2);

        assertEquals(receivedString.substring(0, bytesToReadCount), stringRead);
        assertEquals(receivedString.length() - bytesToReadCount, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadEmptyStringWithCharsetWhenNoDataAvailable() throws Exception {
        verify(connector).registerIncomingMessageListener(any(InboundSolMessageListener.class));

        String stringRead = serialOverLan.readString(Charset.forName("UTF-8"));

        assertEquals(0, stringRead.length());
    }

    @Test
    public void shouldReadWholeAvailableStringWithCharsetWhenMessageReceived() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        Charset charset = Charset.forName("UTF-8");
        String receivedString = "給中國人留言";
        receiveMessage(messageListener, (byte) 1, receivedString.getBytes(charset));

        String stringRead = serialOverLan.readString(charset);

        assertEquals(receivedString, stringRead);
        assertEquals(0, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadEmptyStringWithCharsetWhenNoDataAvailableAndReadSpecificNumber() throws Exception {
        verify(connector).registerIncomingMessageListener(any(InboundSolMessageListener.class));

        String stringRead = serialOverLan.readString(Charset.forName("UTF-8"),15);

        assertEquals(0, stringRead.length());
    }

    @Test
    public void shouldReadLimitedStringWithCharsetWhenMessageReceived() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        Charset charset = Charset.forName("UTF-8");
        String receivedString = "texte français";
        receiveMessage(messageListener, (byte) 1, receivedString.getBytes(charset));

        int bytesToReadCount = 5;
        String stringRead = serialOverLan.readString(charset, bytesToReadCount);

        assertEquals("texte", stringRead);
        assertEquals(receivedString.getBytes(charset).length - bytesToReadCount, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldReadLimitedStringWithCharsetWhenMessageArrivedBeforeTimeout() throws Exception {
        InboundSolMessageListener messageListener = getSolMessageListener();

        Charset charset = Charset.forName("UTF-8");
        String receivedString = "Międlić na okrągło";
        receiveMessage(messageListener, (byte) 1, receivedString.getBytes(charset));

        int bytesToReadCount = 10;
        String stringRead = serialOverLan.readString(charset, bytesToReadCount, 2);

        assertEquals("Międlić ", stringRead);
        assertEquals(receivedString.getBytes(charset).length - bytesToReadCount, messageListener.getAvailableBytesCount());
    }

    @Test
    public void shouldSuccessfullySendOperations() throws Exception {
        Set<SolOperation> operations = new HashSet<SolOperation>() {{
            add(SolOperation.Break);
            add(SolOperation.RingWOR);
            add(SolOperation.CTS);
        }};

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) 0);
        PayloadCoder payloadCoder = preparePayloadCoder(operations, responseMessage);

        boolean success = serialOverLan.invokeOperations(operations.toArray(new SolOperation[0]));

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertTrue("Operations should be sent successfully", success);
    }

    @Test
    public void shouldFailSendingOperationsWhenExceptionOccurs() throws Exception {
        Set<SolOperation> operations = new HashSet<SolOperation>() {{
            add(SolOperation.FlushOutbound);
        }};

        doThrow(new IllegalStateException("EXPECTED test exception")).when(connector).sendMessage(any(ConnectionHandle.class), any(PayloadCoder.class));

        PayloadCoder payloadCoder = preparePayloadCoder(operations, null);

        boolean success = serialOverLan.invokeOperations(operations.toArray(new SolOperation[0]));

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Sending operations after exception is thrown should fail", success);
    }

    @Test
    public void shouldFailSendingOperationsWhenNackForWholePacket() throws Exception {
        Set<SolOperation> operations = new HashSet<SolOperation>() {{
            add(SolOperation.DCD_DSR);
            add(SolOperation.FlushInbound);
            add(SolOperation.FlushOutbound);
        }};

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.NACK, (byte) 1, (byte) 0);
        PayloadCoder payloadCoder = preparePayloadCoder(operations, responseMessage);

        boolean success = serialOverLan.invokeOperations(operations.toArray(new SolOperation[0]));

        verify(connector).sendMessage(eq(connectionHandle), eq(payloadCoder));
        assertFalse("Sending operations should fail when packet was NACKed", success);
    }

    @Test
    public void shouldActivateSolPayloadAfterConstruction() throws Exception {
        verify(connector).sendMessage(eq(connectionHandle), eq(getPayloadActivationStatus));
        verify(connector).sendMessage(eq(connectionHandle), eq(activateSolPayload));
    }

    @Test
    public void shouldDeactivatePayloadWhenClosed() throws Exception {
        DeactivatePayload deactivatePayload = new DeactivatePayload(cipherSuite, PayloadType.Sol, 1);

        serialOverLan.close();

        verify(connector).sendMessage(eq(connectionHandle), eq(deactivatePayload));
    }

    @Test
    public void shouldNotDeactivatePayloadThatIsAlreadyDeactivatedWhenClosed() throws Exception {
        DeactivatePayload deactivatePayload = new DeactivatePayload(cipherSuite, PayloadType.Sol, 1);

        serialOverLan.close();
        serialOverLan.close();

        verify(connector).sendMessage(eq(connectionHandle), eq(deactivatePayload));
    }

    @Test
    public void shouldRaisePrivilegesWhenActivateSessionAndPrivilegesTooLow() throws Exception {
        IPMIException privilegeException = new IPMIException(CompletionCode.InsufficentPrivilege);
        when(connector.sendMessage(eq(connectionHandle), eq(activateSolPayload)))
                .thenThrow(privilegeException)
                .thenReturn(activateSolPayloadResponse);

        new SerialOverLan(connector, session);

        SetSessionPrivilegeLevel setSessionPrivilegeLevel = new SetSessionPrivilegeLevel(IpmiVersion.V20,
                cipherSuite, AuthenticationType.RMCPPlus, PrivilegeLevel.Administrator);

        verify(connector).sendMessage(eq(connectionHandle), refEq(setSessionPrivilegeLevel));
    }

    @Test
    public void shouldOpenNewSessionWhenSeparateSessionConstructorIsUsed() throws Exception {
        InetAddress remoteHost = InetAddress.getByName("1.2.3.4");
        String user = "test_user";
        String password = "test_pass";

        when(connector.createConnection(any(InetAddress.class), anyInt())).thenReturn(connectionHandle);
        when(connector.openSession(any(ConnectionHandle.class), any(String.class), any(String.class), any(byte[].class)))
                .thenReturn(session);

        new SerialOverLan(connector, remoteHost.getHostAddress(), remotePort, user, password, new SpecificCipherSuiteSelector(cipherSuite));

        verify(connector).createConnection(eq(remoteHost), eq(remotePort));
        verify(connector).getAvailableCipherSuites(eq(connectionHandle));
        verify(connector).getChannelAuthenticationCapabilities(eq(connectionHandle), eq(cipherSuite), eq(PrivilegeLevel.Administrator));
        verify(connector).openSession(eq(connectionHandle), eq(user), eq(password), any(byte[].class));
    }

    @Test
    public void shouldOpenNewSessionWhenSolIsListeningOnSeparatePortWithoutExitingSession() throws Exception {
        InetAddress remoteAddress = InetAddress.getByName(remoteHost);
        int solPort = 1111;

        when(activateSolPayloadResponse.getPayloadUdpPortNumber()).thenReturn(solPort);

        ConnectionHandle connectionHandleCopy = mock(ConnectionHandle.class);
        when(connectionHandleCopy.getRemoteAddress()).thenReturn(remoteAddress);
        when(connectionHandleCopy.getRemotePort()).thenReturn(solPort);
        when(connectionHandleCopy.getUser()).thenReturn(user);
        when(connectionHandleCopy.getPassword()).thenReturn(password);
        when(connectionHandleCopy.getCipherSuite()).thenReturn(cipherSuite);

        Session newSession = mock(Session.class);
        when(newSession.getConnectionHandle()).thenReturn(connectionHandleCopy);

        when(connector.getExistingSessionForCriteria(any(InetAddress.class), anyInt(), any(String.class))).thenReturn(null);
        when(connector.createConnection(any(InetAddress.class), anyInt())).thenReturn(connectionHandleCopy);
        when(connector.openSession(any(ConnectionHandle.class), any(String.class), any(String.class), any(byte[].class)))
                .thenReturn(newSession);

        new SerialOverLan(connector, session);

        verify(connector).getExistingSessionForCriteria(eq(remoteAddress), eq(solPort), eq(user));
        verify(connector).createConnection(eq(remoteAddress), eq(solPort));
        verify(connector).openSession(eq(connectionHandleCopy), eq(user), eq(password), any(byte[].class));
        verify(connector).sendMessage(eq(connectionHandleCopy), eq(getPayloadActivationStatus));
        verify(connector).sendMessage(eq(connectionHandleCopy), eq(activateSolPayload));
    }

    @Test
    public void shouldUseExistingSessionWhenSolIsListeningOnSeparatePortWithExistingSession() throws Exception {
        InetAddress remoteAddress = InetAddress.getByName(remoteHost);
        int solPort = 1024;

        when(activateSolPayloadResponse.getPayloadUdpPortNumber()).thenReturn(solPort);

        ConnectionHandle connectionHandleCopy = mock(ConnectionHandle.class);
        when(connectionHandleCopy.getRemoteAddress()).thenReturn(remoteAddress);
        when(connectionHandleCopy.getRemotePort()).thenReturn(solPort);
        when(connectionHandleCopy.getUser()).thenReturn(user);
        when(connectionHandleCopy.getPassword()).thenReturn(password);
        when(connectionHandleCopy.getCipherSuite()).thenReturn(cipherSuite);

        Session existingSession = mock(Session.class);
        when(existingSession.getConnectionHandle()).thenReturn(connectionHandleCopy);

        when(connector.getExistingSessionForCriteria(any(InetAddress.class), anyInt(), any(String.class)))
                .thenReturn(existingSession);

        new SerialOverLan(connector, session);

        verify(connector).getExistingSessionForCriteria(eq(remoteAddress), eq(solPort), eq(user));
        verify(connector).sendMessage(eq(connectionHandleCopy), eq(getPayloadActivationStatus));
        verify(connector).sendMessage(eq(connectionHandleCopy), eq(activateSolPayload));
        verify(connector, never()).createConnection(eq(remoteAddress), eq(solPort));
        verify(connector, never()).openSession(eq(connectionHandleCopy), eq(user), eq(password), any(byte[].class));
    }

    @Test
    public void shouldNotifyListenerAboutRequestEventWhenRegistered() throws Exception {
        InboundSolMessageListener inboundSolMessageListener = getSolMessageListener();

        SolEventListener listener = mock(SolEventListener.class);
        serialOverLan.registerEventListener(listener);

        Set<SolStatus> statuses = new HashSet<SolStatus>() {{
            add(SolStatus.RtsAsserted);
            add(SolStatus.DtrAsserted);
        }};

        SolInboundStatusField statusField = new SolInboundStatusField(statuses);
        SolInboundMessage message = new SolInboundMessage((byte) 1, (byte) 0, (byte) 0, statusField);

        inboundSolMessageListener.notify(message);

        verify(listener).processRequestEvent(eq(statuses));
    }

    @Test
    public void shouldNotNotifyListenerWhenRequestEventHasNoStatuses() throws Exception {
        InboundSolMessageListener inboundSolMessageListener = getSolMessageListener();

        SolEventListener listener = mock(SolEventListener.class);
        serialOverLan.registerEventListener(listener);

        SolInboundStatusField statusField = new SolInboundStatusField(new HashSet<SolStatus>());
        SolInboundMessage message = new SolInboundMessage((byte) 1, (byte) 0, (byte) 0, statusField);

        inboundSolMessageListener.notify(message);

        verify(listener, never()).processRequestEvent(anySetOf(SolStatus.class));
    }

    @Test
    public void shouldNotNotifyListenerAboutRequestEventWhenUnregistered() throws Exception {
        InboundSolMessageListener inboundSolMessageListener = getSolMessageListener();

        SolEventListener listener = mock(SolEventListener.class);
        serialOverLan.registerEventListener(listener);
        serialOverLan.unregisterEventListener(listener);

        Set<SolStatus> statuses = new HashSet<SolStatus>() {{
            add(SolStatus.SolDeactivated);
        }};

        SolInboundStatusField statusField = new SolInboundStatusField(statuses);
        SolInboundMessage message = new SolInboundMessage((byte) 1, (byte) 0, (byte) 0, statusField);

        inboundSolMessageListener.notify(message);

        verify(listener, never()).processRequestEvent(eq(statuses));
    }

    @Test
    public void shouldNotifyListenerAboutResponseEventWhenRegistered() throws Exception {
        SolEventListener listener = mock(SolEventListener.class);
        serialOverLan.registerEventListener(listener);

        byte[] bytesToWrite = new byte[] {7, 7, 7};

        Set<SolStatus> statuses = new HashSet<SolStatus>() {{
            add(SolStatus.CharacterTransferUnavailable);
            add(SolStatus.RtsAsserted);
            add(SolStatus.SolDeactivated);
        }};

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) bytesToWrite.length, statuses);
        preparePayloadCoder(bytesToWrite, responseMessage);

        serialOverLan.writeBytes(bytesToWrite);

        verify(listener).processResponseEvent(eq(statuses), eq(bytesToWrite), eq(new HashSet<SolOperation>()));
    }

    @Test
    public void shouldNotNotifyListenerWhenResponseEventHasNoStatuses() throws Exception {
        SolEventListener listener = mock(SolEventListener.class);
        serialOverLan.registerEventListener(listener);

        byte[] bytesToWrite = new byte[] {7, 7, 7};

        IpmiMessage responseMessage = prepareResponseIpmiAckMessage(SolAckState.ACK, (byte) 1, (byte) bytesToWrite.length,
                new HashSet<SolStatus>());
        preparePayloadCoder(bytesToWrite, responseMessage);

        serialOverLan.writeBytes(bytesToWrite);

        verify(listener, never()).processResponseEvent(anySetOf(SolStatus.class), any(byte[].class), anySetOf(SolOperation.class));
    }

    private IpmiMessage prepareResponseIpmiAckMessage(SolAckState ackState, byte ackSequenceNumber, byte acceptedCharacters) {
        return prepareResponseIpmiAckMessage(ackState, ackSequenceNumber, acceptedCharacters, new HashSet<SolStatus>());
    }

    private IpmiMessage prepareResponseIpmiAckMessage(SolAckState ackState, byte ackSequenceNumber, byte acceptedCharacters,
                                                      Set<SolStatus> statuses) {
        SolInboundStatusField solInboundStatusField = new SolInboundStatusField(ackState, statuses);

        SolInboundMessage responsePayload = new SolInboundMessage((byte) 0, ackSequenceNumber, acceptedCharacters, solInboundStatusField);

        IpmiMessage responseMessage = mock(IpmiMessage.class);
        when(responseMessage.getPayload()).thenReturn(responsePayload);

        return responseMessage;
    }

    private PayloadCoder preparePayloadCoder(byte[] bytesToWrite, IpmiMessage responseMessage) throws Exception {
        PayloadCoder expectedPayloadCoder = new SolCoder(bytesToWrite, cipherSuite);

        if (responseMessage != null) {
            doReturn(expectedPayloadCoder.getResponseData(responseMessage))
                    .when(connector).sendMessage(eq(connectionHandle), eq(expectedPayloadCoder));
            doReturn(expectedPayloadCoder.getResponseData(responseMessage))
                    .when(connector).retryMessage(eq(connectionHandle), anyByte(), eq(PayloadType.Sol));
        }

        return expectedPayloadCoder;
    }

    private PayloadCoder preparePayloadCoder(Set<SolOperation> operations, IpmiMessage responseMessage) throws Exception {
        PayloadCoder expectedPayloadCoder = new SolCoder(operations, cipherSuite);

        if (responseMessage != null) {
            doReturn(expectedPayloadCoder.getResponseData(responseMessage))
                    .when(connector).sendMessage(eq(connectionHandle), eq(expectedPayloadCoder));
            doReturn(expectedPayloadCoder.getResponseData(responseMessage))
                    .when(connector).retryMessage(eq(connectionHandle), anyByte(), eq(PayloadType.Sol));
        }

        return expectedPayloadCoder;
    }

    private InboundSolMessageListener getSolMessageListener() {
        ArgumentCaptor<InboundSolMessageListener> solListenerCaptor = ArgumentCaptor.forClass(InboundSolMessageListener.class);
        verify(connector).registerIncomingMessageListener(solListenerCaptor.capture());

        return solListenerCaptor.getValue();
    }

    private void receiveMessage(InboundSolMessageListener messageListener, byte receivedSequenceNumber, byte[] receivedBytes) {
        SolInboundStatusField inboundStatusField = new SolInboundStatusField(new HashSet<SolStatus>());
        SolInboundMessage receivedPayload = new SolInboundMessage(receivedSequenceNumber, (byte) 0, (byte) 0, inboundStatusField);
        receivedPayload.setData(receivedBytes);

        messageListener.notify(receivedPayload);
    }

}