/*
 * IpmiError.java 
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

import java.io.IOException;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;

/**
 * Class that wraps exception that was cause of not receiving message.
 */
public class IpmiError extends IpmiResponse {
    private final IOException exception;

    /**
     * @return {@link IOException} that caused message delivery to fail.
     */
    public IOException getException() {
        return exception;
    }

    public IpmiError(IOException exception, int tag, ConnectionHandle handle) {
        super(tag, handle);
        this.exception = exception;
    }
}
