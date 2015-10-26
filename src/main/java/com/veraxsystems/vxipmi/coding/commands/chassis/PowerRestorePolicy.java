/*
 * PowerRestorePolicy.java 
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
 * Chassis power restore policy.
 */
public enum PowerRestorePolicy {
	/**
	 * Chassis stays powered off after AC/mains returns
	 */
	PoweredOff,
	/**
	 * After AC returns, power is restored to the state that was in effect when
	 * AC/mains was lost
	 */
	PowerRestored,
	/**
	 * Chassis always powers up after AC/mains returns
	 */
	PoweredUp,
}
