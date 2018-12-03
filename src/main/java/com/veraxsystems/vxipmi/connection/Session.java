/*
 * Session.java
 * Created on 31.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.connection;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;

/**
 * Single opened IPMI session handle. It contains all information necessary to distinguish session among others.
 */
public class Session {

    private final int sessionId;
    private final ConnectionHandle connectionHandle;

    public Session(int sessionId, ConnectionHandle connectionHandle) {
        this.sessionId = sessionId;
        this.connectionHandle = connectionHandle;
    }

    public int getSessionId() {
        return sessionId;
    }

    public ConnectionHandle getConnectionHandle() {
        return connectionHandle;
    }

}
