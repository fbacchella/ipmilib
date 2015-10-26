/*
 * SensorRecord.java 
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

import java.nio.charset.Charset;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Wrapper for SDR entry.
 */
public abstract class SensorRecord {

	private int id;
	private int sdrVersion;
	private byte recordType;
	private int recordLength;

	/**
	 * Parses sensor record raw data
	 * @param recordData
	 * @return {@link SensorRecord} populated with data
	 * @throws IllegalArgumentException
	 */
	public static SensorRecord populateSensorRecord(byte[] recordData)
			throws IllegalArgumentException {

		if (recordData.length < 5) {
			throw new IllegalArgumentException("Record data is too short");
		}

		SensorRecord sensorRecord = null;

		byte recType = recordData[3];

		switch (recType) {
		case RecordTypes.FULL_SENSOR_RECORD://
			sensorRecord = new FullSensorRecord();
			break;
		case RecordTypes.BMC_MESSAGE_CHANNEL_INFO_RECORD:
			//Deprecated
			throw new IllegalArgumentException("This format is deprecated");
		case RecordTypes.COMPACT_SENSOR_RECORD://
			sensorRecord = new CompactSensorRecord();
			break;
		case RecordTypes.DEVICE_RELATIVE_ENTITY_ASSOCIATION_RECORD:
			// TODO: Test if server with such records will be available
			sensorRecord = new DeviceRelativeEntityAssiciationRecord();
			break;
		case RecordTypes.ENTITY_ASSOCIATION_RECORD://
			sensorRecord = new EntityAssociationRecord();
			break;
		case RecordTypes.EVENT_ONLY_RECORD:
			// TODO: Test if server with such records will be available
			sensorRecord = new EventOnlyRecord();
			break;
		case RecordTypes.FRU_DEVICE_LOCATOR_RECORD://
			sensorRecord = new FruDeviceLocatorRecord();
			break;
		case RecordTypes.GENERIC_DEVICE_LOCATOR_RECORD:
			//TODO: Test if server with such records will be available
			sensorRecord = new GenericDeviceLocatorRecord();
			break;
		case RecordTypes.MANAGEMENT_CONTROLLER_CONFIRMATION_RECORD:
			//TODO: Test if server with such records will be available
			sensorRecord = new ManagementControllerConfirmationRecord();
			break;
		case RecordTypes.MANAGEMENT_CONTROLLER_DEVICE_LOCATOR_RECORD://
			sensorRecord = new ManagementControllerDeviceLocatorRecord();
			break;
		case RecordTypes.OEM_RECORD://
			sensorRecord = new OemRecord();
			break;
		default:
			throw new IllegalArgumentException("Invalid record type: "
					+ recType);
		}

		byte[] buffer = new byte[4];

		buffer[0] = recordData[0];
		buffer[1] = recordData[1];
		buffer[2] = 0;
		buffer[3] = 0;

		sensorRecord.setId(TypeConverter.littleEndianByteArrayToInt(buffer));

		sensorRecord.setSdrVersion(TypeConverter
				.littleEndianBcdByteToInt(recordData[2]));

		sensorRecord.setRecordType(recordData[3]);

		sensorRecord.setRecordLength(TypeConverter.byteToInt(recordData[4]));

		sensorRecord.populateTypeSpecficValues(recordData, sensorRecord);

		return sensorRecord;
	}

	/**
	 * Decodes record data which depends on record type
	 * 
	 * @param recordData
	 *            - raw data containing whole record
	 * @param record
	 *            - {@link SensorRecord} being populated
	 */
	protected abstract void populateTypeSpecficValues(byte[] recordData,
			SensorRecord record);

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setSdrVersion(int sdrVersion) {
		this.sdrVersion = sdrVersion;
	}

	public int getSdrVersion() {
		return sdrVersion;
	}

	public void setRecordType(byte recordType) {
		this.recordType = recordType;
	}

	public byte getRecordType() {
		return recordType;
	}

	public void setRecordLength(int recordLength) {
		this.recordLength = recordLength;
	}

	public int getRecordLength() {
		return recordLength;
	}

	/**
	 * Decodes name encoded in one of the available formats.
	 * 
	 * @param codingType
	 *            - byte containing Sensor 'ID' String Type
	 * @param name
	 *            - Sensor ID String bytes
	 * @return decoded name
	 */
	protected String decodeName(byte codingType, byte[] name) {
		switch ((TypeConverter.byteToInt(codingType) & 0xc0) >> 6) {
		case 0: // unicode
			return new String(name, Charset.forName("UTF-8"));
		case 1: // BCD plus
			return TypeConverter.decodeBcdPlus(name);
		case 2: // 6-bit packed ASCII
			return TypeConverter.decode6bitAscii(name);
		case 3: // 8-bit ASCII + Latin 1
			return new String(name, Charset.forName("ISO-8859-1"));
		default:
			throw new IllegalArgumentException("Invalid coding type.");
		}
	}
}
