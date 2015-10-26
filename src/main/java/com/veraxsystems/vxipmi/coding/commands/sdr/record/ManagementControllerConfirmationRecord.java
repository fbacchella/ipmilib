/*
 * ManagementController.java 
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

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * This record can be used by utility software to record that a given controller
 * has been discovered in the system. Later, the record information can be used
 * by software to confirm that the same controller is still present.
 */
public class ManagementControllerConfirmationRecord extends SensorRecord {

	private int deviceSlaveAddress;

	private int deviceId;

	private int channelNumber;

	private int deviceRevision;

	private int firmwareRevision1;

	private int firmwareRevision2;

	private int ipmiVersion;

	private int manufacturerId;

	private int productId;

	private byte[] deviceGuid;

	@Override
	protected void populateTypeSpecficValues(byte[] recordData,
			SensorRecord record) {

		setDeviceSlaveAddress(TypeConverter.byteToInt(recordData[5]) >> 1);

		setDeviceId(TypeConverter.byteToInt(recordData[6]));

		setChannelNumber((TypeConverter.byteToInt(recordData[7]) & 0xf0) >> 4);

		setDeviceRevision(TypeConverter.byteToInt(recordData[7]) & 0xf);

		setFirmwareRevision1(TypeConverter.byteToInt(recordData[8]) & 0x7f);

		setFirmwareRevision2(TypeConverter
				.littleEndianBcdByteToInt(recordData[9]));

		setIpmiVersion(TypeConverter.littleEndianBcdByteToInt(recordData[10]));

		byte[] buffer = new byte[4];

		System.arraycopy(recordData, 11, buffer, 0, 3);

		buffer[3] = 0;

		setManufacturerId(TypeConverter.littleEndianByteArrayToInt(buffer));

		System.arraycopy(recordData, 14, buffer, 0, 2);

		buffer[2] = 0;
		buffer[3] = 0;

		setProductId(TypeConverter.littleEndianByteArrayToInt(buffer));

		byte[] guid = new byte[16];

		System.arraycopy(recordData, 16, guid, 0, 16);

		setDeviceGuid(guid);
	}

	public int getDeviceSlaveAddress() {
		return deviceSlaveAddress;
	}

	public void setDeviceSlaveAddress(int deviceSlaveAddress) {
		this.deviceSlaveAddress = deviceSlaveAddress;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public int getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}

	public int getDeviceRevision() {
		return deviceRevision;
	}

	public void setDeviceRevision(int deviceRevision) {
		this.deviceRevision = deviceRevision;
	}

	public int getFirmwareRevision1() {
		return firmwareRevision1;
	}

	public void setFirmwareRevision1(int firmwareRevision1) {
		this.firmwareRevision1 = firmwareRevision1;
	}

	public int getFirmwareRevision2() {
		return firmwareRevision2;
	}

	public void setFirmwareRevision2(int firmwareRevision2) {
		this.firmwareRevision2 = firmwareRevision2;
	}

	public int getIpmiVersion() {
		return ipmiVersion;
	}

	public void setIpmiVersion(int ipmiVersion) {
		this.ipmiVersion = ipmiVersion;
	}

	public int getManufacturerId() {
		return manufacturerId;
	}

	public void setManufacturerId(int manufacturerId) {
		this.manufacturerId = manufacturerId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public byte[] getDeviceGuid() {
		return deviceGuid;
	}

	public void setDeviceGuid(byte[] deviceGuid) {
		this.deviceGuid = deviceGuid;
	}

}
