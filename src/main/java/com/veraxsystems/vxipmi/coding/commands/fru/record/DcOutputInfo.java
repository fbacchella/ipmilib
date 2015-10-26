/*
 * DcOutputInfo.java 
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
 * DC output Information record from FRU Multi Record Area
 */
public class DcOutputInfo extends MultiRecordInfo {
	
	private int outputNumber;
	
	/**
	 * The unit is 10mV.
	 */
	private int nominalVoltage;
	
	/**
	 * The unit is 10mV.
	 */
	private int maximumNegativeDeviation;
	
	/**
	 * The unit is 10mV.
	 */
	private int maximumPositiveDeviation;
	
	/**
	 * Creates and populates record
	 * 
	 * @param fruData
	 *            - raw data containing record
	 * @param offset
	 *            - offset to the record in the data
	 */
	public DcOutputInfo(byte[] fruData, int offset) {
		super();
		// TODO: Test when server containing such records will be available

		outputNumber = TypeConverter.byteToInt(fruData[offset]) & 0xf;
		
		nominalVoltage = TypeConverter.byteToInt(fruData[offset+1]);
		nominalVoltage |= TypeConverter.byteToInt(fruData[offset+2]) << 8;		
		nominalVoltage = TypeConverter.decode2sComplement(nominalVoltage, 15);
		
		maximumNegativeDeviation = TypeConverter.byteToInt(fruData[offset+3]);
		maximumNegativeDeviation |= TypeConverter.byteToInt(fruData[offset+4]) << 8;		
		maximumNegativeDeviation = TypeConverter.decode2sComplement(maximumNegativeDeviation, 15);
		
		maximumPositiveDeviation = TypeConverter.byteToInt(fruData[offset+5]);
		maximumPositiveDeviation |= TypeConverter.byteToInt(fruData[offset+6]) << 8;		
		maximumPositiveDeviation = TypeConverter.decode2sComplement(maximumPositiveDeviation, 15);
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

	public int getMaximumNegativeDeviation() {
		return maximumNegativeDeviation;
	}

	public void setMaximumNegativeDeviation(int maximumNegativeDeviation) {
		this.maximumNegativeDeviation = maximumNegativeDeviation;
	}

	public int getMaximumPositiveDeviation() {
		return maximumPositiveDeviation;
	}

	public void setMaximumPositiveDeviation(int maximumPositiveDeviation) {
		this.maximumPositiveDeviation = maximumPositiveDeviation;
	}

}
