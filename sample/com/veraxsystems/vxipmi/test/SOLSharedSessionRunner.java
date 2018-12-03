/*
 * SOLSharedSessionRunner.java
 * Created on 16.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.test;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sol.SerialOverLan;
import com.veraxsystems.vxipmi.api.sol.SolEventListener;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.payload.sol.SolOperation;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.connection.Session;

import java.net.InetAddress;
import java.util.Set;

/**
 * This sample shows how to open Serial over LAN communication on existing session and how to send and receive SOL messages.
 */
public class SOLSharedSessionRunner {

    public static void main(String[] args) throws Exception {
        // Create the connector, specify port that will be used to communicate
        // with the remote host. The UDP layer starts listening at this port, so
        // no 2 connectors can work at the same time on the same port.
        final IpmiConnector connector = new IpmiConnector(6000);
        System.out.println("Connector created");

        // Create the connection and get the handle, specify IP address of the
        // remote host. The connection is being registered in ConnectionManager,
        // the handle will be needed to identify it among other connections
        // (target IP address isn't enough, since we can handle multiple
        // connections to the same host)
        final ConnectionHandle handle = connector.createConnection(InetAddress.getByName("192.168.1.1"));
        System.out.println("Connection created");

        // Get available cipher suites list via getAvailableCipherSuites and
        // pick one of them that will be used further in the session.
        CipherSuite cs = connector.getAvailableCipherSuites(handle).get(0);
        System.out.println("Cipher suite picked");

        // Provide chosen cipher suite and privilege level to the remote host.
        // From now on, your connection handle will contain these information.
        connector.getChannelAuthenticationCapabilities(handle, cs, PrivilegeLevel.Administrator);
        System.out.println("Channel authentication capabilities receivied");

        // Start the session, provide username and password, and optionally the
        // BMC key (only if the remote host has two-key authentication enabled,
        // otherwise this parameter should be null)
        Session session = connector.openSession(handle, "user", "pass", null);
        System.out.println("Session open");

        // Activates SOL payload on the server side, using existing session
        final SerialOverLan serialOverLan = new SerialOverLan(connector, session);

        // Register listener thread so that it can asynchronously print incoming messages.
        final Thread listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    String text = serialOverLan.readString();

                    if (!text.isEmpty()) {
                        System.out.print(text);
                    }
                }
            }
        });

        listenerThread.start();

        // Register event listener for listening for Statuses reported by remote serial port's controller
        SolEventListener solEventListener = new SolEventListener() {
            @Override
            public void processRequestEvent(Set<SolStatus> statuses) {
                System.out.println("Request Event received; statuses: " + statuses);
            }

            @Override
            public void processResponseEvent(Set<SolStatus> statuses, byte[] correspondingRequestData, Set<SolOperation> correspondingRequestOperations) {
                if (!correspondingRequestOperations.isEmpty()) {
                    System.out.println("Response event received for operations '" + correspondingRequestOperations + "'; " +
                            "statuses: " + statuses);
                } else {
                    System.out.println("Response event received for message '" + new String(correspondingRequestData) + "'; " +
                            "statuses: " + statuses);
                }
            }
        };

        serialOverLan.registerEventListener(solEventListener);

        // Send some messages over to the remote server
        boolean success = serialOverLan.writeString("Hello ");
        System.out.println("'Hello' was " + (success ? "" : "not ") + "sent");

        success = serialOverLan.writeBytes("World!".getBytes());
        System.out.println("'World!' was " + (success ? "" : "not ") + "sent");

        // Send also Break operation to the BMC.
        // SolOperation enum contains all available operations that can be invoked with SOL.
        success = serialOverLan.invokeOperations(SolOperation.Break);
        System.out.println("Sending break was " + (success ? "" : "not ") + "successful");

        System.out.println("Waiting for incoming SOL messages. Press ENTER to stop.");
        System.in.read();

        // Deactivates Serial over LAN payload
        serialOverLan.close();

        // Close the session
        connector.closeSession(handle);
        System.out.println("Session closed");

        // Close connection manager and release the listener port.
        connector.tearDown();
        System.out.println("Connection manager closed");

        listenerThread.interrupt();
    }
}
