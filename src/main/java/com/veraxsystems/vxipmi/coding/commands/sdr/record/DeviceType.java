/*
 * DeviceType.java 
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

import org.apache.log4j.Logger;

public enum DeviceType {
	Eeprom24C04(DeviceType.EEPROM24C04),
	Eeprom24C08(DeviceType.EEPROM24C08),
	Eeprom24C16(DeviceType.EEPROM24C16),
	Eeprom24C17(DeviceType.EEPROM24C17),
	Eeprom24C32(DeviceType.EEPROM24C32),
	Eeprom24C64(DeviceType.EEPROM24C64),
	FRUInventoryDevice(DeviceType.FRUINVENTORYDEVICE),
	Other(DeviceType.OTHER),
	OEMSpecifiedDevice(DeviceType.OEMSPECIFIEDDEVICE),
	DS1624TemperatureSensor(DeviceType.DS1624TEMPERATURESENSOR),
	PCF8570256ByteRAM(DeviceType.PCF8570256BYTERAM),
	PCF8573ClockCalendar(DeviceType.PCF8573CLOCKCALENDAR),
	PCF8574AIoPort(DeviceType.PCF8574AIOPORT),
	PCF8583ClockCalendar(DeviceType.PCF8583CLOCKCALENDAR),
	PCF8593ClockCalendar(DeviceType.PCF8593CLOCKCALENDAR),
	ClockCalendar(DeviceType.CLOCKCALENDAR),
	PCF8591AdDaConverter(DeviceType.PCF8591ADDACONVERTER),
	IoPort(DeviceType.IOPORT),
	AdConverter(DeviceType.ADCONVERTER),
	DaConverter(DeviceType.DACONVERTER),
	DS1621TemperatureSensor(DeviceType.DS1621TEMPERATURESENSOR),
	AdDaConverter(DeviceType.ADDACONVERTER),
	LCDControllerDriver(DeviceType.LCDCONTROLLERDRIVER),
	CoreLogicDevice(DeviceType.CORELOGICDEVICE),
	LMC6874IntelligentBatteryController(DeviceType.LMC6874INTELLIGENTBATTERYCONTROLLER),
	IntelligentBatteryController(DeviceType.INTELLIGENTBATTERYCONTROLLER),
	ComboManagementASIC(DeviceType.COMBOMANAGEMENTASIC),
	Maxim1617TemperatureSensor(DeviceType.MAXIM1617TEMPERATURESENSOR),
	LM75TemperatureSensor(DeviceType.LM75TEMPERATURESENSOR),
	HecetaASIC(DeviceType.HECETAASIC),
	Eeprom24C01(DeviceType.EEPROM24C01),
	Eeprom24C02(DeviceType.EEPROM24C02),
	;
	private static final int EEPROM24C04 = 10;
	private static final int EEPROM24C08 = 11;
	private static final int EEPROM24C16 = 12;
	private static final int EEPROM24C17 = 13;
	private static final int EEPROM24C32 = 14;
	private static final int EEPROM24C64 = 15;
	private static final int FRUINVENTORYDEVICE = 16;
	private static final int OTHER = 191;
	private static final int OEMSPECIFIEDDEVICE = 192;
	private static final int DS1624TEMPERATURESENSOR = 2;
	private static final int PCF8570256BYTERAM = 20;
	private static final int PCF8573CLOCKCALENDAR = 21;
	private static final int PCF8574AIOPORT = 22;
	private static final int PCF8583CLOCKCALENDAR = 23;
	private static final int PCF8593CLOCKCALENDAR = 24;
	private static final int CLOCKCALENDAR = 25;
	private static final int PCF8591ADDACONVERTER = 26;
	private static final int IOPORT = 27;
	private static final int ADCONVERTER = 28;
	private static final int DACONVERTER = 29;
	private static final int DS1621TEMPERATURESENSOR = 3;
	private static final int ADDACONVERTER = 30;
	private static final int LCDCONTROLLERDRIVER = 31;
	private static final int CORELOGICDEVICE = 32;
	private static final int LMC6874INTELLIGENTBATTERYCONTROLLER = 33;
	private static final int INTELLIGENTBATTERYCONTROLLER = 34;
	private static final int COMBOMANAGEMENTASIC = 35;
	private static final int MAXIM1617TEMPERATURESENSOR = 36;
	private static final int LM75TEMPERATURESENSOR = 4;
	private static final int HECETAASIC = 5;
	private static final int EEPROM24C01 = 8;
	private static final int EEPROM24C02 = 9;

	private int code;
	
	private static Logger logger = Logger.getLogger(DeviceType.class);

	DeviceType(int code) {
		this.code = code;
	}
	public int getCode() {
		return code;
	}
	public static DeviceType parseInt(int value) {
		switch(value) {
		case EEPROM24C04:
			return Eeprom24C04;
		case EEPROM24C08:
			return Eeprom24C08;
		case EEPROM24C16:
			return Eeprom24C16;
		case EEPROM24C17:
			return Eeprom24C17;
		case EEPROM24C32:
			return Eeprom24C32;
		case EEPROM24C64:
			return Eeprom24C64;
		case FRUINVENTORYDEVICE:
			return FRUInventoryDevice;
		case OTHER:
			return Other;
		case OEMSPECIFIEDDEVICE:
			return OEMSpecifiedDevice;
		case DS1624TEMPERATURESENSOR:
			return DS1624TemperatureSensor;
		case PCF8570256BYTERAM:
			return PCF8570256ByteRAM;
		case PCF8573CLOCKCALENDAR:
			return PCF8573ClockCalendar;
		case PCF8574AIOPORT:
			return PCF8574AIoPort;
		case PCF8583CLOCKCALENDAR:
			return PCF8583ClockCalendar;
		case PCF8593CLOCKCALENDAR:
			return PCF8593ClockCalendar;
		case CLOCKCALENDAR:
			return ClockCalendar;
		case PCF8591ADDACONVERTER:
			return PCF8591AdDaConverter;
		case IOPORT:
			return IoPort;
		case ADCONVERTER:
			return AdConverter;
		case DACONVERTER:
			return DaConverter;
		case DS1621TEMPERATURESENSOR:
			return DS1621TemperatureSensor;
		case ADDACONVERTER:
			return AdDaConverter;
		case LCDCONTROLLERDRIVER:
			return LCDControllerDriver;
		case CORELOGICDEVICE:
			return CoreLogicDevice;
		case LMC6874INTELLIGENTBATTERYCONTROLLER:
			return LMC6874IntelligentBatteryController;
		case INTELLIGENTBATTERYCONTROLLER:
			return IntelligentBatteryController;
		case COMBOMANAGEMENTASIC:
			return ComboManagementASIC;
		case MAXIM1617TEMPERATURESENSOR:
			return Maxim1617TemperatureSensor;
		case LM75TEMPERATURESENSOR:
			return LM75TemperatureSensor;
		case HECETAASIC:
			return HecetaASIC;
		case EEPROM24C01:
			return Eeprom24C01;
		case EEPROM24C02:
			return Eeprom24C02;
		default:
			logger.error("Invalid value: " + value);
			return Other;
		}
	}
}