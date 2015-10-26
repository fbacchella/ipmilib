/*
 * PlainMassage.java 
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

/**
 * Represents IPMI payload fully specified by user - contains wrapped byte array
 * which is returned as a payload via {@link #getPayloadData()}. Used for
 * OpenSession and RAKP messages.
 */
public class PlainMessage extends IpmiPayload {

	@Override
	public byte[] getPayloadData() {
		return getData();
	}

	@Override
	public int getPayloadLength() {
		return getData().length;
	}
	
	/**
	 * Creates IPMI payload.
	 * @param data
	 * - byte array containing payload for IPMI message.
	 */
	public PlainMessage(byte[] data) {
		setData(data);
	}

	@Override
	public byte[] getIpmiCommandData() {
		return getData();
	}

}
