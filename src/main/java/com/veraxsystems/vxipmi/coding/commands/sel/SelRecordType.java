/*
 * SelRecordType.java 
 * Created on 2011-08-11
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sel;

public enum SelRecordType {
	OemTimestamped(SelRecordType.OEMTIMESTAMPED), System(SelRecordType.SYSTEM), OemNonTimestamped(
			SelRecordType.OEMNONTIMESTAMPED), ;
	/**
	 * Represents OEM timestamped record type (C0h-DFh)
	 */
	private static final int OEMTIMESTAMPED = 192;
	private static final int SYSTEM = 2;
	/**
	 * Represents OEM timestamped record type (E0h-FFh)
	 */
	private static final int OEMNONTIMESTAMPED = 224;

	private int code;

	SelRecordType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static SelRecordType parseInt(int value) {
		if (value == SYSTEM) {
			return System;
		}
		if (value > OEMNONTIMESTAMPED) {
			return OemNonTimestamped;
		}
		if (value > OEMTIMESTAMPED) {
			return OemTimestamped;
		}
		throw new IllegalArgumentException("Invalid value: " + value);
	}
}