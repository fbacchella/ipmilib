/*
 * OpenSessionAck.java 
 * Created on 2011-08-22
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
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.OpenSessionComplete;
import com.veraxsystems.vxipmi.sm.states.Rakp1Waiting;

/**
 * Performs transition from {@link OpenSessionComplete} to {@link Rakp1Waiting}.
 * 
 * @see StateMachine
 */
public class OpenSessionAck extends Default {

	private int managedSystemSessionId;
	private String username;
	private String password;
	private byte[] bmcKey;

	/**
	 * Provides data required to send RAKP Message 1.
	 * 
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 * @param sequenceNumber
	 *            - a sequence number for the message
	 * @param managedSystemSessionId
	 *            - The Managed System's Session ID for this session. Must be as
	 *            returned by the Managed System in the Open Session Response
	 *            message.
	 * @param privilegeLevel
	 *            - Requested Maximum {@link PrivilegeLevel}
	 * @param username
	 *            - ASCII character Name that the user at the Remote Console
	 *            wishes to assume for this session. It's length cannot exceed
	 *            16.
	 * @param password
	 *            - password matching username
	 * @param bmcKey
	 *            - BMC specific key. Should be null if Get Channel
	 *            Authentication Capabilities Response indicated that Kg is
	 *            disabled which means that 'one-key' logins are being used (
	 *            {@link GetChannelAuthenticationCapabilitiesResponseData#isKgEnabled()}
	 *            == false)
	 */
	public OpenSessionAck(CipherSuite cipherSuite,
			PrivilegeLevel privilegeLevel, int sequenceNumber,
			int managedSystemSessionId, String username, String password,
			byte[] bmcKey) {
		super(cipherSuite, sequenceNumber, privilegeLevel);
		this.managedSystemSessionId = managedSystemSessionId;
		this.username = username;
		this.password = password;
		this.bmcKey = bmcKey;
	}

	public int getManagedSystemSessionId() {
		return managedSystemSessionId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public byte[] getBmcKey() {
		return bmcKey;
	}
}
