/*
 * GetFruInventoryAreaInfo.java 
 * Created on 2011-08-11
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.fru;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.veraxsystems.vxipmi.coding.commands.CommandCodes;
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
 * A wrapper class for Get FRU Inventory Area Info Command.
 */
public class GetFruInventoryAreaInfo extends IpmiCommandCoder {

	private int fruId;

	/**
	 * Initiates GetFruInventoryAreaInfo for both encoding and decoding.
	 * 
	 * @param version
	 *            - IPMI version of the command.
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 * @param authenticationType
	 *            - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
	 * @param fruId
	 *            - ID of the FRU to get info from. Must be less than 256.
	 */
	public GetFruInventoryAreaInfo(IpmiVersion version,
			CipherSuite cipherSuite, AuthenticationType authenticationType,
			int fruId) {
		super(version, cipherSuite, authenticationType);

		if (fruId > 255) {
			throw new IllegalArgumentException("FRU ID cannot exceed 255");
		}

		this.fruId = fruId;
	}

	@Override
	public byte getCommandCode() {
		return CommandCodes.GET_FRU_INVENTORY_AREA_INFO;
	}

	@Override
	public NetworkFunction getNetworkFunction() {
		return NetworkFunction.StorageRequest;
	}

	@Override
	protected IpmiPayload preparePayload(int sequenceNumber)
			throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] payload = new byte[] { TypeConverter.intToByte(fruId) };
		return new IpmiLanRequest(getNetworkFunction(), getCommandCode(),
				payload, TypeConverter.intToByte(sequenceNumber % 64));
	}

	@Override
	public ResponseData getResponseData(IpmiMessage message)
			throws IllegalArgumentException, IPMIException,
			NoSuchAlgorithmException, InvalidKeyException {

		if (!isCommandResponse(message)) {
			throw new IllegalArgumentException(
					"This is not a response for Get FRU Inventory Info command");
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

		if (raw == null || raw.length != 3) {
			throw new IllegalArgumentException(
					"Invalid response payload length");
		}

		GetFruInventoryAreaInfoResponseData responseData = new GetFruInventoryAreaInfoResponseData();

		byte[] buffer = new byte[4];

		buffer[0] = raw[0];
		buffer[1] = raw[1];
		buffer[2] = 0;
		buffer[3] = 0;

		responseData.setFruInventoryAreaSize(TypeConverter
				.littleEndianByteArrayToInt(buffer));

		responseData.setFruUnit(BaseUnit.parseInt(TypeConverter
				.byteToInt(raw[2]) & 0x1));

		return responseData;
	}
}
