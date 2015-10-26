/*
 * ModifierUnitUsage.java 
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

public enum ModifierUnitUsage {
	
	None(ModifierUnitUsage.NONE),
	/**
	 * Unit = Basic Unit / Modifier Unit
	 */
	Divide(ModifierUnitUsage.DIVIDE),
	/**
	 * Unit = Basic Unit * Modifier Unit
	 */
	Mulitply(ModifierUnitUsage.MULITPLY), ;

	private static final int NONE = 0;
	private static final int DIVIDE = 1;
	private static final int MULITPLY = 2;

	private int code;

	ModifierUnitUsage(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ModifierUnitUsage parseInt(int value) {
		switch (value) {
		case NONE:
			return None;
		case DIVIDE:
			return Divide;
		case MULITPLY:
			return Mulitply;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}