/*
 * ConnectionException.java 
 * Created on 2011-08-24
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.connection;

/**
 * Exception indicating that connecting to the remote machine failed.
 */
public class ConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3912859025179839078L;

	public ConnectionException(String message) {
		super(message);
	}
}
