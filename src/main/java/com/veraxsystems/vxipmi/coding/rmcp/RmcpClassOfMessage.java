/*
 * RmcpClassOfMessage.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.rmcp;

/**
 * Types of RMCP messages.
 */
public enum RmcpClassOfMessage {
	/**
	 * ASF ACK Class of Message
	 */
	Ack(RmcpClassOfMessage.ACK), Asf(RmcpClassOfMessage.ASF),
	/**
	 * OEM-defined Class of Message
	 */
	Oem(RmcpClassOfMessage.OEM), Ipmi(RmcpClassOfMessage.IPMI), ;
	
	private static final int ACK = 134;
	private static final int ASF = 6;
	private static final int OEM = 8;
	private static final int IPMI = 7;

	private int code;

	RmcpClassOfMessage(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static RmcpClassOfMessage parseInt(int value) {
		switch (value) {
		case ACK:
			return Ack;
		case ASF:
			return Asf;
		case OEM:
			return Oem;
		case IPMI:
			return Ipmi;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}