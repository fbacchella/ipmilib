/*
 * AuthenticationRakpHmacSha1.java 
 * Created on 2011-07-27
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * RAKP-HMAC-SHA1 authentication algorithm.
 */
public class AuthenticationRakpHmacSha1 extends AuthenticationAlgorithm {

	private Mac mac;
	
	/**
	 * Initiates RAKP-HMAC-SHA1 authentication algorithm.
	 * @throws NoSuchAlgorithmException
	 *             - when initiation of the algorithm fails
	 */
	public AuthenticationRakpHmacSha1() throws NoSuchAlgorithmException {
		mac = Mac.getInstance("HmacSHA1");
	}
	
	@Override
	public byte getCode() {
		return SecurityConstants.AA_RAKP_HMAC_SHA1;
	}

	@Override
	public boolean checkKeyExchangeAuthenticationCode(byte[] data, byte[] key,
			String password) throws NoSuchAlgorithmException,
			InvalidKeyException {
		byte[] check = getKeyExchangeAuthenticationCode(data, password);
		return Arrays.equals(check, key);
	}

	@Override
	public byte[] getKeyExchangeAuthenticationCode(byte[] data,
		String password)
			throws NoSuchAlgorithmException, InvalidKeyException {
		
		byte[] key = password.getBytes();
				
		SecretKeySpec sKey = new SecretKeySpec(key, "HmacSHA1");
		mac.init(sKey);
		
		return mac.doFinal(data);
	}

	@Override
	public boolean doIntegrityCheck(byte[] data, byte[] reference, byte[] sik) throws InvalidKeyException, NoSuchAlgorithmException {

		SecretKeySpec sKey = new SecretKeySpec(sik, "HmacSHA1");
		mac.init(sKey);
		
		byte[] result = new byte[getIntegrityCheckBaseLength()];
		
		System.arraycopy(mac.doFinal(data), 0, result, 0, getIntegrityCheckBaseLength());
		
		return Arrays.equals(result, reference);
	}

	@Override
	public int getKeyLength() {
		return 20;
	}

	@Override
	public int getIntegrityCheckBaseLength() {
		return 12;
	}

}
