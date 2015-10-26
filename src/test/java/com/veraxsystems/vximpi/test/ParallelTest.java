package com.veraxsystems.vximpi.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
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
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

public class ParallelTest {

	private static IpmiConnector connector;

	private static Properties properties;
	
	private static final int CONNECTION_COUNT = 2;
	
	private List<ConnectionHandle> connections;

	private boolean succeeded;
	
	public static int cnter = 0;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		connector = new IpmiConnector(6666);
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
		cnter = 0;
		connections = new ArrayList<ConnectionHandle>();
		
		for(int i = 0; i < CONNECTION_COUNT; ++i) {
			if(i % 2 == 0) {
				ConnectionHandle connection = connector.createConnection(InetAddress
						.getByName(properties.getProperty("testIp")));
				connections.add(connection);
			} else {
				ConnectionHandle connection = connector.createConnection(InetAddress
						.getByName(properties.getProperty("testIp2")));
				connections.add(connection);
			}
		}
		succeeded = true;
	}

	@After
	public void tearDown() throws Exception {
		for(ConnectionHandle handle : connections) {
			connector.closeConnection(handle);
		}
	}

	/**
	 * Tests {@link IpmiConnector#getAvailableCipherSuites(ConnectionHandle)}
	 * 
	 * @throws Exception
	 */
	public void testGetAvailableCipherSuites() throws Exception {
		System.out
				.println("[JUnit] [SyncApiTest] Testing GetAvailableCipherSuites");
		for(ConnectionHandle handle : connections) {
			connector.getAvailableCipherSuites(handle);
		}
	}

	/**
	 * Tests
	 * {@link IpmiConnector#getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)}
	 * 
	 * @throws Exception
	 */
	public void testGetChannelAuthenticationCapabilities() throws Exception {
		System.out
				.println("[JUnit] [SyncApiTest] Testing GetAvailableCipherSuites");
		System.out
				.println("[JUnit] [SyncApiTest] Testing GetChannelAuthenticationCapabilities");
		for(ConnectionHandle handle : connections) {
			CipherSuite cs = connector.getAvailableCipherSuites(handle).get(3);
			connector.getChannelAuthenticationCapabilities(handle, cs,
					PrivilegeLevel.User);
		}
	}

	/**
	 * Tests
	 * {@link IpmiConnector#openSession(ConnectionHandle, String, String, byte[])}
	 * 
	 * @throws Exception
	 */
	public void testOpenSession() throws Exception {
		System.out.println("[JUnit] [SyncApiTest] Testing OpenSession");
		testGetChannelAuthenticationCapabilities();
		for(int i = 0; i < CONNECTION_COUNT; ++i) {
			if(i % 2 == 0) {
				connector.openSession(connections.get(i), properties.getProperty("username"),
						properties.getProperty("password"), null);
			} else {
				connector.openSession(connections.get(i), properties.getProperty("username2"),
						properties.getProperty("password2"), null);
			}
		}
	}

	/**
	 * Tests
	 * {@link IpmiConnector#sendMessage(ConnectionHandle, com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSendMessage() throws Exception {
		System.out.println("[JUnit] [SyncApiTest] Testing sending message");

		testOpenSession();

		List<Thread> threads = new ArrayList<Thread>();
		
		for(ConnectionHandle handle : connections) {
			threads.add(new Thread(new ConnectionRunner(handle)));
		}

		for(Thread th : threads) {
			th.start();
		}
		
		for(Thread th : threads) {
			th.join();
		}

		if (!succeeded) {
			fail();
		}
	}
	
	private class ConnectionRunner implements Runnable
	{

		final int cnt = 30;
		private ConnectionHandle connection;
		private int id;
		private int msgCnt;
		
		public ConnectionRunner(ConnectionHandle handle) {
			connection = handle;
			id = cnter++;
			msgCnt = 0;
		}
		
		
		@Override
		public void run() {
			Logger logger = Logger.getLogger(getClass());
			try {
				for (int i = 0; i < cnt; ++i) {
					GetChassisStatusResponseData responseData = (GetChassisStatusResponseData) connector
							.sendMessage(connection,
									new GetChassisStatus(IpmiVersion.V20,
											connection.getCipherSuite(),
											AuthenticationType.RMCPPlus));
					logger.info("[" + id + "] Received message " + msgCnt++ + " from " + id + ": " + responseData.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
				succeeded = false;
			}
		}
		
	}
}
