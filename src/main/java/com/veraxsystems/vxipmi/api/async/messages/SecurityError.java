/*
 * SecurityError.java
 * Created on 2025-01-11
 */
package com.veraxsystems.vxipmi.api.async.messages;

import java.security.GeneralSecurityException;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;

/**
 * Class that wraps exception that was cause of not receiving message.
 */
public class SecurityError extends IpmiResponse {
    private final GeneralSecurityException exception;

    /**
     * @return {@link GeneralSecurityException} that caused message delivery to fail.
     */
    public GeneralSecurityException getException() {
        return exception;
    }

    public SecurityError(GeneralSecurityException exception, int tag, ConnectionHandle handle) {
        super(tag, handle);
        this.exception = exception;
    }
}
