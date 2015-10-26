/*
 * GetSensorReadingResponseData.java 
 * Created on 2011-08-09
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sdr;

import java.util.ArrayList;
import java.util.List;

import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.CompactSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.EventOnlyRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.ReadingType;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.FullSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.SensorType;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Wrapper for Get Sensor Reading response.
 */
public class GetSensorReadingResponseData implements ResponseData {
	private byte sensorReading;

	/**
	 * This bit is set to indicate that a 're-arm' or 'Set Event Receiver'
	 * command has been used to request an update of the sensor status, and that
	 * update has not occurred yet. Software should use this bit to avoid
	 * getting an incorrect status while the first sensor update is in progress.
	 */
	private boolean sensorStateValid;

	/**
	 * Contains state of the sensor if it is threshold-based.
	 */
	private SensorState sensorState;

	/**
	 * Contains state of the sensor if it is discrete.
	 */
	private boolean[] statesAsserted;

	public double getSensorReading(FullSensorRecord sensorRecord) {
		return sensorRecord.calcFormula(TypeConverter.byteToInt(sensorReading));
	}

	public double getPlainSensorReading() {
		return TypeConverter.byteToInt(sensorReading);
	}

	public void setSensorReading(byte sensorReading) {
		this.sensorReading = sensorReading;
	}

	public boolean isSensorStateValid() {
		return sensorStateValid;
	}

	public void setSensorStateValid(boolean sensorStateValid) {
		this.sensorStateValid = sensorStateValid;
	}

	/**
	 * Contains state of the sensor if it is threshold-based.
	 */
	public SensorState getSensorState() {
		return sensorState;
	}

	public void setSensorState(SensorState sensorState) {
		this.sensorState = sensorState;
	}

	/**
	 * Contains state of the sensor if it is discrete.
	 * 
	 * @param sensorEventReadingType
	 *            - value received via
	 *            {@link FullSensorRecord#getEventReadingType()},
	 *            {@link CompactSensorRecord#getEventReadingType()} or
	 *            {@link EventOnlyRecord#getEventReadingType()}
	 */
	public List<ReadingType> getStatesAsserted(SensorType sensorType,
			int sensorEventReadingType) {
		ArrayList<ReadingType> list = new ArrayList<ReadingType>();
		for (int i = 0; i < statesAsserted.length; ++i) {
			if (statesAsserted[i]) {
				list.add(ReadingType.parseInt(sensorType,
						sensorEventReadingType, i));
			}
		}
		return list;
	}

	public void setStatesAsserted(boolean[] statesAsserted) {
		this.statesAsserted = statesAsserted;
	}
}
