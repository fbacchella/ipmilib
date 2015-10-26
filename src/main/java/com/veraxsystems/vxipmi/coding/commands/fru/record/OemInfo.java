/*
 * OemInfo.java 
 * Created on 2011-08-16
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.fru.record;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * OEM record from FRU Multi Record Area
 */
public class OemInfo extends MultiRecordInfo {

	private int manufacturerId;
	
	private byte[] oemData;	
	
	/**
	 * Creates and populates record
	 * 
	 * @param fruData
	 *            - raw data containing record
	 * @param offset
	 *            - offset to the record in the data
	 * @param length
	 *            - length of the record
	 */
	public OemInfo(byte[] fruData, int offset, int length) {
		super();
		// TODO: Test when server containing such records will be available
		
		byte[] buffer = new byte[4];

		System.arraycopy(fruData, offset, buffer, 0, 3);
		buffer[3] = 0;

		manufacturerId = TypeConverter.littleEndianByteArrayToInt(buffer);
		
		oemData = new byte[length - 3];
		
		System.arraycopy(fruData, offset+3, oemData, 0, length-3);

	}

	public int getManufacturerId() {
		return manufacturerId;
	}

	public void setManufacturerId(int manufacturerId) {
		this.manufacturerId = manufacturerId;
	}

	public byte[] getOemData() {
		return oemData;
	}

	public void setOemData(byte[] oemData) {
		this.oemData = oemData;
	}

}
