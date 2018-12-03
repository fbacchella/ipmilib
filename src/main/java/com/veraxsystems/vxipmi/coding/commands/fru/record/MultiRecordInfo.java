/*
 * MultiRecordInfo.java 
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

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Record from FRU Multi Record Area
 */
public abstract class MultiRecordInfo extends FruRecord {

    public MultiRecordInfo() {
        super();
    }

    /**
     * Creates and populates record
     *
     * @param fruData
     *            - raw data containing record
     * @param offset
     *            - offset to the record in the data
     */
    public static MultiRecordInfo populateMultiRecord(final byte[] fruData, final int offset) {
        MultiRecordInfo recordInfo = null;

        // TODO: Test when server containing such records will be available

        if ((TypeConverter.byteToInt(fruData[offset + 1]) & 0xf) != 0x2) {
            throw new IllegalArgumentException("Invalid FRU record version");
        }

        FruMultiRecordType recordType = FruMultiRecordType
                .parseInt(TypeConverter.byteToInt(fruData[offset]));

        int length = TypeConverter.byteToInt(fruData[offset + 2]);

        int currentOffset = offset + 5;

        switch (recordType) {
        case PowerSupplyInformation:
            recordInfo = new PowerSupplyInfo(fruData, currentOffset);
            break;
        case DcOutput:
            recordInfo = new DcOutputInfo(fruData, currentOffset);
            break;
        case DcLoad:
            recordInfo = new DcLoadInfo(fruData, currentOffset);
            break;
        case ManagementAccessRecord:
            recordInfo = new ManagementAccessInfo(fruData, currentOffset, length);
            break;
        case BaseCompatibilityRecord:
            recordInfo = new BaseCompatibilityInfo(fruData, currentOffset, length);
            break;
        case ExtendedCompatibilityRecord:
            recordInfo = new ExtendedCompatibilityInfo(fruData, currentOffset, length);
            break;
        case OemRecord:
            recordInfo = new OemInfo(fruData, currentOffset, length);
            break;
        default:
            throw new IllegalArgumentException("Unsupported record type");
        }

        return recordInfo;
    }
}
