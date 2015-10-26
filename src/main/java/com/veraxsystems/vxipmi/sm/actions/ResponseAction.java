/*
 * ResponseAction.java 
 * Created on 2011-08-18
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm.actions;

import com.veraxsystems.vxipmi.coding.commands.ResponseData;

/**
 * {@link StateMachineAction} carrying {@link ResponseData} for the message in the process
 * of the session challenge.
 */
public class ResponseAction extends StateMachineAction {

	public ResponseAction(ResponseData ipmiResponseData) {
		setIpmiResponseData(ipmiResponseData);
	}

	private ResponseData ipmiResponseData;

	public void setIpmiResponseData(ResponseData ipmiResponseData) {
		this.ipmiResponseData = ipmiResponseData;
	}

	public ResponseData getIpmiResponseData() {
		return ipmiResponseData;
	}
}
