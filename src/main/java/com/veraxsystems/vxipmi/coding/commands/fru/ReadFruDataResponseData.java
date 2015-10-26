/*
 * ReadFruDataResponseData.java 
 * Created on 2011-08-11
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.fru;

import com.veraxsystems.vxipmi.coding.commands.ResponseData;

/**
 * Wrapper for Read FRU Data response. Contains only raw data, because size of
 * the FRU Inventory Area might exceed size of the communication packet so it
 * might come in many {@link ReadFruDataResponseData} packets and it must be
 * decoded by {@link ReadFruData#decodeFruData(java.util.List)}.
 */
public class ReadFruDataResponseData implements ResponseData {
	private byte[] fruData;

	public void setFruData(byte[] fruData) {
		this.fruData = fruData;
	}

	public byte[] getFruData() {
		return fruData;
	}
}
