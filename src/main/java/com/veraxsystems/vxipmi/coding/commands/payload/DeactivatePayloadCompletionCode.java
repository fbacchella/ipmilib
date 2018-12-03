/*
 * DeactivatePayloadSpecificCompletionCode.java
 * Created on 01-06-2017
 *
 * Copyright (c) Verax Systems 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.coding.commands.payload;

/**
 * Enumeration of specific completion codes for {@link DeactivatePayload} command.
 */
public enum DeactivatePayloadCompletionCode {

    PAYLOAD_ALREADY_DEACTIVATED(0x80, "Payload already deactivated"),
    PAYLOAD_TYPE_IS_DISABLED(0x81, "Given payload type is not configured to be enabled for activation");


    private final int code;
    private final String message;

    DeactivatePayloadCompletionCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static DeactivatePayloadCompletionCode parseInt(int code) {
        for (DeactivatePayloadCompletionCode completionCode : values()) {
            if (completionCode.getCode() == code) {
                return completionCode;
            }
        }

        return null;
    }
}
