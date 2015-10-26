/*
 * BaseUnit.java 
 * Created on 2011-08-11
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.fru;

/**
 * Represents unit which is used to access FRU
 */
public enum BaseUnit {
	Bytes(BaseUnit.BYTES), Words(BaseUnit.WORDS), ;
	private static final int BYTES = 0;
	private static final int WORDS = 1;
	private static final int BYTESIZE = 1;
	private static final int WORDSIZE = 16;

	private int code;

	BaseUnit(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static BaseUnit parseInt(int value) {
		switch (value) {
		case BYTES:
			return Bytes;
		case WORDS:
			return Words;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}

	/**
	 * Returns size of the unit in bytes.
	 */
	public int getSize() {
		switch (this) {
		case Bytes:
			return BYTESIZE;
		case Words:
			return WORDSIZE;
		default:
			throw new IllegalArgumentException("Invalid value: " + this);
		}
	}
}