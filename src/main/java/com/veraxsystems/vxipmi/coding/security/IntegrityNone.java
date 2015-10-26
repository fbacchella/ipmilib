/*
 * IntegrityNone.java 
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
 * Class representing RAKP-None integrity algorithm
 */
public class IntegrityNone extends IntegrityAlgorithm {

	public IntegrityNone() {
		super();
	}

	@Override
	public byte getCode() {
		return SecurityConstants.IA_NONE;
	}

	@Override
	public byte[] generateAuthCode(byte[] base) {
		return null;
	}

}
