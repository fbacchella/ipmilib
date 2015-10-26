/*
 * GetChannelCipherSuitesPending.java 
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
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelCipherSuites;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.CiphersWaiting;
import com.veraxsystems.vxipmi.sm.states.State;

/**
 * Performed in {@link CiphersWaiting} {@link State} indcates that not all
 * available {@link CipherSuite}s were received from the remote system and more
 * {@link GetChannelCipherSuites} commands are needed.
 * 
 * @see StateMachine
 */
public class GetChannelCipherSuitesPending extends Default {

	public GetChannelCipherSuitesPending(int sequenceNumber) {
		super(CipherSuite.getEmpty(), sequenceNumber,
				PrivilegeLevel.MaximumAvailable);
	}

}
