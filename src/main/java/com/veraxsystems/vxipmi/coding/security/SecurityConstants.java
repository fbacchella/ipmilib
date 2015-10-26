/*
 * SecurityConstants.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.security;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Set of constants. Byte constants are encoded as pseudo unsigned bytes.
 * SecurityConstants doesn't use {@link TypeConverter} because fields need to be
 * runtime constants.
 * 
 * @see TypeConverter#byteToInt(byte)
 * @see TypeConverter#intToByte(int)
 */
public final class SecurityConstants {
	/**
	 * Authentication algorithm = RAKP-none
	 */
	public static final byte AA_RAKP_NONE = 0x0;
	/**
	 * Authentication algorithm = RAKP-HMAC-SHA1
	 */
	public static final byte AA_RAKP_HMAC_SHA1 = 0x1;
	/**
	 * Authentication algorithm = RAKP-HMAC-MD5
	 */
	public static final byte AA_RAKP_HMAC_MD5 = 0x2;
	/**
	 * Authentication algorithm = RAKP-HMAC-SHA256
	 */
	public static final byte AA_RAKP_HMAC_SHA256 = 0x3;

	/**
	 * Integrity algorithm = none
	 */
	public static final byte IA_NONE = 0x0;

	/**
	 * Integrity algorithm = HMAC-SHA1-96
	 */
	public static final byte IA_HMAC_SHA1_96 = 0x1;

	/**
	 * Integrity algorithm = HMAC-MD5-128
	 */
	public static final byte IA_HMAC_MD5_128 = 0x2;

	/**
	 * Integrity algorithm = MD5-128
	 */
	public static final byte IA_MD5_128 = 0x3;

	/**
	 * Integrity algorithm = HMAC-SHA256-128
	 */
	public static final byte IA_HMAC_SHA256_128 = 0x4;

	/**
	 * Confidentiality algorithm = None
	 */
	public static final byte CA_NONE = 0x0;

	/**
	 * Confidentiality algorithm = AES-CBC-128
	 */
	public static final byte CA_AES_CBC128 = 0x1;

	/**
	 * Confidentiality algorithm = xRC4-128
	 */
	public static final byte CA_XRC4_128 = 0x2;

	/**
	 * Confidentiality algorithm = xRC4-40
	 */
	public static final byte CA_XRC4_40 = 0x3;

	private SecurityConstants() {
	}
}
