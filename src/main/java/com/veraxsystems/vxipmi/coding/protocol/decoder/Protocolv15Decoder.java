/*
 * Protocolv15Decoder.java 
 * Created on 2011-07-26
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.protocol.decoder;

import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv15Message;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Decodes IPMI v1.5 session header and retrieves encrypted payload.
 */
public class Protocolv15Decoder extends ProtocolDecoder {

	public Protocolv15Decoder() {
		super();
	}

	/**
	 * Decodes IPMI v1.5 message fields.
	 * 
	 * @param rmcpMessage
	 *            - RMCP message to decode.
	 * @return decoded message
	 * @see Ipmiv15Message
	 * @throws IllegalArgumentException
	 *             when delivered RMCP message does not contain encapsulated
	 *             IPMI message.
	 */
	@Override
	public IpmiMessage decode(RmcpMessage rmcpMessage)
			throws IllegalArgumentException {
		Ipmiv15Message message = new Ipmiv15Message();

		byte[] raw = rmcpMessage.getData();

		message.setAuthenticationType(decodeAuthenticationType(raw[0]));

		int offset = 1;

		message.setSessionSequenceNumber(decodeSessionSequenceNumber(raw,
				offset));

		offset += 4;

		message.setSessionID(decodeSessionID(raw, offset));

		offset += 4;

		if (message.getAuthenticationType() != AuthenticationType.None) {
			message.setAuthCode(decodeAuthCode(raw, offset));
			offset += 16;
		}

		int payloadLength = decodePayloadLength(raw, offset);
		
		message.setPayloadLength(payloadLength);
		++offset;

		message.setPayload(decodePayload(raw, offset,
				payloadLength,
				message.getConfidentialityAlgorithm()));

		offset += payloadLength;

		return message;
	}

	/**
	 * Decodes authentication code.
	 * 
	 * @param rawMessage
	 *            - Byte array holding whole message data.
	 * @param offset
	 *            - Offset to authentication code in header.
	 * @return authentication code.
	 */
	private byte[] decodeAuthCode(byte[] rawMessage, int offset) {
		byte[] authCode = new byte[16];

		System.arraycopy(rawMessage, offset, authCode, 0, 16);

		return authCode;
	}

	@Override
	protected int decodePayloadLength(byte[] rawData, int offset) {
		return TypeConverter.byteToInt(rawData[offset]);
	}
}
