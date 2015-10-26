/*
 * AuthenticationRakpNone.java 
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
 * RAKP-None authentication algorithm.
 */
public class AuthenticationRakpNone extends AuthenticationAlgorithm {

	@Override
	public byte getCode() {
		return SecurityConstants.AA_RAKP_NONE;
	}

	/**
	 * Checks value of the Key Exchange Authentication Code in RAKP messages
	 * using the RAKP-None algorithm.
	 */
	@Override
	public boolean checkKeyExchangeAuthenticationCode(byte[] data, byte[] key, String password) {
		return true;
	}

	/**
	 * Calculates value of the Key Exchange Authentication Code in RAKP messages
	 * using the RAKP-None algorithm.
	 */
	@Override
	public byte[] getKeyExchangeAuthenticationCode(byte[] data,
			String password) {
		return new byte[0];
	}

	/**
	 * Performs Integrity Check in RAKP 4 message
	 * using the RAKP-None algorithm.
	 */
	@Override
	public boolean doIntegrityCheck(byte[] data, byte[] reference, byte[] sik) {
		return true;
	}

	@Override
	public int getKeyLength() {
		return 0;
	}

	@Override
	public int getIntegrityCheckBaseLength() {
		return 0;
	}

}
