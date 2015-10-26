/*
 * ConnectionManagerTest.java 
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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;
import junit.framework.TestCase;

import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.connection.Connection;
import com.veraxsystems.vxipmi.connection.ConnectionListener;
import com.veraxsystems.vxipmi.connection.ConnectionManager;
import com.veraxsystems.vxipmi.connection.queue.MessageQueue;
import com.veraxsystems.vxipmi.transport.UdpMessenger;

/**
 * Tests the {@link ConnectionManager} class.
 */
public class ConnectionManagerTest extends TestCase {

	private ConnectionManager manager;
	private Properties properties;
	
	private static Logger logger = Logger.getLogger(ConnectionManagerTest.class);
	
	private static final int PORT = 6666;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		properties = new Properties();
		properties.load(new FileInputStream(
				"src/test/resources/test.properties"));
		manager = new ConnectionManager(PORT);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		logger.info("-----------------------------");
		manager.close();
	}

	private static boolean lock;
	private static Integer finishedCorrectly;

	/**
	 * Tests sending multiple parallel sessionless messages and providng them
	 * proper tags.
	 */
	@Test
	public void testOverload() {
		logger.info("Testing parallel sessionless messages");
		lock = true;
		int runs = 200;
		finishedCorrectly = runs;
		for (int i = 0; i < runs; ++i) {
			OverloadRunner or = null;
			try {
				or = new OverloadRunner(i, InetAddress.getByName(properties
						.getProperty("testIp")));
			} catch (UnknownHostException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			or.start();
		}
		lock = false;

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		logger.info("Packets sent: "
				+ UdpMessenger.getSentPackets());
		assertEquals((Integer) 0, finishedCorrectly);
	}

	/**
	 * Tests message timeout. After a timeout message should is removed form
	 * {@link MessageQueue} and error message is returned.
	 */
	@Test
	public void testTimeout() {
		logger.info("Testing message timeout");
		logger.info("Expecting exception");
		lock = true;
		int runs = 1;
		finishedCorrectly = runs;
		OverloadRunner or = null;
		for (int i = 0; i < runs; ++i) {
			or = null;
			try {
				or = new OverloadRunner(i,
						InetAddress.getByName("192.168.127.200"));
			} catch (UnknownHostException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			or.start();
		}
		lock = false;

		try {
			Thread.sleep(40000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals((Integer) runs, finishedCorrectly);
		assertEquals(false, or.isCorrect());
	}

	/**
	 * Tests retrying sending a message after failure in a first attempt.
	 */
	@Test
	public void testRetry() {
		logger.info("Testing retry");
		logger.info("Expecting exception");
		lock = true;
		int runs = 1;
		finishedCorrectly = runs;
		OverloadRunner or = null;
		for (int i = 0; i < runs; ++i) {
			or = null;
			try {
				or = new OverloadRunner(i,
						InetAddress.getByName("192.168.127.200"));
			} catch (UnknownHostException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			or.start();
		}
		lock = false;

		try {
			Thread.sleep(40000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals((Integer) runs, finishedCorrectly);
		assertEquals(false, or.isCorrect());
	}

	private static Integer receivedMessages;
	private static int sent;

	/**
	 * Tests sending in-session message.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testSessionMessages() throws FileNotFoundException, IOException {
		logger.info("Testing Session Messages");
		// lock = true;
		int messages = 100;
		sent = 0;
		receivedMessages = 0;
		finishedCorrectly = messages;
		OverloadRunner or = null;
		try {
			or = new OverloadRunner(0, InetAddress.getByName(properties
					.getProperty("testIp")));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		// or.start();
		// lock = false;

		or.sendSessionMessages(messages);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals((Integer) messages, receivedMessages);
	}

	private class OverloadRunner extends Thread implements ConnectionListener {

		private int index;
		private InetAddress address;
		private boolean correct = true;

		public boolean isCorrect() {
			return correct;
		}

		public OverloadRunner(int index, InetAddress address) {
			this.index = index;
			this.address = address;
		}

		@Override
		public void run() {
			super.run();
			int index = -1;
			try {
				index = manager.createConnection(address);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			assertEquals(true, index != -1);

			manager.registerListener(index, this);

			while (lock) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Date d = new Date();
			try {
				manager.getAvailableCipherSuites(index);
			} catch (Exception e) {
				correct = false;
				e.printStackTrace();
				fail(e.getMessage());
			}
			synchronized (finishedCorrectly) {
				finishedCorrectly--;
			}
			logger.info("Process " + this.index
					+ " - time spent sending: "
					+ (new Date().getTime() - d.getTime()));
		}

		public void sendSessionMessages(int count)
				throws FileNotFoundException, IOException {
			int index = -1;
			index = manager.createConnection(address);
			assertEquals(true, index != -1);

			manager.registerListener(index, this);

			List<CipherSuite> suites = null;
			try {
				suites = manager.getAvailableCipherSuites(index);
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}

			CipherSuite cs = suites.get(2);

			try {
				manager.getChannelAuthenticationCapabilities(index, cs,
						PrivilegeLevel.User);
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}

			try {
				manager.startSession(index, cs, PrivilegeLevel.User,
						properties.getProperty("username"),
						properties.getProperty("password"), null);
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}

			Connection c = manager.getConnection(index);

			for (int i = 0; i < count; ++i) {
				try {
					int result = c.sendIpmiCommand(new GetChassisStatus(
							IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));
					if (result < 0) {
						Thread.sleep(10);
						--i;
					} else {
						++sent;
						logger.info("Sent " + sent + " messages");
					}
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		}

		@Override
		public void notify(ResponseData responseData, int handle, int tag,
				Exception exception) {
			if (exception != null) {
				System.out.println(exception.getMessage());
			} else if (responseData == null) {
				logger.info("Message timed out");
			} else {
				synchronized (receivedMessages) {
					++receivedMessages;
				}
				logger.info("Received answer " + tag + " : "
						+ responseData.getClass().getSimpleName());
			}
		}
	}
}
