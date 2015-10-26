/*
 * IPMIException.java 
 * Created on 2011-07-25
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload.lan;

import com.veraxsystems.vxipmi.coding.payload.CompletionCode;

public class IPMIException extends Exception {

	private static final long serialVersionUID = 1L;

	private CompletionCode completionCode;

	public IPMIException(CompletionCode completionCode) {
		setCompletionCode(completionCode);
	}

	private void setCompletionCode(CompletionCode completionCode) {
		this.completionCode = completionCode;
	}

	public CompletionCode getCompletionCode() {
		return completionCode;
	}

	@Override
	public String getMessage() {
		return completionCode.getMessage();
	}
}
