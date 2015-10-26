/*
 * ConfidentialityNone.java 
 * Created on 2011-07-25
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.security;

/**
 * Class representing no confidentiality algorithm.
 */
public class ConfidentialityNone extends ConfidentialityAlgorithm {

	public ConfidentialityNone() {
		super();
	}
	
	@Override
	public byte getCode() {
		return SecurityConstants.CA_NONE;
	}

	@Override
	public byte[] encrypt(byte[] data) {
		return data;
	}

	@Override
	public byte[] decrypt(byte[] data) {
		return data;
	}
	
	@Override
	public int getConfidentialityOverheadSize(int payloadSize) {
		return 0;
	}
	
}
