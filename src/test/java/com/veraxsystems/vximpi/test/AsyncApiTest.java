/*
 * AsyncApiTest.java 
 * Created on 2011-09-20
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vximpi.test;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.async.IpmiAsyncConnector;
import com.veraxsystems.vxipmi.api.async.IpmiResponseListener;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponse;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponseData;
import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.SecurityConstants;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the Asynchronous API
 */
@RunWith(MockitoJUnitRunner.class)
public class AsyncApiTest implements IpmiResponseListener {

    private static Logger logger = Logger.getLogger(AsyncApiTest.class);
    private static final int PORT = 6666;

    private static IpmiAsyncConnector connector;
    private static  Properties properties;
    private IpmiResponse response;

    @Mock
    private ConnectionHandle handle;

    @Mock
    private ConnectionManager connectionManager;

    @Mock
    private Connection connection;

    private List<CipherSuite> availableCipherSuites;

    @BeforeClass
    public static void setUpBeforeClass() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(
                    "src/test/resources/test.properties"));
            connector = new IpmiAsyncConnector(PORT);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() {
        connector.tearDown();
    }

    @Before
    public void setUp() throws Exception {
        Whitebox.setInternalState(connector, "connectionManager", connectionManager);

        int handleId = 1;

        when(handle.getHandle()).thenReturn(handleId);

        when(connection.isSessionValid()).thenReturn(true);

        availableCipherSuites = Arrays.asList(new CipherSuite((byte) 0, SecurityConstants.AA_RAKP_NONE, SecurityConstants.CA_NONE, SecurityConstants.IA_NONE),
                new CipherSuite((byte) 1, SecurityConstants.AA_RAKP_HMAC_SHA1, SecurityConstants.CA_NONE, SecurityConstants.IA_NONE),
                new CipherSuite((byte) 2, SecurityConstants.AA_RAKP_HMAC_SHA1, SecurityConstants.CA_NONE, SecurityConstants.IA_HMAC_SHA1_96),
                new CipherSuite((byte) 3, SecurityConstants.AA_RAKP_HMAC_SHA1, SecurityConstants.CA_AES_CBC128, SecurityConstants.IA_HMAC_SHA1_96));


        when(connectionManager.getAvailableCipherSuites(eq(handleId))).thenReturn(availableCipherSuites);
        when(connectionManager.getConnection(eq(handleId))).thenReturn(connection);
        when(connectionManager.createConnection(any(InetAddress.class), anyInt())).thenReturn(handleId);

        response = null;

        connector.registerListener(this);
    }

    @After
    public void tearDown() throws Exception {
        connector.closeSession(handle);
    }

    @Test
    public void shouldReturnProperResultWhenGetAvailableCipherSuites() throws Exception {
        logger.info("Testing GetAvailableCipherSuites");
        List<CipherSuite> cipherSuites = connector.getAvailableCipherSuites(handle);

        verify(connectionManager).getAvailableCipherSuites(eq(handle.getHandle()));
        assertEquals(availableCipherSuites, cipherSuites);
    }

    @Test
    public void shouldFillAuthenticationDataInConnectionHandleWhenGetChannelAuthenticationCapabilities() throws Exception {
        logger.info("Testing GetAvailableCipherSuites");
        logger.info("Testing GetChannelAuthenticationCapabilities");

        when(connectionManager.getChannelAuthenticationCapabilities(anyInt(), any(CipherSuite.class), any(PrivilegeLevel.class)))
                .thenReturn(new GetChannelAuthenticationCapabilitiesResponseData());

        PrivilegeLevel privilegeLevel = PrivilegeLevel.User;
        CipherSuite cs = connector.getAvailableCipherSuites(handle).get(3);
        connector.getChannelAuthenticationCapabilities(handle, cs, privilegeLevel);

        verify(connectionManager).getChannelAuthenticationCapabilities(eq(handle.getHandle()), eq(cs), eq(privilegeLevel));
        verify(handle).setCipherSuite(eq(cs));
        verify(handle).setPrivilegeLevel(eq(privilegeLevel));
    }

    @Test
    public void shouldStartSessionOnConnectionManagerWhenOpenSession() throws Exception {
        logger.info("Testing OpenSession");

        CipherSuite cipherSuite = CipherSuite.getEmpty();
        PrivilegeLevel privilegeLevel = PrivilegeLevel.User;
        String user = properties.getProperty("username");
        String password = properties.getProperty("password");
        InetAddress address = InetAddress.getByName(properties.getProperty("testIp"));

        when(handle.getCipherSuite()).thenReturn(cipherSuite);
        when(handle.getPrivilegeLevel()).thenReturn(privilegeLevel);
        when(handle.getRemoteAddress()).thenReturn(address);
        when(handle.getUser()).thenReturn(user);
        when(handle.getPassword()).thenReturn(password);

        connector.openSession(handle, user, password, null);

        verify(connectionManager).startSession(eq(handle.getHandle()), eq(cipherSuite), eq(privilegeLevel),
                eq(user), eq(password), any(byte[].class));
    }

    @Test
    public void shouldSendMessageAndReportResponseWhenSendMessage() throws Exception {
        logger.info("Testing sending message");

        int tag = 5;

        when(connection.sendMessage(any(PayloadCoder.class), anyBoolean())).thenReturn(tag);

        PayloadCoder payloadCoder = new GetChassisStatus(IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus);

        int resultTag = connector.sendMessage(handle, payloadCoder, false);

        verify(connectionManager).getConnection(eq(handle.getHandle()));
        verify(connection).sendMessage(eq(payloadCoder), eq(false));

        assertEquals(tag, resultTag);

        ResponseData responseData = new GetChassisStatusResponseData();
        connector.processResponse(responseData, handle.getHandle(), resultTag, null);

        assertThat(response, instanceOf(IpmiResponseData.class));
        assertEquals(tag, response.getTag());
        assertEquals(responseData, ((IpmiResponseData) response).getResponseData());
    }

    @Override
    public void notify(IpmiResponse response) {
        this.response = response;
    }

}
