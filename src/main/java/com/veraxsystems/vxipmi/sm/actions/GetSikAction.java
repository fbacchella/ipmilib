/*
 * GetSikAction.java 
 * Created on 2011-08-23
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm.actions;

/**
 * Returns the Session Integrity Key calculated after receiving RAKP Message 2.
 */
public class GetSikAction extends StateMachineAction {
	private byte[] sik;
	
	public GetSikAction(byte[] sik) {
		this.sik = sik;
	}
	
	public byte[] getSik() {
		return sik;
	}
}
