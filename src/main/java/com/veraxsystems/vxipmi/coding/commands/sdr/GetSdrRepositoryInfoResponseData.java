/*
 * GetSdrRepositoryInfoResponseData.java 
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

/**
 * Wrapper for Get SDR Repository Info response.
 */
public class GetSdrRepositoryInfoResponseData implements ResponseData {
	/**
	 * Version number of the SDR command set for the SDR Device.
	 */
	private int sdrVersion;
	
	/**
	 * Number of records in the SDR Repository
	 */
	private int recordCount;
	
	/**
	 * Most recent addition timestamp.
	 */
	private int addTimestamp;
	
	/**
	 * Most recent erase (delete or clear) timestamp.
	 */
	private int delTimestamp;
	
	/**
	 * Reserve SDR Repository command supported
	 */
	private boolean reserveSupported;

	public void setSdrVersion(int sdrVersion) {
		this.sdrVersion = sdrVersion;
	}

	public int getSdrVersion() {
		return sdrVersion;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setAddTimestamp(int addTimestamp) {
		this.addTimestamp = addTimestamp;
	}

	public int getAddTimestamp() {
		return addTimestamp;
	}

	public void setDelTimestamp(int delTimestamp) {
		this.delTimestamp = delTimestamp;
	}

	public int getDelTimestamp() {
		return delTimestamp;
	}

	public void setReserveSupported(boolean reserveSupported) {
		this.reserveSupported = reserveSupported;
	}

	public boolean isReserveSupported() {
		return reserveSupported;
	}
	
	
}
