/*
 * RmcpPingMessage.java 
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
 * A wrapper class for ASF ping message.
 */
public class RmcpPingMessage extends RmcpMessage {
	/**
	 * Prepares a ready to send ASF ping message.
	 * 
	 * @param sequenceNumber
	 *            Used for pairing request with response. Can't be 255.
	 * @throws IllegalArgumentException
	 *             when sequence number is 255.
	 */
	public RmcpPingMessage(int sequenceNumber) throws IllegalArgumentException {
		super();
		if (sequenceNumber > 254 || sequenceNumber < 0) {
			throw new IllegalArgumentException(
					"Sequence number must be in range 0-254");
		}
		setVersion(RmcpVersion.RMCP1_0);
		setClassOfMessage(RmcpClassOfMessage.Asf);
		setData(preparePingMessage(sequenceNumber));
	}

	private byte[] preparePingMessage(int sequenceNumber) {
		byte[] message = new byte[8];

		// set IANA Enterprise Number
		System.arraycopy(TypeConverter.intToByteArray(RmcpConstants.ASFIANA),
				0, message, 0, 4);

		// set message type as presence ping
		message[4] = RmcpConstants.PRESENCE_PING;

		// set message tag (ASF version of a sequence number)
		message[5] = TypeConverter.intToByte(sequenceNumber);

		// reserved
		message[6] = 0;

		// data length
		message[7] = 0;

		return message;
	}
}
