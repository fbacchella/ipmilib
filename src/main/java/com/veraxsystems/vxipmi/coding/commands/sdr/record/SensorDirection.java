/*
 * SensorDirection.java 
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
 * Indicates whether the sensor is monitoring an input or output relative to the
 * given Entity. E.g. if the sensor is monitoring a current, this can be used to
 * specify whether it is an input voltage or an output voltage.
 */
public enum SensorDirection {
	Unspecified(SensorDirection.UNSPECIFIED), 
	Input(SensorDirection.INPUT), 
	Output(SensorDirection.OUTPUT), 
	Reserved(SensorDirection.RESERVED), ;
	
	private static final int UNSPECIFIED = 0;
	private static final int INPUT = 1;
	private static final int OUTPUT = 2;
	private static final int RESERVED = 3;

	private int code;

	SensorDirection(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static SensorDirection parseInt(int value) {
		switch (value) {
		case UNSPECIFIED:
			return Unspecified;
		case INPUT:
			return Input;
		case OUTPUT:
			return Output;
		case RESERVED:
			return Reserved;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}