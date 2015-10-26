/*
 * SyncApiTest.java 
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
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdr;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepository;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepositoryResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.SensorRecord;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

/**
 * Tests synchronous API.
 * 
 * @see IpmiConnector
 */
public class SyncApiTest {

	private static IpmiConnector connector;

	private static Properties properties;

	private ConnectionHandle connection;
	
	private static Logger logger = Logger.getLogger(SyncApiTest.class);
	
	private static final int STANDARD_CIPHER_SUITE = 3;
	
	private static final int PORT = 6666;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		connector = new IpmiConnector(PORT);
		properties = new Properties();
		properties.load(new FileInputStream(
				"src/test/resources/test.properties"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		connector.tearDown();
	}

	@Before
	public void setUp() throws Exception {
		connection = connector.createConnection(InetAddress
				.getByName(properties.getProperty("testIp")));
	}

	@After
	public void tearDown() throws Exception {
		connector.closeSession(connection);
	}

	/**
	 * Tests {@link IpmiConnector#getAvailableCipherSuites(ConnectionHandle)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAvailableCipherSuites() throws Exception {
		logger.info("Testing GetAvailableCipherSuites");
		connector.getAvailableCipherSuites(connection);
	}

	/**
	 * Tests
	 * {@link IpmiConnector#getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetChannelAuthenticationCapabilities() throws Exception {
		logger.info("Testing GetAvailableCipherSuites");
		logger.info("Testing GetChannelAuthenticationCapabilities");

		CipherSuite cs = connector.getAvailableCipherSuites(connection).get(STANDARD_CIPHER_SUITE);
		connector.getChannelAuthenticationCapabilities(connection, cs,
				PrivilegeLevel.User);

	}

	/**
	 * Tests
	 * {@link IpmiConnector#openSession(ConnectionHandle, String, String, byte[])}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOpenSession() throws Exception {
		logger.info("Testing OpenSession");
		testGetChannelAuthenticationCapabilities();
		connector.openSession(connection, properties.getProperty("username"),
				properties.getProperty("password"), null);
	}

	/**
	 * Tests
	 * {@link IpmiConnector#sendMessage(ConnectionHandle, com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSendMessage() throws Exception {
		logger.info("Testing sending message");

		testOpenSession();

		connector.sendMessage(connection, new GetChassisStatus(IpmiVersion.V20,
				connection.getCipherSuite(), AuthenticationType.RMCPPlus));
	}
	
	@Test
	public void testSdrPartialRead() throws Exception {
		logger.info("Testing SDR partial read");

		testOpenSession();
		int reservationId = ((ReserveSdrRepositoryResponseData) connector
				.sendMessage(connection, new ReserveSdrRepository(
						IpmiVersion.V20, connection.getCipherSuite(),
						AuthenticationType.RMCPPlus)))
				.getReservationId();
		GetSdrResponseData data1 = (GetSdrResponseData) connector.sendMessage(connection, new GetSdr(IpmiVersion.V20, connection.getCipherSuite(), AuthenticationType.RMCPPlus, reservationId, 0));
		
		byte[] sensorData = data1.getSensorRecordData();
		
		GetSdrResponseData data2 = (GetSdrResponseData) connector.sendMessage(connection, new GetSdr(IpmiVersion.V20, connection.getCipherSuite(), AuthenticationType.RMCPPlus, reservationId, 0, 0, sensorData.length / 2));
		GetSdrResponseData data3 = (GetSdrResponseData) connector.sendMessage(connection, new GetSdr(IpmiVersion.V20, connection.getCipherSuite(), AuthenticationType.RMCPPlus, reservationId, 0, sensorData.length / 2, sensorData.length - sensorData.length / 2));
		
		byte[] merged = new byte[sensorData.length];
		
		System.arraycopy(data2.getSensorRecordData(), 0, merged, 0, data2.getSensorRecordData().length);
		System.arraycopy(data3.getSensorRecordData(), 0, merged, data2.getSensorRecordData().length, data3.getSensorRecordData().length);
		
		assertEquals(sensorData.length, merged.length);
		
		for(int i = 0; i < sensorData.length; ++i) {
			assertEquals(sensorData[i], merged[i]);
		}
		
		SensorRecord record = SensorRecord.populateSensorRecord(sensorData);
		
		assertNotNull(record);
	}
}
