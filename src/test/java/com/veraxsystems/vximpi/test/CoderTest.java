/*
 * CoderTest.java 
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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.fru.BaseUnit;
import com.veraxsystems.vxipmi.coding.commands.fru.GetFruInventoryAreaInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.GetFruInventoryAreaInfoResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.ReadFruData;
import com.veraxsystems.vxipmi.coding.commands.fru.ReadFruDataResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.record.FruRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdr;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrRepositoryInfo;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReading;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepository;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepositoryResponseData;
import com.veraxsystems.vxipmi.coding.commands.sel.GetSelEntry;
import com.veraxsystems.vxipmi.coding.commands.sel.GetSelInfo;
import com.veraxsystems.vxipmi.coding.commands.sel.ReserveSel;
import com.veraxsystems.vxipmi.coding.commands.sel.ReserveSelResponseData;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionException;
import com.veraxsystems.vxipmi.connection.ConnectionListener;
import com.veraxsystems.vxipmi.connection.ConnectionManager;

import junit.framework.TestCase;

/**
 * Tests message encoders and decoders.
 */
public class CoderTest extends TestCase {

	private Properties properties;

	private ConnectionManager connectionManager;
	private Connection connection;
	private CipherSuite cs;
	private ConnectionListenerImpl listener;
	
	private static Logger logger = Logger.getLogger(CoderTest.class);
	
	private static final int PORT = 6666;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		properties = new Properties();
		properties.load(new FileInputStream("src/test/resources/test.properties"));

		connectionManager = new ConnectionManager(PORT);

		int index = connectionManager.createConnection(InetAddress
				.getByName(properties.getProperty("testIp")));
		List<CipherSuite> cipherSuites = connectionManager
				.getAvailableCipherSuites(index);

		assertTrue(cipherSuites.size() > 3);

		cs = cipherSuites.get(3);

		connectionManager.getChannelAuthenticationCapabilities(index, cs,
				PrivilegeLevel.User);

		connectionManager.startSession(index, cs, PrivilegeLevel.User,
				properties.getProperty("username"),
				properties.getProperty("password"), null);

		listener = new ConnectionListenerImpl();

		connectionManager.registerListener(index, listener);

		connection = connectionManager.getConnection(index);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		connection.closeSession();
		connection.disconnect();
		connectionManager.close();
	}

	/**
	 * Tests {@link GetChassisStatus}.
	 * @throws Exception
	 */
	@Test
	public void testGetChassisStatus() throws Exception {
		logger.info("Testing Get Chassis Status");
		sendCommand(new GetChassisStatus(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus));
	}

	/**
	 * Tests {@link GetFruInventoryAreaInfo}
	 * @throws Exception
	 */
	@Test
	public void testGetFruInventoryArea() throws Exception {
		logger.info("Testing Get Fru Inventory Area");
		sendCommand(new GetFruInventoryAreaInfo(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus, 0));
	}

	/**
	 * Tests {@link ReadFruData}
	 * @throws Exception
	 */
	@Test
	public void testReadFruData() throws Exception {
		logger.info("Testing Read Fru Data");
		sendCommand(new GetFruInventoryAreaInfo(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus, 0));

		GetFruInventoryAreaInfoResponseData responseData = (GetFruInventoryAreaInfoResponseData) listener.responseData;

		List<ReadFruDataResponseData> datas = new ArrayList<ReadFruDataResponseData>();

		int size = 100;
		for (int i = 0; i < responseData.getFruInventoryAreaSize(); i += size) {
			if (i + size > responseData.getFruInventoryAreaSize())
				size = responseData.getFruInventoryAreaSize() % 100;
			sendCommand(new ReadFruData(IpmiVersion.V20, cs,
					AuthenticationType.RMCPPlus, 0, BaseUnit.Bytes, i, size));
			datas.add((ReadFruDataResponseData) listener.responseData);
		}

		List<FruRecord> records = ReadFruData.decodeFruData(datas);

		assertTrue(records.size() > 0);
	}

	/**
	 * Tests {@link GetSdrRepositoryInfo}
	 * @throws Exception
	 */
	@Test
	public void testGetSdrRepositoryInfo() throws Exception {
		logger.info("Testing Get Sdr Repository Info");
		sendCommand(new GetSdrRepositoryInfo(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus));
	}

	/**
	 * Tests {@link ReserveSdrRepository}
	 * @throws Exception
	 */
	@Test
	public void testReserveSdrRepository() throws Exception {
		logger.info("Testing Reserve Sdr Repository");
		sendCommand(new ReserveSdrRepository(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus));
	}

	/**
	 * Tests {@link GetSdr}
	 * @throws Exception
	 */
	@Test
	public void testGetSdr() throws Exception {
		logger.info("Testing Get Sdr");
		sendCommand(new ReserveSdrRepository(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus));
		int id = ((ReserveSdrRepositoryResponseData) listener.responseData)
				.getReservationId();
		sendCommand(new GetSdr(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus, id, 0));
	}

	/**
	 * Tests {@link ReserveSdrRepository}
	 * @throws Exception
	 */
	@Test
	public void testGetSensorReading() throws Exception {
		logger.info("Testing Get Sensor Reading");
		sendCommand(new ReserveSdrRepository(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus));
		int id = ((ReserveSdrRepositoryResponseData) listener.responseData)
				.getReservationId();
		sendCommand(new GetSdr(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus, id, 1));
		sendCommand(new GetSensorReading(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus, 1));
	}

	/**
	 * Tests {@link GetSelInfo}
	 * @throws Exception
	 */
	@Test
	public void testGetSelInfo() throws Exception {
		logger.info("Testing Get Sel Info");
		sendCommand(new GetSelInfo(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus));
	}

	/**
	 * Tests {@link ReserveSel}
	 * @throws Exception
	 */
	@Test
	public void testReserveSel() throws Exception {
		logger.info("Testing Reserve Sel");
		sendCommand(new ReserveSel(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus));
	}

	/**
	 * Tests {@link GetSelEntry}
	 * @throws Exception
	 */
	@Test
	public void testGetSelEntry() throws Exception {
		logger.info("Testing Get Sel Entry");
		sendCommand(new ReserveSel(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus));
		int id = ((ReserveSelResponseData) listener.responseData)
				.getReservationId();
		sendCommand(new GetSelEntry(IpmiVersion.V20, cs,
				AuthenticationType.RMCPPlus, id, 1));
	}

	private void sendCommand(IpmiCommandCoder coder)
			throws ArithmeticException, ConnectionException,
			InterruptedException {
		connection.sendIpmiCommand(coder);

		int time = 0;

		int timeout = 5000;

		while (!listener.responseArrived && time < timeout) {
			Thread.sleep(1);
		}

		assertNotNull(listener.getResponseData());

		logger.info("Received answer for "
				+ coder.getClass().getSimpleName());
	}
	
	private class ConnectionListenerImpl implements ConnectionListener {

		private ResponseData responseData;
		private boolean responseArrived;

		public ResponseData getResponseData() {
			responseArrived = false;
			return responseData;
		}

		public ConnectionListenerImpl() {
			responseArrived = false;
			responseData = null;
		}

		@Override
		public void notify(ResponseData responseData, int handle, int tag,
				Exception exception) {
			this.responseData = responseData;
			if (exception != null) {
				System.out.println(exception.getMessage());
				exception.printStackTrace();
				this.responseData = null;
			}
			responseArrived = true;
			logger.info("Response arrived");
		}
	}
}
