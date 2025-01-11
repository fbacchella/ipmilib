/*
 * ErrorAction.java 
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

import java.security.GeneralSecurityException;

/**
 * Action representing an exception handed to the machine owner.
 */
public class SecurityErrorAction extends ExceptionAction<GeneralSecurityException> {
   public SecurityErrorAction(GeneralSecurityException e) {
        super(e);
    }
}
