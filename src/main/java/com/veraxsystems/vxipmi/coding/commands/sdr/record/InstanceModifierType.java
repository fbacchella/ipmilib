/*
 * InstanceModifierType.java 
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

public enum InstanceModifierType {
	Numeric(InstanceModifierType.NUMERIC),
	Alpha(InstanceModifierType.ALPHA),
	;
	private static final int NUMERIC = 0;
	private static final int ALPHA = 1;

	private int code;

	InstanceModifierType(int code) {
		this.code = code;
	}
	public int getCode() {
		return code;
	}
	public static InstanceModifierType parseInt(int value) {
		switch(value) {
		case NUMERIC:
			return Numeric;
		case ALPHA:
			return Alpha;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}