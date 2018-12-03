/*
 * CommandsTestHelper.java
 * Created on 17.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.coding.commands;

import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityNone;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Helper class for classes testing IPMI commands.
 */
public class CommandsTestHelper {

    public static IpmiMessage prepareTestResposeMessage(CompletionCode completionCode, byte commandCode, NetworkFunction networkFunction) {
        IpmiLanResponse payload = new IpmiLanResponse(new byte[] {0, 0, 0, 0, 0, 0, 0});
        payload.setCompletionCode(TypeConverter.intToByte(completionCode.getCode()));
        payload.setCommand(commandCode);
        payload.setNetworkFunction(networkFunction);
        payload.setData(new byte[100]);

        IpmiMessage message = new Ipmiv20Message(new ConfidentialityNone());
        message.setPayload(payload);
        return message;
    }

    private CommandsTestHelper() {}
}
