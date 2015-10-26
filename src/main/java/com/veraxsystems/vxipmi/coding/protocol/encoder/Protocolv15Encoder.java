/*
 * Protocolv15Encoder.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.protocol.encoder;

import java.security.InvalidKeyException;

import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv15Message;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Encodes IPMI v1.5 message
 */
public class Protocolv15Encoder extends ProtocolEncoder {

	/**
	 * @param ipmiMessage
	 *            - IPMI message to be encoded. Must be {@link Ipmiv15Message}.
	 * @throws InvalidKeyException
	 *             - when initiation of the confidentiality algorithm fails
	 * @throws IllegalArgumentException
	 *             when IPMI protocol version is incorrect.
	 * @see Ipmiv15Message
	 */
	@Override
	public byte[] encode(IpmiMessage ipmiMessage) throws InvalidKeyException {
		if (!(ipmiMessage instanceof Ipmiv15Message)) {
			throw new IllegalArgumentException(
					"IPMIMessage must be in 1.5 version.");
		}
		Ipmiv15Message message = (Ipmiv15Message) ipmiMessage;

		byte[] raw = new byte[getMessageLength(message)];

		raw[0] = encodeAuthenticationType(message.getAuthenticationType());

		int offset = 1;

		encodeSessionSequenceNumber(message.getSessionSequenceNumber(), raw,
				offset);
		offset += 4;

		encodeSessionId(message.getSessionID(), raw, offset);
		offset += 4;

		if (message.getAuthenticationType() != AuthenticationType.None) {
			encodeAuthenticationCode(message.getAuthCode(), raw, offset);
			offset += message.getAuthCode().length;
		}

		byte[] payload = message.getPayload().getEncryptedPayload();

		if (payload == null) {
			message.getPayload().encryptPayload(
					message.getConfidentialityAlgorithm());
			payload = message.getPayload().getEncryptedPayload();
		}

		encodePayloadLength(payload.length, raw, offset);
		++offset;

		offset = encodePayload(payload, raw, offset);

		offset = encodeSessionTrailer(raw, offset);

		return raw;
	}

	private int getMessageLength(IpmiMessage ipmiMessage) {
		int length = 11
				+ ipmiMessage.getConfidentialityAlgorithm()
						.getConfidentialityOverheadSize(
								ipmiMessage.getPayloadLength())
				+ ipmiMessage.getPayloadLength();

		if (ipmiMessage.getAuthenticationType() != AuthenticationType.None) {
			length += 16;
		}

		return length;
	}

	/**
	 * Inserts authentication code into message at given offset.
	 * 
	 * @param authCode
	 * @param message
	 *            - message being encoded
	 * @param offset
	 * @throws IndexOutOfBoundsException
	 *             when message is too short to hold value at given offset
	 */
	private void encodeAuthenticationCode(byte[] authCode, byte[] message,
			int offset) throws IndexOutOfBoundsException {
		if (authCode.length + offset > message.length) {
			throw new IndexOutOfBoundsException("Message is too short");
		}
		System.arraycopy(authCode, 0, message, offset, authCode.length);
	}

	@Override
	protected void encodePayloadLength(int value, byte[] message, int offset) {
		message[offset] = TypeConverter.intToByte(value);
	}

	/**
	 * Creates session trailer. <br>
	 * Creates an inserts Legacy PAD.
	 * 
	 * @param message
	 *            - IPMI message being created
	 * @param offset
	 *            - Should point at the beginning of the session trailer.
	 * @throws IndexOutOfBoundsException
	 *             when message is too short to hold value at given offset
	 * @return Offset pointing after Authorization Code
	 */
	private int encodeSessionTrailer(byte[] message, int offset)
			throws IndexOutOfBoundsException {
		if (1 + offset > message.length) {
			throw new IndexOutOfBoundsException("Message is too short");
		}

		message[offset] = 0;
		++offset;

		return offset;
	}
}
