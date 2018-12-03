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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * HMAC-SHA1-96 integrity algorithm.
 */
public class IntegrityHmacSha1_96 extends IntegrityAlgorithm {

    public static final String ALGORITHM_NAME = "HmacSHA1";
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
        mac = Mac.getInstance(ALGORITHM_NAME);
    }

    @Override
    public void initialize(byte[] sik) throws InvalidKeyException {
        super.initialize(sik);

        SecretKeySpec k1 = new SecretKeySpec(sik, ALGORITHM_NAME);

        mac.init(k1);

        k1 = new SecretKeySpec(mac.doFinal(CONST1), ALGORITHM_NAME);

        mac.init(k1);
    }

    @Override
    public byte getCode() {
        return SecurityConstants.IA_HMAC_SHA1_96;
    }

    @Override
    public byte[] generateAuthCode(final byte[] base) {
        if (sik == null) {
            throw new NullPointerException("Algorithm not initialized.");
        }

        byte[] result = new byte[12];
        byte[] updatedBase;

        if(base[base.length - 2] == 0 /*there are no integrity pad bytes*/) {
            updatedBase = injectIntegrityPad(base,12);
        } else {
            updatedBase = base;
        }

        System.arraycopy(mac.doFinal(updatedBase), 0, result, 0, 12);

        return result;
    }

}
