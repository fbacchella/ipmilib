/*
 * ChassisInfo.java 
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

import java.util.ArrayList;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * FRU record containing Chassis info.<br>
 * This area is used to hold Serial Number, Part Number, and other information
 * about the system chassis.
 */
public class ChassisInfo extends FruRecord {

	private ChassisType chassisType;

	private String chassisPartNumber = "";

	private String chassisSerialNumber = "";

	private String[] customChassisInfo = new String[0];

	/**
	 * Creates and populates record
	 * 
	 * @param fruData
	 *            - raw data containing record
	 * @param offset
	 *            - offset to the record in the data
	 */
	public ChassisInfo(byte[] fruData, int offset) {
		super();

		if (fruData[offset] != 0x1) {
			throw new IllegalArgumentException("Invalid format version");
		}

		chassisType = ChassisType.parseInt(TypeConverter
				.byteToInt(fruData[offset + 2]));

		int partNumber = TypeConverter.byteToInt(fruData[offset + 3]);

		offset += 4;

		int index = 0;

		ArrayList<String> customInfo = new ArrayList<String>();

		while (partNumber != 0xc1 && offset < fruData.length) {

			int partType = (partNumber & 0xc0) >> 6;

			int partDataLength = (partNumber & 0x3f);

			if (partDataLength > 0 && partDataLength + offset < fruData.length) {

				byte[] partNumberData = new byte[partDataLength];

				System.arraycopy(fruData, offset, partNumberData, 0,
						partDataLength);

				offset += partDataLength;

				switch (index) {
				case 0:
					setChassisPartNumber(FruRecord.decodeString(partType,
							partNumberData, true));
					break;
				case 1:
					setChassisSerialNumber(FruRecord.decodeString(partType,
							partNumberData, true));
					break;
				default:
					if (partDataLength == 0) {
						continue;
					}
					customInfo.add(FruRecord.decodeString(partType,
							partNumberData, true));
					break;
				}
			}

			partNumber = TypeConverter.byteToInt(fruData[offset]);

			++offset;

			++index;
		}

		customChassisInfo = new String[customInfo.size()];
		customChassisInfo = customInfo.toArray(customChassisInfo);
	}

	public ChassisType getChassisType() {
		return chassisType;
	}

	public void setChassisType(ChassisType chassisType) {
		this.chassisType = chassisType;
	}

	public String getChassisPartNumber() {
		return chassisPartNumber;
	}

	public void setChassisPartNumber(String chassisPartNumber) {
		this.chassisPartNumber = chassisPartNumber;
	}

	public String getChassisSerialNumber() {
		return chassisSerialNumber;
	}

	public void setChassisSerialNumber(String chassisSerialNumber) {
		this.chassisSerialNumber = chassisSerialNumber;
	}

	public String[] getCustomChassisInfo() {
		return customChassisInfo;
	}

}
