/*
 * IpmiConnector.java 
 * Created on 2011-09-08
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.api.sync;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.async.IpmiAsyncConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.PropertiesManager;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionException;
import com.veraxsystems.vxipmi.connection.ConnectionManager;
import com.veraxsystems.vxipmi.connection.StateConnectionException;

/**
 * <p> Synchronous API for connecting to BMC via IPMI. </p> <p> Creating connection consists of the following steps:
 * <li>Create {@link Connection} and get associated with it {@link ConnectionHandle} via
 * {@link #createConnection(InetAddress)} <li>Get {@link CipherSuite}s that are available for the connection via
 * {@link #getAvailableCipherSuites(ConnectionHandle)} <li>Pick {@link CipherSuite} and {@link PrivilegeLevel} that will
 * be used during session and get {@link GetChannelAuthenticationCapabilitiesResponseData} to find out allowed
 * authentication options via
 * {@link #getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)} <li>Provide username,
 * password and (if the BMC needs it) the BMC Kg key and start session via
 * {@link #openSession(ConnectionHandle, String, String, byte[])} </p> <br> <p> Send message register via
 * {@link #sendMessage(ConnectionHandle, IpmiCommandCoder)} </p> <br> <p> To close session call
 * {@link #closeSession(ConnectionHandle)} </p> <p> When done with work, clean up via {@link #tearDown()} </p> <br>
 */
public class IpmiConnector {

    private static Logger logger = Logger.getLogger(IpmiConnector.class);

    private IpmiAsyncConnector asyncConnector;

    private int retries;

    private int idleTime;

    private Random random = new Random(System.currentTimeMillis());

    /**
     * Starts {@link IpmiConnector} and initiates the {@link ConnectionManager} at the given port. Wildcard IP address
     * will be used.
     * @param port
     * - the port that will be used by {@link IpmiAsyncConnector} to communicate with the remote hosts.
     * @throws FileNotFoundException
     * when properties file was not found
     * @throws IOException
     * when properties file was not found
     */
    public IpmiConnector(int port) throws FileNotFoundException, IOException {
        asyncConnector = new IpmiAsyncConnector(port);
        loadProperties();
    }

    /**
     * Starts {@link IpmiConnector} and initiates the {@link ConnectionManager} at the given port and IP interface.
     * @param port
     * - the port that will be used by {@link IpmiAsyncConnector} to communicate with the remote hosts.
     * @param address
     * - the IP address that will be used by {@link IpmiAsyncConnector} to communicate with the remote hosts.
     * @throws FileNotFoundException
     * when properties file was not found
     * @throws IOException
     * when properties file was not found
     */
    public IpmiConnector(int port, InetAddress address) throws FileNotFoundException, IOException {
        asyncConnector = new IpmiAsyncConnector(port, address);
        loadProperties();
    }

    private void loadProperties() throws IOException {
        PropertiesManager manager = PropertiesManager.getInstance();
        retries = Integer.parseInt(manager.getProperty("retries"));
        idleTime = Integer.parseInt(manager.getProperty("idleTime"));
    }

    /**
     * Creates connection to the remote host.
     * @param address
     * - {@link InetAddress} of the remote host
     * @return handle to the connection to the remote host
     * @throws IOException
     * when properties file was not found
     * @throws FileNotFoundException
     * when properties file was not found
     */
    public ConnectionHandle createConnection(InetAddress address) throws FileNotFoundException, IOException {
        return asyncConnector.createConnection(address);
    }

    /**
     * Gets {@link CipherSuite}s available for the connection with the remote host.
     * @param connectionHandle
     * {@link ConnectionHandle} to the connection created before
     * @see #createConnection(InetAddress)
     * @return list of the {@link CipherSuite}s that are allowed during the connection
     * @throws Exception
     * when sending message to the managed system fails
     */
    public List<CipherSuite> getAvailableCipherSuites(ConnectionHandle connectionHandle) throws Exception {
        return asyncConnector.getAvailableCipherSuites(connectionHandle);
    }


    public List<CipherSuite> getAllCipherSuites(ConnectionHandle connectionHandle) throws Exception {
        return asyncConnector.getAllCipherSuites(connectionHandle);
    }

    /**
     * Gets the authentication capabilities for the connection with the remote host.
     * @param connectionHandle
     * - {@link ConnectionHandle} associated with the host
     * @param cipherSuite
     * - {@link CipherSuite} that will be used during the connection
     * @param requestedPrivilegeLevel
     * - {@link PrivilegeLevel} that is requested for the session
     * @return - {@link GetChannelAuthenticationCapabilitiesResponseData}
     * @throws ConnectionException
     * when connection is in the state that does not allow to perform this operation.
     * @throws Exception
     * when sending message to the managed system fails
     */
    public GetChannelAuthenticationCapabilitiesResponseData getChannelAuthenticationCapabilities(
            ConnectionHandle connectionHandle, CipherSuite cipherSuite, PrivilegeLevel requestedPrivilegeLevel)
            throws Exception {
        return asyncConnector.getChannelAuthenticationCapabilities(connectionHandle, cipherSuite,
                requestedPrivilegeLevel);
    }

    /**
     * Establishes the session with the remote host.
     * @param connectionHandle
     * - {@link ConnectionHandle} associated with the remote host.
     * @param username
     * - the username
     * @param password
     * - password matching the username
     * @param bmcKey
     * - the key that should be provided if the two-key authentication is enabled, null otherwise.
     * @throws ConnectionException
     * when connection is in the state that does not allow to perform this operation.
     * @throws Exception
     * when sending message to the managed system or initializing one of the cipherSuite's algorithms fails
     */
    public void openSession(ConnectionHandle connectionHandle, String username, String password, byte[] bmcKey)
            throws Exception {
        asyncConnector.openSession(connectionHandle, username, password, bmcKey);
    }

    /**
     * Closes the session with the remote host if it is currently in open state.
     * @param connectionHandle
     * - {@link ConnectionHandle} associated with the remote host.
     * @throws ConnectionException
     * when connection is in the state that does not allow to perform this operation.
     * @throws Exception
     * when sending message to the managed system or initializing one of the cipherSuite's algorithms fails
     */
    public void closeSession(ConnectionHandle connectionHandle) throws Exception {
        asyncConnector.closeSession(connectionHandle);
    }

    /**
     * Sends the IPMI message to the remote host.
     * @param connectionHandle
     * - {@link ConnectionHandle} associated with the remote host.
     * @param request
     * - {@link IpmiCommandCoder} containing the request to be sent
     * @return {@link ResponseData} for the <b>request</b>
     * @throws ConnectionException
     * when connection is in the state that does not allow to perform this operation.
     * @throws Exception
     * when sending message to the managed system or initializing one of the cipherSuite's algorithms fails
     */
    public ResponseData sendMessage(ConnectionHandle connectionHandle, IpmiCommandCoder request) throws Exception {
        int tries = 0;
        MessageListener listener = new MessageListener(connectionHandle);
        asyncConnector.registerListener(listener);
        ResponseData data = null;
        int previousTag = -1;
        while (data == null) {
            try {
                ++tries;
                int tag = asyncConnector.sendMessage(connectionHandle, request);
                logger.debug("Sending message with tag " + tag + ", try " + tries + ", previous tag " + previousTag);
                previousTag = tag;
                data = listener.waitForAnswer(tag);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (InterruptedException e) {
                throw e;
            } catch (StateConnectionException e) {
                throw e;
            } catch (IPMIException e) {
                if (e.getCompletionCode() == CompletionCode.InitializationInProgress
                        || e.getCompletionCode() == CompletionCode.InsufficientResources
                        || e.getCompletionCode() == CompletionCode.NodeBusy
                        || e.getCompletionCode() == CompletionCode.Timeout) {
                    if (tries > retries) {
                        throw e;
                    } else {
                        long sleepTime = (random.nextLong() % (idleTime / 2)) + (idleTime / 2);

                        Thread.sleep(sleepTime);
                        logger.warn("Receiving message failed, retrying", e);
                    }
                } else {
                    throw e;
                }
            } catch (Exception e) {
                if (tries > retries) {
                    throw e;
                } else {
                	if(e instanceof IOException) {
                		// Network exception common case, no stack needed
                        logger.warn("Receiving message failed " + e.getMessage() + ", retrying");                		
                	} else {
                		// Unexcepted exception, needs investigation
                        logger.warn("Receiving message failed, retrying", e);
                		
                	}
                    long sleepTime = (random.nextLong() % (idleTime / 2)) + (idleTime / 2);
                    Thread.sleep(sleepTime);
                }
            }
        }
        asyncConnector.unregisterListener(listener);
        return data;
    }

    /**
     * Closes the connection with the given handle
     */
    public void closeConnection(ConnectionHandle handle) {
        asyncConnector.closeConnection(handle);
    }

    /**
     * Finalizes the connector and closes all connections.
     */
    public void tearDown() {
        asyncConnector.tearDown();
    }

    /**
     * Changes the timeout value for connection with the given handle.
     * @param handle
     * - {@link ConnectionHandle} associated with the remote host.
     * @param timeout
     * - new timeout value in ms
     */
    public void setTimeout(ConnectionHandle handle, int timeout) {
        asyncConnector.setTimeout(handle, timeout);
    }
}
