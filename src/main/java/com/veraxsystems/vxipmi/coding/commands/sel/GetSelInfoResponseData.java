/*
 * GetSelInfoResponseData.java 
 * Created on 2011-08-11
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.sel;

import java.util.Date;

import com.veraxsystems.vxipmi.coding.commands.ResponseData;

/**
 * Wrapper for Get SEL Info response
 */
public class GetSelInfoResponseData implements ResponseData {
	private int selVersion;
	
	private int entriesCount;
	
	/**
	 * Most recent addition timestamp.
	 */
	private Date additionTimestamp;
	
	/**
	 * Most recent erase timestamp.
	 */
	private Date eraseTimestamp;

	public int getSelVersion() {
		return selVersion;
	}

	public void setSelVersion(int selVersion) {
		this.selVersion = selVersion;
	}

	public int getEntriesCount() {
		return entriesCount;
	}

	public void setEntriesCount(int entriesCount) {
		this.entriesCount = entriesCount;
	}

	public Date getAdditionTimestamp() {
		return additionTimestamp;
	}

	public void setAdditionTimestamp(Date additionTimestamp) {
		this.additionTimestamp = additionTimestamp;
	}

	public Date getEraseTimestamp() {
		return eraseTimestamp;
	}

	public void setEraseTimestamp(Date eraseTimestamp) {
		this.eraseTimestamp = eraseTimestamp;
	}
}
