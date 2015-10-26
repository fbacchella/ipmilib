/*
 * Ipmiv20Message.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.protocol;

import java.security.InvalidKeyException;

import com.veraxsystems.vxipmi.coding.protocol.encoder.IpmiEncoder;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityAlgorithm;

/**
 * Wrapper class for IPMI v2.0 message
 */
public class Ipmiv20Message extends IpmiMessage {
	private boolean payloadEncrypted;

	private boolean payloadAuthenticated;

	private PayloadType payloadType;

	private int oemIANA;

	private Object oemPayloadID;

	public void setPayloadEncrypted(boolean payloadEncrypted) {
		this.payloadEncrypted = payloadEncrypted;
	}

	public boolean isPayloadEncrypted() {
		return payloadEncrypted;
	}

	public void setPayloadAuthenticated(boolean payloadAuthenticated) {
		this.payloadAuthenticated = payloadAuthenticated;
	}

	public boolean isPayloadAuthenticated() {
		return payloadAuthenticated;
	}

	public void setPayloadType(PayloadType payloadType) {
		this.payloadType = payloadType;
	}

	public PayloadType getPayloadType() {
		return payloadType;
	}

	public void setOemIANA(int oemIANA) {
		this.oemIANA = oemIANA;
	}

	public int getOemIANA() {
		return oemIANA;
	}

	public void setOemPayloadID(Object oemPayloadID) {
		this.oemPayloadID = oemPayloadID;
	}

	public Object getOemPayloadID() {
		return oemPayloadID;
	}
	
	public Ipmiv20Message(ConfidentialityAlgorithm confidentialityAlgorithm) {
		setConfidentialityAlgorithm(confidentialityAlgorithm);
	}

	/**
	 * Gets base for integrity algorithm calculations. If used for the message
	 * being created, does not contain AuthCode field so amount of Integrity Pad
	 * bytes cannot be calculated. Therefore integrity algorithms using this
	 * base must add proper amount of the Integrity Pad bytes and modify the Pad
	 * Length byte.
	 * 
	 * @param encoder
	 *            - {@link IpmiEncoder} to be used to convert message to byte
	 *            array format. Must be able to handle null authCode field with
	 *            {@link #isPayloadAuthenticated()} == true since this method is
	 *            used in its generation.
	 * @return base for integrity algorithm calculations
	 * @throws InvalidKeyException 
	 *             - when initiation of the confidentiality algorithm fails
	 */
	public byte[] getIntegrityAlgorithmBase(IpmiEncoder encoder) throws InvalidKeyException {
		return encoder.encode(this);
	}
}
