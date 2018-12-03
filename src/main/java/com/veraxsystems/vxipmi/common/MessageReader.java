/*
 * ByteArraysReader.java
 * Created on 18.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.common;

/**
 * Class used for reading subsequent byte subarrays from message, that is also a byte array.
 */
public class MessageReader {

    private final byte[] message;
    private int pointer = 0;

    /**
     * Create new instance of {@link MessageReader}, that will operate on given message.
     */
    public MessageReader(byte[] message) {
        if (message == null) {
            throw new NullPointerException("Message cannot be null");
        }

        this.message = message;
    }

    public byte[] readNextField(int fieldLength) {
        byte[] fieldData = new byte[fieldLength];
        System.arraycopy(message, pointer, fieldData, 0, fieldLength);

        pointer += fieldLength;

        return fieldData;
    }

}
