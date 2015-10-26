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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.sel.ReserveSel;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionException;
import com.veraxsystems.vxipmi.transport.UdpMessenger;

/**
 * Tests for the {@link Connection} class.
 */
public class ConnectionTest extends TestCase {

	private UdpMessenger messenger;
	private Connection connection;
	private CipherSuite cs;
	private Properties properties;
	
	private static Logger logger = Logger.getLogger(ConnectionTest.class);
	
	private static final int PORT = 6666;
	
	private static final int CIPHER_SUITE = 2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		properties = new Properties();
		properties.load(new FileInputStream(
				"src/test/resources/test.properties"));
		messenger = new UdpMessenger(PORT);
		connection = new Connection(messenger, 0);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (connection != null)
			connection.disconnect();
		messenger.closeConnection();
	}

	/**
	 * Tests {@link Connection#getAvailableCipherSuites(int)},
	 * {@link Connection#getChannelAuthenticationCapabilities(int, CipherSuite, PrivilegeLevel)}
	 * and
	 * {@link Connection#startSession(int, CipherSuite, PrivilegeLevel, String, String, byte[])}
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testSessionChallenge() throws FileNotFoundException,
			IOException {
		logger.info("Testing Session Challenge");
		try {
			connection.connect(
					InetAddress.getByName(properties.getProperty("testIp")),
					30000);
		} catch (UnknownHostException e) {
			fail(e.getMessage());
		}
		try {
			cs = connection.getAvailableCipherSuites(0).get(CIPHER_SUITE);
			connection.getChannelAuthenticationCapabilities(0, cs,
					PrivilegeLevel.User);
			connection.startSession(0, cs, PrivilegeLevel.User,
					properties.getProperty("username"),
					properties.getProperty("password"), null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	/**
	 * Tests
	 * {@link Connection#sendIpmiCommand(com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder)}
	 * 
	 * @throws ConnectionException
	 * @throws InterruptedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testSendMessage() throws ConnectionException,
			InterruptedException, FileNotFoundException, IOException {
		logger.info("Testing Send Message");
		testSessionChallenge();
		try {
			connection.sendIpmiCommand(new ReserveSel(IpmiVersion.V20, cs,
					AuthenticationType.RMCPPlus));
			// Thread.sleep(100);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
		// TODO: some asserts
		Thread.sleep(100);
		connection.closeSession();
	}

	/**
	 * Tests if the connection is kept up by sending no-op commands properly.
	 * 
	 * @throws ConnectionException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testConnectionKeepup() throws ConnectionException,
			FileNotFoundException, IOException {
		logger.info("Testing Connection keepup");
		testSessionChallenge();
		try {
			Thread.sleep(00000);
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		try {
			connection.sendIpmiCommand(new ReserveSel(IpmiVersion.V20, cs,
					AuthenticationType.RMCPPlus));
			Thread.sleep(100);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
		// TODO: some asserts
		connection.closeSession();
	}

	/**
	 * Tests acquiring many parallel session and recognizing proper responses.
	 * 
	 * @throws UnknownHostException
	 * @throws InterruptedException
	 */
	@Test
	public void testParallelSessions() throws UnknownHostException,
			InterruptedException {

		logger.info("Testing Parallel Sessions");
		connection = null;
		for (int j = 0; j < 3; ++j) {
			for (int i = 0; i < 3; ++i) {
				SessionRunner sr = new SessionRunner(
						InetAddress.getByName(properties.getProperty("testIp")),
						messenger);
				sr.start();
			}
			Thread.sleep(3000);
		}
	}
}
