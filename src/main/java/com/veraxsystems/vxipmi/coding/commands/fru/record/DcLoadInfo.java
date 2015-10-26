/*
 * DcLoadInfo.java 
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
 * DC output Information record from FRU Multi Record Area<br>
 * This record is used to describe the maximum load that a device requires from
 * a particular DC Output.
 */
public class DcLoadInfo extends MultiRecordInfo {

	private int outputNumber;

	/**
	 * The unit is 10mV.
	 */
	private int nominalVoltage;

	/**
	 * The unit is 10mV.
	 */
	private int minimumVoltage;

	/**
	 * The unit is 10mV.
	 */
	private int maximumVoltage;

	/**
	 * The unit is 10mA.
	 */
	private int minimumCurrentLoad;
	
	/**
	 * The unit is 10mA.
	 */
	private int maximumCurrentLoad;

	/**
	 * Creates and populates record
	 * 
	 * @param fruData
	 *            - raw data containing record
	 * @param offset
	 *            - offset to the record in the data
	 */
	public DcLoadInfo(byte[] fruData, int offset) {
		super();
		// TODO: Test when server containing such records will be available
		
		outputNumber = TypeConverter.byteToInt(fruData[offset]) & 0xf;
		
		nominalVoltage = TypeConverter.byteToInt(fruData[offset+1]);
		nominalVoltage |= TypeConverter.byteToInt(fruData[offset+2]) << 8;		
		nominalVoltage = TypeConverter.decode2sComplement(nominalVoltage, 15);
		
		minimumVoltage = TypeConverter.byteToInt(fruData[offset+3]);
		minimumVoltage |= TypeConverter.byteToInt(fruData[offset+4]) << 8;		
		minimumVoltage = TypeConverter.decode2sComplement(minimumVoltage, 15);
		
		maximumVoltage = TypeConverter.byteToInt(fruData[offset+5]);
		maximumVoltage |= TypeConverter.byteToInt(fruData[offset+6]) << 8;		
		maximumVoltage = TypeConverter.decode2sComplement(maximumVoltage, 15);
		
		minimumCurrentLoad = TypeConverter.byteToInt(fruData[offset+9]);
		minimumCurrentLoad |= TypeConverter.byteToInt(fruData[offset+10]) << 8;
		
		maximumCurrentLoad = TypeConverter.byteToInt(fruData[offset+11]);
		maximumCurrentLoad |= TypeConverter.byteToInt(fruData[offset+12]) << 8;	
	}

	public int getOutputNumber() {
		return outputNumber;
	}

	public void setOutputNumber(int outputNumber) {
		this.outputNumber = outputNumber;
	}

	public int getNominalVoltage() {
		return nominalVoltage;
	}

	public void setNominalVoltage(int nominalVoltage) {
		this.nominalVoltage = nominalVoltage;
	}

	public int getMaximumVoltage() {
		return maximumVoltage;
	}

	public void setMaximumVoltage(int maximumVoltage) {
		this.maximumVoltage = maximumVoltage;
	}

	public int getMinimumVoltage() {
		return minimumVoltage;
	}

	public void setMinimumVoltage(int minimumVoltage) {
		this.minimumVoltage = minimumVoltage;
	}

	public int getMaximumCurrentLoad() {
		return maximumCurrentLoad;
	}

	public void setMaximumCurrentLoad(int maximumCurrentLoad) {
		this.maximumCurrentLoad = maximumCurrentLoad;
	}

	public int getMinimumCurrentLoad() {
		return minimumCurrentLoad;
	}

	public void setMinimumCurrentLoad(int minimumCurrentLoad) {
		this.minimumCurrentLoad = minimumCurrentLoad;
	}

}
