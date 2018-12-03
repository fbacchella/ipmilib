/*
 * MessageComposer.java
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
 * Class used for composing byte messages aout from smaller byte subarrays.
 */
public class MessageComposer {

    private final byte[] message;
    private int pointer = 0;

    public static MessageComposer get(int messageSize) {
        return new MessageComposer(messageSize);
    }

    private MessageComposer(int messageSize) {
        if (messageSize < 0) {
            throw new IllegalArgumentException("Message size cannot be negative");
        }

        this.message = new byte[messageSize];
    }

    /**
     * Append single-byte field to the message.
     *
     * @param fieldData
     *          single byte containing data that should be appended
     */
    public MessageComposer appendField(byte fieldData) {
        message[pointer++] = fieldData;

        return this;
    }

    /**
     * Append byte arrach field to the message.
     *
     * @param fieldData
     *          byte array containing data that should be appended
     */
    public MessageComposer appendField(byte[] fieldData) {
        System.arraycopy(fieldData, 0, message, pointer, fieldData.length);

        pointer += fieldData.length;

        return this;
    }

    /**
     * Returns final message consisting all messages appended till now.
     *
     * @return composed message
     */
    public byte[] getMessage() {
        return message;
    }
}
