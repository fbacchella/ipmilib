/*
 * IpmiPayload.java 
 * Created on 2011-08-02
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload;

import java.security.InvalidKeyException;

import com.veraxsystems.vxipmi.coding.security.ConfidentialityAlgorithm;

/**
 * Payload for IPMI messages
 */
public abstract class IpmiPayload {

	private byte[] data;

	private byte[] encryptedPayload;

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	/**
	 * Returns encrypted payload encoded in byte array.
	 * 
	 * Migth be null if payload was not yet encrypted.
	 * 
	 * @see #encryptPayload(ConfidentialityAlgorithm)
	 */
	public byte[] getEncryptedPayload() {
		return encryptedPayload;
	}

	/**
	 * Returns unencrypted payload encoded in byte array (owner is responsible
	 * for encryption).
	 * 
	 * @return payload
	 */
	public abstract byte[] getPayloadData();

	/**
	 * Returns encoded but UNENCRYPTED payload length.
	 */
	public abstract int getPayloadLength();

	/**
	 * Returns IPMI command encapsulated in IPMI Payload.
	 */
	public abstract byte[] getIpmiCommandData();

	/**
	 * Encrypts {@link #getPayloadData()}.
	 * 
	 * @param confidentialityAlgorithm
	 *            {@link ConfidentialityAlgorithm} to be used to encrypt payload
	 *            data.
	 * @throws InvalidKeyException
	 *             - when confidentiality algorithm fails.
	 * @see IpmiPayload#getEncryptedPayload()
	 */
	public void encryptPayload(ConfidentialityAlgorithm confidentialityAlgorithm)
			throws InvalidKeyException {
		encryptedPayload = confidentialityAlgorithm.encrypt(getPayloadData());
	}
}
