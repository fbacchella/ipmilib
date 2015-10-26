/*
 * Constants.java 
 * Created on 2011-08-18
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.common;

/**
 * Holds constants common for whole library.
 */
public final class Constants {

	/**
	 * The port BMC is listening for IPMI messages on.
	 */
	public static final int IPMI_PORT = 0x26F;
	
	public static final int TIMEOUT = 500;
	
	private Constants() {
	}
}
