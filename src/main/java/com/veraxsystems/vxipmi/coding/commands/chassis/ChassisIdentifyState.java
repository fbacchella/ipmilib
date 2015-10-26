/*
 * ChassisIdentifyState.java 
 * Created on 2011-08-28
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.chassis;

/**
 * Chassis Identify State.
 */
public enum ChassisIdentifyState {
	Off(ChassisIdentifyState.OFF), 
	TemporaryOn(ChassisIdentifyState.TEMPORARYON), 
	IndefiniteOn(ChassisIdentifyState.INDEFINITEON), ;
	
	private static final int OFF = 0;
	private static final int TEMPORARYON = 1;
	private static final int INDEFINITEON = 2;

	private int code;

	ChassisIdentifyState(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ChassisIdentifyState parseInt(int value) {
		switch (value) {
		case OFF:
			return Off;
		case TEMPORARYON:
			return TemporaryOn;
		case INDEFINITEON:
			return IndefiniteOn;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}