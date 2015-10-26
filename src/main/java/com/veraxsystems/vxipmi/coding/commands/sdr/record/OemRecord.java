/*
 * OemRecord.java 
 * Created on 2011-08-04
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sdr.record;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * OEM specific record.
 */
public class OemRecord extends SensorRecord {

	private int manufacturerId;
	
	private byte[] oemData;
	
	@Override
	protected void populateTypeSpecficValues(byte[] recordData,
			SensorRecord record) {
		
		byte[] buffer = new byte[4];
		
		System.arraycopy(recordData, 5, buffer, 0, 3);
		
		buffer[3] = 0;
		
		setManufacturerId(TypeConverter.littleEndianByteArrayToInt(buffer));
		
		byte[] data = new byte[recordData.length - 8];
		
		System.arraycopy(recordData, 8, data, 0, data.length);
		
		setOemData(data);		
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
