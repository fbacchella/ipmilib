/*
 * IpmiMessage.java 
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

import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityAlgorithm;

/**
 * Wrapper class for IPMI message
 */
public abstract class IpmiMessage {
    private AuthenticationType authenticationType;

    private int sessionSequenceNumber;
    
    private int sessionID;

    private byte[] authCode;

    private IpmiPayload payload;
    
    private int payloadLength;
    
    /**
     * Confidentiality Algorithm used for encryption and decryption.
     */
    private ConfidentialityAlgorithm confidentialityAlgorithm;

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setSessionSequenceNumber(int sessionSequenceNumber) {
        this.sessionSequenceNumber = sessionSequenceNumber;
    }

    public int getSessionSequenceNumber() {
        return sessionSequenceNumber;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setAuthCode(byte[] authCode) {
        this.authCode = authCode;
    }

    public byte[] getAuthCode() {
        return authCode;
    }

    /**
     * Sets {@link #payload} and {@link #payloadLength}
     * @param payload
     */
    public void setPayload(IpmiPayload payload) {
    	setPayloadLength(payload.getPayloadLength());
        this.payload = payload;
    }

    public IpmiPayload getPayload() {
        return payload;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    /**
     * @return Length of the UNENCRYPTED payload.
     */
    public int getPayloadLength() {
        return payloadLength;
    }

	public void setConfidentialityAlgorithm(ConfidentialityAlgorithm confidentialityAlgorithm) {
		this.confidentialityAlgorithm = confidentialityAlgorithm;
	}

	public ConfidentialityAlgorithm getConfidentialityAlgorithm() {
		return confidentialityAlgorithm;
	}

}
