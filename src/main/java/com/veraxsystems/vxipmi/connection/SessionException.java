/*
 * SessionException.java
 * Created on 09.06.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.connection;

/**
 * Exception that can be thrown when there are some problems with session, for example when establishig new session.
 */
public class SessionException extends Exception {

    public SessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
