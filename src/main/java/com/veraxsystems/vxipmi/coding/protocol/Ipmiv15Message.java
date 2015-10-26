/*
 * Ipmiv15Message.java 
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

import com.veraxsystems.vxipmi.coding.security.ConfidentialityNone;

/**
 * Wrapper for IPMI v.1.5 message
 */
public class Ipmiv15Message extends IpmiMessage {

	public Ipmiv15Message() {
		setConfidentialityAlgorithm(new ConfidentialityNone());
	}

	@Override
	public void setAuthenticationType(AuthenticationType authenticationType) {
		if (authenticationType == AuthenticationType.RMCPPlus) {
			throw new IllegalArgumentException(
					"IPMIv1.5 does not support RMCP+");
		}
		super.setAuthenticationType(authenticationType);
	}
}
