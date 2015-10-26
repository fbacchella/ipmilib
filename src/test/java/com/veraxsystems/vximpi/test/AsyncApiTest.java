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

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.async.IpmiAsyncConnector;
import com.veraxsystems.vxipmi.api.async.IpmiListener;
import com.veraxsystems.vxipmi.api.async.messages.IpmiError;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponse;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

/**
 * Tests for the Asynchronous API
 */
public class AsyncApiTest implements IpmiListener {

	private static IpmiAsyncConnector connector;
	private static Properties properties;
	private ConnectionHandle handle;
	private IpmiResponse response;
	
	private static Logger logger = Logger.getLogger(AsyncApiTest.class);
	
	private static final int PORT = 6666;

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
		response = null;

		connector.registerListener(this);
		handle = connector.createConnection(InetAddress.getByName(properties
				.getProperty("testIp")));

	}

	@After
	public void tearDown() throws Exception {
		connector.closeSession(handle);
	}

	/**
	 * Tests
	 * {@link IpmiAsyncConnector#getAvailableCipherSuites(ConnectionHandle)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAvailableCipherSuites() throws Exception {
		logger.info("Testing GetAvailableCipherSuites");
		connector.getAvailableCipherSuites(handle);
	}

	/**
	 * Tests
	 * {@link IpmiAsyncConnector#getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetChannelAuthenticationCapabilities() throws Exception {
		logger.info("Testing GetAvailableCipherSuites");
		logger.info("Testing GetChannelAuthenticationCapabilities");

		CipherSuite cs = connector.getAvailableCipherSuites(handle).get(3);
		connector.getChannelAuthenticationCapabilities(handle, cs,
				PrivilegeLevel.User);

	}

	/**
	 * Tests
	 * {@link IpmiAsyncConnector#openSession(ConnectionHandle, String, String, byte[])}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOpenSession() throws Exception {
		logger.info("Testing OpenSession");
		testGetChannelAuthenticationCapabilities();
		connector.openSession(handle, properties.getProperty("username"),
				properties.getProperty("password"), null);
	}

	/**
	 * Tests
	 * {@link IpmiAsyncConnector#sendMessage(ConnectionHandle, com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSendMessage() throws Exception {
		logger.info("Testing sending message");

		testOpenSession();

		connector.sendMessage(handle, new GetChassisStatus(IpmiVersion.V20,
				handle.getCipherSuite(), AuthenticationType.RMCPPlus));

		while (response == null)
			Thread.sleep(1);
		if (response instanceof IpmiError) {
			fail(((IpmiError) response).getException().getMessage());
		}
	}

	@Override
	public void notify(IpmiResponse response) {
		this.response = response;
	}

}
