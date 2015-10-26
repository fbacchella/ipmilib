/*
 * DecoderRunner.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatusResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.BaseUnit;
import com.veraxsystems.vxipmi.coding.commands.fru.GetFruInventoryAreaInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.GetFruInventoryAreaInfoResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.ReadFruData;
import com.veraxsystems.vxipmi.coding.commands.fru.ReadFruDataResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.record.BoardInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.record.ChassisInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.record.FruRecord;
import com.veraxsystems.vxipmi.coding.commands.fru.record.ProductInfo;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdr;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrRepositoryInfo;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrRepositoryInfoResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReading;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReadingResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepository;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepositoryResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.CompactSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.FruDeviceLocatorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.ReadingType;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.FullSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.RateUnit;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.SensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sel.GetSelEntry;
import com.veraxsystems.vxipmi.coding.commands.sel.GetSelEntryResponseData;
import com.veraxsystems.vxipmi.coding.commands.sel.GetSelInfo;
import com.veraxsystems.vxipmi.coding.commands.sel.GetSelInfoResponseData;
import com.veraxsystems.vxipmi.coding.commands.sel.ReserveSel;
import com.veraxsystems.vxipmi.coding.commands.sel.ReserveSelResponseData;
import com.veraxsystems.vxipmi.coding.commands.sel.SelRecord;
import com.veraxsystems.vxipmi.coding.commands.session.CloseSession;
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
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.decoder.PlainCommandv20Decoder;
import com.veraxsystems.vxipmi.coding.protocol.decoder.Protocolv15Decoder;
import com.veraxsystems.vxipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv15Encoder;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.SecurityConstants;
import com.veraxsystems.vxipmi.common.TypeConverter;

import javax.crypto.NoSuchPaddingException;

import org.apache.log4j.Logger;

/**
 * Test driver for Encoder/Decoder
 */
public class DecoderRunner extends Thread {

	private DatagramSocket socket;

	private static int managedSeqNum;
	private static boolean lock;

	private static Rakp1 r1;
	private static Rakp1ResponseData r1rd;
	private static CipherSuite cs = new CipherSuite((byte) 0,
			SecurityConstants.AA_RAKP_HMAC_SHA1, (byte) 0, (byte) 0);
	
	private static Logger logger = Logger.getLogger(DecoderRunner.class);

	private static int cssrcv = 16;

	private static int reservation;

	private static int nextRecId = 0;

	private static byte[] cssrec;

	private static List<ReadFruDataResponseData> rd;
	
	private static int fruId = 0;
	
	private static int fruSize = 528;
	
	public static void main(String[] args) throws IOException,
			InterruptedException, NoSuchAlgorithmException,
			InvalidKeyException, IllegalArgumentException {

		logger.info(DateFormat.getInstance().format(
				new Date(new Date().getTime())));
		
		//StateMachineTest stateMachineTest = new StateMachineTest();
		
		//stateMachineTest.testSessionUpkeep();

		lock = true;

		DecoderRunner dr = new DecoderRunner();

		dr.socket = new DatagramSocket(6666);

		dr.start();

		Properties properties = new Properties();
		properties.load(new FileInputStream("src/test/resources/test.properties"));
		
		Thread.sleep(100);

		InetAddress ad = InetAddress.getByName((String)properties.get("testIp"));
		// InetAddress ad = InetAddress.getByName("192.168.100.190");
		// byte[] outmsg = RmcpEncoder.encode(new RmcpPingMessage((byte) 1));

		byte index = 0;

		while (cssrcv >= 16) {

			Thread.sleep(300);

			lock = true;

			byte[] outmsg = Encoder.encode(new Protocolv20Encoder(),
					new GetChannelCipherSuites(TypeConverter.intToByte(0xE),
							index), 0, 0);

			++index;
			DatagramPacket packet = new DatagramPacket(outmsg, outmsg.length,
					ad, 0x26F);

			dr.socket.send(packet);

			while (lock) {
				Thread.sleep(1);
			}
		}

		List<CipherSuite> csl = CipherSuite.getCipherSuites(cssrec);

		for (CipherSuite c : csl) {
			try {
				logger.info(c.getId() + ": "
						+ c.getAuthenticationAlgorithm().getCode() + " "
						+ c.getIntegrityAlgorithm().getCode() + " "
						+ c.getConfidentialityAlgorithm().getCode());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		cs = csl.get(2);

		Thread.sleep(300);

		byte[] outmsg = Encoder
				.encode(new Protocolv15Encoder(),
						new GetChannelAuthenticationCapabilities(IpmiVersion.V15,
								IpmiVersion.V20, cs, PrivilegeLevel.User,
								TypeConverter.intToByte(14)), 0, 0);

		DatagramPacket packet = new DatagramPacket(outmsg, outmsg.length, ad,
				0x26F);

		dr.socket.send(packet);

		Thread.sleep(150);

		outmsg = Encoder.encode(new Protocolv20Encoder(), new OpenSession(44,
				PrivilegeLevel.MaximumAvailable, cs), 0, 0);

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);

		Thread.sleep(300);

		while (lock) {
			Thread.sleep(1);
		}

		lock = true;

				
		r1 = new Rakp1(managedSeqNum, PrivilegeLevel.User, (String)properties.get("username"), (String)properties.get("password"),
				null, cs);

		outmsg = Encoder.encode(new Protocolv20Encoder(), r1, 1, 0);

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);

		Thread.sleep(150);
		
		while (lock) {
			Thread.sleep(1);
		}

		try {
			cs.initializeAlgorithms(r1.calculateSik(r1rd));
		} catch (NoSuchPaddingException e) {
			logger.error(e.getMessage(), e);
		}

		outmsg = Encoder.encode(new Protocolv20Encoder(), new Rakp3((byte) 0,
				managedSeqNum, cs, r1, r1rd), 1, 0);

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);

		Thread.sleep(150);

		outmsg = Encoder.encode(new Protocolv20Encoder(), new GetChassisStatus(
				IpmiVersion.V20, cs, AuthenticationType.RMCPPlus), 1, r1
				.getManagedSystemSessionId());

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);

		Thread.sleep(300);

		outmsg = Encoder.encode(new Protocolv20Encoder(),
				new GetSdrRepositoryInfo(IpmiVersion.V20, cs,
						AuthenticationType.RMCPPlus), 2, r1
						.getManagedSystemSessionId());

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);
		Thread.sleep(300);

		outmsg = Encoder.encode(new Protocolv20Encoder(),
				new ReserveSdrRepository(IpmiVersion.V20, cs,
						AuthenticationType.RMCPPlus), 3, r1
						.getManagedSystemSessionId());

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);

		int seq = 4;

		lock = true;

		while (lock) {
			Thread.sleep(1);
		}

		while (nextRecId < 65535) {

			Thread.sleep(200);

			logger.info(">>Sending request for record " + nextRecId);

			outmsg = Encoder.encode(new Protocolv20Encoder(), new GetSdr(
					IpmiVersion.V20, cs, AuthenticationType.RMCPPlus,
					reservation, nextRecId), seq++, r1
					.getManagedSystemSessionId());

			packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

			dr.socket.send(packet);
			lock = true;

			while (lock) {
				Thread.sleep(1);
			}

			if (nextRecId > 0) {
				logger.info(">>Sending request for reading " + nextRecId);

				outmsg = Encoder.encode(new Protocolv20Encoder(),
						new GetSensorReading(IpmiVersion.V20, cs,
								AuthenticationType.RMCPPlus, nextRecId), seq++,
						r1.getManagedSystemSessionId());

				packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

				dr.socket.send(packet);

				lock = true;

				while (lock && nextRecId < 65535) {
					Thread.sleep(1);
				}
			}

		}

		 nextRecId = 0;

		Thread.sleep(300);

		logger.info(">>Sending GetSelInfo");

		outmsg = Encoder.encode(new Protocolv20Encoder(), new GetSelInfo(
				IpmiVersion.V20, cs, AuthenticationType.RMCPPlus), seq++, r1
				.getManagedSystemSessionId());

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);

		Thread.sleep(300);

		logger.info(">>Sending Reserve SEL");

		outmsg = Encoder.encode(new Protocolv20Encoder(), new ReserveSel(
				IpmiVersion.V20, cs, AuthenticationType.RMCPPlus), seq++, r1
				.getManagedSystemSessionId());

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);

		lock = true;

		while (lock) {
			Thread.sleep(1);
		}

		while (nextRecId < 65535) {

			Thread.sleep(200);

			logger.info(">>Sending request for SEL record " + nextRecId);

			outmsg = Encoder.encode(new Protocolv20Encoder(), new GetSelEntry(
					IpmiVersion.V20, cs, AuthenticationType.RMCPPlus,
					reservation, nextRecId), seq++, r1
					.getManagedSystemSessionId());

			packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

			dr.socket.send(packet);
			lock = true;

			while (lock) {
				Thread.sleep(1);
			}
		}

		Thread.sleep(300);

		logger.info(">>Sending GetFruInventoryAreaInfo");
		
		outmsg = Encoder.encode(new Protocolv20Encoder(),
				new GetFruInventoryAreaInfo(IpmiVersion.V20, cs,
						AuthenticationType.RMCPPlus, fruId), seq++, r1
						.getManagedSystemSessionId());

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);
		
		for(int i = 0; i < fruSize; i += 100) {


			Thread.sleep(300);

			logger.info(">>Sending ReadFruData");
			
			int cnt = 100;
			if(i + cnt > fruSize) {
				cnt = fruSize % 100;
			}

			outmsg = Encoder.encode(new Protocolv20Encoder(),
					new ReadFruData(IpmiVersion.V20, cs,
							AuthenticationType.RMCPPlus, fruId, BaseUnit.Bytes, i, cnt), seq++, r1
							.getManagedSystemSessionId());

			packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

			dr.socket.send(packet);

		}

		Thread.sleep(300);

		outmsg = Encoder.encode(
				new Protocolv20Encoder(),
				new CloseSession(IpmiVersion.V20, cs,
						AuthenticationType.RMCPPlus, r1
								.getManagedSystemSessionId()), seq++, r1
						.getManagedSystemSessionId());

		packet = new DatagramPacket(outmsg, outmsg.length, ad, 0x26F);

		dr.socket.send(packet);

		Thread.sleep(1000);

		dr.socket.close();

	}

	@Override
	public void run() {

		super.run();

		cssrec = new byte[0];

		byte[] buffer = null;

		while (cssrcv >= 16) {
			DatagramPacket resp = new DatagramPacket(new byte[256], 256);

			try {
				socket.receive(resp);
				buffer = new byte[resp.getLength()];
				System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			GetChannelCipherSuitesResponseData data = null;

			try {
				data = (GetChannelCipherSuitesResponseData) Decoder.decode(
						buffer, new Protocolv20Decoder(CipherSuite.getEmpty()),
						new GetChannelCipherSuites());
			} catch (IllegalArgumentException e1) {
				logger.error(e1.getMessage(), e1);
			} catch (IPMIException e1) {
				logger.error(e1.getMessage(), e1);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage(), e);
			} catch (InvalidKeyException e) {
				logger.error(e.getMessage(), e);
			}

			if (data.getCipherSuiteData() != null) {
				cssrcv = data.getCipherSuiteData().length;

				logger.info(data.getCipherSuiteData().length);

				byte[] temp = new byte[cssrec.length + cssrcv];

				System.arraycopy(cssrec, 0, temp, 0, cssrec.length);
				System.arraycopy(data.getCipherSuiteData(), 0, temp,
						cssrec.length, cssrcv);
				cssrec = temp;

			} else {
				cssrcv = 0;
				logger.info(0);
			}

			lock = false;
		}

		DatagramPacket resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		GetChannelAuthenticationCapabilitiesResponseData data = null;

		try {
			data = (GetChannelAuthenticationCapabilitiesResponseData) Decoder
					.decode(buffer, new Protocolv15Decoder(),
							new GetChannelAuthenticationCapabilities(
									IpmiVersion.V15, IpmiVersion.V20, cs));
		} catch (IllegalArgumentException e1) {
			logger.error(e1.getMessage(), e1);
		} catch (IPMIException e1) {
			logger.error(e1.getMessage(), e1);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info("---------------------------------------------");

		logger.info(data.getChannelNumber());
		logger.info(data.isIpmiv20Support());
		logger.info(data.getAuthenticationTypes().toString());
		logger.info(data.isKgEnabled());
		logger.info(data.isPerMessageAuthenticationEnabled());
		logger.info(data.isUserLevelAuthenticationEnabled());
		logger.info(data.isNonNullUsernamesEnabled());
		logger.info(data.isNullUsernamesEnabled());
		logger.info(data.isAnonymusLoginEnabled());
		logger.info(data.getOemId());
		logger.info(data.getOemData());

		logger.info("##############################################");

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
			logger.info(">>>> " + resp.getLength());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		OpenSessionResponseData data2 = null;

		try {
			data2 = (OpenSessionResponseData) Decoder.decode(buffer,
					new PlainCommandv20Decoder(CipherSuite.getEmpty()),
					new OpenSession(CipherSuite.getEmpty()));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info(data2.getMessageTag());
		logger.info(data2.getStatusCode());
		logger.info(data2.getPrivilegeLevel());
		logger.info(data2.getRemoteConsoleSessionId());
		logger.info(data2.getManagedSystemSessionId());
		logger.info(data2.getAuthenticationAlgorithm());
		logger.info(data2.getConfidentialityAlgorithm());
		logger.info(data2.getIntegrityAlgorithm());

		managedSeqNum = data2.getManagedSystemSessionId();
		lock = false;

		logger.info("---------------------------------------------");

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		Rakp1ResponseData data3 = null;

		try {
			data3 = (Rakp1ResponseData) Decoder.decode(buffer,
					new PlainCommandv20Decoder(CipherSuite.getEmpty()), r1);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		r1rd = data3;

		logger.info(data3.getMessageTag());
		logger.info(data3.getStatusCode());
		logger.info(data3.getRemoteConsoleSessionId());
		logger.info(data3.getManagedSystemGuid());

		logger.info("---------------------------------------------");
		lock = false;

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		Rakp3ResponseData data4 = null;

		try {
			data4 = (Rakp3ResponseData) Decoder.decode(buffer,
					new PlainCommandv20Decoder(CipherSuite.getEmpty()),
					new Rakp3(cs, r1, r1rd));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info(data4.getMessageTag());
		logger.info(data4.getStatusCode());
		logger.info(data4.getConsoleSessionId());

		logger.info("---------------------------------------------");

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		GetChassisStatusResponseData data5 = null;

		try {
			data5 = (GetChassisStatusResponseData) Decoder.decode(buffer,
					new Protocolv20Decoder(cs), new GetChassisStatus(
							IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info(data5.getPowerRestorePolicy());
		logger.info(data5.isPowerControlFault());
		logger.info(data5.isPowerFault());
		logger.info(data5.isInterlock());
		logger.info(data5.isPowerOverload());
		logger.info(data5.isPowerOn());

		logger.info("________");

		logger.info(data5.wasIpmiPowerOn());
		logger.info(data5.wasPowerFault());
		logger.info(data5.wasInterlock());
		logger.info(data5.wasPowerOverload());

		logger.info("________");

		logger.info(data5.isChassisIdentifyCommandSupported());
		if (data5.isChassisIdentifyCommandSupported()) {
			logger.info(data5.getChassisIdentifyState());
		}
		logger.info(data5.coolingFaultDetected());
		logger.info(data5.driveFaultDetected());
		logger.info(data5.isFrontPanelLockoutActive());
		logger.info(data5.isChassisIntrusionActive());

		logger.info("________");

		logger.info(data5.isFrontPanelButtonCapabilitiesSet());

		if (data5.isFrontPanelButtonCapabilitiesSet()) {
			try {
				logger.info(data5.isStandbyButtonDisableAllowed());
				logger.info(data5
						.isDiagnosticInterruptButtonDisableAllowed());
				logger.info(data5.isResetButtonDisableAllowed());
				logger.info(data5.isPowerOffButtonDisableAllowed());
				logger.info(data5.isStandbyButtonDisabled());
				logger.info(data5.isDiagnosticInterruptButtonDisabled());
				logger.info(data5.isResetButtonDisabled());
				logger.info(data5.isPowerOffButtonDisabled());
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			}

		}

		logger.info("---------------------------------------------");

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		GetSdrRepositoryInfoResponseData data6 = null;

		try {
			data6 = (GetSdrRepositoryInfoResponseData) Decoder.decode(buffer,
					new Protocolv20Decoder(cs), new GetSdrRepositoryInfo(
							IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info(data6.getSdrVersion());
		logger.info(data6.getRecordCount());
		logger.info(data6.getAddTimestamp());
		logger.info(data6.getDelTimestamp());
		logger.info(data6.isReserveSupported());

		logger.info("---------------------------------------------");

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		ReserveSdrRepositoryResponseData data7 = null;

		try {
			data7 = (ReserveSdrRepositoryResponseData) Decoder.decode(buffer,
					new Protocolv20Decoder(cs), new ReserveSdrRepository(
							IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info(data7.getReservationId());

		reservation = data7.getReservationId();

		logger.info("<<Received ReserveSdrRepo response");

		lock = false;

		logger.info("---------------------------------------------");

		while (nextRecId < 65535) {

			resp = new DatagramPacket(new byte[256], 256);

			try {
				socket.receive(resp);
				buffer = new byte[resp.getLength()];
				System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			GetSdrResponseData data8 = null;

			try {
				data8 = (GetSdrResponseData) Decoder.decode(buffer,
						new Protocolv20Decoder(cs), new GetSdr(IpmiVersion.V20,
								cs, AuthenticationType.RMCPPlus, 0, 0));
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage(), e);
			} catch (IPMIException e) {
				logger.info(e.getMessage());
				logger.error(e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage(), e);
			} catch (InvalidKeyException e) {
				logger.error(e.getMessage(), e);
			}

			// logger.info(data8.getNextRecordId());
			SensorRecord record = SensorRecord.populateSensorRecord(data8.getSensorRecordData());
			logger.info(record.toString());

			// nextRecId = record.getId();

			if (record instanceof FullSensorRecord) {
				nextRecId = TypeConverter.byteToInt(((FullSensorRecord) record).getSensorNumber());
			} else if (record instanceof CompactSensorRecord) {
				nextRecId = TypeConverter
						.byteToInt(((CompactSensorRecord) record).getSensorNumber());
			} else {
				nextRecId = -1;
			}

			logger.info("<<Reading Id " + nextRecId);

			if (record instanceof FullSensorRecord) {
				FullSensorRecord rec = (FullSensorRecord) record;
				logger.info("*" + rec.getName());
				logger.info("Reading type: " + rec.getEventReadingType());
				logger.info("Lower critical threshold: "
						+ rec.getLowerCriticalThreshold());
				logger.info("Upper critical threshold: "
						+ rec.getUpperCriticalThreshold());
				logger.info("Tolerance: +/- "
						+ rec.getTolerance()
						+ " "
						+ rec.getSensorBaseUnit().toString()
						+ (rec.getRateUnit() != RateUnit.None ? " per "
								+ rec.getRateUnit() : ""));
				logger.info("Resolution: "
						+ rec.getSensorResolution()
						+ " "
						+ rec.getSensorBaseUnit().toString()
						+ (rec.getRateUnit() != RateUnit.None ? " per "
								+ rec.getRateUnit() : ""));
			}
			if (record instanceof CompactSensorRecord) {
				CompactSensorRecord rec = (CompactSensorRecord) record;
				logger.info("*" + rec.getName());
				logger.info("Reading type: " + rec.getEventReadingType());
				logger.info("Sensor type: " + rec.getSensorType());
			}
			if (record instanceof FruDeviceLocatorRecord) {
				FruDeviceLocatorRecord rec = (FruDeviceLocatorRecord) record;
				logger.info(rec.getName());
				logger.info(rec.getDeviceType());
				logger.info("FRU entity ID: " + rec.getFruEntityId());
				logger.info("FRU access address: " + rec.getDeviceAccessAddress());
				logger.info("FRU device ID: " + rec.getDeviceId());
				logger.info("FRU logical: " + rec.isLogical());
			}

			lock = false;
			if (nextRecId > 0) {
				resp = new DatagramPacket(new byte[256], 256);

				try {
					socket.receive(resp);
					buffer = new byte[resp.getLength()];
					System.arraycopy(resp.getData(), 0, buffer, 0,
							buffer.length);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				GetSensorReadingResponseData data9 = null;

				try {
					data9 = (GetSensorReadingResponseData) Decoder.decode(
							buffer, new Protocolv20Decoder(cs),
							new GetSensorReading(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus, 0));

					if (record instanceof FullSensorRecord) {
						FullSensorRecord rec = (FullSensorRecord) record;
						logger.info(data9.getSensorReading(rec)
								+ " "
								+ rec.getSensorBaseUnit().toString()
								+ (rec.getRateUnit() != RateUnit.None ? " per "
										+ rec.getRateUnit() : ""));
					}
					if (record instanceof CompactSensorRecord) {
						CompactSensorRecord rec = (CompactSensorRecord) record;
						// logger.info(rec.getEventReadingType());
						List<ReadingType> events = data9.getStatesAsserted(
								rec.getSensorType(), rec.getEventReadingType());
						String s = "";
						for (int i = 0; i < events.size(); ++i) {
							s += events.get(i) + ", ";
						}
						logger.info(s);

					}
				} catch (IllegalArgumentException e) {
					logger.error(e.getMessage(), e);
				} catch (IPMIException e) {
					logger.info(e.getMessage());
					logger.error(e.getMessage(), e);
				} catch (NoSuchAlgorithmException e) {
					logger.error(e.getMessage(), e);
				} catch (InvalidKeyException e) {
					logger.error(e.getMessage(), e);
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}

			nextRecId = data8.getNextRecordId();

			logger.info("---------------------------------------------");

			lock = false;
		}

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		GetSelInfoResponseData data10 = null;

		try {
			data10 = (GetSelInfoResponseData) Decoder.decode(buffer,
					new Protocolv20Decoder(cs), new GetSelInfo(IpmiVersion.V20,
							cs, AuthenticationType.RMCPPlus));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info(data10.getSelVersion());
		logger.info(data10.getEntriesCount());
		logger.info(DateFormat.getInstance().format(
				data10.getAdditionTimestamp()));
		logger.info(DateFormat.getInstance().format(
				data10.getEraseTimestamp()));

		logger.info("---------------------------------------------");

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		ReserveSelResponseData data11 = null;

		try {
			data11 = (ReserveSelResponseData) Decoder.decode(buffer,
					new Protocolv20Decoder(cs), new ReserveSel(IpmiVersion.V20,
							cs, AuthenticationType.RMCPPlus));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info(data11.getReservationId());

		reservation = data11.getReservationId();
		reservation = 0;

		lock = false;

		logger.info("---------------------------------------------");

		while (nextRecId < 65535) {

			resp = new DatagramPacket(new byte[256], 256);

			try {
				socket.receive(resp);
				buffer = new byte[resp.getLength()];
				System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			GetSelEntryResponseData data12 = null;

			try {
				data12 = (GetSelEntryResponseData) Decoder.decode(buffer,
						new Protocolv20Decoder(cs), new GetSelEntry(
								IpmiVersion.V20, cs, AuthenticationType.RMCPPlus, 0, 0));
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage(), e);
			} catch (IPMIException e) {
				logger.info(e.getMessage());
				logger.error(e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage(), e);
			} catch (InvalidKeyException e) {
				logger.error(e.getMessage(), e);
			}

			logger.info(data12.getSelRecord().toString());

			SelRecord rec = data12.getSelRecord();

			logger.info("Sensor: " + rec.getSensorType());
			logger.info(rec.getTimestamp());
			logger.info(rec.getEventDirection());
			logger.info(rec.getEvent());

			nextRecId = data12.getNextRecordId();

			lock = false;

			logger.info("---------------------------------------------");
		}

		resp = new DatagramPacket(new byte[256], 256);

		try {
			socket.receive(resp);
			buffer = new byte[resp.getLength()];
			System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		GetFruInventoryAreaInfoResponseData data13 = null;

		try {
			data13 = (GetFruInventoryAreaInfoResponseData) Decoder.decode(
					buffer, new Protocolv20Decoder(cs),
					new GetFruInventoryAreaInfo(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus, 0));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IPMIException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		}

		logger.info("FRU inventory area size: "
				+ data13.getFruInventoryAreaSize());

		logger.info("FRU Unit: " + data13.getFruUnit());

		logger.info("---------------------------------------------");
		
		rd = new ArrayList<ReadFruDataResponseData>();
		
		for(int i = 0; i < fruSize; i +=100) {
			resp = new DatagramPacket(new byte[256], 256);

			try {
				socket.receive(resp);
				buffer = new byte[resp.getLength()];
				System.arraycopy(resp.getData(), 0, buffer, 0, buffer.length);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			ReadFruDataResponseData data14 = null;

			try {
				data14 = (ReadFruDataResponseData) Decoder.decode(buffer,
						new Protocolv20Decoder(cs), new ReadFruData(IpmiVersion.V20,
								cs, AuthenticationType.RMCPPlus, 0, BaseUnit.Bytes, 0, 0));
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage(), e);
			} catch (IPMIException e) {
				logger.error(e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage(), e);
			} catch (InvalidKeyException e) {
				logger.error(e.getMessage(), e);
			}
			
			rd.add(data14);

			logger.info(data14.getFruData().length);

			logger.info("---------------------------------------------");

		}
			
			List<FruRecord> records = ReadFruData.decodeFruData(rd);
			
			for(FruRecord r : records) {
				if(r instanceof ChassisInfo) {
					ChassisInfo chassisInfo = (ChassisInfo) r;
					logger.info("Chassis info:");
					logger.info("Chassis type: " + chassisInfo.getChassisType());
					logger.info("Chassis part number: " + chassisInfo.getChassisPartNumber());
					logger.info("Chassis serial number: " + chassisInfo.getChassisSerialNumber());
					for(String info : chassisInfo.getCustomChassisInfo()) {
						logger.info("Custom chassis info: " + info);
					}
					logger.info("---------------------------------------------");
				} else if(r instanceof BoardInfo) {
					BoardInfo boardInfo = (BoardInfo) r;
					logger.info("Board info:");
					logger.info("Board MFG date: " + boardInfo.getMfgDate().toString());
					logger.info("Board manufacturer: " + boardInfo.getBoardManufacturer());
					logger.info("Board product name: " + boardInfo.getBoardProductName());
					logger.info("Board part number: " + boardInfo.getBoardPartNumber());
					logger.info("Board serial number: " + boardInfo.getBoardSerialNumber());
					for(String info : boardInfo.getCustomBoardInfo()) {
						logger.info("Custom board info: " + info);
					}
					logger.info("---------------------------------------------");
				} else if(r instanceof ProductInfo) {
					ProductInfo productInfo = (ProductInfo) r;
					logger.info("Product info:");
					logger.info("Product manufacturer: " + productInfo.getManufacturerName());
					logger.info("Product product name: " + productInfo.getProductName());
					logger.info("Product part number: " + productInfo.getProductModelNumber());
					logger.info("Product version: " + productInfo.getProductVersion());
					logger.info("Product serial number: " + productInfo.getProductSerialNumber());
					logger.info("Product asset tag: " + productInfo.getAssetTag());
					for(String info : productInfo.getCustomProductInfo()) {
						logger.info("Custom board info: " + info);
					}
					logger.info("---------------------------------------------");
				}
			}
	}
}
