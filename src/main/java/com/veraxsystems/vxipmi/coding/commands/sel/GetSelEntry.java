/*
 * GetSelEntry.java 
 * Created on 2011-08-11
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sel;

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
 * Wrapper for Get Sel Entry request
 */
public class GetSelEntry extends IpmiCommandCoder {

	private int reservationId;

	private int recordId;

	/**
	 * Initiates GetSelEntry for both encoding and decoding.
	 * 
	 * @param version
	 *            - IPMI version of the command.
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 * @param authenticationType
	 *            - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
	 * @param reservationId
	 *            - SEL reservation ID received via {@link ReserveSel} command
	 *            or 0 if requesting for whole record
	 * @param recordId
	 *            - ID of the record to get
	 */
	public GetSelEntry(IpmiVersion version, CipherSuite cipherSuite,
			AuthenticationType authenticationType, int reservationId,
			int recordId) {
		super(version, cipherSuite, authenticationType);
		this.recordId = recordId;
		this.reservationId = reservationId;
	}

	@Override
	public byte getCommandCode() {
		return CommandCodes.GET_SEL_ENTRY;
	}

	@Override
	public NetworkFunction getNetworkFunction() {
		return NetworkFunction.StorageRequest;
	}

	@Override
	protected IpmiPayload preparePayload(int sequenceNumber)
			throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] payload = new byte[6];

		byte[] buffer = TypeConverter.intToByteArray(reservationId);

		payload[0] = buffer[3];
		payload[1] = buffer[2]; // reservation ID

		buffer = TypeConverter.intToByteArray(recordId);

		payload[2] = buffer[3];
		payload[3] = buffer[2]; // record ID

		payload[4] = 0;
		payload[5] = TypeConverter.intToByte(0xff);

		return new IpmiLanRequest(getNetworkFunction(), getCommandCode(),
				payload, TypeConverter.intToByte(sequenceNumber % 64));
	}

	@Override
	public ResponseData getResponseData(IpmiMessage message)
			throws IllegalArgumentException, IPMIException,
			NoSuchAlgorithmException, InvalidKeyException {
		if (!isCommandResponse(message)) {
			throw new IllegalArgumentException(
					"This is not a response for Get SEL Entry command");
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

		if (raw == null || raw.length < 3) {
			throw new IllegalArgumentException(
					"Invalid response payload length");
		}

		GetSelEntryResponseData responseData = new GetSelEntryResponseData();

		byte[] buffer = new byte[4];

		buffer[0] = raw[0];
		buffer[1] = raw[1];
		buffer[2] = 0;
		buffer[3] = 0;

		responseData.setNextRecordId(TypeConverter
				.littleEndianByteArrayToInt(buffer));

		byte[] recordData = new byte[raw.length - 2];

		System.arraycopy(raw, 2, recordData, 0, recordData.length);

		responseData.setSelRecord(SelRecord.populateSelRecord(recordData));

		return responseData;
	}

}
