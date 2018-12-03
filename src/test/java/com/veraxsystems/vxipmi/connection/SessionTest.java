/*
 * SessionTest.java
 * Created on 01-06-2017
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class SessionTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldReturnSessionIdPassedInConstructor() throws Exception {
        int sessionId = 15;
        Session session = new Session(sessionId, null);

        assertEquals(sessionId, session.getSessionId());
    }

    @Test
    public void shouldReturnConnectionHandlePassedInConstructor() throws Exception {
        ConnectionHandle connectionHandle = new ConnectionHandle(10, InetAddress.getByName("1.1.1.1"), 20);
        Session session = new Session(1, connectionHandle);

        assertSame(connectionHandle, session.getConnectionHandle());
    }

}