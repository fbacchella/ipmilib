/*
 * RmcpConstants.java 
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
 * Set of constants.
 * Byte constants are encoded as pseudo unsigned bytes.
 * RMCPConstants doesn't use {@link TypeConverter} because 
 * fields need to be runtime constants.
 * @see TypeConverter#byteToInt(byte)
 * @see TypeConverter#intToByte(int)
 */
public final class RmcpConstants {
	
	/**
	 * RMCP version 1.0
	 */
	public static final byte RMCP_V1_0 = 0x06;
	
	/**
	 * IANA Enterprise Number = ASF IANA
	 */
	public static final int ASFIANA = 4542;
	
	/**
	 * ASF Message type = Presence Ping
	 */
	public static final byte PRESENCE_PING = (byte) (0x80 - 256);
	
	private RmcpConstants() {
	}
}
