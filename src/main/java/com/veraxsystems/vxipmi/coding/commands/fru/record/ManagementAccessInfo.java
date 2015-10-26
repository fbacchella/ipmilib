/*
 * ManagementAccessInfo.java 
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
 * Management Access Information record from FRU Multi Record Area
 */
public class ManagementAccessInfo extends MultiRecordInfo {

	private ManagementAccessRecordType recordType;

	private String accessInfo;

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
	public ManagementAccessInfo(byte[] fruData, int offset, int length) {
		super();
		// TODO: Test when server containing such records will be available

		recordType = ManagementAccessRecordType.parseInt(TypeConverter
				.byteToInt(fruData[offset]));

		byte[] buffer = new byte[length - 1];
		
		System.arraycopy(fruData, offset + 1, buffer, 0, length - 1);
		
		accessInfo = new String(buffer);
	}

	public ManagementAccessRecordType getRecordType() {
		return recordType;
	}

	public void setRecordType(ManagementAccessRecordType recordType) {
		this.recordType = recordType;
	}

	public String getAccessInfo() {
		return accessInfo;
	}

	public void setAccessInfo(String accessInfo) {
		this.accessInfo = accessInfo;
	}

}
