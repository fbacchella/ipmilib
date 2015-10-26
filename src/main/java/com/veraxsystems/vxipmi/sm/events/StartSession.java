/*
 * StartSession.java 
 * Created on 2011-08-23
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm.events;

import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.Rakp3Complete;
import com.veraxsystems.vxipmi.sm.states.SessionValid;

/**
 * Acknowledges starting the session after receiving RAKP Message 4 (
 * {@link StateMachine} transits from {@link Rakp3Complete} to
 * {@link SessionValid})
 * 
 * @see StateMachine
 */
public class StartSession extends StateMachineEvent {
	private CipherSuite cipherSuite;
	private int sessionId;
	
	public StartSession(CipherSuite cipherSuite, int sessionId) {
		this.cipherSuite = cipherSuite;
		this.sessionId = sessionId;
	}

	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}

	public int getSessionId() {
		return sessionId;
	}
}
