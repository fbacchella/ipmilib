/*
 * RateUnit.java 
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

public enum RateUnit {
	Microseconds(RateUnit.US, "us"), Miliseconds(RateUnit.MS, "ms"), Seconds(
			RateUnit.S, "s"), Minutes(RateUnit.MIN, "min"), Hours(RateUnit.H,
			"h"), Days(RateUnit.D, "days"), None(RateUnit.NONE, "") ;

	private static final int NONE = 0x0;
	private static final int US = 0x1;
	private static final int MS = 0x2;
	private static final int S = 0x3;
	private static final int MIN = 0x4;
	private static final int H = 0x5;
	private static final int D = 0x6;

	private int code;
	private String text;

	RateUnit(int code, String value) {
		this.code = code;
		this.text = value;
	}

	public int getCode() {
		return code;
	}

	public String getUnit() {
		return text;
	}

	public static RateUnit parseInt(int value) {
		switch (value) {
		case NONE:
			return None;
		case US:
			return Microseconds;
		case MS:
			return Miliseconds;
		case S:
			return Seconds;
		case MIN:
			return Minutes;
		case H:
			return Hours;
		case D:
			return Days;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}
