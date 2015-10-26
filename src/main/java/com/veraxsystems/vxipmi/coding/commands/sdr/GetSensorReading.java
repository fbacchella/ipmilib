/*
 * GetSensorReading.java 
 * Created on 2011-08-09
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sdr;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanRequest;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Wrapper for Get Sensor Reading request.
 */
public class GetSensorReading extends IpmiCommandCoder {

	private byte sensorId;

	/**
	 * Initiates class for both encoding and decoding.
	 * 
	 * @param version
	 *            - IPMI version of the command.
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 * @param authenticationType
	 *            - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
	 * @param sensorId
	 *            - ID of the sensor which reading is to be retrieved
	 */
	public GetSensorReading(IpmiVersion version, CipherSuite cipherSuite,
			AuthenticationType authenticationType, int sensorId) {
		super(version, cipherSuite, authenticationType);
		this.sensorId = TypeConverter.intToByte(sensorId);
	}

	@Override
	public byte getCommandCode() {
		return 0x2d;
	}

	@Override
	public NetworkFunction getNetworkFunction() {
		return NetworkFunction.SensorRequest;
	}

	@Override
	protected IpmiPayload preparePayload(int sequenceNumber)
			throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] payloadData = new byte[] { sensorId };
		return new IpmiLanRequest(getNetworkFunction(), getCommandCode(),
				payloadData, TypeConverter.intToByte(sequenceNumber % 64));
	}

	@Override
	public ResponseData getResponseData(IpmiMessage message)
			throws IllegalArgumentException, IPMIException,
			NoSuchAlgorithmException, InvalidKeyException {

		if (!isCommandResponse(message)) {
			throw new IllegalArgumentException(
					"This is not a response for Get Sensor Reading command");
		}
		if (!(message.getPayload() instanceof IpmiLanResponse)) {
			throw new IllegalArgumentException("Invalid response payload");
		}
		if (((IpmiLanResponse) message.getPayload()).getCompletionCode() != CompletionCode.Ok) {
			throw new IPMIException(
					((IpmiLanResponse) message.getPayload())
							.getCompletionCode());
		}

		byte[] raw = message.getPayload().getIpmiCommandData();

		if (raw.length < 3) {
			throw new IllegalArgumentException("Invalid response length");
		}

		GetSensorReadingResponseData responseData = new GetSensorReadingResponseData();

		responseData.setSensorReading(raw[0]);

		responseData
				.setSensorStateValid((TypeConverter.byteToInt(raw[1]) & 0x20) == 0);

		responseData.setSensorState(SensorState.parseInt((TypeConverter
				.byteToInt(raw[2])) & 0x3f));

		boolean[] states = null;

		if (raw.length > 3) {
			states = new boolean[16];
		} else {
			states = new boolean[8];
		}

		for (int i = 0; i < 8; ++i) {
			states[i] = (TypeConverter.byteToInt(raw[2]) & (0x1 << i)) != 0;
		}

		if (raw.length > 3) {
			for (int i = 0; i < 7; ++i) {
				states[i + 8] = (TypeConverter.byteToInt(raw[3]) & (0x1 << i)) != 0;
			}
			states[15] = false;
		}

		responseData.setStatesAsserted(states);

		return responseData;
	}

}
