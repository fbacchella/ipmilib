/*
 * CipherSuite.java 
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
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import com.veraxsystems.vxipmi.coding.commands.session.GetChannelCipherSuites;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelCipherSuitesResponseData;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Provides cipher suite (authentication, confidentiality and integrity
 * algorithms used during the session).
 */
public class CipherSuite {

	private byte id;

	private byte authenticationAlgorithm;
	private byte integrityAlgorithm;
	private byte confidentialityAlgorithm;

	private AuthenticationAlgorithm aa;
	private ConfidentialityAlgorithm ca;
	private IntegrityAlgorithm ia;

	public byte getId() {
		return id;
	}

	public CipherSuite(byte id, byte authenticationAlgorithm,
			byte confidentialityAlgorithm, byte integrityAlgorithm) {
		this.id = id;
		this.authenticationAlgorithm = (authenticationAlgorithm);
		this.confidentialityAlgorithm = (confidentialityAlgorithm);
		this.integrityAlgorithm = (integrityAlgorithm);
	}

	/**
	 * Initializes algorithms contained in this {@link CipherSuite}.
	 * 
	 * @param sik
	 *            - Session Integrity Key calculated during the opening of the
	 *            session or user password if 'one-key' logins are enabled.
	 * @throws IllegalArgumentException
	 * @throws InvalidKeyException
	 *             - when initiation of the algorithm fails
	 * @throws NoSuchAlgorithmException
	 *             - when initiation of the algorithm fails
	 * @throws NoSuchPaddingException
	 *             - when initiation of the algorithm fails
	 */
	public void initializeAlgorithms(byte[] sik) throws InvalidKeyException,
			IllegalArgumentException, NoSuchAlgorithmException,
			NoSuchPaddingException {
		getIntegrityAlgorithm().initialize(sik);
		getConfidentialityAlgorithm().initialize(sik);
	}

	/**
	 * Returns instance of AuthenticationAlgorithm class.
	 * 
	 * @throws IllegalArgumentException
	 *             when authentication algorithm code is incorrect.
	 */
	public AuthenticationAlgorithm getAuthenticationAlgorithm()
			throws IllegalArgumentException {
		if (aa != null && aa.getCode() != authenticationAlgorithm) {
			throw new IllegalArgumentException(
					"Invalid authentication algorithm code");
		}
		switch (authenticationAlgorithm) {
		case SecurityConstants.AA_RAKP_NONE:
			if (aa == null) {
				aa = new AuthenticationRakpNone();
			}
			return aa;
		case SecurityConstants.AA_RAKP_HMAC_SHA1:
			if (aa == null) {
				try {
					aa = new AuthenticationRakpHmacSha1();
				} catch (NoSuchAlgorithmException e) {
					throw new IllegalArgumentException(
							"Initiation of the algorithm failed", e);
				}
			}
			return aa;
		case SecurityConstants.AA_RAKP_HMAC_MD5:
			// TODO: RAKP HMAC MD5
			throw new IllegalArgumentException("Not yet implemented.");
		case SecurityConstants.AA_RAKP_HMAC_SHA256:
			// TODO: RAKP HMAC Sha256
			throw new IllegalArgumentException("Not yet implemented.");
		default:
			throw new IllegalArgumentException(
					"Invalid authentication algorithm.");

		}
	}

	/**
	 * Returns instance of IntegrityAlgorithm class.
	 * 
	 * @throws IllegalArgumentException
	 *             when integrity algorithm code is incorrect.
	 * @throws NoSuchAlgorithmException
	 *             - when initiation of the algorithm fails
	 */
	public IntegrityAlgorithm getIntegrityAlgorithm()
			throws IllegalArgumentException {
		if (ia != null && ia.getCode() != integrityAlgorithm) {
			throw new IllegalArgumentException(
					"Invalid integrity algorithm code");
		}
		switch (integrityAlgorithm) {
		case SecurityConstants.IA_NONE:
			if (ia == null) {
				ia = new IntegrityNone();
			}
			return ia;
		case SecurityConstants.IA_HMAC_SHA1_96:
			if (ia == null) {
				try {
					ia = new IntegrityHmacSha1_96();
				} catch (NoSuchAlgorithmException e) {
					throw new IllegalArgumentException(
							"Initiation of the algorithm failed", e);
				}
			}
			return ia;
		case SecurityConstants.IA_HMAC_SHA256_128:
			// TODO: HMAC SHA256-128
			throw new IllegalArgumentException("Not yet implemented.");
		case SecurityConstants.IA_MD5_128:
			// TODO: MD5-128
			throw new IllegalArgumentException("Not yet implemented.");
		case SecurityConstants.IA_HMAC_MD5_128:
			// TODO: HMAC MD5-128
			throw new IllegalArgumentException("Not yet implemented.");
		default:
			throw new IllegalArgumentException("Invalid integrity algorithm.");

		}
	}

	/**
	 * Returns instance of ConfidentialityAlgorithm class.
	 * 
	 * @throws IllegalArgumentException
	 *             when confidentiality algorithm code is incorrect.
	 */
	public ConfidentialityAlgorithm getConfidentialityAlgorithm()
			throws IllegalArgumentException {
		if (ca != null && ca.getCode() != confidentialityAlgorithm) {
			throw new IllegalArgumentException(
					"Invalid confidentiality algorithm code");
		}
		switch (confidentialityAlgorithm) {
		case SecurityConstants.CA_NONE:
			if (ca == null) {
				ca = new ConfidentialityNone();
			}
			return ca;
		case SecurityConstants.CA_AES_CBC128:
			if (ca == null) {
				ca = new ConfidentialityAesCbc128();
			}
			return ca;
		case SecurityConstants.CA_XRC4_40:
			// TODO: XRc4-40
			throw new IllegalArgumentException("Not yet implemented.");
		case SecurityConstants.CA_XRC4_128:
			// TODO: XRc4-128
			throw new IllegalArgumentException("Not yet implemented.");
		default:
			throw new IllegalArgumentException(
					"Invalid confidentiality algorithm.");

		}
	}

	/**
	 * Builds Cipher Suites collection from raw data received by
	 * {@link GetChannelCipherSuites} commands. Cannot be executed in
	 * {@link GetChannelCipherSuitesResponseData} since data comes in 16-byte
	 * packets and is fragmented. Supports only one integrity and one
	 * confidentiality algorithm per suite.
	 * 
	 * @param bytes
	 *            - concatenated Cipher Suite Records received by
	 *            {@link GetChannelCipherSuites} commands.
	 * @return list of Cipher Suites supported by BMC.
	 */
	public static List<CipherSuite> getCipherSuites(byte[] bytes) {
		ArrayList<CipherSuite> suites = new ArrayList<CipherSuite>();

		int offset = 0;

		while (offset < bytes.length) {
			byte id = bytes[offset + 1];
			if (bytes[offset] == TypeConverter.intToByte(0xC0)) {
				offset += 2;
			} else {
				offset += 5;
			}
			byte aa = bytes[offset];
			byte ca = -1;
			byte ia = -1;
			++offset;
			while (offset < bytes.length
					&& bytes[offset] != TypeConverter.intToByte(0xC0)
					&& bytes[offset] != TypeConverter.intToByte(0xC1)) {
				if ((TypeConverter.byteToInt(bytes[offset]) & 0xC0) == 0x80) {
					ca = TypeConverter.intToByte(TypeConverter
							.byteToInt(bytes[offset]) & 0x3f);
				} else if ((TypeConverter.byteToInt(bytes[offset]) & 0xC0) == 0x40) {
					ia = TypeConverter.intToByte(TypeConverter
							.byteToInt(bytes[offset]) & 0x3f);
				}
				++offset;
			}
			suites.add(new CipherSuite(id, aa, ca, ia));
		}

		return suites;
	}

	/**
	 * @return {@link CipherSuite} with algorithms set to
	 *         {@link AuthenticationRakpNone}, {@link ConfidentialityNone} and
	 *         {@link IntegrityNone}.
	 */
	public static CipherSuite getEmpty() {
		return new CipherSuite((byte) 0, (byte) 0, (byte) 0, (byte) 0);
	}
}
