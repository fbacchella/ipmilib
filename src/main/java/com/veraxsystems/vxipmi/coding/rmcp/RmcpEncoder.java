/*
 * RmcpEncoder.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.rmcp;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Encodes RMCPMessage into RMCP packet.
 */
public final class RmcpEncoder {

	private RmcpEncoder() {
	}

	/**
	 * Encodes RMCPMessage into RMCP packet.
	 * 
	 * @param message
	 *            - RMCP message to be encoded
	 * @return byte data containing ready to send RMCP packet
	 */
	public static byte[] encode(RmcpMessage message) {
		byte[] data = new byte[message.getData().length + 4];

		data[0] = encodeVersion(message.getVersion());

		data[1] = 0; // reserved

		data[2] = encodeSequenceNumber(message.getSequenceNumber());

		data[3] = encodeRMCPClassOfMessage(message.getClassOfMessage());

		encodeData(message.getData(), data);

		return data;
	}

	private static byte encodeVersion(RmcpVersion version) {
		switch (version) {
		case RMCP1_0:
			return RmcpConstants.RMCP_V1_0;
		default:
			throw new IllegalArgumentException("Invalid RMCP version");
		}
	}

	private static byte encodeSequenceNumber(byte sequenceNumber) {
		return sequenceNumber;
	}

	private static byte encodeRMCPClassOfMessage(
			RmcpClassOfMessage classOfMessage) {
		return TypeConverter.intToByte(classOfMessage.getCode());
	}

	/**
	 * Copies data to message
	 * 
	 * @param data
	 *            - source data of RMCPMessage
	 * @param message
	 *            - result message
	 */
	private static void encodeData(byte[] data, byte[] message) {
		System.arraycopy(data, 0, message, 4, data.length);
	}
}
