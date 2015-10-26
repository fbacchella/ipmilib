/*
 * PowerSupplyInfo.java 
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
 * Power Supply Information record from FRU Multi Record Area
 */
public class PowerSupplyInfo extends MultiRecordInfo {

	/**
	 * Overall Capacity in Watts
	 */
	private int capacity;

	/**
	 * The highest instantaneous VA value that this supply draws during
	 * operation.
	 */
	private int peakVa;

	/**
	 * Maximum inrush of current, in Amps, into the power supply.
	 */
	private int maximumInrush;

	/**
	 * This specifies the low end of acceptable voltage into the power supply.
	 * The units are 10mV.
	 */
	private int lowEndInputVoltage1;

	/**
	 * This specifies the high end of acceptable voltage into the power supply.
	 * The units are 10mV.
	 */
	private int highEndInputVoltage1;

	/**
	 * This specifies the low end of acceptable voltage into the power supply.
	 * This field would be used if the power supply did not support autoswitch.
	 * Range 1 would define the 110V range, while range 2 would be used for
	 * 220V. The units are 10mV.
	 */
	private int lowEndInputVoltage2;

	/**
	 * This specifies the high end of acceptable voltage into the power supply.
	 * This field would be used if the power supply did not support autoswitch.
	 * Range 1 would define the 110V range, while range 2 would be used for
	 * 220V. The units are 10mV.
	 */
	private int highEndInputVoltage2;

	/**
	 * This specifies the low end of acceptable frequency range into the power
	 * supply.
	 */
	private int lowEndInputFrequencyRange;

	/**
	 * This specifies the high end of acceptable frequency range into the power
	 * supply.
	 */
	private int highEndInputFrequencyRange;

	/**
	 * Creates and populates record
	 * 
	 * @param fruData
	 *            - raw data containing record
	 * @param offset
	 *            - offset to the record in the data
	 */
	public PowerSupplyInfo(byte[] fruData, int offset) {
		super();

		// TODO: Test when server containing such records will be available

		capacity = TypeConverter.byteToInt(fruData[offset]) & 0xf;
		capacity |= TypeConverter.byteToInt(fruData[offset + 1]) << 4;

		peakVa = TypeConverter.byteToInt(fruData[offset + 2]);
		peakVa |= TypeConverter.byteToInt(fruData[offset + 3]) << 8;

		if (peakVa == 0xffff) {
			peakVa = -1;
		}

		maximumInrush = TypeConverter.byteToInt(fruData[offset + 4]);

		lowEndInputVoltage1 = TypeConverter.byteToInt(fruData[offset + 6]);
		lowEndInputVoltage1 |= TypeConverter.byteToInt(fruData[offset + 7]) << 8;

		highEndInputVoltage1 = TypeConverter.byteToInt(fruData[offset + 8]);
		highEndInputVoltage1 |= TypeConverter.byteToInt(fruData[offset + 9]) << 8;

		lowEndInputVoltage2 = TypeConverter.byteToInt(fruData[offset + 10]);
		lowEndInputVoltage2 |= TypeConverter.byteToInt(fruData[offset + 11]) << 8;

		highEndInputVoltage2 = TypeConverter.byteToInt(fruData[offset + 12]);
		highEndInputVoltage2 |= TypeConverter.byteToInt(fruData[offset + 13]) << 8;

		lowEndInputFrequencyRange = TypeConverter
				.byteToInt(fruData[offset + 14]);
		highEndInputFrequencyRange = TypeConverter
				.byteToInt(fruData[offset + 15]);
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getPeakVa() {
		return peakVa;
	}

	public void setPeakVa(int peakVa) {
		this.peakVa = peakVa;
	}

	public int getMaximumInrush() {
		return maximumInrush;
	}

	public void setMaximumInrush(int maximumInrush) {
		this.maximumInrush = maximumInrush;
	}

	public int getLowEndInputVoltage1() {
		return lowEndInputVoltage1;
	}

	public void setLowEndInputVoltage1(int lowEndInputVoltage1) {
		this.lowEndInputVoltage1 = lowEndInputVoltage1;
	}

	public int getHighEndInputVoltage1() {
		return highEndInputVoltage1;
	}

	public void setHighEndInputVoltage1(int highEndInputVoltage1) {
		this.highEndInputVoltage1 = highEndInputVoltage1;
	}

	public int getLowEndInputVoltage2() {
		return lowEndInputVoltage2;
	}

	public void setLowEndInputVoltage2(int lowEndInputVoltage2) {
		this.lowEndInputVoltage2 = lowEndInputVoltage2;
	}

	public int getHighEndInputVoltage2() {
		return highEndInputVoltage2;
	}

	public void setHighEndInputVoltage2(int highEndInputVoltage2) {
		this.highEndInputVoltage2 = highEndInputVoltage2;
	}

	public int getLowEndInputFrequencyRange() {
		return lowEndInputFrequencyRange;
	}

	public void setLowEndInputFrequencyRange(int lowEndInputFrequencyRange) {
		this.lowEndInputFrequencyRange = lowEndInputFrequencyRange;
	}

	public int getHighEndInputFrequencyRange() {
		return highEndInputFrequencyRange;
	}

	public void setHighEndInputFrequencyRange(int highEndInputFrequencyRange) {
		this.highEndInputFrequencyRange = highEndInputFrequencyRange;
	}

}
