/*
 * GenericDeviceLocatorRecord.java 
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
 * This record is used to store the location and type information for devices on
 * the IPMB or management controller private busses that are neither IPMI FRU
 * devices nor IPMI management controllers.
 */
public class GenericDeviceLocatorRecord extends SensorRecord {

	private int deviceAccessAddress;

	private int deviceSlaveAddress;

	private int channelNumber;

	private int accessLun;

	private int busId;

	private int addressSpan;

	private DeviceType deviceType;

	private int deviceTypeModifier;

	private int entityId;

	private int entityInstance;

	private String name;

	@Override
	protected void populateTypeSpecficValues(byte[] recordData,
			SensorRecord record) {

		setDeviceAccessAddress((TypeConverter.byteToInt(recordData[5]) & 0xfe) >> 1);

		setDeviceSlaveAddress((TypeConverter.byteToInt(recordData[6]) & 0xfe) >> 1);

		setChannelNumber(((TypeConverter.byteToInt(recordData[6]) & 0x1) << 3)
				| ((TypeConverter.byteToInt(recordData[7]) & 0xe0) >> 5));

		setAccessLun((TypeConverter.byteToInt(recordData[7]) & 0x18) >> 3);

		setBusId(TypeConverter.byteToInt(recordData[7]) & 0x3);
		
		setAddressSpan(TypeConverter.byteToInt(recordData[8]) & 0x3);
		
		setDeviceType(DeviceType.parseInt(TypeConverter.byteToInt(recordData[10])));
		setDeviceTypeModifier(TypeConverter.byteToInt(recordData[11]));
		
		setEntityId(TypeConverter.byteToInt(recordData[12]));
		setEntityInstance(TypeConverter.byteToInt(recordData[13]));

		
		byte[] name = new byte[recordData.length - 17];
		
		System.arraycopy(recordData, 17, name, 0, name.length);
		
		setName(decodeName(recordData[16], name));		
	}

	public int getDeviceAccessAddress() {
		return deviceAccessAddress;
	}

	public void setDeviceAccessAddress(int deviceAccessAddress) {
		this.deviceAccessAddress = deviceAccessAddress;
	}

	public int getDeviceSlaveAddress() {
		return deviceSlaveAddress;
	}

	public void setDeviceSlaveAddress(int deviceSlaveAddress) {
		this.deviceSlaveAddress = deviceSlaveAddress;
	}

	public int getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}

	public int getAccessLun() {
		return accessLun;
	}

	public void setAccessLun(int accessLun) {
		this.accessLun = accessLun;
	}

	public int getBusId() {
		return busId;
	}

	public void setBusId(int busId) {
		this.busId = busId;
	}

	public int getAddressSpan() {
		return addressSpan;
	}

	public void setAddressSpan(int addressSpan) {
		this.addressSpan = addressSpan;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public int getDeviceTypeModifier() {
		return deviceTypeModifier;
	}

	public void setDeviceTypeModifier(int deviceTypeModifier) {
		this.deviceTypeModifier = deviceTypeModifier;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public int getEntityInstance() {
		return entityInstance;
	}

	public void setEntityInstance(int entityInstance) {
		this.entityInstance = entityInstance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
