/*
 * RmcpMessage.java 
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

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * A wrapper class for RMCP message.
 */
public class RmcpMessage {
    private RmcpVersion version;
	private byte sequenceNumber;
	private RmcpClassOfMessage classOfMessage;
	private byte[] data;
	
	public RmcpMessage() {
	    setSequenceNumber(0xff);
	}

	public void setVersion(RmcpVersion version) {
		this.version = version;
	}

	public RmcpVersion getVersion() {
		return version;
	}

	/**
	 * Set RMCP sequence number. Must be 0-254 if ACK is desired, 255 if no ACK is desired.
	 * @param sequenceNumber
	 */
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = TypeConverter.intToByte(sequenceNumber);
	}

	public byte getSequenceNumber() {
		return sequenceNumber;
	}
	
	public int getIntSequenceNumber() {
        return TypeConverter.byteToInt(sequenceNumber);
    }

	public void setClassOfMessage(RmcpClassOfMessage classOfMessage) {
		this.classOfMessage = classOfMessage;
	}

	public RmcpClassOfMessage getClassOfMessage() {
		return classOfMessage;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}
}
