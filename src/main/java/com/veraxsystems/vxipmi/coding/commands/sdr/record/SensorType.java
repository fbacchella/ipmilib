/*
 * SensorType.java 
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

/**
 * Type of the sensor.
 */
public enum SensorType {

	CoolingDevice(SensorType.COOLINGDEVICE),
	OtherUnitsBasedSensor(SensorType.OTHERUNITSBASEDSENSOR),
	Memory(SensorType.MEMORY),
	DriveBay(SensorType.DRIVEBAY),
	PostMemoryResize(SensorType.POSTMEMORYRESIZE),
	SystemFirmwareProgess(SensorType.SYSTEMFIRMWAREPROGESS),
	EventLoggingDisabled(SensorType.EVENTLOGGINGDISABLED),
	BootError(SensorType.BOOTERROR),
	Watchdog1(SensorType.WATCHDOG1),
	OsBoot(SensorType.OSBOOT),
	SystemEvent(SensorType.SYSTEMEVENT),
	OsStop(SensorType.OSSTOP),
	CriticalInterrupt(SensorType.CRITICALINTERRUPT),
	SlotConnector(SensorType.SLOTCONNECTOR),
	SystemAcpiPowerState(SensorType.SYSTEMACPIPOWERSTATE),
	Watchdog2(SensorType.WATCHDOG2),
	PlatformAlert(SensorType.PLATFORMALERT),
	EntityPresence(SensorType.ENTITYPRESENCE),
	MonitorAsicIc(SensorType.MONITORASICIC),
	Lan(SensorType.LAN),
	Temperature(SensorType.TEMPERATURE),
	Voltage(SensorType.VOLTAGE),
	Current(SensorType.CURRENT),
	Fan(SensorType.FAN),
	PhysicalSecurity(SensorType.PHYSICALSECURITY),
	PlatformSecurityViolationAttempt(SensorType.PLATFORMSECURITYVIOLATIONATTEMPT),
	Processor(SensorType.PROCESSOR),
	PowerSupply(SensorType.POWERSUPPLY),
	PowerUnit(SensorType.POWERUNIT),
	ButtonSwitch(SensorType.BUTTONSWITCH),
	ModuleBoard(SensorType.MODULEBOARD),
	MicrocontrollerCoprocessor(SensorType.MICROCONTROLLERCOPROCESSOR),
	AddInCard(SensorType.ADDINCARD),
	Chassis(SensorType.CHASSIS),
	Chipset(SensorType.CHIPSET),
	OtherFru(SensorType.OTHERFRU),
	ManagementSubsystemHealth(SensorType.MANAGEMENTSUBSYSTEMHEALTH),
	CableInterconnect(SensorType.CABLEINTERCONNECT),
	Battery(SensorType.BATTERY),
	Terminator(SensorType.TERMINATOR),
	SessionAudit(SensorType.SESSIONAUDIT),
	SystemBoot(SensorType.SYSTEMBOOT),
	VersionChange(SensorType.VERSIONCHANGE),
	FruState(SensorType.FRUSTATE),
	Oem(SensorType.OEM),
	OemReserved(SensorType.OEMRESERVED)
	;

	private static final int COOLINGDEVICE = 10;
	private static final int OTHERUNITSBASEDSENSOR = 11;
	private static final int MEMORY = 12;
	private static final int DRIVEBAY = 13;
	private static final int POSTMEMORYRESIZE = 14;
	private static final int SYSTEMFIRMWAREPROGESS = 15;
	private static final int EVENTLOGGINGDISABLED = 16;
	private static final int BOOTERROR = 30;
	private static final int WATCHDOG1 = 17;
	private static final int OSBOOT = 31;
	private static final int SYSTEMEVENT = 18;
	private static final int OSSTOP = 32;
	private static final int CRITICALINTERRUPT = 19;
	private static final int SLOTCONNECTOR = 33;
	private static final int SYSTEMACPIPOWERSTATE = 34;
	private static final int WATCHDOG2 = 35;
	private static final int PLATFORMALERT = 36;
	private static final int ENTITYPRESENCE = 37;
	private static final int MONITORASICIC = 38;
	private static final int LAN = 39;
	private static final int TEMPERATURE = 1;
	private static final int VOLTAGE = 2;
	private static final int CURRENT = 3;
	private static final int FAN = 4;
	private static final int PHYSICALSECURITY = 5;
	private static final int PLATFORMSECURITYVIOLATIONATTEMPT = 6;
	private static final int PROCESSOR = 7;
	private static final int POWERSUPPLY = 8;
	private static final int POWERUNIT = 9;
	private static final int BUTTONSWITCH = 20;
	private static final int MODULEBOARD = 21;
	private static final int MICROCONTROLLERCOPROCESSOR = 22;
	private static final int ADDINCARD = 23;
	private static final int CHASSIS = 24;
	private static final int CHIPSET = 25;
	private static final int OTHERFRU = 26;
	private static final int MANAGEMENTSUBSYSTEMHEALTH = 40;
	private static final int CABLEINTERCONNECT = 27;
	private static final int BATTERY = 41;
	private static final int TERMINATOR = 28;
	private static final int SESSIONAUDIT = 42;
	private static final int SYSTEMBOOT = 29;
	private static final int VERSIONCHANGE = 43;
	private static final int FRUSTATE = 44;
	private static final int OEM = 192; 
	private static final int OEMRESERVED = 118; 

	private int code;
	private static Logger logger = Logger.getLogger(SensorType.class);

	SensorType(int code) {
		this.code = code;
	}
	public int getCode() {
		return code;
	}
	public static SensorType parseInt(int value) {
		switch(value) {
		case COOLINGDEVICE:
			return CoolingDevice;
		case OTHERUNITSBASEDSENSOR:
			return OtherUnitsBasedSensor;
		case MEMORY:
			return Memory;
		case DRIVEBAY:
			return DriveBay;
		case POSTMEMORYRESIZE:
			return PostMemoryResize;
		case SYSTEMFIRMWAREPROGESS:
			return SystemFirmwareProgess;
		case EVENTLOGGINGDISABLED:
			return EventLoggingDisabled;
		case BOOTERROR:
			return BootError;
		case WATCHDOG1:
			return Watchdog1;
		case OSBOOT:
			return OsBoot;
		case SYSTEMEVENT:
			return SystemEvent;
		case OSSTOP:
			return OsStop;
		case CRITICALINTERRUPT:
			return CriticalInterrupt;
		case SLOTCONNECTOR:
			return SlotConnector;
		case SYSTEMACPIPOWERSTATE:
			return SystemAcpiPowerState;
		case WATCHDOG2:
			return Watchdog2;
		case PLATFORMALERT:
			return PlatformAlert;
		case ENTITYPRESENCE:
			return EntityPresence;
		case MONITORASICIC:
			return MonitorAsicIc;
		case LAN:
			return Lan;
		case TEMPERATURE:
			return Temperature;
		case VOLTAGE:
			return Voltage;
		case CURRENT:
			return Current;
		case FAN:
			return Fan;
		case PHYSICALSECURITY:
			return PhysicalSecurity;
		case PLATFORMSECURITYVIOLATIONATTEMPT:
			return PlatformSecurityViolationAttempt;
		case PROCESSOR:
			return Processor;
		case POWERSUPPLY:
			return PowerSupply;
		case POWERUNIT:
			return PowerUnit;
		case BUTTONSWITCH:
			return ButtonSwitch;
		case MODULEBOARD:
			return ModuleBoard;
		case MICROCONTROLLERCOPROCESSOR:
			return MicrocontrollerCoprocessor;
		case ADDINCARD:
			return AddInCard;
		case CHASSIS:
			return Chassis;
		case CHIPSET:
			return Chipset;
		case OTHERFRU:
			return OtherFru;
		case MANAGEMENTSUBSYSTEMHEALTH:
			return ManagementSubsystemHealth;
		case CABLEINTERCONNECT:
			return CableInterconnect;
		case BATTERY:
			return Battery;
		case TERMINATOR:
			return Terminator;
		case SESSIONAUDIT:
			return SessionAudit;
		case SYSTEMBOOT:
			return SystemBoot;
		case VERSIONCHANGE:
			return VersionChange;
		case FRUSTATE:
			return FruState;			
		case OEMRESERVED:
		    return OemReserved;
		default:
			if(value >= OEM) {
				return Oem;
			}
			logger.error("Invalid value: " + value);
			return Oem;
		}
	}
}