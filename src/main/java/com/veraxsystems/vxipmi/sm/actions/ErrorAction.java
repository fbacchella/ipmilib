/*
 * ErrorAction.java 
 * Created on 2011-08-18
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
 * Action representing an exception handed to the machine owner.
 */
public class ErrorAction extends StateMachineAction {
	
	private Exception exception;
	
	public ErrorAction(Exception e) {
		exception = e;
	}
	
	public Exception getException() {
		return exception;
	}
}
