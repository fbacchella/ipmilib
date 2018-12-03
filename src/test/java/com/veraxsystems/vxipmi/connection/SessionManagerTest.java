/*
 * SessionManagerTest.java
 * Created on 13-06-2017
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.connection;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SessionManagerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private SessionManager sessionManager;

    @Before
    public void setUp() throws Exception {
        sessionManager = new SessionManager();
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringIncompleteConnectionHandle() throws Exception {
        ConnectionHandle connectionHandle = new ConnectionHandle(1, InetAddress.getByName("100.100.100.100"),
                5000);

        expectedException.expect(IllegalArgumentException.class);

        sessionManager.registerSession(1, connectionHandle);
    }

    @Test
    public void shouldReturnPreviouslyRegisteredSessionWhenRegisteringSessionForSameConnectionTwice() throws Exception {
        ConnectionHandle connectionHandle = createCompleteConnectionHandle(1, "1.2.3.4", 10,
                "admin", "pass");

        int firstSessionId = 1;

        Session firstSession = sessionManager.registerSession(firstSessionId, connectionHandle);
        Session secondSession = sessionManager.registerSession(2, connectionHandle);

        assertSame(firstSession, secondSession);
        assertEquals(firstSessionId, secondSession.getSessionId());
    }

    @Test
    public void shouldReturnNullWhenGettingSessionAndEmptySessionsList() throws Exception {
        assertNull(sessionManager.getSessionForCriteria(InetAddress.getByName("1.1.1.1"), 200, "user"));
    }

    @Test
    public void shouldReturnSessionRegisteredForConnectionWhenGettingSession() throws Exception {
        ConnectionHandle connectionHandle = createCompleteConnectionHandle(2, "2.2.2.2",
                333, "user", "pass");
        int sessionId = 5;

        sessionManager.registerSession(sessionId, connectionHandle);
        Session session = sessionManager.getSessionForCriteria(connectionHandle.getRemoteAddress(),
                connectionHandle.getRemotePort(), connectionHandle.getUser());

        assertEquals(sessionId, session.getSessionId());
        assertSame(connectionHandle, session.getConnectionHandle());
    }

    @Test
    public void shouldNotReturnSessionWhenGettingSessionWithIncompleteConnectionHandle() throws Exception {
        int handle = 1;
        InetAddress remoteAddress = InetAddress.getByName("3.3.3.3");
        int remotePort = 333;
        String user = "user";
        String password = "pass";

        ConnectionHandle connectionHandle = createCompleteConnectionHandle(handle, remoteAddress.getHostAddress(),
                remotePort, user, password);
        int sessionId = 5;

        sessionManager.registerSession(sessionId, connectionHandle);

        assertNull(sessionManager.getSessionForCriteria(remoteAddress, remotePort, null));
    }

    @Test
    public void shouldNotReturnSessionWhenGettingSessionWithUnmatchingConnectionHandle() throws Exception {
        ConnectionHandle connectionHandle = createCompleteConnectionHandle(3, "15.16.17.18",
                1, "some_user", "and_his_pass");
        int sessionId = 100;

        sessionManager.registerSession(sessionId, connectionHandle);

        assertNull(sessionManager.getSessionForCriteria(InetAddress.getByName("33.44.55.66"), 2, "other_user"));
    }

    @Test
    public void shouldReturnSessionWhenGettingSessionWithMatchingConnectionHandleButOtherId() throws Exception {
        String remoteAddress = "7.6.5.4";
        int remotePort = 1000;
        String user = "user";
        String password = "pass";
        int sessionId = 5;

        ConnectionHandle connectionHandle = createCompleteConnectionHandle(100, remoteAddress,
                remotePort, user, password);
        sessionManager.registerSession(sessionId, connectionHandle);

        Session session = sessionManager.getSessionForCriteria(InetAddress.getByName(remoteAddress), remotePort, user);

        assertEquals(sessionId, session.getSessionId());
        assertSame(connectionHandle, session.getConnectionHandle());
    }

    @Test
    public void shouldUnregisterSessionWhenItWasPreviouslyRegistered() throws Exception {
        ConnectionHandle connectionHandle = createCompleteConnectionHandle(2, "2.2.2.2",
                333, "user", "pass");
        int sessionId = 5;

        sessionManager.registerSession(sessionId, connectionHandle);
        sessionManager.unregisterSession(connectionHandle);

        assertNull(sessionManager.getSessionForCriteria(connectionHandle.getRemoteAddress(),
                connectionHandle.getRemotePort(), connectionHandle.getUser()));
    }

    @Test
    public void shouldNotUnregisterSessionWhenConnectionHandleIdDoesntMatch() throws Exception {
        String remoteAddress = "15.16.17.18";
        int remotePort = 1;
        String user = "some_user";
        String password = "and_his_pass";

        int sessionId = 16;

        ConnectionHandle connectionHandle = createCompleteConnectionHandle(1, remoteAddress, remotePort, user, password);
        sessionManager.registerSession(sessionId, connectionHandle);

        ConnectionHandle connectionHandleToUnregister = createCompleteConnectionHandle(10, remoteAddress,
                remotePort, user, password);
        sessionManager.unregisterSession(connectionHandleToUnregister);

        Session session = sessionManager.getSessionForCriteria(connectionHandle.getRemoteAddress(),
                connectionHandle.getRemotePort(), connectionHandle.getUser());

        assertEquals(sessionId, session.getSessionId());
        assertSame(connectionHandle, session.getConnectionHandle());
    }

    private ConnectionHandle createCompleteConnectionHandle(int handle, String remoteAddress, int remotePort,
                String user, String password) throws UnknownHostException {
        ConnectionHandle connectionHandle = new ConnectionHandle(handle, InetAddress.getByName(remoteAddress), remotePort);
        connectionHandle.setUser(user);
        connectionHandle.setPassword(password);

        return connectionHandle;
    }
}