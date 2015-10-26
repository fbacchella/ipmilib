/*
 * MultiRecordInfo.java 
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
 * Record from FRU Multi Record Area
 */
public abstract class MultiRecordInfo extends FruRecord {

	public MultiRecordInfo() {
		super();
	}

	/**
	 * Creates and populates record
	 * 
	 * @param fruData
	 *            - raw data containing record
	 * @param offset
	 *            - offset to the record in the data
	 */
	public static MultiRecordInfo populateMultiRecord(byte[] fruData, int offset) {
		MultiRecordInfo recordInfo = null;

		// TODO: Test when server containing such records will be available

		if ((TypeConverter.byteToInt(fruData[offset + 1]) & 0xf) != 0x2) {
			throw new IllegalArgumentException("Invalid FRU record version");
		}

		FruMultiRecordType recordType = FruMultiRecordType
				.parseInt(TypeConverter.byteToInt(fruData[offset]));

		int length = TypeConverter.byteToInt(fruData[offset + 2]);

		offset += 5;

		switch (recordType) {
		case PowerSupplyInformation:
			recordInfo = new PowerSupplyInfo(fruData, offset);
			break;
		case DcOutput:
			recordInfo = new DcOutputInfo(fruData, offset);
			break;
		case DcLoad:
			recordInfo = new DcLoadInfo(fruData, offset);
			break;
		case ManagementAccessRecord:
			recordInfo = new ManagementAccessInfo(fruData, offset, length);
			break;
		case BaseCompatibilityRecord:
			recordInfo = new BaseCompatibilityInfo(fruData, offset, length);
			break;
		case ExtendedCompatibilityRecord:
			recordInfo = new ExtendedCompatibilityInfo(fruData, offset, length);
			break;
		case OemRecord:
			recordInfo = new OemInfo(fruData, offset, length);
			break;
		default:
			throw new IllegalArgumentException("Unsupported record type");
		}

		return recordInfo;
	}
}
