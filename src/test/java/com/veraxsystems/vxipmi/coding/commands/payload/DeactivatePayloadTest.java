/*
 * DeactivatePayloadTest.java
 * Created on 01.06.2017
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
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.common.TypeConverter;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DeactivatePayloadTest {

    private final PayloadType payloadType = PayloadType.Sol;
    private DeactivatePayload deactivatePayload;
    private int payloadInstance = 3;


    @Before
    public void setUp() throws Exception {
        this.deactivatePayload = new DeactivatePayload(payloadType, payloadInstance);
    }

    @Test
    public void shouldReturnProperCommandCode() throws Exception {
        assertEquals(CommandCodes.DEACTIVATE_PAYLOAD, deactivatePayload.getCommandCode());
    }

    @Test
    public void shouldReturnProperNetworkFunction() throws Exception {
        assertEquals(NetworkFunction.ApplicationRequest, deactivatePayload.getNetworkFunction());
    }

    @Test
    public void shouldReturnNonNullResponseDataOfProperType() throws Exception {
        IpmiMessage message = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok, CommandCodes.DEACTIVATE_PAYLOAD, NetworkFunction.ApplicationResponse);
        ResponseData response = deactivatePayload.getResponseData(message);

        assertNotNull("Should not return null response data", response);
        assertThat(response, instanceOf(DeactivatePayloadResponseData.class));
    }

    @Test
    public void shouldReturnNonNullPayloadWhenPreparePayload() throws Exception {
        assertNotNull("Prepared payload object should not be null", deactivatePayload.preparePayload(1));
    }

    @Test
    public void shouldReturnDataWithProperLengthWhenPreparePayload() throws Exception {
        IpmiPayload payload = deactivatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertNotNull("Data in prepared payload should not be null", rawData);
        assertEquals(6, rawData.length);
    }

    @Test
    public void shouldReturnValidPayloadTypeAndInstanceWhenPreparePayload() throws Exception {
        byte expectedPayloadTypeByte = TypeConverter.intToByte(payloadType.getCode());
        int expectedPayloadInstanceByte = TypeConverter.intToByte(payloadInstance);

        IpmiPayload payload = deactivatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertEquals(expectedPayloadTypeByte, rawData[0]);
        assertEquals(expectedPayloadInstanceByte, rawData[1]);
    }

    @Test
    public void shouldReturnReservedBytesAs0WhenPreparePayload() throws Exception {
        IpmiPayload payload = deactivatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertEquals(0, rawData[2]);
        assertEquals(0, rawData[3]);
        assertEquals(0, rawData[4]);
        assertEquals(0, rawData[5]);
    }
}