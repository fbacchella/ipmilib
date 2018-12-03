/*
 * GetChannelPayloadSupportTest.java
 * Created on 2017-05-16
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.coding.commands.payload;

import com.veraxsystems.vxipmi.coding.commands.CommandCodes;
import com.veraxsystems.vxipmi.coding.commands.CommandsTestHelper;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanRequest;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityNone;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GetChannelPayloadSupportTest {

    private GetChannelPayloadSupport command;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.command = new GetChannelPayloadSupport();
    }

    @Test
    public void getCommandCodeReturnsValidCode() throws Exception {
        assertEquals(CommandCodes.GET_CHANNEL_PAYLOAD_SUPPORT, command.getCommandCode());
    }

    @Test
    public void getChannelNumberReturnsPreviouslyAssignedChannel() throws Exception {
        byte channelNumber = 10;
        command.setChannelNumber(channelNumber);
        assertEquals(channelNumber, command.getChannelNumber());
    }

    @Test
    public void getNetworkFunctionReturnsValidFunction() throws Exception {
        assertEquals(NetworkFunction.ApplicationRequest, command.getNetworkFunction());
    }

    @Test
    public void preparePayloadPreparesValidObject() throws Exception {
        byte channelNumber = 10;
        command.setChannelNumber(channelNumber);
        IpmiPayload payload = command.preparePayload(15);
        assertThat(payload, instanceOf(IpmiLanRequest.class));
        assertEquals(1, payload.getData().length);
        assertEquals(channelNumber, payload.getData()[0]);
    }

    @Test
    public void getResponseDataReturnsValidResponseObject() throws Exception {
        IpmiMessage message = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok, CommandCodes.GET_CHANNEL_PAYLOAD_SUPPORT, NetworkFunction.ApplicationResponse);
        ResponseData response = command.getResponseData(message);

        assertNotNull("Response data cannot be null",  response);
    }

    @Test
    public void getResponseDataThrowsExceptionWhenWrongCommandResponse() throws Exception {
        IpmiMessage message = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok, CommandCodes.CHASSIS_CONTROL, NetworkFunction.ApplicationResponse);
        expectedException.expect(IllegalArgumentException.class);

        command.getResponseData(message);
    }

    @Test
    public void getResponseDataThrowsExceptionWhenCompletionCodeNotOk() throws Exception {
        IpmiMessage message = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.CannotRespond, CommandCodes.GET_CHANNEL_PAYLOAD_SUPPORT, NetworkFunction.ApplicationResponse);
        expectedException.expect(IPMIException.class);

        command.getResponseData(message);
    }

    @Test
    public void getResponseDataThrowsExceptionWhenPayloadOfWrongType() throws Exception {
        IpmiLanRequest payload = new IpmiLanRequest(NetworkFunction.ApplicationRequest, CommandCodes.GET_CHANNEL_PAYLOAD_SUPPORT,
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0}, (byte) 1);
        IpmiMessage message = new Ipmiv20Message(new ConfidentialityNone());
        message.setPayload(payload);

        expectedException.expect(IllegalArgumentException.class);

        command.getResponseData(message);
    }

}