/*
 * ConnectionTest.java 
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

import com.veraxsystems.vxipmi.coding.Encoder;
import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.sel.ReserveSel;
import com.veraxsystems.vxipmi.coding.commands.sel.ReserveSelResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelCipherSuites;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelCipherSuitesResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.OpenSession;
import com.veraxsystems.vxipmi.coding.commands.session.OpenSessionResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp1;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp1ResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp3;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp3ResponseData;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.protocol.encoder.ProtocolEncoder;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv15Encoder;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.SecurityConstants;
import com.veraxsystems.vxipmi.common.Constants;
import com.veraxsystems.vxipmi.common.Randomizer;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionListener;
import com.veraxsystems.vxipmi.connection.SessionManager;
import com.veraxsystems.vxipmi.sm.actions.MessageAction;
import com.veraxsystems.vxipmi.sm.actions.ResponseAction;
import com.veraxsystems.vxipmi.sm.actions.StateMachineAction;
import com.veraxsystems.vxipmi.transport.UdpMessage;
import com.veraxsystems.vxipmi.transport.UdpMessenger;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Tests for the {@link Connection} class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Randomizer.class, Connection.class, SessionManager.class})
@PowerMockIgnore("javax.crypto.*")
public class ConnectionTest {

    private static Logger logger = Logger.getLogger(ConnectionTest.class);
    private static final int CIPHER_SUITE = 2;

    @Mock
    private UdpMessenger messenger;

    private Connection connection;
    private Properties properties;
    private Timer timerSpy;

    private int pingPeriod = 30000;
    private int sessionId = 100;
    private int managedSystemSessionId = 1;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        properties.load(new FileInputStream(
                "src/test/resources/test.properties"));
        connection = new Connection(messenger, 0);
        timerSpy = spy(new Timer());

        whenNew(Timer.class).withNoArguments().thenReturn(timerSpy);
        doNothing().when(timerSpy).schedule(any(TimerTask.class), anyLong(), anyLong());

        mockStatic(SessionManager.class);
        when(SessionManager.generateSessionId()).thenReturn(sessionId);

        mockStatic(Randomizer.class);
        when(Randomizer.getInt()).thenReturn(222);

        connection.connect(InetAddress.getByName(properties.getProperty("testIp")), Constants.IPMI_PORT, pingPeriod);
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null)
            connection.disconnect();
    }

    /**
     * Tests {@link Connection#getAvailableCipherSuites(int)},
     * {@link Connection#getChannelAuthenticationCapabilities(int, CipherSuite, PrivilegeLevel)}
     * and
     * {@link Connection#startSession(int, CipherSuite, PrivilegeLevel, String, String, byte[])}
     */
    @Test
    public void testSessionChallenge() throws Exception {
        logger.info("Testing Session Challenge");

        try {
            List<CipherSuite> availableCipherSuites = verifyGettingAvailableCipherSuites();

            CipherSuite cs = availableCipherSuites.get(CIPHER_SUITE);

            verifyGettingAuthenticationCapabilities(cs);

            verifyStartingSession(cs);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    /**
     * Tests
     * {@link Connection#sendMessage(PayloadCoder, boolean)}
     */
    @Test
    public void testSendMessage() throws Exception {
        logger.info("Testing Send Message");

        ConnectionListener listener = mock(ConnectionListener.class);
        connection.registerListener(listener);

        try {
            CipherSuite cs = verifyGettingAvailableCipherSuites().get(CIPHER_SUITE);
            verifyGettingAuthenticationCapabilities(cs);
            verifyStartingSession(cs);

            PayloadCoder payloadCoder = spy(new ReserveSel(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));
            ReserveSelResponseData expectedResponseData = new ReserveSelResponseData();

            doReturn(expectedResponseData).when(payloadCoder).getResponseData(any(Ipmiv20Message.class));

            byte sequenceNumber = 1;

            IpmiLanResponse responsePayload = mock(IpmiLanResponse.class);
            when(responsePayload.getSequenceNumber()).thenReturn(sequenceNumber);

            Ipmiv20Message responseMessage = new Ipmiv20Message(cs.getConfidentialityAlgorithm());
            responseMessage.setPayloadType(PayloadType.Ipmi);
            responseMessage.setPayload(responsePayload);
            responseMessage.setSessionID(managedSystemSessionId);
            responseMessage.setSessionSequenceNumber(sequenceNumber);

            UdpMessage udpMessage = mockUdpMessage(sequenceNumber, managedSystemSessionId, payloadCoder, new MessageAction(responseMessage), new Protocolv20Encoder());

            int tag = connection.sendMessage(payloadCoder, false);

            verify(listener).processResponse(eq(expectedResponseData), eq(connection.getHandle()), eq(tag), isNull(Exception.class));
            verify(messenger).send(refEq(udpMessage));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    /**
     * Tests if the connection is kept up by sending no-op commands properly.
     */
    @Test
    public void testConnectionKeepup() throws Exception {
        logger.info("Testing Connection keepup");

        try {
            CipherSuite cs = verifyGettingAvailableCipherSuites().get(CIPHER_SUITE);
            verifyGettingAuthenticationCapabilities(cs);
            verifyStartingSession(cs);

            long pingPeriodLong = (long) pingPeriod;
            verify(timerSpy).schedule(eq(connection), eq(pingPeriodLong), eq(pingPeriodLong));

            byte sequenceNumber = 1;

            PayloadCoder keepupPayloadCoder = new GetChannelAuthenticationCapabilities(IpmiVersion.V20, IpmiVersion.V20,
                    cs, PrivilegeLevel.Callback, TypeConverter.intToByte(0xe));

            IpmiLanResponse responsePayload = mock(IpmiLanResponse.class);
            when(responsePayload.getSequenceNumber()).thenReturn(sequenceNumber);

            Ipmiv20Message responseMessage = new Ipmiv20Message(cs.getConfidentialityAlgorithm());
            responseMessage.setPayloadType(PayloadType.Ipmi);
            responseMessage.setPayload(responsePayload);
            responseMessage.setSessionID(managedSystemSessionId);
            responseMessage.setSessionSequenceNumber(sequenceNumber);

            UdpMessage keepupUdpMessage = mockUdpMessage(sequenceNumber, managedSystemSessionId, keepupPayloadCoder,
                    new MessageAction(responseMessage), new Protocolv20Encoder());

            connection.run();

            verify(messenger).send(refEq(keepupUdpMessage));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        connection.closeSession();
    }

    private List<CipherSuite> verifyGettingAvailableCipherSuites() throws Exception {
        byte[] availableCipherSuitesRaw = new byte[] {(byte) 0xC0, (byte) 0, SecurityConstants.AA_RAKP_NONE, SecurityConstants.CA_NONE | -128, SecurityConstants.IA_NONE | 64,
                (byte) 0xC0, (byte) 1, SecurityConstants.AA_RAKP_HMAC_SHA1, SecurityConstants.CA_NONE | -128, SecurityConstants.IA_NONE | 64,
                (byte) 0xC0, (byte) 2, SecurityConstants.AA_RAKP_HMAC_SHA1, SecurityConstants.CA_NONE | -128, SecurityConstants.IA_HMAC_SHA1_96 | 64};

        PayloadCoder getCipherSuitesPayloadCoder = new GetChannelCipherSuites(TypeConverter.intToByte(0xE), (byte) 0);

        GetChannelCipherSuitesResponseData getCipherSuitesResponseData = new GetChannelCipherSuitesResponseData();
        getCipherSuitesResponseData.setCipherSuiteData(availableCipherSuitesRaw);

        UdpMessage getCipherSuitesUdpMessage = mockUdpMessage(0, 0, getCipherSuitesPayloadCoder,
                new ResponseAction(getCipherSuitesResponseData),
                new Protocolv20Encoder());

        List<CipherSuite> expectedCipherSuites = CipherSuite.getCipherSuites(availableCipherSuitesRaw);

        List<CipherSuite> availableCipherSuites = connection.getAvailableCipherSuites(0);
        assertThat(availableCipherSuites, samePropertyValuesAs(expectedCipherSuites));

        verify(messenger).send(refEq(getCipherSuitesUdpMessage));

        return availableCipherSuites;
    }

    private void verifyGettingAuthenticationCapabilities(CipherSuite cs) throws Exception {
        PayloadCoder getAuthenticationPayloadCoder = new GetChannelAuthenticationCapabilities(IpmiVersion.V15,
                IpmiVersion.V20, cs, PrivilegeLevel.User, (byte) 0xE);

        ResponseData responseData = new GetChannelAuthenticationCapabilitiesResponseData();

        UdpMessage authenticationUdpMessage = mockUdpMessage(0, 0, getAuthenticationPayloadCoder,
                new ResponseAction(responseData), new Protocolv15Encoder());

        connection.getChannelAuthenticationCapabilities(0, cs,
                PrivilegeLevel.User);

        verify(messenger).send(refEq(authenticationUdpMessage));
    }

    private void verifyStartingSession(CipherSuite cs) throws Exception {
        OpenSession openSessionPayloadCoder = new OpenSession(sessionId, PrivilegeLevel.User, cs);
        OpenSessionResponseData openSessionResponseData = new OpenSessionResponseData();
        openSessionResponseData.setManagedSystemSessionId(managedSystemSessionId);

        UdpMessage openSessionUdpMessage = mockUdpMessage(0, 0, openSessionPayloadCoder,
                new ResponseAction(openSessionResponseData), new Protocolv20Encoder());

        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        Rakp1 rakp1 = new Rakp1(managedSystemSessionId, PrivilegeLevel.User, username, password, null, cs);
        Rakp1ResponseData rakp1ResponseData = new Rakp1ResponseData();
        rakp1ResponseData.setManagedSystemGuid(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        rakp1ResponseData.setManagedSystemRandomNumber(new byte[] {17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32});

        UdpMessage rakp1UdpMessage = mockUdpMessage(0, 0, rakp1, new ResponseAction(rakp1ResponseData),
                new Protocolv20Encoder());

        Rakp3 rakp3 = new Rakp3((byte) 0, managedSystemSessionId, cs, rakp1, rakp1ResponseData);
        Rakp3ResponseData rakp3ResponseData = new Rakp3ResponseData();

        UdpMessage rakp3UdpMessage = mockUdpMessage(0, 0, rakp3, new ResponseAction(rakp3ResponseData),
                new Protocolv20Encoder());

        connection.startSession(0, cs, PrivilegeLevel.User,
                username,
                password, null);

        verify(messenger).send(refEq(openSessionUdpMessage));
        verify(messenger).send(refEq(rakp1UdpMessage));
        verify(messenger).send(refEq(rakp3UdpMessage));
    }

    private UdpMessage mockUdpMessage(int sequenceNumber, int sessionId, PayloadCoder payloadCoder, final StateMachineAction responseAction, ProtocolEncoder protocolEncoder)
            throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        byte[] rawMessageBody = Encoder.encode(protocolEncoder, payloadCoder, sequenceNumber, sequenceNumber, sessionId);

        UdpMessage udpMessage = new UdpMessage();
        udpMessage.setAddress(connection.getRemoteMachineAddress());
        udpMessage.setPort(connection.getRemoteMachinePort());
        udpMessage.setMessage(rawMessageBody);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                connection.notify(responseAction);
                return null;
            }
        }).when(messenger).send(refEq(udpMessage));

        return udpMessage;
    }
}
