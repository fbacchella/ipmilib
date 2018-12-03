/*
 * ManagementAccessRecordType.java 
 * Created on 2011-08-16
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.fru.record;

import org.apache.log4j.Logger;

public enum ManagementAccessRecordType {
    SystemManagementUrl(ManagementAccessRecordType.SYSTEMMANAGEMENTURL), SystemName(
            ManagementAccessRecordType.SYSTEMNAME), SystemPingAddress(ManagementAccessRecordType.SYSTEMPINGADDRESS), ComponentManagementURL(
            ManagementAccessRecordType.COMPONENTMANAGEMENTURL), Unspecified(ManagementAccessRecordType.UNSPECIFIED), ;
    private static final int SYSTEMMANAGEMENTURL = 1;

    private static final int SYSTEMNAME = 2;

    private static final int SYSTEMPINGADDRESS = 3;

    private static final int COMPONENTMANAGEMENTURL = 4;

    private static final int UNSPECIFIED = 0;

    private static Logger logger = Logger.getLogger(ManagementAccessRecordType.class);

    private int code;

    ManagementAccessRecordType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ManagementAccessRecordType parseInt(int value) {
        switch (value) {
        case SYSTEMMANAGEMENTURL:
            return SystemManagementUrl;
        case SYSTEMNAME:
            return SystemName;
        case SYSTEMPINGADDRESS:
            return SystemPingAddress;
        case COMPONENTMANAGEMENTURL:
            return ComponentManagementURL;
        default:
            logger.error("Invalid value: " + value);
            return Unspecified;
        }
    }
}