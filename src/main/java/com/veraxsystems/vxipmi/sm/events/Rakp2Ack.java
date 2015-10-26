/*
 * Rakp2Ack.java 
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

import com.veraxsystems.vxipmi.coding.commands.session.Rakp1ResponseData;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.Rakp1Complete;
import com.veraxsystems.vxipmi.sm.states.Rakp3Waiting;

/**
 * Performs transition from {@link Rakp1Complete} to {@link Rakp3Waiting}.
 * 
 * @see StateMachine
 */
public class Rakp2Ack extends StateMachineEvent {
	private byte statusCode;
	private CipherSuite cipherSuite;
	private int sequenceNumber;
	private int managedSystemSessionId;
	private Rakp1ResponseData rakp1ResponseData;

	/**
	 * Prepares {@link Rakp2Ack}.
	 * 
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 *            Only authentication algorithm is used at this point of
	 *            creating a session.
	 * @param statusCode
	 *            - Status of the previous message.
	 * @param sequenceNumber
	 * 
	 * @param managedSystemSessionId
	 *            - The Managed System's Session ID for this session. Must be as
	 *            returned by the Managed System in the Open Session Response
	 *            message.
	 * @param rakp1ResponseData
	 *            - RAKP Message 2 received earlier in the authentification
	 *            process
	 */
	public Rakp2Ack(CipherSuite cipherSuite, int sequenceNumber,
			byte statusCode, int managedSystemSessionId,
			Rakp1ResponseData rakp1ResponseData) {
		this.statusCode = statusCode;
		this.cipherSuite = cipherSuite;
		this.sequenceNumber = sequenceNumber;
		this.managedSystemSessionId = managedSystemSessionId;
		this.rakp1ResponseData = rakp1ResponseData;
	}

	public byte getStatusCode() {
		return statusCode;
	}

	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public int getManagedSystemSessionId() {
		return managedSystemSessionId;
	}

	public Rakp1ResponseData getRakp1ResponseData() {
		return rakp1ResponseData;
	}
}
