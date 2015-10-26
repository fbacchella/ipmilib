/*
 * ProtocolEncoder.java 
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
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Adds IPMI session header to encrypted payload. Should be used to encode
 * regular RMCP+ messages (excluding Open Session and RAKP Messages)
 */
public abstract class ProtocolEncoder implements IpmiEncoder {

	/**
	 * Encodes IPMI message.
	 * 
	 * @param ipmiMessage
	 *            - IPMI message to encode.
	 * @return IPMI message encoded into byte array.
	 * @throws InvalidKeyException
	 *             - when initiation of the confidentiality algorithm fails
	 * @see IpmiMessage
	 */
	@Override
	public abstract byte[] encode(IpmiMessage ipmiMessage)
			throws InvalidKeyException;

	protected byte encodeAuthenticationType(
			AuthenticationType authenticationType) {
		return TypeConverter.intToByte(authenticationType.getCode());
	}

	/**
	 * Converts integer value into byte array using little endian convention and
	 * inserts it into message at given offset.
	 * 
	 * @param value
	 * @param message
	 *            - IPMI message being created
	 * @param offset
	 * @throws IndexOutOfBoundsException
	 *             when message is too short to hold value at given offset
	 */
	protected void encodeInt(int value, byte[] message, int offset)
			throws IndexOutOfBoundsException {
		byte[] array = TypeConverter.intToLittleEndianByteArray(value);

		if (array.length + offset > message.length) {
			throw new IndexOutOfBoundsException("Message is too short");
		}

		System.arraycopy(array, 0, message, offset, array.length);
	}

	/**
	 * Encodes session sequence number and inserts it into message at given
	 * offset.
	 * 
	 * @param value
	 * @param message
	 *            - IPMI message being created
	 * @param offset
	 * @throws IndexOutOfBoundsException
	 *             when message is too short to hold value at given offset
	 */
	protected void encodeSessionSequenceNumber(int value, byte[] message,
			int offset) throws IndexOutOfBoundsException {
		encodeInt(value, message, offset);
	}

	/**
	 * Encodes session id and inserts it into message at given offset.
	 * 
	 * @param value
	 * @param message
	 *            - IPMI message being created
	 * @param offset
	 * @throws IndexOutOfBoundsException
	 *             when message is too short to hold value at given offset
	 */
	protected void encodeSessionId(int value, byte[] message, int offset)
			throws IndexOutOfBoundsException {
		encodeInt(value, message, offset);
	}

	/**
	 * Encodes payload length and inserts it into message at given offset.
	 * 
	 * @param value
	 * @param message
	 *            - IPMI message being created
	 * @param offset
	 * @throws IndexOutOfBoundsException
	 *             when message is too short to hold value at given offset
	 */
	protected abstract void encodePayloadLength(int value, byte[] message,
			int offset) throws IndexOutOfBoundsException;

	/**
	 * Encodes payload and inserts it into message at given offset. <br>
	 * When payload == null it will not be inserted.
	 * 
	 * @param payload
	 *            - IPMI payload to encode.
	 * @param message
	 *            - IPMI message being created.
	 * @param offset
	 * @throws IndexOutOfBoundsException
	 *             when message is too short to hold value at given offset
	 * @return offset + encoded message length
	 */
	protected int encodePayload(byte[] payload, byte[] message, int offset)
			throws IndexOutOfBoundsException {
		if (payload == null) {
			return offset;
		}
		if (payload.length + offset > message.length) {
			throw new IndexOutOfBoundsException("Message is too short");
		}
		System.arraycopy(payload, 0, message, offset, payload.length);
		return offset + payload.length;
	}
}
