/*
 * SOLException.java
 * Created on 05.06.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.api.sol;

/**
 * Exception representing problem that might have been encoutered during Serial over LAN communication.
 */
public class SOLException extends Exception {

    public SOLException(String message) {
        super(message);
    }

    public SOLException(String message, Throwable cause) {
        super(message, cause);
    }
}
