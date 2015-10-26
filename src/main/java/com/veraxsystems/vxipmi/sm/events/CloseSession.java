/*
 * CloseSession.java 
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

import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.Authcap;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import com.veraxsystems.vxipmi.sm.states.State;

/**
 * {@link StateMachineEvent} that will make {@link StateMachine} in the
 * {@link SessionValid} {@link State} to send
 * {@link com.veraxsystems.vxipmi.coding.commands.session.CloseSession} and
 * transit to {@link Authcap} {@link State} the session.
 */
public class CloseSession extends StateMachineEvent {
	private int sessionId;
	private int sequenceNumber;

	/**
	 * Prepares {@link CloseSession}
	 * 
	 * @param sessionId
	 *            - managed system session ID
	 * 
	 * @param sequenceNumber
	 *            - generated sequence number for the message to send
	 */
	public CloseSession(int sessionId, int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
		this.sessionId = sessionId;
	}

	public int getSessionId() {
		return sessionId;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}
}
