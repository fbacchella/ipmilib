/*
 * ExtendedCompatibilityInfo.java 
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

/**
 * Extended Compatibility Information record from FRU Multi Record Area<br>
 * It seems to be the same as {@link BaseCompatibilityInfo} except of
 * {@link ManagementAccessRecordType} code.
 */
public class ExtendedCompatibilityInfo extends BaseCompatibilityInfo {

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
	public ExtendedCompatibilityInfo(byte[] fruData, int offset, int length) {
		super(fruData, offset, length);
		// TODO: Test when server containing such records will be available
	}

}
