/*
 * GetChannelCipherSuitesResponseData.java 
 * Created on 2011-08-01
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
 * Wrapper for Get Channel Cipher Suites response
 */
public class GetChannelCipherSuitesResponseData implements ResponseData {

	private byte channelNumber;

	private byte[] cipherSuiteData;

	public void setChannelNumber(byte channelNumber) {
		this.channelNumber = channelNumber;
	}

	public byte getChannelNumber() {
		return channelNumber;
	}

	public void setCipherSuiteData(byte[] cipherSuiteData) {
		this.cipherSuiteData = cipherSuiteData;
	}

	public byte[] getCipherSuiteData() {
		return cipherSuiteData;
	}
}
