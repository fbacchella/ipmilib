/*
 * ReserveSelResponseData.java 
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
 * Wrapper for Reserve SEL command response.
 */
public class ReserveSelResponseData implements ResponseData {
	private int reservationId;

	public void setReservationId(int reservationId) {
		this.reservationId = reservationId;
	}

	public int getReservationId() {
		return reservationId;
	}
	
}
