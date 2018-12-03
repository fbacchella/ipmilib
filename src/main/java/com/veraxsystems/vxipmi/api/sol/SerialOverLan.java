/*
 * SerialOverLan.java
 * Created on 31.05.2017
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
import com.veraxsystems.vxipmi.coding.payload.sol.SolMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolOperation;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.sol.SolCoder;
import com.veraxsystems.vxipmi.coding.sol.SolResponseData;
import com.veraxsystems.vxipmi.common.Constants;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.connection.Session;
import com.veraxsystems.vxipmi.connection.SessionException;
import com.veraxsystems.vxipmi.connection.SessionManager;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Entry point for the Serial Over LAN (SOL) communication. Use all SOL operations through this class.
 */
public class SerialOverLan implements Closeable {

    private static final Logger logger = Logger.getLogger(SerialOverLan.class);

    private final IpmiConnector connector;
    private final Session session;
    private final InboundSolMessageListener inboundMessageListener;
    private final List<SolEventListener> eventListeners;

    private boolean isSessionInternal;
    private int payloadInstance;
    private int maxPayloadSize;
    private boolean closed;

    /**
     * Creates connection with IPMI using given {@link IpmiConnector}, connected to remote machine on given address and port,
     * and opens a new {@link Session} for SOL communication.
     * This constructor should be used only when you have no other connection opened on this port.
     *
     * @param connector
     *          {@link IpmiConnector} that will be used for communication
     * @param remoteHost
     *          IP address of the remote server
     * @param remotePort
     *          UDP port number of the remote server
     * @param user
     *          IPMI user name
     * @param password
     *          IPMI password
     * @param cipherSuiteSelectionHandler
     *          {@link CipherSuiteSelectionHandler} that will allow to select {@link CipherSuite} among available ones.
     *
     * @throws SOLException when any problem occur during establishing session or activating SOL payload.
     */
    public SerialOverLan(IpmiConnector connector, String remoteHost, int remotePort, String user, String password,
                         CipherSuiteSelectionHandler cipherSuiteSelectionHandler) throws SOLException, SessionException {
        this(connector, SessionManager.establishSession(connector, remoteHost, remotePort, user, password, cipherSuiteSelectionHandler));

        this.isSessionInternal = true;
    }

    /**
     * Creates connection with IPMI using given {@link IpmiConnector}, connected to remote machine on given address and default IPMI port,
     * and opens a new {@link Session} for SOL communication.
     * This constructor should be used only when you have no other connection opened on this port.
     *
     * @param connector
     *          {@link IpmiConnector} that will be used for communication
     * @param remoteHost
     *          IP address of the remote server
     * @param user
     *          IPMI user name
     * @param password
     *          IPMI password
     * @param cipherSuiteSelectionHandler
     *          {@link CipherSuiteSelectionHandler} that will allow to select {@link CipherSuite} among available ones.
     *
     * @throws SOLException when any problem occur during establishing session or activating SOL payload.
     */
    public SerialOverLan(IpmiConnector connector, String remoteHost, String user, String password,
                         CipherSuiteSelectionHandler cipherSuiteSelectionHandler) throws SOLException, SessionException {
        this(connector, remoteHost, Constants.IPMI_PORT, user, password, cipherSuiteSelectionHandler);
    }

    /**
     * Tries to open SOL communication on given existing session. When it appears that separate session must be opened
     * to handle SOL messages (for example SOL payload must be activated on separate port), the new connection and session
     * are automatically established.
     *
     * @param connector
     *          {@link IpmiConnector} that will be used for communication
     * @param session
     *          Existing session that should be reused (if possible) for SOL communication.
     */
    public SerialOverLan(IpmiConnector connector, Session session) throws SOLException, SessionException {
        this.connector = connector;

        int solPayloadPort = activatePayload(connector, session.getConnectionHandle());

        this.session = resolveSession(connector, session.getConnectionHandle(), session, solPayloadPort);

        this.eventListeners = new LinkedList<SolEventListener>();
        this.inboundMessageListener = new InboundSolMessageListener(connector,
                this.session.getConnectionHandle(), eventListeners);

        connector.registerIncomingMessageListener(inboundMessageListener);
        this.closed = false;
    }

    /**
     * Given potential session object, connection data and port on which SOL should be activated,
     * decides what session should be finally used to SOL communication.
     *
     * @param connector
     *          {@link IpmiConnector} that will be used for communication
     * @param connectionHandle
     *          {@link ConnectionHandle} representing single connection to managed system.
     * @param session
     *          Existing session that should be reused (if possible) for SOL communication.
     * @param solPayloadPort
     *          UDP port on which managed system listens for SOL communication.
     * @return Actual session (existing or newly established), that should be used for SOL communication.
     * @throws SOLException
     *          If any unrecoverable error occurs.
     * @throws SessionException
     *          If new session could not be established.
     */
    private Session resolveSession(IpmiConnector connector, ConnectionHandle connectionHandle, Session session, int solPayloadPort) throws SOLException, SessionException {
        if (solPayloadPort != connectionHandle.getRemotePort()) {
            Session alternativeSession = connector.getExistingSessionForCriteria(connectionHandle.getRemoteAddress(),
                    solPayloadPort, connectionHandle.getUser());

            if (alternativeSession == null) {
                CipherSuiteSelectionHandler cipherSuiteSelector = new SpecificCipherSuiteSelector(connectionHandle.getCipherSuite());

                alternativeSession = SessionManager.establishSession(connector, connectionHandle.getRemoteAddress().getHostAddress(),
                        solPayloadPort, connectionHandle.getUser(), connectionHandle.getPassword(), cipherSuiteSelector);
                this.isSessionInternal = true;

            } else {
                this.isSessionInternal = false;
            }

            activatePayload(connector, alternativeSession.getConnectionHandle());

            return alternativeSession;
        } else {
            this.isSessionInternal = false;
            return session;
        }
    }

    /**
     * Tries to activate SOL payload in the session associated to given {@link ConnectionHandle}.
     * If first activation try fails due to insufficient privileges, raises the session privileges
     * to maximum available and tries to activate payload once again.
     *
     * @param connector
     *          {@link IpmiConnector} that will be used for communication
     * @param connectionHandle
     *          {@link ConnectionHandle} representing single connection to managed system.
     * @return UDP port number on which managed system listenes for SOL messages.
     * @throws SOLException
     *          when any unrecoverable error occurred.
     */
    private int activatePayload(IpmiConnector connector, ConnectionHandle connectionHandle) throws SOLException {
        try {
            this.payloadInstance = getFirstAvailablePayloadInstance(connector, connectionHandle);

            ActivateSolPayload activatePayload = new ActivateSolPayload(connectionHandle.getCipherSuite(), payloadInstance);
            ActivateSolPayloadResponseData activatePayloadResponseData = getActivatePayloadResponse(connector,
                    connectionHandle, activatePayload);

            this.maxPayloadSize = activatePayloadResponseData.getInboundPayloadSize();

            return activatePayloadResponseData.getPayloadUdpPortNumber();

        } catch (Exception e) {
            throw new SOLException("Cannot activate SOL payload due to exception during activation process", e);
        }
    }

    private ActivateSolPayloadResponseData getActivatePayloadResponse(IpmiConnector connector, ConnectionHandle connectionHandle, ActivateSolPayload activatePayload) throws Exception {
        ActivateSolPayloadResponseData activatePayloadResponseData;

        try {
            activatePayloadResponseData = (ActivateSolPayloadResponseData) connector.sendMessage(connectionHandle,
                    activatePayload);
        } catch (IPMIException e) {
            if (e.getCompletionCode() == CompletionCode.InsufficentPrivilege) {
                raiseSessionPrivileges(connector, connectionHandle);

                activatePayloadResponseData = (ActivateSolPayloadResponseData) connector.sendMessage(
                        connectionHandle, activatePayload);
            } else {
                throw e;
            }
        }

        return activatePayloadResponseData;
    }

    /**
     * Checks for available SOL payload instances and, if any, returns first available.
     *
     * @param connector
     *          {@link IpmiConnector} that will be used for communication
     * @param connectionHandle
     *          {@link ConnectionHandle} representing single connection to managed system.
     * @return number of first available SOL payload instance.
     * @throws Exception
     *          If any exception occurred during communication or if no available instances were found.
     */
    private int getFirstAvailablePayloadInstance(IpmiConnector connector, ConnectionHandle connectionHandle) throws Exception {
        GetPayloadActivationStatus getPayloadActivationStatus = new GetPayloadActivationStatus(connectionHandle.getCipherSuite(),
                PayloadType.Sol);
        GetPayloadActivationStatusResponseData getActivationResponseData = (GetPayloadActivationStatusResponseData) connector.sendMessage(
                connectionHandle, getPayloadActivationStatus);

        if (getActivationResponseData.getInstanceCapacity() <= 0 || getActivationResponseData.getAvailableInstances().isEmpty()) {
            throw new SOLException("Cannot activate SOL payload, as there are no available payload instances.");
        }

        return TypeConverter.byteToInt(getActivationResponseData.getAvailableInstances().get(0));
    }

    /**
     * Sends proper command to managed system in order to raise user privileges in given session to maximum available level.
     *
     * @param connector
     *          {@link IpmiConnector} that will be used for communication
     * @param connectionHandle
     *          {@link ConnectionHandle} representing single connection to managed system.
     * @throws Exception
     *           If any exception occurred during communication.
     */
    private void raiseSessionPrivileges(IpmiConnector connector, ConnectionHandle connectionHandle) throws Exception {
        SetSessionPrivilegeLevel setSessionPrivilegeLevel = new SetSessionPrivilegeLevel(IpmiVersion.V20,
                connectionHandle.getCipherSuite(), AuthenticationType.RMCPPlus, PrivilegeLevel.Administrator);
        connector.sendMessage(connectionHandle, setSessionPrivilegeLevel);
    }


    /**
     * Writes single byte to the port.
     * This operation blocks until all data can be sent to remote server and is either accepted or rejected by the server.
     *
     * @param singleByte
     *          a byte to write
     * @return true if byte was successfully sent and acknowledged by remote server, false otherwise.
     */
    public boolean writeByte(byte singleByte) {
       return writeBytes(new byte[] {singleByte});
    }

    /**
     * Writes bytes array to the port.
     * This operation blocks until all data can be sent to remote server and is either accepted or rejected by the server.
     *
     * @param buffer
     *          an array of bytes to write
     * @return true if all bytes were successfully sent and acknowledged by remote server, false otherwise.
     */
    public boolean writeBytes(byte[] buffer) {
        boolean result = true;

        int maxBufferSize = maxPayloadSize - SolMessage.PAYLOAD_HEADER_LENGTH;
        byte[] remainingBytes = buffer;
        int currentIndex = 0;

        while (remainingBytes.length - currentIndex > maxBufferSize) {
            byte[] bufferChunk = Arrays.copyOfRange(remainingBytes, currentIndex, maxBufferSize);
            currentIndex += maxBufferSize;

            result &= sendMessage(bufferChunk);

            if (!result) {
                return false;
            }
        }

        if (remainingBytes.length - currentIndex > 0) {
            remainingBytes = Arrays.copyOfRange(remainingBytes, currentIndex, remainingBytes.length);
            result &= sendMessage(remainingBytes);
        }

        return result;
    }

    /**
     * Writes single integer (in range from 0 to 255) to the port.
     * This operation blocks until all data can be sent to remote server and is either accepted or rejected by the server.
     *
     * @param singleInt
     *          an integer value to write (must be in range from 0 to 255)
     * @return true if integer was successfully sent and acknowledged by remote server, false otherwise.
     */
    public boolean writeInt(int singleInt) {
        return writeBytes(new byte[] {TypeConverter.intToByte(singleInt)});
    }

    /**
     * Writes integers (in range from 0 to 255) array to the port.
     * This operation blocks until all data can be sent to remote server and is either accepted or rejected by the server.
     *
     * @param buffer
     *          an array of integer values to write (each must be in range from 0 to 255)
     * @return true if all integers were successfully sent and acknowledged by remote server, false otherwise.
     */
    public boolean writeIntArray(int[] buffer) {
        byte[] byteBuffer = new byte[buffer.length];

        for (int i = 0; i < buffer.length; i++) {
            byteBuffer[i] = TypeConverter.intToByte(buffer[i]);
        }

        return writeBytes(byteBuffer);
    }

    /**
     * Writes {@link String} to port, using platform's default {@link Charset} when converting {@link String} to byte array.
     * This operation blocks until all data can be sent to remote server and is either accepted or rejected by the server.
     *
     * @param string
     *          a string to write to the port.
     * @return true if whole string was successfully sent and acknowledged by remote server, false otherwise.
     */
    public boolean writeString(String string) {
        return writeBytes(string.getBytes());
    }

    /**
     * Writes {@link String} to port, using given {@link Charset} when converting {@link String} to byte array.
     * This operation blocks until all data can be sent to remote server and is either accepted or rejected by the server.
     *
     * @param string
     *          a string to write to port
     * @param charset
     *          {@link Charset} that the string is encoded in
     * @return true if whole string was successfully sent and acknowledged by remote server, false otherwise.
     */
    public boolean writeString(String string, Charset charset) {
        return writeBytes(string.getBytes(charset));
    }

    /**
     * Read all available bytes from the port.
     * Returns immediately, without waiting for data to be available.
     *
     * @return all bytes that could be read or empty array if no bytes were available.
     */
    public byte[] readBytes() {
        return readBytes(inboundMessageListener.getAvailableBytesCount());
    }

    /**
     * Reads at max given number of bytes from the port.
     * Returns immediately, without waiting for data to be available.
     *
     * @param byteCount
     *          maximum number of bytes that should be read
     * @return byte array containing bytes that could be read, but no more than given byteCount.
     * Returns empty array if no bytes were available.
     */
    public byte[] readBytes(int byteCount) {
        return inboundMessageListener.readBytes(byteCount);
    }

    /**
     * Reads at max given number of bytes from the port.
     * This operation blocks until given number of bytes is available to be read or until given timeout is hit.
     *
     * @param byteCount
     *          maximum number of bytes that should be read.
     * @param timeout
     *          maximum time in milliseconds that we want to wait for all available bytes
     * @return byte array containing bytes that could be read, but no more than byteCount.
     * When the timeout is hit, returns just bytes that were available or empty array if no bytes were available.
     */
    public byte[] readBytes(int byteCount, int timeout) {
        waitForData(byteCount, timeout);

        return readBytes(byteCount);
    }

    /**
     * Reads all available bytes from the port as integer (in range from 0 to 255) values array.
     * Returns immediately, without waiting for data to be available.
     *
     * @return all bytes that could be read as int array (each value in range from 0 to 255) or empty array if no data was available.
     */
    public int[] readIntArray() {
        return readIntArray(inboundMessageListener.getAvailableBytesCount());
    }

    /**
     * Reads at max given number of integer values (in range from 0 to 255) from the port.
     * Returns immediately, without waiting for data to be available.
     *
     * @param byteCount
     *          maximum number of bytes that should be read
     * @return integer array containing integer values that could be read, but no more than given byteCount (each value in range from 0 to 255).
     * Returns empty array if no data was available.
     */
    public int[] readIntArray(int byteCount) {
        byte[] bytesArray = readBytes(byteCount);
        int[] intArray = new int[bytesArray.length];

        for (int i = 0; i < bytesArray.length; i++) {
            intArray[i] = TypeConverter.byteToInt(bytesArray[i]);
        }

        return intArray;
    }

    /**
     * Reads at max given number of integer values (in range from 0 to 255) from the port.
     * This operation blocks until given number of bytes is available to be read or until given timeout is hit.
     *
     * @param byteCount
     *          maximum number of bytes that should be read.
     * @param timeout
     *          maximum time in milliseconds that we want to wait for all available data
     * @return integer array containing integer values that could be read, but no more than byteCount (each value in range from 0 to 255).
     * When the timeout is hit, returns just data that was available or empty array if no data was available.
     */
    public int[] readIntArray(int byteCount, int timeout) {
        waitForData(byteCount, timeout);

        return readIntArray(byteCount);
    }

    /**
     * Read all available bytes from the port and converts them to {@link String} using platform's default {@link Charset}.
     * Returns immediately, without waiting for data to be available.
     *
     * @return all bytes that could be read as {@link String}.
     */
    public String readString() {
        return readString(inboundMessageListener.getAvailableBytesCount());
    }

    /**
     * Reads at max given number of bytes from the port, converting them to {@link String} using platform's default {@link Charset}.
     * Returns immediately, without waiting for data to be available.
     *
     * @param byteCount
     *          maximum number of bytes that should be read
     * @return all bytes that could be read as {@link String}, but no more than given byteCount.
     */
    public String readString(int byteCount) {
        return new String(readBytes(byteCount));
    }

    /**
     * Reads at max given number of bytes from the port, converting them to {@link String} using platform's default {@link Charset}.
     * This operation blocks until given number of bytes is available to be read or until given timeout is hit.
     *
     * @param byteCount
     *          maximum number of bytes that should be read.
     * @param timeout
     *          maximum time in milliseconds that we want to wait for all available bytes
     * @return all bytes that could be read as {@link String}, but no more than given byteCount.
     * When the timeout is hit, returns just bytes that were available.
     */
    public String readString(int byteCount, int timeout) {
        waitForData(byteCount, timeout);

        return readString(byteCount);
    }

    /**
     * Read all available bytes from the port and converts them to {@link String} using given {@link Charset}.
     * Returns immediately, without waiting for data to be available.
     *
     * @param charset
     *          {@link Charset} that will be used when converting bytes to {@link String}
     * 
     * @return all bytes that could be read as {@link String}.
     */
    public String readString(Charset charset) {
        return readString(charset, inboundMessageListener.getAvailableBytesCount());
    }

    /**
     * Reads at max given number of bytes from the port, converting them to {@link String} using given {@link Charset}.
     * Returns immediately, without waiting for data to be available.
     *
     * @param charset
     *          {@link Charset} that will be used when converting bytes to {@link String}
     * @param byteCount
     *          maximum number of bytes that should be read
     * @return all bytes that could be read as {@link String}, but no more than given byteCount.
     */
    public String readString(Charset charset, int byteCount) {
        return new String(readBytes(byteCount), charset);
    }

    /**
     * Reads at max given number of bytes from the port, converting them to {@link String} using given {@link Charset}.
     * This operation blocks until given number of bytes is available to be read or until given timeout is hit.
     *
     * @param charset
     *          {@link Charset} that will be used when converting bytes to {@link String}
     * @param byteCount
     *          maximum number of bytes that should be read.
     * @param timeout
     *          maximum time in milliseconds that we want to wait for all available bytes
     * @return all bytes that could be read as {@link String}, but no more than given byteCount.
     * When the timeout is hit, returns just bytes that were available.
     */
    public String readString(Charset charset, int byteCount, int timeout) {
        waitForData(byteCount, timeout);

        return readString(charset, byteCount);
    }

    private void waitForData(int wantedByteCount, int timeout) {
        long startTime = System.currentTimeMillis();

        while (isTooFewBytesAvailable(wantedByteCount) && timeoutNotHit(timeout, startTime)) {
            // NOP, just waiting
        }
    }

    private boolean isTooFewBytesAvailable(int wantedByteCount) {
        return inboundMessageListener.getAvailableBytesCount() < wantedByteCount;
    }

    private boolean timeoutNotHit(int timeout, long startTime) {
        return System.currentTimeMillis() - startTime < timeout;
    }

    /**
     * Invokes given SOL-specific operations on remote serial port.
     *
     * @param operations
     *          a bunch of {@link SolOperation}s that should be invoked
     * @return true if operations were successfully sent and acknowledged by remote server, false otherwise.
     */
    public boolean invokeOperations(SolOperation... operations) {
        Set<SolOperation> operationSet = new HashSet<SolOperation>();

        for (SolOperation operation : operations) {
            operationSet.add(operation);
        }

        return sendMessage(operationSet);
    }

    /**
     * Registers new {@link SolEventListener} that will be informed about SOL events fired by remote system.
     *
     * @param listener
     *          listener to be registered
     */
    public void registerEventListener(SolEventListener listener) {
        eventListeners.add(listener);
    }

    /**
     * Unregister given {@link SolEventListener}, preventing it from receiving SOL events from remote server.
     *
     * @param listener
     *          listener to be unregistered
     */
    public void unregisterEventListener(SolEventListener listener) {
        eventListeners.remove(listener);
    }

    private boolean sendMessage(byte[] characterData) {
        SolCoder payloadCoder = new SolCoder(characterData, session.getConnectionHandle().getCipherSuite());

        SolResponseData responseData = sendPayload(payloadCoder);

        notifyResponseListeners(characterData, new HashSet<SolOperation>(), responseData);

        byte[] remainingCharacterData = characterData;

        while (isNackForMessagePart(responseData, remainingCharacterData.length)) {
            remainingCharacterData = Arrays.copyOfRange(remainingCharacterData,
                    responseData.getAcceptedCharactersNumber(), remainingCharacterData.length);

            SolCoder partialPayloadCoder = new SolCoder(remainingCharacterData, session.getConnectionHandle().getCipherSuite());
            responseData = sendPayload(partialPayloadCoder);

            notifyResponseListeners(remainingCharacterData, new HashSet<SolOperation>(), responseData);
        }

        return responseData != null && responseData.getAcknowledgeState() == SolAckState.ACK;
    }

    private boolean sendMessage(Set<SolOperation> operations) {
        SolCoder payloadCoder = new SolCoder(operations, session.getConnectionHandle().getCipherSuite());

        SolResponseData responseData = sendPayload(payloadCoder);

        notifyResponseListeners(new byte[0], operations, responseData);

        return responseData != null && responseData.getAcknowledgeState() == SolAckState.ACK;
    }

    private void notifyResponseListeners(byte[] characterData, Set<SolOperation> solOperations, SolResponseData responseData) {
        if (responseData != null && responseData.getStatuses() != null && !responseData.getStatuses().isEmpty()) {
            for (SolEventListener listener : eventListeners) {
                listener.processResponseEvent(responseData.getStatuses(), characterData, solOperations);
            }
        }
    }

    private SolResponseData sendPayload(PayloadCoder payloadCoder) {
        ConnectionHandle connectionHandle = session.getConnectionHandle();

        try {
            SolResponseData responseData = (SolResponseData) connector.sendMessage(connectionHandle, payloadCoder);

            int actualRetries = 0;

            while (isNackForWholeMessage(responseData) && actualRetries < connector.getRetries()) {
                actualRetries++;

                responseData = (SolResponseData) connector.retryMessage(connectionHandle,
                        responseData.getRequestSequenceNumber(), PayloadType.Sol);
            }

            return responseData;
        } catch (Exception e) {
            logger.error("Error while sending message", e);
            return null;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            try {
                ConnectionHandle connectionHandle = session.getConnectionHandle();

                DeactivatePayload deactivatePayload = new DeactivatePayload(connectionHandle.getCipherSuite(),
                        PayloadType.Sol, payloadInstance);

                connector.sendMessage(connectionHandle, deactivatePayload);

                if (isSessionInternal) {
                    connector.closeSession(connectionHandle);
                    connector.tearDown();
                }

                closed = true;
            } catch (Exception e) {
                throw new IOException("Error while closing Serial over LAN instance", e);
            }

        }
    }

    private boolean isNackForWholeMessage(SolResponseData responseData) {
        return responseData != null
                && responseData.getAcknowledgeState() == SolAckState.NACK
                && responseData.getAcceptedCharactersNumber() == 0;
    }

    private boolean isNackForMessagePart(SolResponseData responseData, int previousMessageDataLength) {
        return responseData != null
                && responseData.getAcknowledgeState() == SolAckState.NACK
                && responseData.getAcceptedCharactersNumber() > 0
                && responseData.getAcceptedCharactersNumber() < previousMessageDataLength;
    }

}
