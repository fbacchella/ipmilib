/*
 * GetSdr.java 
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
 * Wrapper for Get SDR command.
 */
public class GetSdr extends IpmiCommandCoder {

	private int reservationId;

	private int recordId;

	private int offset;

	private int bytesToRead;

	/**
	 * Initiates GetSdr for both encoding and decoding. 'Offset info record' and
	 * 'bytes to read' fields are set to read whole record.
	 * 
	 * @param version
	 *            - IPMI version of the command.
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 * @param authenticationType
	 *            - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
	 * @param reservationId
	 *            - SDR reservation ID received via {@link ReserveSdrRepository}
	 *            command
	 * @param recordId
	 *            - ID of the record to get
	 */
	public GetSdr(IpmiVersion version, CipherSuite cipherSuite,
			AuthenticationType authenticationType, int reservationId,
			int recordId) {
		this(version, cipherSuite, authenticationType, reservationId, recordId,
				0, 0xFF);
	}

	/**
	 * Initiates GetSdr for both encoding and decoding.
	 * 
	 * @param version
	 *            - IPMI version of the command.
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 * @param authenticationType
	 *            - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
	 * @param reservationId
	 *            - SDR reservation ID received via {@link ReserveSdrRepository}
	 *            command
	 * @param recordId
	 *            - ID of the record to get
	 * @param offset
	 *            - the offset into record, at which reading should be started
	 * @param bytesToRead
	 *            - number of bytes to read
	 */
	public GetSdr(IpmiVersion version, CipherSuite cipherSuite,
			AuthenticationType authenticationType, int reservationId,
			int recordId, int offset, int bytesToRead) {
		super(version, cipherSuite, authenticationType);
		this.recordId = recordId;
		this.reservationId = reservationId;
		this.offset = offset;
		this.bytesToRead = bytesToRead;
	}

	@Override
	public byte getCommandCode() {
		return CommandCodes.GET_SDR;
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

		payload[4] = TypeConverter.intToByte(offset);
		payload[5] = TypeConverter.intToByte(bytesToRead);

		return new IpmiLanRequest(getNetworkFunction(), getCommandCode(),
				payload, TypeConverter.intToByte(sequenceNumber % 64));
	}

	@Override
	public ResponseData getResponseData(IpmiMessage message)
			throws IllegalArgumentException, IPMIException,
			NoSuchAlgorithmException, InvalidKeyException {
		if (!isCommandResponse(message)) {
			throw new IllegalArgumentException(
					"This is not a response for Get SDR command");
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

		GetSdrResponseData responseData = new GetSdrResponseData();

		byte[] buffer = new byte[4];

		buffer[0] = raw[0];
		buffer[1] = raw[1];
		buffer[2] = 0;
		buffer[3] = 0;

		responseData.setNextRecordId(TypeConverter
				.littleEndianByteArrayToInt(buffer));

		byte[] recordData = new byte[raw.length - 2];

		System.arraycopy(raw, 2, recordData, 0, recordData.length);

		responseData.setSensorRecordData(recordData);

		return responseData;
	}

}
