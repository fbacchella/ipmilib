/*
 * StateMachineTest.java 
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
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepository;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelCipherSuitesResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.OpenSessionResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp1ResponseData;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp3ResponseData;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.security.AuthenticationRakpHmacSha1;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityAesCbc128;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityAlgorithm;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityNone;
import com.veraxsystems.vxipmi.coding.security.IntegrityHmacSha1_96;
import com.veraxsystems.vxipmi.common.Constants;
import com.veraxsystems.vxipmi.sm.MachineObserver;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.ResponseAction;
import com.veraxsystems.vxipmi.sm.events.AuthenticationCapabilitiesReceived;
import com.veraxsystems.vxipmi.sm.events.Authorize;
import com.veraxsystems.vxipmi.sm.events.Default;
import com.veraxsystems.vxipmi.sm.events.DefaultAck;
import com.veraxsystems.vxipmi.sm.events.GetChannelCipherSuitesPending;
import com.veraxsystems.vxipmi.sm.events.OpenSessionAck;
import com.veraxsystems.vxipmi.sm.events.Rakp2Ack;
import com.veraxsystems.vxipmi.sm.events.Sendv20Message;
import com.veraxsystems.vxipmi.sm.events.StartSession;
import com.veraxsystems.vxipmi.sm.events.Timeout;
import com.veraxsystems.vxipmi.sm.states.Authcap;
import com.veraxsystems.vxipmi.sm.states.AuthcapWaiting;
import com.veraxsystems.vxipmi.sm.states.Ciphers;
import com.veraxsystems.vxipmi.sm.states.CiphersWaiting;
import com.veraxsystems.vxipmi.sm.states.OpenSessionComplete;
import com.veraxsystems.vxipmi.sm.states.OpenSessionWaiting;
import com.veraxsystems.vxipmi.sm.states.Rakp1Complete;
import com.veraxsystems.vxipmi.sm.states.Rakp1Waiting;
import com.veraxsystems.vxipmi.sm.states.Rakp3Complete;
import com.veraxsystems.vxipmi.sm.states.Rakp3Waiting;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import com.veraxsystems.vxipmi.sm.states.Uninitialized;
import com.veraxsystems.vxipmi.transport.UdpMessage;
import com.veraxsystems.vxipmi.transport.UdpMessenger;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link StateMachine}
 */
@RunWith(MockitoJUnitRunner.class)
public class StateMachineTest {

    private static Logger logger = Logger.getLogger(StateMachineTest.class);

    private com.veraxsystems.vxipmi.sm.StateMachine machine;

    private int sequenceNumber = 0;
    private CipherSuite cs;
    private int sessionId = 1230;
    private int managedSystemSessionId = 1;
    private int port = Constants.IPMI_PORT;
    private InetAddress address;

    @Mock
    private MachineObserver machineObserver;

    @Mock
    private UdpMessenger messenger;

    private Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        properties.load(new FileInputStream("src/test/resources/test.properties"));

        address = InetAddress.getByName(properties.getProperty("testIp"));

        machine = new com.veraxsystems.vxipmi.sm.StateMachine(messenger);
        machine.register(machineObserver);
        machine.start(address, port);
    }

    @After
    public void tearDown() throws Exception {
        machine.stop();
    }

    /**
     * Tests {@link StateMachine} set up.
     */
    @Test
    public void testSetUp() throws Exception {
        logger.info("Testing State Machine set up");

        verify(messenger).register(eq(machine));
    }

    /**
     * Tests {@link CiphersWaiting} timeout.
     */
    @Test
    public void testCwTimeout() throws Exception {
        logger.info("Testing CiphersWaiting timeout");
        machine.doTransition(new GetChannelCipherSuitesPending(getSequenceNumber()));
        assertEquals(CiphersWaiting.class, machine.getCurrent().getClass());
        machine.doTransition(new Timeout());
        assertEquals(Uninitialized.class, machine.getCurrent().getClass());
    }

    /**
     * Tests {@link Ciphers}
     */
    @Test
    public void testSmCiphers() throws Exception {
        logger.info("Testing Ciphers");
        doGetCipherSuites(new CipherSuite((byte) 2,
                new AuthenticationRakpHmacSha1().getCode(),
                new ConfidentialityNone().getCode(),
                new IntegrityHmacSha1_96().getCode()));
        assertEquals(machine.getCurrent().getClass(), AuthcapWaiting.class);
    }

    /**
     * Tests {@link Authcap}
     */
    @Test
    public void testAuthCap() throws Exception {
        logger.info("Testing Authcap");
        testSmCiphers();

        doGetAuthCap();
    }

    /**
     * Tests {@link Authcap} timeout.
     */
    @Test
    public void testAuthCapTimeout() throws Exception {
        logger.info("Testing Authcap timeout");
        testSmCiphers();

        machine.doTransition(new Timeout());
        assertEquals(Ciphers.class, machine.getCurrent().getClass());
    }

    /**
     * Tests {@link OpenSessionAck}.
     */
    @Test
    public void testOpenSession() throws Exception {
        logger.info("Testing OpenSession");
        testAuthCap();

        doOpenSession();
    }

    /**
     * Tests {@link OpenSessionAck} timeout.
     */
    @Test
    public void testOpenSessionTimeout() throws Exception {
        logger.info("Testing OpenSession timeout");
        testAuthCap();
        machine.doTransition(new Timeout());
        assertEquals(Authcap.class, machine.getCurrent().getClass());
    }

    /**
     * Tests {@link Rakp2Ack}
     */
    @Test
    public void testRakp1() throws Exception {
        logger.info("Testing Rakp1");
        testOpenSession();
        doRakp1();
    }

    /**
     * Tests {@link Rakp2Ack} timeout.
     */
    @Test
    public void testRakp1Timeout() throws Exception {
        logger.info("Testing Rakp1 timeout");
        testOpenSession();
        machine.doTransition(new Timeout());
        assertEquals(Authcap.class, machine.getCurrent().getClass());
    }

    /**
     * Tests {@link StartSession}
     */
    @Test
    public void testStartSession() throws Exception {
        logger.info("Testing StartSession");
        testRakp1();
        doRakp3();
    }

    /**
     * Tests {@link StartSession} timeout
     */
    @Test
    public void testRakp3Timeout() throws Exception {
        logger.info("Testing StartSession timeout");
        testRakp1();
        machine.doTransition(new Timeout());
        assertEquals(Authcap.class, machine.getCurrent().getClass());
    }

    /**
     * Tests {@link Sendv20Message}
     */
    @Test
    public void testSendInSessionMessage() throws Exception {
        logger.info("Testing sending in-session message");
        testStartSession();

        int sequenceNumber = getSequenceNumber();

        PayloadCoder payloadCoder = new ReserveSdrRepository(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus);

        byte[] rawMessage = Encoder.encode(new Protocolv20Encoder(), payloadCoder, sequenceNumber, sequenceNumber, managedSystemSessionId);

        UdpMessage expectedUdpMessage = new UdpMessage();
        expectedUdpMessage.setAddress(address);
        expectedUdpMessage.setPort(port);
        expectedUdpMessage.setMessage(rawMessage);

        machine.doTransition(new Sendv20Message(payloadCoder, managedSystemSessionId, sequenceNumber, sequenceNumber));

        verify(messenger).send(refEq(expectedUdpMessage));
    }

    /**
     * Tests {@link com.veraxsystems.vxipmi.sm.events.CloseSession}
     */
    @Test
    public void testCloseSession() throws Exception {
        logger.info("Testing CloseSession");
        testStartSession();

        int sequenceNumber = getSequenceNumber();

        machine.doTransition(new com.veraxsystems.vxipmi.sm.events.CloseSession(
                managedSystemSessionId, sequenceNumber, sequenceNumber));
        assertEquals(Authcap.class, machine.getCurrent().getClass());
    }

    /**
     * Tests {@link ConfidentialityAesCbc128} encryption algorithm.
     */
    @Test
    public void testEncryption() throws Exception {
        logger.info("Testing encryption");

        byte[] encryptedData = new byte[] {5, 5, 5, 5, 5, 5, 5, 5};

        ConfidentialityAlgorithm confidentialityAlgorithm = mock(ConfidentialityAesCbc128.class);
        when(confidentialityAlgorithm.encrypt(any(byte[].class))).thenReturn(encryptedData);
        when(confidentialityAlgorithm.getCode()).thenCallRealMethod();
        when(confidentialityAlgorithm.getConfidentialityOverheadSize(anyInt())).thenCallRealMethod();


        doGetCipherSuites(new CipherSuite((byte) 3,
                new AuthenticationRakpHmacSha1().getCode(),
                new ConfidentialityAesCbc128().getCode(),
                new IntegrityHmacSha1_96().getCode()));
        assertEquals(machine.getCurrent().getClass(), AuthcapWaiting.class);
        doGetAuthCap();
        doOpenSession();
        doRakp1();
        doRakp3();
        int sequenceNumber = getSequenceNumber();

        Whitebox.setInternalState(cs, "ca", confidentialityAlgorithm);

        PayloadCoder payloadCoder = new ReserveSdrRepository(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus);

        byte[] rawMessage = Encoder.encode(new Protocolv20Encoder(), payloadCoder, sequenceNumber, sequenceNumber, managedSystemSessionId);

        UdpMessage expectedUdpMessage = new UdpMessage();
        expectedUdpMessage.setAddress(address);
        expectedUdpMessage.setPort(port);
        expectedUdpMessage.setMessage(rawMessage);

        machine.doTransition(new Sendv20Message(payloadCoder, managedSystemSessionId, sequenceNumber, sequenceNumber));

        verify(messenger).send(refEq(expectedUdpMessage));
    }

    private void doGetCipherSuites(CipherSuite cipherSuite) {
        machine.doTransition(new GetChannelCipherSuitesPending(getSequenceNumber()));

        ResponseAction responseAction = new ResponseAction(new GetChannelCipherSuitesResponseData());
        machine.doExternalAction(responseAction);

        verify(machineObserver).notify(eq(responseAction));

        cs = cipherSuite;

        machine.doTransition(new DefaultAck());
        machine.doTransition(new Default(cs, getSequenceNumber(), PrivilegeLevel.User));
    }

    private void doGetAuthCap() {
        ResponseAction responseAction = new ResponseAction(new GetChannelAuthenticationCapabilitiesResponseData());
        machine.doExternalAction(responseAction);

        verify(machineObserver).notify(eq(responseAction));

        machine.doTransition(new AuthenticationCapabilitiesReceived(666, PrivilegeLevel.MaximumAvailable));

        assertEquals(Authcap.class, machine.getCurrent().getClass());

        machine.doTransition(new Authorize(cs, getSequenceNumber(), PrivilegeLevel.User, ++sessionId));
        assertEquals(OpenSessionWaiting.class, machine.getCurrent().getClass());
    }

    private void doOpenSession() {
        ResponseAction responseAction = new ResponseAction(new OpenSessionResponseData());
        machine.doExternalAction(responseAction);

        verify(machineObserver).notify(eq(responseAction));

        machine.doTransition(new DefaultAck());

        assertEquals(OpenSessionComplete.class, machine.getCurrent().getClass());

        machine.doTransition(new OpenSessionAck(cs, PrivilegeLevel.User,
                getSequenceNumber(), managedSystemSessionId, properties
                        .getProperty("username"), properties
                        .getProperty("password"), null));

        assertEquals(Rakp1Waiting.class, machine.getCurrent().getClass());
    }

    private void doRakp1() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Rakp1ResponseData data = new Rakp1ResponseData();
        data.setManagedSystemRandomNumber(new byte[16]);
        data.setManagedSystemGuid(new byte[16]);

        ResponseAction responseAction = new ResponseAction(data);
        machine.doExternalAction(responseAction);

        verify(machineObserver).notify(eq(responseAction));

        machine.doTransition(new DefaultAck());

        assertEquals(Rakp1Complete.class, machine.getCurrent().getClass());

        machine.doTransition(new Rakp2Ack(cs, getSequenceNumber(), (byte) 0, managedSystemSessionId, data));

        assertEquals(Rakp3Waiting.class, machine.getCurrent().getClass());

        cs.initializeAlgorithms(new byte[] {1, 2, 3});
    }

    private void doRakp3() {
        ResponseAction responseAction = new ResponseAction(new Rakp3ResponseData());
        machine.doExternalAction(responseAction);

        verify(machineObserver).notify(eq(responseAction));

        machine.doTransition(new DefaultAck());

        assertEquals(Rakp3Complete.class, machine.getCurrent().getClass());

        machine.doTransition(new StartSession(cs, sessionId));

        assertEquals(SessionValid.class, machine.getCurrent().getClass());
    }

    /**
     * The sequence number generated by the {@link StateMachine} to keep BMC
     * happy when re-sending a message. Differs from sequence number given with
     * the message to pair message and response. Sequence number is
     * auto-incremented. Should be used only for encoding messages.
     */
    public int getSequenceNumber() {
        sequenceNumber %= Short.MAX_VALUE;
        return sequenceNumber++;
    }
}
