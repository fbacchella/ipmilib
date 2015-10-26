/*
 * IntegrityHmacSha1_96.java 
 * Created on 2011-08-01
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC-SHA1-96 integrity algorithm.
 */
public class IntegrityHmacSha1_96 extends IntegrityAlgorithm {

	private Mac mac;

	private static final byte[] CONST1 = new byte[] { 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

	/**
	 * Initiates HMAC-SHA1-96 integrity algorithm.
	 * 
	 * @throws NoSuchAlgorithmException
	 *             - when initiation of the algorithm fails
	 */
	public IntegrityHmacSha1_96() throws NoSuchAlgorithmException {
		mac = Mac.getInstance("HmacSHA1");
	}
	
	@Override
	public void initialize(byte[] sik) throws InvalidKeyException {
		super.initialize(sik);
		
		SecretKeySpec k1 = new SecretKeySpec(sik, "HmacSHA1");

		mac.init(k1);
		
		k1 = new SecretKeySpec(mac.doFinal(CONST1), "HmacSHA1");
		
		mac.init(k1);
	}

	@Override
	public byte getCode() {
		return SecurityConstants.IA_HMAC_SHA1_96;
	}

	@Override
	public byte[] generateAuthCode(byte[] base) {
		if (sik == null) {
			throw new NullPointerException("Algorithm not initialized.");
		}

		byte[] result = new byte[12];
		
		if(base[base.length - 2] == 0 /*there are no integrity pad bytes*/) {
			base = injectIntegrityPad(base,12);
		}

		System.arraycopy(mac.doFinal(base), 0, result, 0, 12);

		return result;
	}

}
