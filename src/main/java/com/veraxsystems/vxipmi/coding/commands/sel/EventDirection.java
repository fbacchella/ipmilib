/*
 * EventDirection.java 
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

public enum EventDirection {

	Assertion(EventDirection.ASSERTION), 
	Deassertion(EventDirection.DEASSERTION), ;

	private static final int ASSERTION = 0;
	private static final int DEASSERTION = 1;

	private int code;

	EventDirection(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static EventDirection parseInt(int value) {
		switch (value) {
		case ASSERTION:
			return Assertion;
		case DEASSERTION:
			return Deassertion;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}