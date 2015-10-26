/*
 * CompactSensorRecord.java 
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
 * Wrapper for Compact Sensor Record format
 */
public class CompactSensorRecord extends SensorRecord {

	private byte sensorOwnerId;

	private AddressType addressType;

	private byte channelNumber;

	private byte sensorOwnerLun;

	private byte sensorNumber;

	private EntityId entityId;

	/**
	 * Entity is physical if true, logical otherwise.
	 */
	private boolean entityPhysical;

	private byte entityInstanceNumber;

	private boolean hysteresisReadable;

	private boolean thresholdsReadable;

	private SensorType sensorType;

	private int eventReadingType;

	private RateUnit rateUnit;

	private ModifierUnitUsage modifierUnitUsage;

	private SensorUnit sensorBaseUnit;

	private SensorUnit sensorModifierUnit;

	private SensorDirection sensorDirection;

	private String name;

	/**
	 * The instance modifier is a character(s) that software can append to the
	 * end of the ID String. This field selects whether the appended
	 * character(s) will be numeric or alpha.
	 */
	private InstanceModifierType idInstanceModifierType;

	/**
	 * Sensor numbers sharing this record are sequential starting with the
	 * sensor number specified by the Sensor Number field for this record.
	 */
	private int shareCount;

	private boolean entityInstanceIncrements;

	/**
	 * Suppose sensor ID is 'Temp' for 'Temperature Sensor', share count = 3, ID
	 * string instance modifier = numeric, instance modifier offset = 5 - then
	 * the sensors could be identified as: Temp 5, Temp 6, Temp 7 <br>
	 * If the modifier = alpha, offset=0 corresponds to 'A', offset=25
	 * corresponds to 'Z', and offset = 26 corresponds to 'AA', thus, for
	 * offset=26 the sensors could be identified as: Temp AA, Temp AB, Temp AC
	 */
	private int idInstanceModifierOffset;

	@Override
	protected void populateTypeSpecficValues(byte[] recordData,
			SensorRecord record) {
		setSensorOwnerId(TypeConverter.intToByte((TypeConverter
				.byteToInt(recordData[5]) & 0xfe) >> 1));

		setAddressType(AddressType.parseInt(TypeConverter
				.byteToInt(recordData[5]) & 0x01));

		setChannelNumber(TypeConverter.intToByte((TypeConverter
				.byteToInt(recordData[6]) & 0xf0) >> 4));

		setSensorOwnerLun(TypeConverter.intToByte(TypeConverter
				.byteToInt(recordData[6]) & 0x3));

		setSensorNumber(recordData[7]);

		setEntityId(EntityId.parseInt(TypeConverter.byteToInt(recordData[8])));

		setEntityPhysical((TypeConverter.byteToInt(recordData[9]) & 0x80) == 0);

		setEntityInstanceNumber(TypeConverter.intToByte(TypeConverter
				.byteToInt(recordData[9]) & 0x7f));

		int hysteresis = (TypeConverter.byteToInt(recordData[11]) & 0x30) >> 4;

		if (hysteresis == 1 /* hysteresis readable */
				|| hysteresis == 2 /* hysteresis readable & settable */) {
			setHysteresisReadable(true);
		} else {
			setHysteresisReadable(false);
		}

		int thresholds = (TypeConverter.byteToInt(recordData[11]) & 0xc) >> 2;

		if (thresholds == 1 /* thresholds readable */
				|| thresholds == 2 /* thresholds readable & settable */) {
			setThresholdsReadable(true);
		} else {
			setThresholdsReadable(false);
		}

		setSensorType(SensorType.parseInt(TypeConverter
				.byteToInt(recordData[12])));

		setEventReadingType(TypeConverter.byteToInt(recordData[13]));

		setRateUnit(RateUnit
				.parseInt((TypeConverter.byteToInt(recordData[20]) & 0x38) >> 3));

		setModifierUnitUsage(ModifierUnitUsage.parseInt((TypeConverter
				.byteToInt(recordData[20]) & 0x6) >> 1));

		setSensorBaseUnit(SensorUnit.parseInt(TypeConverter
				.byteToInt(recordData[21])));

		setSensorModifierUnit(SensorUnit.parseInt(TypeConverter
				.byteToInt(recordData[22])));

		setSensorDirection(SensorDirection.parseInt((TypeConverter
				.byteToInt(recordData[23]) & 0xc0) >> 6));

		setIdInstanceModifierType(InstanceModifierType.parseInt((TypeConverter
				.byteToInt(recordData[23]) & 0x30) >> 4));

		setShareCount(TypeConverter.byteToInt(recordData[23]) & 0xf);

		setEntityInstanceIncrements((TypeConverter.byteToInt(recordData[24]) & 0x80) != 0);

		setIdInstanceModifierOffset(TypeConverter.byteToInt(recordData[24]) & 0x7f);

		if (recordData.length >= 32) {
			byte[] name = new byte[recordData.length - 32];

			System.arraycopy(recordData, 32, name, 0, name.length);

			setName(decodeName(recordData[31], name));
		}
	}

	public byte getSensorOwnerId() {
		return sensorOwnerId;
	}

	public void setSensorOwnerId(byte sensorOwnerId) {
		this.sensorOwnerId = sensorOwnerId;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public void setAddressType(AddressType addressType) {
		this.addressType = addressType;
	}

	public byte getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(byte channelNumber) {
		this.channelNumber = channelNumber;
	}

	public byte getSensorOwnerLun() {
		return sensorOwnerLun;
	}

	public void setSensorOwnerLun(byte sensorOwnerLun) {
		this.sensorOwnerLun = sensorOwnerLun;
	}

	public byte getSensorNumber() {
		return sensorNumber;
	}

	public void setSensorNumber(byte sensorNumber) {
		this.sensorNumber = sensorNumber;
	}

	public EntityId getEntityId() {
		return entityId;
	}

	public void setEntityId(EntityId entityId) {
		this.entityId = entityId;
	}

	public boolean isEntityPhysical() {
		return entityPhysical;
	}

	public void setEntityPhysical(boolean entityPhysical) {
		this.entityPhysical = entityPhysical;
	}

	public byte getEntityInstanceNumber() {
		return entityInstanceNumber;
	}

	public void setEntityInstanceNumber(byte entityInstanceNumber) {
		this.entityInstanceNumber = entityInstanceNumber;
	}

	public boolean isHysteresisReadable() {
		return hysteresisReadable;
	}

	public void setHysteresisReadable(boolean hysteresisReadable) {
		this.hysteresisReadable = hysteresisReadable;
	}

	public boolean isThresholdsReadable() {
		return thresholdsReadable;
	}

	public void setThresholdsReadable(boolean thresholdsReadable) {
		this.thresholdsReadable = thresholdsReadable;
	}

	public SensorType getSensorType() {
		return sensorType;
	}

	public void setSensorType(SensorType sensorType) {
		this.sensorType = sensorType;
	}

	public int getEventReadingType() {
		return eventReadingType;
	}

	public void setEventReadingType(int eventReadingType) {
		this.eventReadingType = eventReadingType;
	}

	public RateUnit getRateUnit() {
		return rateUnit;
	}

	public void setRateUnit(RateUnit rateUnit) {
		this.rateUnit = rateUnit;
	}

	public ModifierUnitUsage getModifierUnitUsage() {
		return modifierUnitUsage;
	}

	public void setModifierUnitUsage(ModifierUnitUsage modifierUnitUsage) {
		this.modifierUnitUsage = modifierUnitUsage;
	}

	public SensorUnit getSensorBaseUnit() {
		return sensorBaseUnit;
	}

	public void setSensorBaseUnit(SensorUnit sensorBaseUnit) {
		this.sensorBaseUnit = sensorBaseUnit;
	}

	public SensorUnit getSensorModifierUnit() {
		return sensorModifierUnit;
	}

	public void setSensorModifierUnit(SensorUnit sensorModifierUnit) {
		this.sensorModifierUnit = sensorModifierUnit;
	}

	public SensorDirection getSensorDirection() {
		return sensorDirection;
	}

	public void setSensorDirection(SensorDirection sensorDirection) {
		this.sensorDirection = sensorDirection;
	}

	public InstanceModifierType getIdInstanceModifierType() {
		return idInstanceModifierType;
	}

	public void setIdInstanceModifierType(
			InstanceModifierType idInstanceModifierType) {
		this.idInstanceModifierType = idInstanceModifierType;
	}

	public int getShareCount() {
		return shareCount;
	}

	public void setShareCount(int shareCount) {
		this.shareCount = shareCount;
	}

	public boolean isEntityInstanceIncrements() {
		return entityInstanceIncrements;
	}

	public void setEntityInstanceIncrements(boolean entityInstanceIncrements) {
		this.entityInstanceIncrements = entityInstanceIncrements;
	}

	public int getIdInstanceModifierOffset() {
		return idInstanceModifierOffset;
	}

	public void setIdInstanceModifierOffset(int idInstanceModifierOffset) {
		this.idInstanceModifierOffset = idInstanceModifierOffset;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	// TODO: Generate multiple records from instance modifiers (?)
}
