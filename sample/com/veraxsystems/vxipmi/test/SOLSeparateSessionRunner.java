/*
 * SOLSeparateSessionRunner.java
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

import com.veraxsystems.vxipmi.api.sol.CipherSuiteSelectionHandler;
import com.veraxsystems.vxipmi.api.sol.SerialOverLan;
import com.veraxsystems.vxipmi.api.sol.SolEventListener;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.payload.sol.SolOperation;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

import java.util.List;
import java.util.Set;

/**
 * This sample shows how to open Serial over LAN communication on separate session and how to send and receive SOL messages.
 */
public class SOLSeparateSessionRunner {

    public static void main(String[] args) throws Exception {
        //Create object that will be used when negotiating Cipher Suite that will be used during session.
        //Given available Cipher Suites returned by BMC, it should choose one of them.
        CipherSuiteSelectionHandler cipherSuitePicker = new CipherSuiteSelectionHandler() {
            @Override
            public CipherSuite choose(List<CipherSuite> availableCipherSuites) {
                return availableCipherSuites.get(3);
            }
        };

        IpmiConnector connector = new IpmiConnector(6000);

        // Open Serial over LAN connection and session and activates SOL payload on the server side.
        // The UDP layer starts listening at this port, so no 2 connectors can work at the same time on the same port.
        final SerialOverLan serialOverLan = new SerialOverLan(connector, "192.168.1.1",
                "user", "pass", cipherSuitePicker);
        System.out.println("Serial over LAN connection and session created");

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

        // Deactivates Serial over LAN payload, close SOL session and tear down connection.
        serialOverLan.close();
        System.out.println("SOL connection and session closed");

        listenerThread.interrupt();
    }
}
