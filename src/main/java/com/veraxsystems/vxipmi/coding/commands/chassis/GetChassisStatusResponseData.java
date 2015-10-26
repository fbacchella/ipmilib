/*
 * GetChassisStatusResponseData.java 
 * Created on 2011-08-28
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.chassis;

import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Wrapper for Get Chassis Status response.
 */
public class GetChassisStatusResponseData implements ResponseData {

	private byte currentPowerState;

	private byte lastPowerEvent;

	private byte miscChassisState;

	private boolean isFrontPanelButtonCapabilitiesSet;

	private byte frontPanelButtonCapabilities;

	public GetChassisStatusResponseData() {
		setFrontPanelButtonCapabilitiesSet(false);
	}

	public void setCurrentPowerState(byte currentPowerState) {
		this.currentPowerState = currentPowerState;
	}

	public byte getCurrentPowerState() {
		return currentPowerState;
	}

	public PowerRestorePolicy getPowerRestorePolicy() {
		switch ((currentPowerState & TypeConverter.intToByte(0x60)) >> 5) {
		case 0:
			return PowerRestorePolicy.PoweredOff;
		case 1:
			return PowerRestorePolicy.PowerRestored;
		case 2:
			return PowerRestorePolicy.PoweredUp;
		default:
			throw new IllegalArgumentException("Invalid Power Restore Policy");
		}
	}

	/**
	 * @return True when controller attempted to turn system power on or off,
	 *         but system did not enter desired state
	 */
	public boolean isPowerControlFault() {
		return ((currentPowerState & TypeConverter.intToByte(0x10)) != 0);
	}

	/**
	 * @return True when fault was detected in main power subsystem.
	 */
	public boolean isPowerFault() {
		return ((currentPowerState & TypeConverter.intToByte(0x8)) != 0);
	}

	/**
	 * @return True when interlock was detected (chassis is presently shut down
	 *         because a chassis panel interlock switch is active)
	 */
	public boolean isInterlock() {
		return ((currentPowerState & TypeConverter.intToByte(0x4)) != 0);
	}

	/**
	 * @return True when system was shut down because of power overload
	 *         condition.
	 */
	public boolean isPowerOverload() {
		return ((currentPowerState & TypeConverter.intToByte(0x2)) != 0);
	}

	/**
	 * @return True when system power is on.
	 */
	public boolean isPowerOn() {
		return ((currentPowerState & TypeConverter.intToByte(0x1)) != 0);
	}

	public void setLastPowerEvent(byte lastPowerEvent) {
		this.lastPowerEvent = lastPowerEvent;
	}

	public byte getLastPowerEvent() {
		return lastPowerEvent;
	}

	/**
	 * @return True when last 'Power is on' state was entered via IPMI command.
	 */
	public boolean wasIpmiPowerOn() {
		return ((lastPowerEvent & TypeConverter.intToByte(0x10)) != 0);
	}

	/**
	 * @return True if last power down caused by power fault.
	 */
	public boolean wasPowerFault() {
		return ((lastPowerEvent & TypeConverter.intToByte(0x8)) != 0);
	}

	/**
	 * @return True if last power down caused by a power interlock being
	 *         activated.
	 */
	public boolean wasInterlock() {
		return ((lastPowerEvent & TypeConverter.intToByte(0x4)) != 0);
	}

	/**
	 * @return True if last power down caused by a Power overload.
	 */
	public boolean wasPowerOverload() {
		return ((lastPowerEvent & TypeConverter.intToByte(0x2)) != 0);
	}

	/**
	 * @return True if AC failed.
	 */
	public boolean acFailed() {
		return ((lastPowerEvent & TypeConverter.intToByte(0x1)) != 0);

	}

	public void setMiscChassisState(byte miscChassisState) {
		this.miscChassisState = miscChassisState;
	}

	public byte getMiscChassisState() {
		return miscChassisState;
	}

	/**
	 * @return True if Chassis Identify command and state info supported.
	 */
	public boolean isChassisIdentifyCommandSupported() {
		return ((miscChassisState & TypeConverter.intToByte(0x40)) != 0);
	}

	public ChassisIdentifyState getChassisIdentifyState() {
		if (!isChassisIdentifyCommandSupported()) {
			throw new IllegalAccessError(
					"Chassis Idetify command and state not supported");
		}

		return ChassisIdentifyState.parseInt((miscChassisState & TypeConverter
				.intToByte(0x30)) >> 4);
	}

	/**
	 * @return True if cooling or fan fault was detected.
	 */
	public boolean coolingFaultDetected() {
		return ((miscChassisState & TypeConverter.intToByte(0x8)) != 0);
	}

	/**
	 * @return True if drive fault was detected.
	 */
	public boolean driveFaultDetected() {
		return ((miscChassisState & TypeConverter.intToByte(0x4)) != 0);
	}

	/**
	 * @return True if Front Panel Lockout active (power off and reset via
	 *         chassis push-buttons disabled.).
	 */
	public boolean isFrontPanelLockoutActive() {
		return ((miscChassisState & TypeConverter.intToByte(0x2)) != 0);
	}

	/**
	 * @return True if Chassis intrusion active is active.
	 */
	public boolean isChassisIntrusionActive() {
		return ((miscChassisState & TypeConverter.intToByte(0x1)) != 0);
	}

	public void setFrontPanelButtonCapabilities(
			byte frontPanelButtonCapabilities) {
		this.frontPanelButtonCapabilities = frontPanelButtonCapabilities;
		setFrontPanelButtonCapabilitiesSet(true);
	}

	public byte getFrontPanelButtonCapabilities() {
		return frontPanelButtonCapabilities;
	}

	/**
	 * @return Standby (sleep) button disable is allowed.
	 * @throws IllegalAccessException
	 *             when Front Panel Button Capabilities wasn't set.
	 */
	public boolean isStandbyButtonDisableAllowed()
			throws IllegalAccessException {
		if (!isFrontPanelButtonCapabilitiesSet()) {
			throw new IllegalAccessException(
					"Front Panel Button Capabilities not set");
		}
		return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x80)) != 0);
	}

	/**
	 * @return Diagnostic Interrupt button disable is allowed.
	 * @throws IllegalAccessException
	 *             when Front Panel Button Capabilities wasn't set.
	 */
	public boolean isDiagnosticInterruptButtonDisableAllowed()
			throws IllegalAccessException {
		if (!isFrontPanelButtonCapabilitiesSet()) {
			throw new IllegalAccessException(
					"Front Panel Button Capabilities not set");
		}
		return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x40)) != 0);
	}

	/**
	 * @return Reset button disable is allowed.
	 * @throws IllegalAccessException
	 *             when Front Panel Button Capabilities wasn't set.
	 */
	public boolean isResetButtonDisableAllowed() throws IllegalAccessException {
		if (!isFrontPanelButtonCapabilitiesSet()) {
			throw new IllegalAccessException(
					"Front Panel Button Capabilities not set");
		}
		return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x20)) != 0);
	}

	/**
	 * @return Power off button disable allowed (in the case there is a single
	 *         combined power/standby (sleep) button, disabling power off also
	 *         disables sleep requests via that button.)
	 * @throws IllegalAccessException
	 *             when Front Panel Button Capabilities wasn't set.
	 */
	public boolean isPowerOffButtonDisableAllowed()
			throws IllegalAccessException {
		if (!isFrontPanelButtonCapabilitiesSet()) {
			throw new IllegalAccessException(
					"Front Panel Button Capabilities not set");
		}
		return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x10)) != 0);
	}

	/**
	 * @return Standby (sleep) button disabled.
	 * @throws IllegalAccessException
	 *             when Front Panel Button Capabilities wasn't set.
	 */
	public boolean isStandbyButtonDisabled() throws IllegalAccessException {
		if (!isFrontPanelButtonCapabilitiesSet()) {
			throw new IllegalAccessException(
					"Front Panel Button Capabilities not set");
		}
		return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x8)) != 0);
	}

	/**
	 * @return Diagnostic Interrupt button disabled.
	 * @throws IllegalAccessException
	 *             when Front Panel Button Capabilities wasn't set.
	 */
	public boolean isDiagnosticInterruptButtonDisabled()
			throws IllegalAccessException {
		if (!isFrontPanelButtonCapabilitiesSet()) {
			throw new IllegalAccessException(
					"Front Panel Button Capabilities not set");
		}
		return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x4)) != 0);
	}

	/**
	 * @return Reset button disabled.
	 * @throws IllegalAccessException
	 *             when Front Panel Button Capabilities wasn't set.
	 */
	public boolean isResetButtonDisabled() throws IllegalAccessException {
		if (!isFrontPanelButtonCapabilitiesSet()) {
			throw new IllegalAccessException(
					"Front Panel Button Capabilities not set");
		}
		return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x2)) != 0);
	}

	/**
	 * @return Power off button disabled (in the case there is a single combined
	 *         power/standby (sleep) button, disabling power off also disables
	 *         sleep requests via that button are also disabled.)
	 * @throws IllegalAccessException
	 *             when Front Panel Button Capabilities wasn't set.
	 */
	public boolean isPowerOffButtonDisabled() throws IllegalAccessException {
		if (!isFrontPanelButtonCapabilitiesSet()) {
			throw new IllegalAccessException(
					"Front Panel Button Capabilities not set");
		}
		return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x1)) != 0);
	}

	private void setFrontPanelButtonCapabilitiesSet(
			boolean isFrontPanelButtonCapabilitiesSet) {
		this.isFrontPanelButtonCapabilitiesSet = isFrontPanelButtonCapabilitiesSet;
	}

	public boolean isFrontPanelButtonCapabilitiesSet() {
		return isFrontPanelButtonCapabilitiesSet;
	}

}
