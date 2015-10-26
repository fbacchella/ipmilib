/*
 * Sendv20Message.java 
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

import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import com.veraxsystems.vxipmi.sm.states.State;

/**
 * Performed in {@link SessionValid} {@link State} will cause {@link #message}
 * to be sent.
 * 
 * @see StateMachine
 */
public class Sendv20Message extends StateMachineEvent {
	private IpmiCommandCoder message;
	private int sessionId;
	private int sequenceNumber;

	/**
	 * Prepares an event for {@link StateMachine} that will perform sending an
	 * IPMI command in v2.0 format. Only possible in {@link SessionValid}
	 * {@link State}
	 * 
	 * @param ipmiCommandCoder
	 *            - The command to send.
	 * @param sessionId
	 *            - managed system session ID
	 * @param sequenceNumber
	 *            - generated sequence number for the message to send
	 */
	public Sendv20Message(IpmiCommandCoder ipmiCommandCoder, int sessionId,
			int sequenceNumber) {
		message = ipmiCommandCoder;
		this.sequenceNumber = sequenceNumber;
		this.sessionId = sessionId;
	}

	public int getSessionId() {
		return sessionId;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public IpmiCommandCoder getCommandCoder() {
		return message;
	}
}
