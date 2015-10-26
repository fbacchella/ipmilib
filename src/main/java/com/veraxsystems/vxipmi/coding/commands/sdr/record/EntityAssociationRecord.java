/*
 * EntityAssociationRecord.java 
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
 * This record is used to present the relationship between entities that
 * contain, or are contained by, other entities.
 */
public class EntityAssociationRecord extends SensorRecord {

	private int containerEntityId;

	private int containerEntityInstance;

	/**
	 * false - contained entities specified as list <br>
	 * true - contained entities specified as range
	 */
	private boolean entitiesAsRange;

	private boolean recordLink;

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

		setEntitiesAsRange((TypeConverter.byteToInt(recordData[7]) & 0x80) != 0);

		setRecordLink((TypeConverter.byteToInt(recordData[7]) & 0x40) != 0);

		setEntityRange1(TypeConverter.byteToInt(recordData[8]));
		setEntityRangeInstance1(TypeConverter.byteToInt(recordData[9]));

		setEntityRange2(TypeConverter.byteToInt(recordData[10]));
		setEntityRangeInstance2(TypeConverter.byteToInt(recordData[11]));

		setEntityRange3(TypeConverter.byteToInt(recordData[12]));
		setEntityRangeInstance3(TypeConverter.byteToInt(recordData[13]));

		setEntityRange4(TypeConverter.byteToInt(recordData[14]));
		setEntityRangeInstance4(TypeConverter.byteToInt(recordData[15]));
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
