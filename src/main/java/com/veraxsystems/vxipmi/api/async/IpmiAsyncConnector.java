/*
 * IpmiAsyncConnector.java 
 * Created on 2011-09-07
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.api.async;

import com.veraxsystems.vxipmi.api.async.messages.IpmiError;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponse;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponseData;
import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.PropertiesManager;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionException;
import com.veraxsystems.vxipmi.connection.ConnectionListener;
import com.veraxsystems.vxipmi.connection.ConnectionManager;
import com.veraxsystems.vxipmi.connection.Session;
import com.veraxsystems.vxipmi.connection.SessionManager;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Asynchronous API for connecting to BMC via IPMI.
 * </p>
 * <p>
 * Creating connection consists of the following steps:
 * </p>
 * <ul>
 *      <li>Create {@link Connection} and get associated with it
 * {@link ConnectionHandle} via {@link #createConnection(InetAddress, int)}
 *      <li>Get {@link CipherSuite}s that are available for the connection via
 * {@link #getAvailableCipherSuites(ConnectionHandle)}
 *       <li>Pick {@link CipherSuite} and {@link PrivilegeLevel} that will be used
 * during session and get
 * {@link GetChannelAuthenticationCapabilitiesResponseData} to find out allowed
 * authentication options via
 * {@link #getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)}
 *      <li>Provide username, password and (if the BMC needs it) the BMC Kg key and
 * start session via
 * {@link #openSession(ConnectionHandle, String, String, byte[])}
 * </ul>
 * <p>
 * To send message register for receiving answers via
 * {@link #registerListener(IpmiResponseListener)} and send message via
 * {@link #sendMessage(ConnectionHandle, PayloadCoder, boolean)}
 * </p>
 * <br>
 * <p>
 * To close session call {@link #closeSession(ConnectionHandle)}
 * </p>
 * <br>
 */
public class IpmiAsyncConnector implements ConnectionListener {
    public static final String FAILED_TO_RECEIVE_ANSWER_CAUSE_MESSAGE = "Failed to receive answer, cause:";
    private ConnectionManager connectionManager;
    private SessionManager sessionManager;
    private int retries;
    private final List<IpmiResponseListener> responseListeners;
    private final List<InboundMessageListener> inboundMessageListeners;

    private static Logger logger = Logger.getLogger(IpmiAsyncConnector.class);

    /**
     * Starts {@link IpmiAsyncConnector} and initiates the
     * {@link ConnectionManager} at the given port. The wildcard IP address will
     * be used.
     *
     * @param port
     *            the port that will be used by {@link IpmiAsyncConnector} to
     *            communicate with the remote hosts.
     * @throws IOException
     *             when properties file was not found
     */
    public IpmiAsyncConnector(int port) throws IOException {
        responseListeners = new ArrayList<IpmiResponseListener>();
        inboundMessageListeners = new ArrayList<InboundMessageListener>();
        connectionManager = new ConnectionManager(port);
        sessionManager = new SessionManager();
        loadProperties();
    }

    /**
     * Starts {@link IpmiAsyncConnector} and initiates the
     * {@link ConnectionManager} at the given port and IP interface.
     *
     * @param port
     *            the port that will be used by {@link IpmiAsyncConnector} to
     *            communicate with the remote hosts.
     * @param address
     *            the IP address that will be used by
     *            {@link IpmiAsyncConnector} to communicate with the remote
     *            hosts.
     * @throws IOException
     *             when properties file was not found
     */
    public IpmiAsyncConnector(int port, InetAddress address) throws IOException {
        responseListeners = new ArrayList<IpmiResponseListener>();
        inboundMessageListeners = new ArrayList<InboundMessageListener>();
        connectionManager = new ConnectionManager(port, address);
        sessionManager = new SessionManager();
        loadProperties();
    }

    private void loadProperties() {
        retries = Integer.parseInt(PropertiesManager.getInstance().getProperty("retries"));
    }

    /**
     * Creates connection to the remote host.
     *
     * @param address
     *            {@link InetAddress} of the remote host
     * @return handle to the connection to the remote host
     * @throws IOException
     *             when properties file was not found
     * @throws FileNotFoundException
     *             when properties file was not found
     */
    public ConnectionHandle createConnection(InetAddress address, int port)
            throws IOException {
        int handle = connectionManager.createConnection(address, port);
        connectionManager.getConnection(handle).registerListener(this);
        return new ConnectionHandle(handle, address, port);
    }

    /**
     * Creates connection to the remote host, with pre set {@link CipherSuite} and {@link PrivilegeLevel}, skipping the
     * getAvailableCipherSuites and getChannelAuthenticationCapabilities phases.
     * @param address {@link InetAddress} of the remote host
     * @return handle to the connection to the remote host
     * @throws IOException
     * when properties file was not found
     * @throws FileNotFoundException
     * when properties file was not found
     */
    public ConnectionHandle createConnection(InetAddress address, int port, CipherSuite cipherSuite, PrivilegeLevel privilegeLevel)
            throws IOException {
        int handle = connectionManager.createConnection(address, port, true);
        connectionManager.getConnection(handle).registerListener(this);

        ConnectionHandle connectionHandle = new ConnectionHandle(handle, address, port);
        connectionHandle.setCipherSuite(cipherSuite);
        connectionHandle.setPrivilegeLevel(privilegeLevel);

        return connectionHandle;
    }

    /**
     * Gets {@link CipherSuite}s available for the connection with the remote
     * host.
     *
     * @param connectionHandle
     *            {@link ConnectionHandle} to the connection created before
     * @see #createConnection(InetAddress, int)
     * @return list of the {@link CipherSuite}s that are allowed during the
     *         connection
     * @throws Exception
     *             when sending message to the managed system fails
     */
    public List<CipherSuite> getAvailableCipherSuites(
            ConnectionHandle connectionHandle) throws Exception {
        int tries = 0;
        List<CipherSuite> result = null;
        while (tries <= retries && result == null) {
            try {
                ++tries;
                result = connectionManager
                        .getAvailableCipherSuites(connectionHandle.getHandle());
            } catch (Exception e) {
                logger.warn(FAILED_TO_RECEIVE_ANSWER_CAUSE_MESSAGE, e);
                if (tries > retries) {
                    throw e;
                }
            }
        }
        return result;
    }

    /**
     * Gets the authentication capabilities for the connection with the remote
     * host.
     *
     * @param connectionHandle
     *            {@link ConnectionHandle} associated with the host
     * @param cipherSuite
     *            {@link CipherSuite} that will be used during the connection
     * @param requestedPrivilegeLevel
     *            {@link PrivilegeLevel} that is requested for the session
     * @return {@link GetChannelAuthenticationCapabilitiesResponseData}
     * @throws ConnectionException
     *             when connection is in the state that does not allow to
     *             perform this operation.
     * @throws Exception
     *             when sending message to the managed system fails
     */
    public GetChannelAuthenticationCapabilitiesResponseData getChannelAuthenticationCapabilities(
            ConnectionHandle connectionHandle, CipherSuite cipherSuite,
            PrivilegeLevel requestedPrivilegeLevel) throws Exception {
        int tries = 0;
        GetChannelAuthenticationCapabilitiesResponseData result = null;
        while (tries <= retries && result == null) {
            try {
                ++tries;
                result = connectionManager
                        .getChannelAuthenticationCapabilities(
                                connectionHandle.getHandle(), cipherSuite,
                                requestedPrivilegeLevel);
                connectionHandle.setCipherSuite(cipherSuite);
                connectionHandle.setPrivilegeLevel(requestedPrivilegeLevel);
            } catch (Exception e) {
                logger.warn(FAILED_TO_RECEIVE_ANSWER_CAUSE_MESSAGE, e);
                if (tries > retries) {
                    throw e;
                }
            }
        }
        return result;
    }

    /**
     * Establishes the session with the remote host.
     *
     * @param connectionHandle
     *            {@link ConnectionHandle} associated with the remote host.
     * @param username
     *            the username
     * @param password
     *            password matching the username
     * @param bmcKey
     *            the key that should be provided if the two-key
     *            authentication is enabled, null otherwise.
     * @throws ConnectionException
     *             when connection is in the state that does not allow to
     *             perform this operation.
     * @throws Exception
     *             when sending message to the managed system or initializing
     *             one of the cipherSuite's algorithms fails
     */
    public Session openSession(ConnectionHandle connectionHandle, String username,
            String password, byte[] bmcKey) throws Exception {
        Session session = null;
        int tries = 0;
        boolean succeded = false;

        connectionHandle.setUser(username);
        connectionHandle.setPassword(password);

        while (tries <= retries && !succeded) {
            try {
                ++tries;
                int sessionId = connectionManager.startSession(connectionHandle.getHandle(),
                        connectionHandle.getCipherSuite(),
                        connectionHandle.getPrivilegeLevel(), username,
                        password, bmcKey);

                session = sessionManager.registerSession(sessionId, connectionHandle);

                succeded = true;
            } catch (Exception e) {
                logger.warn(FAILED_TO_RECEIVE_ANSWER_CAUSE_MESSAGE, e);
                if (tries > retries) {
                    throw e;
                }
            }
        }

        return session;
    }

    /**
     * Returns session already bound to given connection handle fulfilling given criteria.
     *
     * @param remoteAddress
     *          IP addres of the managed system
     * @param remotePort
     *          UDP port of the managed system
     * @param user
     *          IPMI user for whom the connection is established
     * @return session object fulfilling given criteria, or null if no session was registered for such connection.
     */
    public Session getExistingSessionForCriteria(InetAddress remoteAddress, int remotePort, String user) {
        return sessionManager.getSessionForCriteria(remoteAddress, remotePort, user);
    }

    /**
     * Closes the session with the remote host if it is currently in open state.
     *
     * @param connectionHandle
     *            - {@link ConnectionHandle} associated with the remote host.
     * @throws ConnectionException
     *             when connection is in the state that does not allow to
     *             perform this operation.
     * @throws Exception
     *             when sending message to the managed system or initializing
     *             one of the cipherSuite's algorithms fails
     */
    public void closeSession(ConnectionHandle connectionHandle)
            throws Exception {
        if (!connectionManager.getConnection(connectionHandle.getHandle())
                .isSessionValid()) {
            return;
        }
        int tries = 0;
        boolean succeded = false;
        while (tries <= retries && !succeded) {
            try {
                ++tries;
                connectionManager.getConnection(connectionHandle.getHandle())
                        .closeSession();
                sessionManager.unregisterSession(connectionHandle);
                succeded = true;
            } catch (Exception e) {
                logger.warn(FAILED_TO_RECEIVE_ANSWER_CAUSE_MESSAGE, e);
                if (tries > retries) {
                    throw e;
                }
            }
        }
        return;
    }

    /**
     * Sends the IPMI message to the remote host.
     *
     * @param connectionHandle
     *            {@link ConnectionHandle} associated with the remote host.
     * @param request
     *            - {@link PayloadCoder} containing the request to be sent
     * @param isOneWay
     *               tells whether this message is one way (needs response) or not.
     * @return ID of the message that will be also attached to the response to
     *         pair request with response if queue was not full and message was
     *         sent, -1 if sending of the message failed.
     *
     * @throws ConnectionException
     *             when connection is in the state that does not allow to
     *             perform this operation.
     * @throws Exception
     *             when sending message to the managed system or initializing
     *             one of the cipherSuite's algorithms fails
     */
    public int sendMessage(ConnectionHandle connectionHandle,
            PayloadCoder request, boolean isOneWay) throws Exception {
        int tries = 0;
        int tag = -1;
        while (tries <= retries && tag < 0) {
            try {
                ++tries;
                while (tag < 0) {
                    tag = connectionManager.getConnection(
                            connectionHandle.getHandle()).sendMessage(
                            request, isOneWay);
                    if (tag < 0) {
                        Thread.sleep(10); // tag < 0 means that MessageQueue is
                                            // full so we need to wait and retry
                    }
                }
                logger.debug("Sending message with tag " + tag + ", try "
                        + tries);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                logger.warn("Failed to send message, cause:", e);
                if (tries > retries) {
                    throw e;
                }
            }
        }
        return tag;
    }

    /**
     * Attempts to retry sending a message.
     *
     * @param connectionHandle
     *            {@link ConnectionHandle} associated with the remote host.
     * @param tag
     *            tag of the message to retry
     * @param messagePayloadType
     *             {@link PayloadType} of the message that should be retried
     * @return new tag if message was retried, -1 if operation failed
     * @throws ConnectionException
     *             when connection isn't in state where sending commands is
     *             allowed
     */
    public int retry(ConnectionHandle connectionHandle, int tag, PayloadType messagePayloadType) throws ConnectionException {
        return connectionManager.getConnection(connectionHandle.getHandle()).retry(tag, messagePayloadType);
    }

    /**
     * Registers the listener so it will be notified of incoming messages.
     *
     * @param listener
     *            {@link IpmiResponseListener} to processResponse
     */
    public void registerListener(IpmiResponseListener listener) {
        synchronized (responseListeners) {
            responseListeners.add(listener);
        }
    }

    /**
     * Unregisters the listener so it will no longer receive notifications of
     * received answers.
     *
     * @param listener
     *            the {@link IpmiResponseListener} to unregister
     */
    public void unregisterListener(IpmiResponseListener listener) {
        synchronized (responseListeners) {
            responseListeners.remove(listener);
        }
    }

    /**
     * Registers the listener for incoming messages.
     *
     * @param listener
     *             the {@link InboundMessageListener} to register.
     */
    public void registerIncomingPayloadListener(InboundMessageListener listener) {
        synchronized (inboundMessageListeners) {
            inboundMessageListeners.add(listener);
        }
    }

    /**
     * Unregisters the listener so it will no longer receive notifications of received messages.
     *
     * @param listener
     *             the {@link InboundMessageListener} to unregister.
     */
    public void unregisterIncomingPayloadListener(InboundMessageListener listener) {
        synchronized (inboundMessageListeners) {
            inboundMessageListeners.remove(listener);
        }
    }

    @Override
    public void processResponse(ResponseData responseData, int handle, int tag, Exception exception) {
        IpmiResponse response = null;
        Connection connection = connectionManager.getConnection(handle);

        if (responseData == null || exception != null) {
            Exception notNullException = exception != null ? exception : new Exception("Empty response");

            response = new IpmiError(notNullException, tag, new ConnectionHandle(
                    handle, connection.getRemoteMachineAddress(), connection.getRemoteMachinePort()));
        } else {
            response = new IpmiResponseData(responseData, tag,
                    new ConnectionHandle(handle, connection.getRemoteMachineAddress(), connection.getRemoteMachinePort()));

        }
        synchronized (responseListeners) {
            for (IpmiResponseListener listener : responseListeners) {
                if (listener != null) {
                    listener.notify(response);
                }
            }
        }
    }

    @Override
    public void processRequest(IpmiPayload payload) {
        for (InboundMessageListener listener : inboundMessageListeners) {
            if (listener.isPayloadSupported(payload)) {
                listener.notify(payload);
            }
        }
    }

    /**
     * Closes the connection with the given handle
     */
    public void closeConnection(ConnectionHandle handle) {
        connectionManager.getConnection(handle.getHandle()).unregisterListener(
                this);
        connectionManager.closeConnection(handle.getHandle());
    }

    /**
     * Finalizes the connector and closes all connections.
     */
    public void tearDown() {
        connectionManager.close();
    }

    /**
     * Changes the timeout value for connection with the given handle.
     * @param handle
     * - {@link ConnectionHandle} associated with the remote host.
     * @param timeout
     * - new timeout value in ms
     */
    public void setTimeout(ConnectionHandle handle, int timeout) {
        connectionManager.getConnection(handle.getHandle()).setTimeout(timeout);
    }

}
