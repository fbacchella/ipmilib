/*
 * Connection.java 
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
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelCipherSuitesResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.OpenSessionResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp1ResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp3ResponseData;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.AuthenticationRakpHmacSha1;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityAesCbc128;
import com.veraxsystems.vxipmi.coding.security.IntegrityHmacSha1_96;
import com.veraxsystems.vxipmi.common.Constants;
import com.veraxsystems.vxipmi.common.PropertiesManager;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.sm.MachineObserver;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.ErrorAction;
import com.veraxsystems.vxipmi.sm.actions.GetSikAction;
import com.veraxsystems.vxipmi.sm.actions.MessageAction;
import com.veraxsystems.vxipmi.sm.actions.ResponseAction;
import com.veraxsystems.vxipmi.sm.actions.StateMachineAction;
import com.veraxsystems.vxipmi.sm.events.AuthenticationCapabilitiesReceived;
import com.veraxsystems.vxipmi.sm.events.Authorize;
import com.veraxsystems.vxipmi.sm.events.CloseSession;
import com.veraxsystems.vxipmi.sm.events.Default;
import com.veraxsystems.vxipmi.sm.events.DefaultAck;
import com.veraxsystems.vxipmi.sm.events.GetChannelCipherSuitesPending;
import com.veraxsystems.vxipmi.sm.events.OpenSessionAck;
import com.veraxsystems.vxipmi.sm.events.Rakp2Ack;
import com.veraxsystems.vxipmi.sm.events.StartSession;
import com.veraxsystems.vxipmi.sm.events.Timeout;
import com.veraxsystems.vxipmi.sm.states.Authcap;
import com.veraxsystems.vxipmi.sm.states.Ciphers;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import com.veraxsystems.vxipmi.sm.states.Uninitialized;
import com.veraxsystems.vxipmi.transport.Messenger;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A connection with the specific remote host.
 */
public class Connection extends TimerTask implements MachineObserver {
    private static final Logger logger = Logger.getLogger(Connection.class);

    private static final int DEFAULT_CIPHER_SUITE = 3;
    private static final int SESSION_SEQUENCE_NUMBER_UPPER_BOUND = Integer.MAX_VALUE / 4;
    private static final String ILLEGAL_CONNECTION_STATE_MESSAGE = "Illegal connection state: ";

    private List<ConnectionListener> listeners;
    private StateMachine stateMachine;

    /**
     * Time in ms after which a message times out.
     */
    private int timeout = -1;
    private StateMachineAction lastAction;
    private int sessionId;
    private int managedSystemSessionId;
    private byte[] sik;

    private int handle;

    public int getHandle() {
        return handle;
    }

    private Map<PayloadType, MessageHandler> messageHandlers;

    private Timer timer;

    private AtomicInteger currentSessionSequenceNumber;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;

        for (MessageHandler messageHandler : messageHandlers.values()) {
            messageHandler.setTimeout(timeout);
        }
    }

    /**
     * Creates the connection.
     *
     * @param messenger
     *            {@link Messenger} associated with the proper
     *            {@link Constants#IPMI_PORT}
     * @param handle
     *            id of the connection
     */
    public Connection(Messenger messenger, int handle) {
        stateMachine = new StateMachine(messenger);
        this.handle = handle;
        listeners = new ArrayList<ConnectionListener>();
        timeout = Integer.parseInt(PropertiesManager.getInstance().getProperty("timeout"));
        messageHandlers = new EnumMap<PayloadType, MessageHandler>(PayloadType.class);
        currentSessionSequenceNumber = new AtomicInteger(0);
    }

    /**
     * Registers the listener so it will receive notifications from this
     * connection
     *
     * @param listener
     *            {@link ConnectionListener} to processResponse
     */
    public void registerListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters the {@link ConnectionListener}
     *
     * @param listener
     *            {@link ConnectionListener} to unregister
     */
    public void unregisterListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Starts the connection to the specified {@link InetAddress}
     *
     * @param address
     *            IP address of the managed system
     * @param pingPeriod
     *            frequency of the no-op commands that will be sent to keep up
     *            the session
     * @throws IOException
     *             when properties file was not found
     * @throws FileNotFoundException
     *             when properties file was not found
     * @see #disconnect()
     */
    public void connect(InetAddress address, int port, int pingPeriod)
            throws IOException {
        connect(address, port, pingPeriod, false);
    }

    /**
     * Starts the connection to the specified {@link InetAddress}
     * @param address
     * - IP address of the managed system
     * @param pingPeriod
     * - frequency of the no-op commands that will be sent to keep up the session
     * @param skipCiphers
     * - determines if the getAvailableCipherSuites and getChannelAuthenticationCapabilities phases should be skipped
     * @throws IOException
     * - when properties file was not found
     * @throws FileNotFoundException
     * - when properties file was not found
     * @see #disconnect()
     */
    public void connect(InetAddress address, int port, int pingPeriod, boolean skipCiphers) throws IOException {
        MessageHandler ipmiMessageHandler = new IpmiMessageHandler(this, timeout);
        messageHandlers.put(PayloadType.Ipmi, ipmiMessageHandler);

        MessageHandler solMessageHandler = new SolMessageHandler(this, timeout);
        messageHandlers.put(PayloadType.Sol, solMessageHandler);

        timer = new Timer();
        timer.schedule(this, pingPeriod, pingPeriod);
        stateMachine.register(this);
        if (skipCiphers) {
            stateMachine.start(address, port);
            sessionId = SessionManager.generateSessionId();
            stateMachine.setCurrent(new Authcap());
        } else {
            stateMachine.start(address, port);
        }
    }

    /**
     * Ends the connection.
     *
     * @see #connect(InetAddress, int, int)
     */
    public void disconnect() {
        timer.cancel();
        stateMachine.stop();

        for (MessageHandler messageHandler : messageHandlers.values()) {
            messageHandler.tearDown();
        }
    }

    /**
     * Checks if the connection is active
     *
     * @return true if the connection is active, false otherwise
     *
     * @see #connect(InetAddress, int, int)
     * @see #disconnect()
     */
    public boolean isActive() {
        return stateMachine.isActive();
    }

    /**
     * Gets from the managed system supported {@link CipherSuite}s. Should be
     * performed only immediately after {@link #connect(InetAddress, int, int)}.
     *
     * @param tag
     *            the integer from range 0-63 to match request with response
     *
     * @return list of the {@link CipherSuite}s supported by the managed system.
     * @throws ConnectionException
     *             when connection is in the state that does not allow to
     *             perform this operation.
     * @throws Exception
     *             when sending message to the managed system fails
     */
    public List<CipherSuite> getAvailableCipherSuites(int tag) throws Exception {

        if (stateMachine.getCurrent().getClass() != Uninitialized.class) {
            throw new ConnectionException(ILLEGAL_CONNECTION_STATE_MESSAGE + stateMachine.getCurrent().getClass().getSimpleName());
        }

        boolean process = true;

        ArrayList<byte[]> rawCipherSuites = new ArrayList<byte[]>();

        while (process) {

            lastAction = null;

            stateMachine.doTransition(new GetChannelCipherSuitesPending(tag));

            waitForResponse();

            ResponseAction action = (ResponseAction) lastAction;

            if (!(action.getIpmiResponseData() instanceof GetChannelCipherSuitesResponseData)) {
                stateMachine.doTransition(new Timeout());
                throw new ConnectionException(
                        "Response data not matching Get Channel Cipher Suites command.");
            }

            GetChannelCipherSuitesResponseData responseData = (GetChannelCipherSuitesResponseData) action
                    .getIpmiResponseData();

            rawCipherSuites.add(responseData.getCipherSuiteData());

            if (responseData.getCipherSuiteData().length < 16) {
                process = false;
            }
        }

        stateMachine.doTransition(new DefaultAck());

        int length = 0;

        for (byte[] partial : rawCipherSuites) {
            length += partial.length;
        }

        byte[] csRaw = new byte[length];

        int index = 0;

        for (byte[] partial : rawCipherSuites) {
            System.arraycopy(partial, 0, csRaw, index, partial.length);
            index += partial.length;
        }

        return CipherSuite.getCipherSuites(csRaw);
    }

    private void waitForResponse() throws Exception {
        int time = 0;

        while (time < timeout && lastAction == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                logger.info(e.getMessage(), e);
            }
            ++time;
        }

        if (lastAction == null) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException("Command timed out");
        }
        if (!(lastAction instanceof ResponseAction || lastAction instanceof GetSikAction)) {
            if (lastAction instanceof ErrorAction) {
                throw ((ErrorAction) lastAction).getException();
            }
            throw new ConnectionException("Invalid StateMachine response: "
                    + lastAction.getClass().getSimpleName());
        }
    }

    /**
     * Queries the managed system for the details of the authentification
     * process. Must be performed after {@link #getAvailableCipherSuites(int)}
     *
     * @param tag
     *            the integer from range 0-63 to match request with response
     * @param cipherSuite
     *            {@link CipherSuite} requested for the session
     * @param requestedPrivilegeLevel
     *            {@link PrivilegeLevel} requested for the session
     * @return {@link GetChannelAuthenticationCapabilitiesResponseData}
     * @throws ConnectionException
     *             when connection is in the state that does not allow to
     *             perform this operation.
     * @throws Exception
     *             when sending message to the managed system fails
     */
    public GetChannelAuthenticationCapabilitiesResponseData getChannelAuthenticationCapabilities(
            int tag, CipherSuite cipherSuite,
            PrivilegeLevel requestedPrivilegeLevel) throws Exception {

        if (stateMachine.getCurrent().getClass() != Ciphers.class) {
            throw new ConnectionException(ILLEGAL_CONNECTION_STATE_MESSAGE
                    + stateMachine.getCurrent().getClass().getSimpleName());
        }

        lastAction = null;

        stateMachine.doTransition(new Default(cipherSuite, tag,
                requestedPrivilegeLevel));

        waitForResponse();

        ResponseAction action = (ResponseAction) lastAction;

        if (!(action.getIpmiResponseData() instanceof GetChannelAuthenticationCapabilitiesResponseData)) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException(
                    "Response data not matching Get Channel Authentication Capabilities command.");
        }

        GetChannelAuthenticationCapabilitiesResponseData responseData = (GetChannelAuthenticationCapabilitiesResponseData) action
                .getIpmiResponseData();

        sessionId = SessionManager.generateSessionId();

        stateMachine.doTransition(new AuthenticationCapabilitiesReceived(
                sessionId, requestedPrivilegeLevel));

        return responseData;
    }

    /**
     * Initiates the session with the managed system. Must be performed after
     * {@link #getChannelAuthenticationCapabilities(int, CipherSuite, PrivilegeLevel)}
     * or {@link #closeSession()}
     *
     * @param tag
     *            the integer from range 0-63 to match request with response
     * @param cipherSuite
     *            {@link CipherSuite} that will be used during the session
     * @param privilegeLevel
     *            requested {@link PrivilegeLevel} - most of the time it will
     *            be {@link PrivilegeLevel#User}
     * @param username
     *            the username
     * @param password
     *            the password matching the username
     * @param bmcKey
     *            the key that should be provided if the two-key
     *            authentication is enabled, null otherwise.
     * @return id of the new session
     * @throws ConnectionException
     *             when connection is in the state that does not allow to
     *             perform this operation.
     * @throws Exception
     *             when sending message to the managed system or initializing
     *             one of the cipherSuite's algorithms fails
     */
    public int startSession(int tag, CipherSuite cipherSuite,
            PrivilegeLevel privilegeLevel, String username, String password,
            byte[] bmcKey) throws Exception {
        if (stateMachine.getCurrent().getClass() != Authcap.class) {
            throw new ConnectionException(ILLEGAL_CONNECTION_STATE_MESSAGE
                    + stateMachine.getCurrent().getClass().getSimpleName());
        }

        lastAction = null;

        // Open Session
        stateMachine.doTransition(new Authorize(cipherSuite, tag,
                privilegeLevel, sessionId));

        waitForResponse();

        ResponseAction action = (ResponseAction) lastAction;

        lastAction = null;

        if (!(action.getIpmiResponseData() instanceof OpenSessionResponseData)) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException(
                    "Response data not matching OpenSession response data");
        }

        managedSystemSessionId = ((OpenSessionResponseData) action
                .getIpmiResponseData()).getManagedSystemSessionId();

        stateMachine.doTransition(new DefaultAck());

        // RAKP 1
        stateMachine.doTransition(new OpenSessionAck(cipherSuite,
                privilegeLevel, tag, managedSystemSessionId, username,
                password, bmcKey));

        waitForResponse();

        action = (ResponseAction) lastAction;

        lastAction = null;

        if (!(action.getIpmiResponseData() instanceof Rakp1ResponseData)) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException(
                    "Response data not matching RAKP Message 2: "
                            + action.getIpmiResponseData().getClass()
                                    .getSimpleName());
        }

        Rakp1ResponseData rakp1ResponseData = (Rakp1ResponseData) action
                .getIpmiResponseData();

        stateMachine.doTransition(new DefaultAck());

        // RAKP 3
        stateMachine.doTransition(new Rakp2Ack(cipherSuite, tag, (byte) 0,
                managedSystemSessionId, rakp1ResponseData));

        waitForResponse();

        action = (ResponseAction) lastAction;

        if (sik == null) {
            throw new ConnectionException("Session Integrity Key is null");
        }

        cipherSuite.initializeAlgorithms(sik);

        lastAction = null;

        if (!(action.getIpmiResponseData() instanceof Rakp3ResponseData)) {
            stateMachine.doTransition(new Timeout());
            throw new ConnectionException(
                    "Response data not matching RAKP Message 4");
        }

        stateMachine.doTransition(new DefaultAck());
        stateMachine.doTransition(new StartSession(cipherSuite, sessionId));

        return sessionId;
    }

    /**
     * Closes the session. Can be performed only if the session is already open.
     *
     * @throws ConnectionException
     *             when connection is in the state that does not allow to
     *             perform this operation.
     */
    public void closeSession() throws ConnectionException {
        if (stateMachine.getCurrent().getClass() != SessionValid.class) {
            throw new ConnectionException(ILLEGAL_CONNECTION_STATE_MESSAGE
                    + stateMachine.getCurrent().getClass().getSimpleName());
        }

        stateMachine.doTransition(new CloseSession(managedSystemSessionId,
                messageHandlers.get(PayloadType.Ipmi).getSequenceNumber(), getNextSessionSequenceNumber()));
    }

    /**
     * Attempts to send IPMI request to the managed system.
     *
     * @param payloadCoder
     *            {@link PayloadCoder} representing the request
     * @param isOneWay
     *               tells whether message is one way or not
     * @return ID of the message that will be also attached to the response to
     *         pair request with response if queue was not full and message was
     *         sent, -1 if sending of the message failed.
     * @throws ConnectionException
     *             when connection isn't in state where sending commands is
     *             allowed
     * @throws ArithmeticException
     *             when {@link Connection} runs out of available ID's for the
     *             messages. If this happens session needs to be restarted.
     */
    public int sendMessage(PayloadCoder payloadCoder, boolean isOneWay) throws ConnectionException {
        MessageHandler messageHandler = messageHandlers.get(payloadCoder.getSupportedPayloadType());

        if (messageHandler == null) {
            // Handler for PayloadType.Ipmi is a default handler and in case no specific is found, we try default one.
            messageHandler = messageHandlers.get(PayloadType.Ipmi);
        }

        return messageHandler.sendMessage(payloadCoder, stateMachine, managedSystemSessionId, isOneWay);
    }

    /**
     * Attempts to retry sending a message.
     *
     * @param tag
     *            tag of the message to retry
     * @param messagePayloadType
     *             {@link PayloadType} of the message that should be retried
     * @return new tag if message was retried, -1 if operation failed
     * @throws ConnectionException
     *             when connection isn't in state where sending commands is
     *             allowed
     */
    public int retry(int tag, PayloadType messagePayloadType) throws ConnectionException {

        MessageHandler messageHandler = messageHandlers.containsKey(messagePayloadType) ?
                messageHandlers.get(messagePayloadType) : messageHandlers.get(PayloadType.Ipmi);

        return messageHandler.retryMessage(tag, stateMachine, managedSystemSessionId);
    }

    private void handleIncomingMessage(Ipmiv20Message message) {
        MessageHandler messageHandler = messageHandlers.get(message.getPayloadType());
        messageHandler.handleIncomingMessage(message);
    }

    public void notifyResponseListeners(int handle, int tag, ResponseData responseData,
                                        Exception exception) {
        for (ConnectionListener listener : listeners) {
            if (listener != null) {
                listener.processResponse(responseData, handle, tag, exception);
            }
        }
    }

    public void notifyRequestListeners(IpmiPayload payload) {
        for (ConnectionListener listener : listeners) {
            if (listener != null) {
                listener.processRequest(payload);
            }
        }
    }

    @Override
    public void notify(StateMachineAction action) {
        if (action instanceof GetSikAction) {
            sik = ((GetSikAction) action).getSik();
        } else if (!(action instanceof MessageAction)) {
            lastAction = action;
            if (action instanceof ErrorAction) {
                ErrorAction errorAction = (ErrorAction) action;
                logger.error(errorAction.getException().getMessage(), errorAction.getException());
            }
        } else {
            handleIncomingMessage(((MessageAction) action).getIpmiv20Message());
        }
    }

    /**
     * {@link TimerTask} runner - periodically sends no-op messages to keep the
     * session up
     */
    @Override
    public void run() {
        int result = -1;
        do {
            try {
                if (!(stateMachine.getCurrent() instanceof SessionValid)) {
                    break;
                }
                result = sendMessage(new com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilities(
                        IpmiVersion.V20, IpmiVersion.V20,
                        ((SessionValid) stateMachine.getCurrent())
                                .getCipherSuite(), PrivilegeLevel.Callback,
                        TypeConverter.intToByte(0xe)), false);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } while (result <= 0);
    }

    public InetAddress getRemoteMachineAddress() {
        return stateMachine.getRemoteMachineAddress();
    }

    public int getRemoteMachinePort() {
        return stateMachine.getRemoteMachinePort();
    }

    /**
     * Checks if session is currently open.
     */
    public boolean isSessionValid() {
        return stateMachine.getCurrent() instanceof SessionValid;
    }

    public int getNextSessionSequenceNumber() {
        int result = currentSessionSequenceNumber.incrementAndGet() % SESSION_SEQUENCE_NUMBER_UPPER_BOUND;

        if (result == 0) {
            throw new ArithmeticException("Session sequence number overload. Reset session.");
        }

        return result;
    }

    /**
     * @return Default cipher suite (3)
     */
    public static CipherSuite getDefaultCipherSuite() {
        try {
            return new CipherSuite((byte) DEFAULT_CIPHER_SUITE, new AuthenticationRakpHmacSha1().getCode(),
                    new ConfidentialityAesCbc128().getCode(), new IntegrityHmacSha1_96().getCode());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Wrong algorithm in default Cipher suite - SHOULD NOT HAPPEN!!!", e);
            return null;
        }
    }

}
