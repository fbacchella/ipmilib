/*
 * IntegrityAlgorithm.java 
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

import java.security.InvalidKeyException;

import com.veraxsystems.vxipmi.coding.commands.session.Rakp1;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Interface for Integrity Algorithms. All classes extending this one must
 * implement constructor(byte[]).
 */
public abstract class IntegrityAlgorithm {

	protected byte[] sik;

	public IntegrityAlgorithm() {

	}

	/**
	 * Initializes Integrity Algorithm
	 * 
	 * @param sik
	 *            - Session Integrity Key calculated during the opening of the
	 *            session or user password if 'one-key' logins are enabled.
	 * @throws InvalidKeyException
	 *             - when initiation of the algorithm fails
	 */
	public void initialize(byte[] sik) throws InvalidKeyException {
		this.sik = sik;
	}

	/**
	 * Returns the algorithm's ID.
	 */
	public abstract byte getCode();

	/**
	 * Creates AuthCode field for message.
	 * 
	 * @param base
	 *            - data starting with the AuthType/Format field up to and
	 *            including the field that immediately precedes the AuthCode
	 *            field
	 * @return AuthCode field. Might be null if empty AuthCOde field is
	 *         generated.
	 * 
	 * @see Rakp1#calculateSik(com.veraxsystems.vxipmi.coding.commands.session.Rakp1ResponseData)
	 */
	public abstract byte[] generateAuthCode(byte[] base);

	/**
	 * Modifies the algorithm base since with null Auth Code during encoding
	 * Integrity Pad isn't calculated.
	 * 
	 * @param base
	 *            - integrity algorithm base without Integrity Pad.
	 * @param authCodeLength
	 *            - expected length of the Auth Code field.
	 * @return - integrity algorithm base with Integrity Pad and updated Pad
	 *         Length field.
	 */
	protected byte[] injectIntegrityPad(byte[] base, int authCodeLength) {
		int pad = 0;
		if ((base.length + authCodeLength) % 4 != 0) {
			pad = 4 - (base.length + authCodeLength) % 4;
		}

		if (pad != 0) {
			byte[] newBase = new byte[base.length + pad];

			System.arraycopy(base, 0, newBase, 0, base.length - 2);

			for (int i = base.length - 2; i < base.length - 2 + pad; ++i) {
				newBase[i] = TypeConverter.intToByte(0xff);
			}

			newBase[newBase.length - 2] = TypeConverter.intToByte(pad);

			newBase[newBase.length - 1] = base[base.length - 1];

			return newBase;
		} else {
			return base;
		}
	}
}
