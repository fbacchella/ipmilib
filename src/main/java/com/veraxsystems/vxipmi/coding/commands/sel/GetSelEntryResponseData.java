/*
 * GetSelEntryResponseData.java 
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

import com.veraxsystems.vxipmi.coding.commands.ResponseData;

/**
 * Wrapper for Get Sel Entry response
 */
public class GetSelEntryResponseData implements ResponseData {

	/**
	 * ID of the next record in the repository.
	 */
	private int nextRecordId;
	
	/**
	 * Sensor record data
	 */
	private SelRecord selRecord;

	public void setNextRecordId(int nextRecordId) {
		this.nextRecordId = nextRecordId;
	}

	public int getNextRecordId() {
		return nextRecordId;
	}

	public void setSelRecord(SelRecord selRecord) {
		this.selRecord = selRecord;
	}

	public SelRecord getSelRecord() {
		return selRecord;
	}
}
