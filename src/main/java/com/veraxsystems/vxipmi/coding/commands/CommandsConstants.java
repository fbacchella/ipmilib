/*
 * CommandsConstants.java 
 * Created on 2011-07-25
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Set of constants. Byte constants are encoded as pseudo unsigned bytes.
 * IpmiLanConstants doesn't use {@link TypeConverter} because fields need to be
 * runtime constants.
 * 
 * @see TypeConverter#byteToInt(byte)
 * @see TypeConverter#intToByte(int)
 */
public final class CommandsConstants {

	/**
	 * Highest available authentication level
	 */
	public static final byte AL_HIGHEST_AVAILABLE = 0x00;

	/**
	 * Authentication level = Callback
	 */
	public static final byte AL_CALLBACK = 0x01;

	/**
	 * Authentication level = User
	 */
	public static final byte AL_USER = 0x02;

	/**
	 * Authentication level = Operator
	 */
	public static final byte AL_OPERATOR = 0x03;

	/**
	 * Authentication level = Administrator
	 */
	public static final byte AL_ADMINISTRATOR = 0x04;

	/**
	 * OEM-defined authentication level
	 */
	public static final byte AL_OEM = 0x05;

	private CommandsConstants() {
	}
}
