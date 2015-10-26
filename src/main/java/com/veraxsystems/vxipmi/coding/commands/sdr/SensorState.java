/*
 * SensorState.java 
 * Created on 2011-08-09
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sdr;

/**
 * Represents state of threshold-based sensor.
 */
public enum SensorState {
	BelowLowerNonRecoverable(SensorState.BELOWLOWERNONRECOVERABLE), AboveUpperNonCritical(
			SensorState.ABOVEUPPERNONCRITICAL), AboveUpperNonRecoverable(
			SensorState.ABOVEUPPERNONRECOVERABLE), BelowLowerNonCritical(
			SensorState.BELOWLOWERNONCRITICAL), BelowLowerCritical(
			SensorState.BELOWLOWERCRITICAL), AboveUpperCritical(
			SensorState.ABOVEUPPERCRITICAL), Ok(SensorState.OK), Invalid(
			SensorState.INVALID);
	private static final int BELOWLOWERNONRECOVERABLE = 4;
	private static final int ABOVEUPPERNONCRITICAL = 8;
	private static final int ABOVEUPPERNONRECOVERABLE = 32;
	private static final int BELOWLOWERNONCRITICAL = 1;
	private static final int BELOWLOWERCRITICAL = 2;
	private static final int ABOVEUPPERCRITICAL = 16;
	private static final int OK = 0;
	private static final int INVALID = -1;

	private int code;

	SensorState(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static SensorState parseInt(int value) {
		if ((value & BELOWLOWERNONRECOVERABLE) != 0) {
			return BelowLowerNonRecoverable;
		}
		if ((value & BELOWLOWERCRITICAL) != 0) {
			return BelowLowerCritical;
		}
		if ((value & ABOVEUPPERNONCRITICAL) != 0) {
			return BelowLowerNonCritical;
		}
		if ((value & ABOVEUPPERNONRECOVERABLE) != 0) {
			return AboveUpperNonRecoverable;
		}
		if ((value & ABOVEUPPERCRITICAL) != 0) {
			return AboveUpperCritical;
		}
		if ((value & ABOVEUPPERNONCRITICAL) != 0) {
			return AboveUpperNonCritical;
		}
		if (value == OK) {
			return Ok;
		}
		return Invalid;
	}
}