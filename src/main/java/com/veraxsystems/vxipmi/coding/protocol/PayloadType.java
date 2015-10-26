/*
 * PayloadType.java 
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

/**
 * Types of IPMI packet payload
 */
public enum PayloadType {
	/**
	 * IPMI packet
	 */
	Ipmi(PayloadType.IPMI),
	/**
	 * Serial over LAN packet
	 */
	Sol(PayloadType.SOL),
	/**
	 * OEM Explicit
	 */
	Oem(PayloadType.OEM), RmcpOpenSessionRequest(
			PayloadType.RMCPOPENSESSIONREQUEST), RmcpOpenSessionResponse(
			PayloadType.RMCPOPENSESSIONRESPONSE),
	/**
	 * RAKP Message 1
	 */
	Rakp1(PayloadType.RAKP1),
	/**
	 * RAKP Message 2
	 */
	Rakp2(PayloadType.RAKP2),
	/**
	 * RAKP Message 3
	 */
	Rakp3(PayloadType.RAKP3),
	/**
	 * RAKP Message 4
	 */
	Rakp4(PayloadType.RAKP4), Oem0(PayloadType.OEM0), Oem1(PayloadType.OEM1), Oem2(
			PayloadType.OEM2), Oem3(PayloadType.OEM3), Oem4(PayloadType.OEM4), Oem5(
			PayloadType.OEM5), Oem6(PayloadType.OEM6), Oem7(PayloadType.OEM7), ;
	private static final int IPMI = 0;
	private static final int SOL = 1;
	private static final int OEM = 2;
	private static final int RMCPOPENSESSIONREQUEST = 16;
	private static final int RMCPOPENSESSIONRESPONSE = 17;
	private static final int RAKP1 = 18;
	private static final int RAKP2 = 19;
	private static final int RAKP3 = 20;
	private static final int RAKP4 = 21;
	private static final int OEM0 = 32;
	private static final int OEM1 = 33;
	private static final int OEM2 = 34;
	private static final int OEM3 = 35;
	private static final int OEM4 = 36;
	private static final int OEM5 = 37;
	private static final int OEM6 = 38;
	private static final int OEM7 = 39;

	private int code;

	PayloadType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static PayloadType parseInt(int value) {
		switch (value) {
		case IPMI:
			return Ipmi;
		case SOL:
			return Sol;
		case OEM:
			return Oem;
		case RMCPOPENSESSIONREQUEST:
			return RmcpOpenSessionRequest;
		case RMCPOPENSESSIONRESPONSE:
			return RmcpOpenSessionResponse;
		case RAKP1:
			return Rakp1;
		case RAKP2:
			return Rakp2;
		case RAKP3:
			return Rakp3;
		case RAKP4:
			return Rakp4;
		case OEM0:
			return Oem0;
		case OEM1:
			return Oem1;
		case OEM2:
			return Oem2;
		case OEM3:
			return Oem3;
		case OEM4:
			return Oem4;
		case OEM5:
			return Oem5;
		case OEM6:
			return Oem6;
		case OEM7:
			return Oem7;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}