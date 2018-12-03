/*
 * ConnectionHandle.java 
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

import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.connection.Connection;

import java.net.InetAddress;

/**
 * Handle to the {@link Connection}
 */
public class ConnectionHandle {
    private int handle;
    private CipherSuite cipherSuite;
    private PrivilegeLevel privilegeLevel;
    private InetAddress remoteAddress;
    private int remotePort;
    private String user;
    private String password;

    public ConnectionHandle(int handle, InetAddress remoteAddress, int remotePort) {
        this.handle = handle;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    public void setCipherSuite(CipherSuite cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    public PrivilegeLevel getPrivilegeLevel() {
        return privilegeLevel;
    }

    public void setPrivilegeLevel(PrivilegeLevel privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }

    public int getHandle() {
        return handle;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionHandle{");
        sb.append("handle=").append(handle);
        sb.append(", cipherSuite=").append(cipherSuite);
        sb.append(", privilegeLevel=").append(privilegeLevel);
        sb.append(", remoteAddress=").append(remoteAddress);
        sb.append(", remotePort=").append(remotePort);
        sb.append(", user='").append(user).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
