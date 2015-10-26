/*
 * AddressType.java 
 * Created on 2011-08-04
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sdr.record;

/**
 * Type of address behind Sensor Record's 'Sensor Owner ID' field.
 */
public enum AddressType {
	IpmbSlaveAddress(AddressType.IPMBSLAVEADDRESS),
	SystemSoftwareId(AddressType.SYSTEMSOFTWAREID),
	;
	private static final int IPMBSLAVEADDRESS = 0;
	private static final int SYSTEMSOFTWAREID = 1;

	private int code;

	AddressType(int code) {
		this.code = code;
	}
	public int getCode() {
		return code;
	}
	public static AddressType parseInt(int value) {
		switch(value) {
		case IPMBSLAVEADDRESS:
			return IpmbSlaveAddress;
		case SYSTEMSOFTWAREID:
			return SystemSoftwareId;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}