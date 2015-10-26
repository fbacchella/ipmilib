/*
 * PowerCommand.java 
 * Created on 2011-09-20
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
 * Specifies types of commands that can be sent to BMC via
 * {@link ChassisControl} command.
 */
public enum PowerCommand {
	/**
	 * Force system into soft off (S4/S45) state. This is for 'emergency'
	 * management power down actions. The command does not initiate a clean
	 * shut-down of the operating system prior to powering down the system.
	 */
	PowerDown(PowerCommand.POWERDOWN), PowerUp(PowerCommand.POWERUP),
	/**
	 * Hard reset. In some implementations, the BMC may not know whether a reset
	 * will cause any particular effect and will pulse the system reset signal
	 * regardless of power state. If the implementation can tell that no action
	 * will occur if a reset is delivered in a given power state, then it is
	 * recommended (but still optional) that a D5h 'Request parameter(s) not
	 * supported in present state.' error completion code be returned.
	 */
	HardReset(PowerCommand.HARDRESET), ;
	private static final int POWERDOWN = 0;
	private static final int POWERUP = 1;
	private static final int HARDRESET = 3;

	private int code;

	PowerCommand(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static PowerCommand parseInt(int value) {
		switch (value) {
		case POWERDOWN:
			return PowerDown;
		case POWERUP:
			return PowerUp;
		case HARDRESET:
			return HardReset;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}