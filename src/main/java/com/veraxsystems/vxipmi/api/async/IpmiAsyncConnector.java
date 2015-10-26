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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.veraxsystems.vxipmi.api.async.messages.IpmiError;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponse;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponseData;
import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.PropertiesManager;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionException;
import com.veraxsystems.vxipmi.connection.ConnectionListener;
import com.veraxsystems.vxipmi.connection.ConnectionManager;

/**
 * <p>
 * Asynchronous API for connecting to BMC via IPMI.
 * </p>
 * <p>
 * Creating connection consists of the following steps:
 * <li>Create {@link Connection} and get associated with it
 * {@link ConnectionHandle} via {@link #createConnection(InetAddress)}
 * <li>Get {@link CipherSuite}s that are available for the connection via
 * {@link #getAvailableCipherSuites(ConnectionHandle)}
 * <li>Pick {@link CipherSuite} and {@link PrivilegeLevel} that will be used
 * during session and get
 * {@link GetChannelAuthenticationCapabilitiesResponseData} to find out allowed
 * authentication options via
 * {@link #getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)}
 * <li>Provide username, password and (if the BMC needs it) the BMC Kg key and
 * start session via
 * {@link #openSession(ConnectionHandle, String, String, byte[])}
 * </p>
 * <br>
 * <p>
 * To send message register for receiving answers via
 * {@link #registerListener(IpmiListener)} and send message via
 * {@link #sendMessage(ConnectionHandle, IpmiCommandCoder)}
 * </p>
 * <br>
 * <p>
 * To close session call {@link #closeSession(ConnectionHandle)}
 * </p>
 * <br>
 */
public class IpmiAsyncConnector implements ConnectionListener {
	private ConnectionManager connectionManager;
	private int retries;
	private List<IpmiListener> listeners;

	private static Logger logger = Logger.getLogger(IpmiAsyncConnector.class);

	/**
	 * Starts {@link IpmiAsyncConnector} and initiates the
	 * {@link ConnectionManager} at the given port. The wildcard IP address will
	 * be used.
	 * 
	 * @param port
	 *            - the port that will be used by {@link IpmiAsyncConnector} to
	 *            communicate with the remote hosts.
	 * @throws IOException
	 *             when properties file was not found
	 */
	public IpmiAsyncConnector(int port) throws IOException {
		listeners = new ArrayList<IpmiListener>();
		connectionManager = new ConnectionManager(port);
		loadProperties();
	}

	/**
	 * Starts {@link IpmiAsyncConnector} and initiates the
	 * {@link ConnectionManager} at the given port and IP interface.
	 * 
	 * @param port
	 *            - the port that will be used by {@link IpmiAsyncConnector} to
	 *            communicate with the remote hosts.
	 * @param address
	 *            - the IP address that will be used by
	 *            {@link IpmiAsyncConnector} to communicate with the remote
	 *            hosts.
	 * @throws IOException
	 *             when properties file was not found
	 */
	public IpmiAsyncConnector(int port, InetAddress address) throws IOException {
		listeners = new ArrayList<IpmiListener>();
		connectionManager = new ConnectionManager(port, address);
		loadProperties();
	}

    private void loadProperties() throws IOException {
        retries = Integer.parseInt(PropertiesManager.getInstance().getProperty("retries"));
    }

	/**
	 * Creates connection to the remote host.
	 * 
	 * @param address
	 *            - {@link InetAddress} of the remote host
	 * @return handle to the connection to the remote host
	 * @throws IOException
	 *             when properties file was not found
	 * @throws FileNotFoundException
	 *             when properties file was not found
	 */
	public ConnectionHandle createConnection(InetAddress address)
			throws FileNotFoundException, IOException {
		int handle = connectionManager.createConnection(address);
		connectionManager.getConnection(handle).registerListener(this);
		return new ConnectionHandle(handle);
	}

	/**
	 * Gets {@link CipherSuite}s available for the connection with the remote
	 * host.
	 * 
	 * @param connectionHandle
	 *            {@link ConnectionHandle} to the connection created before
	 * @see #createConnection(InetAddress)
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
				logger.warn("Failed to receive answer, cause:", e);
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
	 *            - {@link ConnectionHandle} associated with the host
	 * @param cipherSuite
	 *            - {@link CipherSuite} that will be used during the connection
	 * @param requestedPrivilegeLevel
	 *            - {@link PrivilegeLevel} that is requested for the session
	 * @return - {@link GetChannelAuthenticationCapabilitiesResponseData}
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
				logger.warn("Failed to receive answer, cause:", e);
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
	 *            - {@link ConnectionHandle} associated with the remote host.
	 * @param username
	 *            - the username
	 * @param password
	 *            - password matching the username
	 * @param bmcKey
	 *            - the key that should be provided if the two-key
	 *            authentication is enabled, null otherwise.
	 * @throws ConnectionException
	 *             when connection is in the state that does not allow to
	 *             perform this operation.
	 * @throws Exception
	 *             when sending message to the managed system or initializing
	 *             one of the cipherSuite's algorithms fails
	 */
	public void openSession(ConnectionHandle connectionHandle, String username,
			String password, byte[] bmcKey) throws Exception {
		int tries = 0;
		boolean succeded = false;
		while (tries <= retries && !succeded) {
			try {
				++tries;
				connectionManager.startSession(connectionHandle.getHandle(),
						connectionHandle.getCipherSuite(),
						connectionHandle.getPrivilegeLevel(), username,
						password, bmcKey);
				succeded = true;
			} catch (Exception e) {
				logger.warn("Failed to receive answer, cause:", e);
				if (tries > retries) {
					throw e;
				}
			}
		}
		return;
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
				succeded = true;
			} catch (Exception e) {
				logger.warn("Failed to receive answer, cause:", e);
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
	 *            - {@link ConnectionHandle} associated with the remote host.
	 * @param request
	 *            - {@link IpmiCommandCoder} containing the request to be sent
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
			IpmiCommandCoder request) throws Exception {
		int tries = 0;
		int tag = -1;
		while (tries <= retries && tag < 0) {
			try {
				++tries;
				while (tag < 0) {
					tag = connectionManager.getConnection(
							connectionHandle.getHandle()).sendIpmiCommand(
							request);
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
	 * Registers the listener so it will be notified of incoming messages.
	 * 
	 * @param listener
	 *            {@link IpmiListener} to notify
	 */
	public void registerListener(IpmiListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Unregisters the listener so it will no longer receive notifications of
	 * received answers.
	 * 
	 * @param listener
	 *            - the {@link IpmiListener} to unregister
	 */
	public void unregisterListener(IpmiListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void notify(ResponseData responseData, int handle, int tag,
			Exception exception) {
		IpmiResponse response = null;
		if (responseData == null || exception != null) {
			if (exception == null) {
				exception = new Exception("Empty response");
			}
			response = new IpmiError(exception, tag, new ConnectionHandle(
					handle));
		} else {
			response = new IpmiResponseData(responseData, tag,
					new ConnectionHandle(handle));

		}
		synchronized (listeners) {
			for (IpmiListener listener : listeners) {
				if (listener != null) {
					listener.notify(response);
				}
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
