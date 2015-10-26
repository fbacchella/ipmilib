/*
 * RmcpDecoder.java 
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
 * Decodes RMCP packet from raw data.
 */
public final class RmcpDecoder {

	private RmcpDecoder() {
	}
	
    /**
     * Decodes the RMCP packet from raw data.
     * @param rawMessage
     * - packet data in a byte form
     * @return RMCPMessage
     * @throws IllegalArgumentException
     * - occurs when message is too short or contains invalid data
     */
    public static RmcpMessage decode(byte[] rawMessage) throws IllegalArgumentException {
        RmcpMessage message = new RmcpMessage();

        if (rawMessage.length < 4) {
            throw new IllegalArgumentException("Message is corrupted");
        }

        message.setVersion(decodeVersion(rawMessage[0]));

        // byte 1 is reserved

        message.setSequenceNumber(decodeSequenceNumber(rawMessage[2]));

        message.setClassOfMessage(decodeClassOfMessage(rawMessage[3]));

        message.setData(decodeData(rawMessage));

        return message;
    }

    private static RmcpVersion decodeVersion(byte version) {
        if (version == RmcpConstants.RMCP_V1_0) {
            return RmcpVersion.RMCP1_0;
        }

        throw new IllegalArgumentException("Illegal RMCP version");
    }

    private static int decodeSequenceNumber(byte sequenceNumber) {
        return TypeConverter.byteToInt(sequenceNumber);
    }

    private static RmcpClassOfMessage decodeClassOfMessage(byte classOfMessage) {
        return RmcpClassOfMessage.parseInt(TypeConverter.byteToInt(classOfMessage) & 0x9f); 
        // bits 5 and 6 are reserved so we need to get rid of them
    }

    private static byte[] decodeData(byte[] rawMessage) {
        byte[] data = new byte[rawMessage.length - 4];
        System.arraycopy(rawMessage, 4, data, 0, data.length);
        return data;
    }

}
