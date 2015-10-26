/*
 * Default.java 
 * Created on 2011-08-18
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm.events;

import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.sm.StateMachine;

/**
 * Generic event that is used in a few transitions.
 * @see StateMachine
 */
public class Default extends StateMachineEvent {
	private CipherSuite cipherSuite;
	private int sequenceNumber;
	private PrivilegeLevel privilegeLevel;
	
	public Default(CipherSuite cipherSuite, int sequenceNumber, PrivilegeLevel privilegeLevel) {
		this.cipherSuite = cipherSuite;
		this.sequenceNumber = sequenceNumber;
		this.privilegeLevel = privilegeLevel;
	}
	
	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public PrivilegeLevel getPrivilegeLevel() {
		return privilegeLevel;
	}
	
	
}
