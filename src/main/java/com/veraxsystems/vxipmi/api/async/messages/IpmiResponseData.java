/*
 * IpmiResponseData.java 
 * Created on 2011-09-07
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.api.async.messages;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;

/**
 * Class that wraps {@link ResponseData} that was received successfully.
 */
public class IpmiResponseData extends IpmiResponse {
	private ResponseData responseData;

	/**
	 * @return {@link ResponseData} received successfully.
	 */
	public ResponseData getResponseData() {
		return responseData;
	}

	public IpmiResponseData(ResponseData data, int tag, ConnectionHandle handle) {
		super(tag, handle);
		responseData = data;
	}
}
