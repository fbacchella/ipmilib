/*
 * VxipmiRunner.java 
 * Created on 2011-09-20
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.test;

import java.net.InetAddress;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.connection.Connection;

public class SkipAuthCapRunner {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        IpmiConnector connector;

        // Create the connector, specify port that will be used to communicate
        // with the remote host. The UDP layer starts listening at this port, so
        // no 2 connectors can work at the same time on the same port.
        connector = new IpmiConnector(6000);
        System.out.println("Connector created");

        // Create the connection and get the handle, specify IP address of the
        // remote host. The connection is being registered in ConnectionManager,
        // the handle will be needed to identify it among other connections
        // (target IP address isn't enough, since we can handle multiple
        // connections to the same host)
        // Since the Get Channel Authentication Capabilities phase will be skipped, preset cipher suite and privilege
        // level is provided
        ConnectionHandle handle = connector.createConnection(InetAddress.getByName("192.168.1.1"),
                Connection.getDefaultCipherSuite(), PrivilegeLevel.User);
        System.out.println("Connection created");

        // Start the session, provide username and password, and optionally the
        // BMC key (only if the remote host has two-key authentication enabled,
        // otherwise this parameter should be null)
        connector.openSession(handle, "user", "pass", null);
        System.out.println("Session open");

        // Send some message and read the response
        GetChassisStatusResponseData rd = (GetChassisStatusResponseData) connector.sendMessage(handle,
                new GetChassisStatus(IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus));

        System.out.println("Received answer");
        System.out.println("System power state is " + (rd.isPowerOn() ? "up" : "down"));

        // Close the session
        connector.closeSession(handle);
        System.out.println("Session closed");

        // Close connection manager and release the listener port.
        connector.tearDown();
        System.out.println("Connection manager closed");
    }

}
