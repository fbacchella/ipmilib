/*
 * Rakp3ResponseData.java 
 * Created on 2011-07-26
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.session;

import com.veraxsystems.vxipmi.coding.commands.ResponseData;

/**
 * A wrapper for RAKP 3 message.
 * @see Rakp3
 */
public class Rakp3ResponseData implements ResponseData {

	private byte messageTag;

	private byte statusCode;

	/**
	 * The Console Session ID specified by the RMCP+ Open Session Request
	 * message associated with this response.
	 */
	private int consoleSessionId;

	public void setMessageTag(byte messageTag) {
		this.messageTag = messageTag;
	}

	public byte getMessageTag() {
		return messageTag;
	}

	public void setStatusCode(byte statusCode) {
		this.statusCode = statusCode;
	}

	public byte getStatusCode() {
		return statusCode;
	}

	public void setConsoleSessionId(int consoleSessionId) {
		this.consoleSessionId = consoleSessionId;
	}

	public int getConsoleSessionId() {
		return consoleSessionId;
	}
}
