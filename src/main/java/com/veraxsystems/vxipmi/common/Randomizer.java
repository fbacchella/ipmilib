/*
 * Randomizer.java 
 * Created on 2011-07-26
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.common;

import java.util.Date;
import java.util.Random;

/**
 * Utility class for generating random numbers.
 * 
 */
public final class Randomizer {
	private static Random rand = new Random(new Date().getTime());

	private Randomizer() {
	}

	/**
	 * @return Generated random {@link Integer}
	 */
	public static int getInt() {
		return rand.nextInt();
	}
}
