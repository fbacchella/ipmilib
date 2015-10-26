/*
 * GetSdrResponseData.java 
 * Created on 2011-08-03
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sdr;

import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.SensorRecord;

/**
 * Wrapper for Get SDR command response.
 */
public class GetSdrResponseData implements ResponseData {

	/**
	 * ID of the next record in the repository.
	 */
	private int nextRecordId;

	/**
	 * Sensor record data
	 */
	private byte[] sensorRecordData;

	public void setNextRecordId(int nextRecordId) {
		this.nextRecordId = nextRecordId;
	}

	public int getNextRecordId() {
		return nextRecordId;
	}

	public void setSensorRecordData(byte[] sensorRecordData) {
		this.sensorRecordData = sensorRecordData;
	}

	/**
	 * @return Unparsed sensor record data. Might contain only part of the
	 *         record, depending on offset and size specified in the request. To
	 *         parse data use {@link SensorRecord#populateSensorRecord(byte[])}.
	 */
	public byte[] getSensorRecordData() {
		return sensorRecordData;
	}
}
