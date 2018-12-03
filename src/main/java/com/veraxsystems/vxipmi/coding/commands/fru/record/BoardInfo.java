/*
 * BoardInfo.java 
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
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * FRU record containing Board info. <br>
 * This area provides Serial Number, Part Number, and other information about
 * the board that the FRU Information Device is located on.
 */
public class BoardInfo extends FruRecord {

    private Date mfgDate;

    private String boardManufacturer = "";

    private String boardProductName = "";

    private String boardSerialNumber = "";

    private String boardPartNumber = "";

    private byte[] fruFileId = new byte[0];

    private String[] customBoardInfo = new String[0];

    private static Logger logger = Logger.getLogger(BoardInfo.class);

    /**
     * Creates and populates record
     *
     * @param fruData
     *            - raw data containing record
     * @param offset
     *            - offset to the record in the data
     */
    public BoardInfo(final byte[] fruData, final int offset) {
        validateFruData(fruData[offset]);

        int languageCode = TypeConverter.byteToInt(fruData[offset + 2]);

        byte[] buffer = new byte[4];

        buffer[0] = fruData[offset + 3];
        buffer[1] = fruData[offset + 4];
        buffer[2] = fruData[offset + 5];
        buffer[3] = 0;

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,
                Locale.ENGLISH);
        try {
            setMfgDate(new Date(df.parse("01/01/96").getTime()
                    + ((long) TypeConverter.littleEndianByteArrayToInt(buffer))
                    * 60000l));
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }

        int partNumber = TypeConverter.byteToInt(fruData[offset + 6]);

        ArrayList<String> customInfo = readCustomInfo(fruData, languageCode, partNumber, offset + 7);

        customBoardInfo = new String[customInfo.size()];
        customBoardInfo = customInfo.toArray(customBoardInfo);
    }

    private void validateFruData(byte fruDatum) {
        if (fruDatum != 0x1) {
            throw new IllegalArgumentException("Invalid format version");
        }
    }

    private ArrayList<String> readCustomInfo(final byte[] fruData, final int languageCode, final int partNumber, final int offset) {
        ArrayList<String> customInfo = new ArrayList<String>();
        int index = 0;

        int currentOffset = offset;
        int currentPartNumber = partNumber;

        while (currentPartNumber != 0xc1 && currentOffset < fruData.length) {

            int partType = (currentPartNumber & 0xc0) >> 6;

            int partDataLength = (currentPartNumber & 0x3f);

            if (partDataLengthWithinBounds(fruData, currentOffset, partDataLength)) {

                byte[] partNumberData = new byte[partDataLength];

                System.arraycopy(fruData, currentOffset, partNumberData, 0,
                        partDataLength);

                currentOffset += partDataLength;

                switch (index) {
                case 0:
                    setBoardManufacturer(FruRecord.decodeString(partType,
                            partNumberData, languageCode != 0
                                    && languageCode != 25));
                    break;
                case 1:
                    setBoardProductName(FruRecord.decodeString(partType,
                            partNumberData, languageCode != 0
                                    && languageCode != 25));
                    break;
                case 2:
                    setBoardSerialNumber(FruRecord.decodeString(partType,
                            partNumberData, true));
                    break;
                case 3:
                    setBoardPartNumber(FruRecord.decodeString(partType,
                            partNumberData, languageCode != 0
                                    && languageCode != 25));
                    break;
                case 4:
                    setFruFileId(partNumberData);
                    break;
                default:
                    if (partDataLength == 0) {
                        currentPartNumber = TypeConverter.byteToInt(fruData[currentOffset]);
                        ++currentOffset;
                        continue;
                    }
                    customInfo.add(FruRecord.decodeString(partType,
                            partNumberData, languageCode != 0
                                    && languageCode != 25));
                    break;
                }
            }

            currentPartNumber = TypeConverter.byteToInt(fruData[currentOffset]);

            ++currentOffset;

            ++index;
        }

        return customInfo;
    }

    private boolean partDataLengthWithinBounds(byte[] fruData, int currentOfset, int partDataLength) {
        return partDataLength > 0 && partDataLength + currentOfset < fruData.length;
    }

    public Date getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(Date mfgDate) {
        this.mfgDate = mfgDate;
    }

    public String getBoardManufacturer() {
        return boardManufacturer;
    }

    public void setBoardManufacturer(String boardManufacturer) {
        this.boardManufacturer = boardManufacturer;
    }

    public String getBoardProductName() {
        return boardProductName;
    }

    public void setBoardProductName(String boardProductName) {
        this.boardProductName = boardProductName;
    }

    public String getBoardSerialNumber() {
        return boardSerialNumber;
    }

    public void setBoardSerialNumber(String boardSerialNumber) {
        this.boardSerialNumber = boardSerialNumber;
    }

    public String getBoardPartNumber() {
        return boardPartNumber;
    }

    public void setBoardPartNumber(String boardPartNumber) {
        this.boardPartNumber = boardPartNumber;
    }

    public byte[] getFruFileId() {
        return fruFileId;
    }

    public void setFruFileId(byte[] fruFileId) {
        this.fruFileId = fruFileId;
    }

    public String[] getCustomBoardInfo() {
        return customBoardInfo;
    }

    public void setCustomBoardInfo(String[] customBoardInfo) {
        this.customBoardInfo = customBoardInfo;
    }

}
