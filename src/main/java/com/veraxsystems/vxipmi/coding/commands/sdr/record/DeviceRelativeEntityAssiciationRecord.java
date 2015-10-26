/*
 * DeviceRelativeEntityAssiciationRecord.java 
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
 * This record is the same as the {@link EntityAssociationRecord}, except that
 * it supports describing associations between entities that have
 * device-relative Entity Instance values as well as system-relative values.
 */
public class DeviceRelativeEntityAssiciationRecord extends SensorRecord {

	private int containerEntityId;

	private int containerEntityInstance;

	private int containerEntityDeviceAddress;

	private int containerEntityDeviceChannel;

	/**
	 * false - contained entities specified as list <br>
	 * true - contained entities specified as range
	 */
	private boolean entitiesAsRange;

	private boolean recordLink;

	private int entityDeviceAddress1;

	private int entityDeviceChannel1;

	/**
	 * If list: Entity ID for contained entity 1 <br>
	 * If range: Entity ID of entity for contained entity range 1
	 */
	private int entityRange1;

	/**
	 * If list: Instance ID for contained entity 1 <br>
	 * If range: Instance ID for first entity in contained entity range 1
	 */
	private int entityRangeInstance1;

	private int entityDeviceAddress2;

	private int entityDeviceChannel2;

	/**
	 * If list: Entity ID for contained entity 2 <br>
	 * If range: Entity ID of entity for contained entity range 2
	 */
	private int entityRange2;

	/**
	 * If list: Instance ID for contained entity 2 <br>
	 * If range: Instance ID for first entity in contained entity range 2
	 */
	private int entityRangeInstance2;

	private int entityDeviceAddress3;

	private int entityDeviceChannel3;

	/**
	 * If list: Entity ID for contained entity 3 <br>
	 * If range: Entity ID of entity for contained entity range 3
	 */
	private int entityRange3;

	/**
	 * If list: Instance ID for contained entity 3 <br>
	 * If range: Instance ID for first entity in contained entity range 3
	 */
	private int entityRangeInstance3;

	private int entityDeviceAddress4;

	private int entityDeviceChannel4;

	/**
	 * If list: Entity ID for contained entity 4 <br>
	 * If range: Entity ID of entity for contained entity range 4
	 */
	private int entityRange4;

	/**
	 * If list: Instance ID for contained entity 4 <br>
	 * If range: Instance ID for first entity in contained entity range 4
	 */
	private int entityRangeInstance4;

	@Override
	protected void populateTypeSpecficValues(byte[] recordData,
			SensorRecord record) {

		setContainerEntityId(TypeConverter.byteToInt(recordData[5]));

		setContainerEntityInstance(TypeConverter.byteToInt(recordData[6]));

		setContainerEntityDeviceAddress(TypeConverter.byteToInt(recordData[7]));

		setContainerEntityDeviceChannel(TypeConverter.byteToInt(recordData[8]));

		setEntitiesAsRange((TypeConverter.byteToInt(recordData[9]) & 0x80) != 0);

		setRecordLink((TypeConverter.byteToInt(recordData[9]) & 0x40) != 0);

		setEntityDeviceAddress1(TypeConverter.byteToInt(recordData[10]));
		setEntityDeviceChannel1(TypeConverter.byteToInt(recordData[11]));
		setEntityRange1(TypeConverter.byteToInt(recordData[12]));
		setEntityRangeInstance1(TypeConverter.byteToInt(recordData[13]));

		setEntityDeviceAddress2(TypeConverter.byteToInt(recordData[14]));
		setEntityDeviceChannel2(TypeConverter.byteToInt(recordData[15]));
		setEntityRange2(TypeConverter.byteToInt(recordData[16]));
		setEntityRangeInstance2(TypeConverter.byteToInt(recordData[17]));

		setEntityDeviceAddress3(TypeConverter.byteToInt(recordData[18]));
		setEntityDeviceChannel3(TypeConverter.byteToInt(recordData[19]));
		setEntityRange3(TypeConverter.byteToInt(recordData[20]));
		setEntityRangeInstance3(TypeConverter.byteToInt(recordData[21]));

		setEntityDeviceAddress4(TypeConverter.byteToInt(recordData[22]));
		setEntityDeviceChannel4(TypeConverter.byteToInt(recordData[23]));
		setEntityRange4(TypeConverter.byteToInt(recordData[24]));
		setEntityRangeInstance4(TypeConverter.byteToInt(recordData[25]));
	}

	public int getContainerEntityId() {
		return containerEntityId;
	}

	public void setContainerEntityId(int containerEntityId) {
		this.containerEntityId = containerEntityId;
	}

	public int getContainerEntityInstance() {
		return containerEntityInstance;
	}

	public void setContainerEntityInstance(int containerEntityInstance) {
		this.containerEntityInstance = containerEntityInstance;
	}

	public int getContainerEntityDeviceAddress() {
		return containerEntityDeviceAddress;
	}

	public void setContainerEntityDeviceAddress(int containerEntityDeviceAddress) {
		this.containerEntityDeviceAddress = containerEntityDeviceAddress;
	}

	public int getContainerEntityDeviceChannel() {
		return containerEntityDeviceChannel;
	}

	public void setContainerEntityDeviceChannel(int containerEntityDeviceChannel) {
		this.containerEntityDeviceChannel = containerEntityDeviceChannel;
	}

	public boolean isEntitiesAsRange() {
		return entitiesAsRange;
	}

	public void setEntitiesAsRange(boolean entitiesAsRange) {
		this.entitiesAsRange = entitiesAsRange;
	}

	public boolean isRecordLink() {
		return recordLink;
	}

	public void setRecordLink(boolean recordLink) {
		this.recordLink = recordLink;
	}

	public int getEntityDeviceAddress1() {
		return entityDeviceAddress1;
	}

	public void setEntityDeviceAddress1(int entityDeviceAddress1) {
		this.entityDeviceAddress1 = entityDeviceAddress1;
	}

	public int getEntityDeviceChannel1() {
		return entityDeviceChannel1;
	}

	public void setEntityDeviceChannel1(int entityDeviceChannel1) {
		this.entityDeviceChannel1 = entityDeviceChannel1;
	}

	public int getEntityRange1() {
		return entityRange1;
	}

	public void setEntityRange1(int entityRange1) {
		this.entityRange1 = entityRange1;
	}

	public int getEntityRangeInstance1() {
		return entityRangeInstance1;
	}

	public void setEntityRangeInstance1(int entityRangeInstance1) {
		this.entityRangeInstance1 = entityRangeInstance1;
	}

	public int getEntityDeviceAddress2() {
		return entityDeviceAddress2;
	}

	public void setEntityDeviceAddress2(int entityDeviceAddress2) {
		this.entityDeviceAddress2 = entityDeviceAddress2;
	}

	public int getEntityDeviceChannel2() {
		return entityDeviceChannel2;
	}

	public void setEntityDeviceChannel2(int entityDeviceChannel2) {
		this.entityDeviceChannel2 = entityDeviceChannel2;
	}

	public int getEntityRange2() {
		return entityRange2;
	}

	public void setEntityRange2(int entityRange2) {
		this.entityRange2 = entityRange2;
	}

	public int getEntityRangeInstance2() {
		return entityRangeInstance2;
	}

	public void setEntityRangeInstance2(int entityRangeInstance2) {
		this.entityRangeInstance2 = entityRangeInstance2;
	}

	public int getEntityDeviceAddress3() {
		return entityDeviceAddress3;
	}

	public void setEntityDeviceAddress3(int entityDeviceAddress3) {
		this.entityDeviceAddress3 = entityDeviceAddress3;
	}

	public int getEntityDeviceChannel3() {
		return entityDeviceChannel3;
	}

	public void setEntityDeviceChannel3(int entityDeviceChannel3) {
		this.entityDeviceChannel3 = entityDeviceChannel3;
	}

	public int getEntityRange3() {
		return entityRange3;
	}

	public void setEntityRange3(int entityRange3) {
		this.entityRange3 = entityRange3;
	}

	public int getEntityRangeInstance3() {
		return entityRangeInstance3;
	}

	public void setEntityRangeInstance3(int entityRangeInstance3) {
		this.entityRangeInstance3 = entityRangeInstance3;
	}

	public int getEntityDeviceAddress4() {
		return entityDeviceAddress4;
	}

	public void setEntityDeviceAddress4(int entityDeviceAddress4) {
		this.entityDeviceAddress4 = entityDeviceAddress4;
	}

	public int getEntityDeviceChannel4() {
		return entityDeviceChannel4;
	}

	public void setEntityDeviceChannel4(int entityDeviceChannel4) {
		this.entityDeviceChannel4 = entityDeviceChannel4;
	}

	public int getEntityRange4() {
		return entityRange4;
	}

	public void setEntityRange4(int entityRange4) {
		this.entityRange4 = entityRange4;
	}

	public int getEntityRangeInstance4() {
		return entityRangeInstance4;
	}

	public void setEntityRangeInstance4(int entityRangeInstance4) {
		this.entityRangeInstance4 = entityRangeInstance4;
	}

}
