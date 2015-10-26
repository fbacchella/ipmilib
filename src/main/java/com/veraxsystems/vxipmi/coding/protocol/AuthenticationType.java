/*
 * AuthenticationType.java 
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
 * Available types of authentication. For IPMI v2.0 format RMCPPlus should be
 * used.
 */
public enum AuthenticationType {
	None(AuthenticationType.NONE), Md2(AuthenticationType.MD2), Md5(
			AuthenticationType.MD5), Simple(AuthenticationType.SIMPLE), Oem(
			AuthenticationType.OEM), RMCPPlus(AuthenticationType.RMCPPLUS), ;
	
	private static final int NONE = 0;
	private static final int MD2 = 1;
	private static final int MD5 = 2;
	private static final int SIMPLE = 4;
	private static final int OEM = 5;
	private static final int RMCPPLUS = 6;

	private int code;

	AuthenticationType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static AuthenticationType parseInt(int value) {
		switch (value) {
		case NONE:
			return None;
		case MD2:
			return Md2;
		case MD5:
			return Md5;
		case SIMPLE:
			return Simple;
		case OEM:
			return Oem;
		case RMCPPLUS:
			return RMCPPlus;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}