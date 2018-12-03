/*
 * GetPayloadActivationStatusTest.java
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
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanRequest;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityNone;
import com.veraxsystems.vxipmi.common.TypeConverter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class GetPayloadActivationStatusTest {

    private final PayloadType payloadType = PayloadType.Sol;
    private GetPayloadActivationStatus getPayloadActivationStatus;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.getPayloadActivationStatus = new GetPayloadActivationStatus(payloadType);
    }

    @Test
    public void shouldReturnProperCommandCode() throws Exception {
        assertEquals(CommandCodes.GET_PAYLOAD_ACTIVATION_STATUS, getPayloadActivationStatus.getCommandCode());
    }

    @Test
    public void shouldReturnProperNetworkFunction() throws Exception {
        assertEquals(NetworkFunction.ApplicationRequest, getPayloadActivationStatus.getNetworkFunction());
    }

    @Test
    public void shouldReturnNonNullProperResponseDataObject() throws Exception {
        IpmiMessage message = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok,
                CommandCodes.GET_PAYLOAD_ACTIVATION_STATUS, NetworkFunction.ApplicationResponse);

        ResponseData responseData = getPayloadActivationStatus.getResponseData(message);

        assertNotNull("Should not return empty response data", responseData);
        assertThat(responseData, instanceOf(GetPayloadActivationStatusResponseData.class));
    }

    @Test
    public void shouldThrowExceptionWhenGetResponseDataAndWrongCommandCode() throws Exception {
        IpmiMessage message = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok,
                CommandCodes.ACTIVATE_PAYLOAD, NetworkFunction.ApplicationResponse);

        expectedException.expect(IllegalArgumentException.class);

        getPayloadActivationStatus.getResponseData(message);
    }

    @Test
    public void shouldThrowExceptionWhenGetResponseDataAndWrongPayloadType() throws Exception {
        IpmiLanRequest payload = new IpmiLanRequest(NetworkFunction.ApplicationRequest, CommandCodes.GET_PAYLOAD_ACTIVATION_STATUS,
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0}, (byte) 1);
        IpmiMessage message = new Ipmiv20Message(new ConfidentialityNone());
        message.setPayload(payload);

        expectedException.expect(IllegalArgumentException.class);

        getPayloadActivationStatus.getResponseData(message);
    }

    @Test
    public void shouldThrowExceptionWhenGetResponseAndCommandCodeNotOk() throws Exception {
        IpmiMessage message = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.DataNotPresent,
                CommandCodes.GET_PAYLOAD_ACTIVATION_STATUS, NetworkFunction.ApplicationResponse);

        expectedException.expect(IPMIException.class);

        getPayloadActivationStatus.getResponseData(message);
    }

    @Test
    public void shouldFillResponseDataWithActualDataFromMessage() throws Exception {
        byte expetcedInstanceCapacity = 12;
        List<Byte> expectedAvailableInstances = Arrays.asList((byte) 1, (byte) 9, (byte) 10);

        byte[] rawData = new byte[] {expetcedInstanceCapacity, TypeConverter.intToByte(254), TypeConverter.intToByte(252)};

        IpmiMessage message = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok,
                CommandCodes.GET_PAYLOAD_ACTIVATION_STATUS, NetworkFunction.ApplicationResponse);
        message.getPayload().setData(rawData);

        GetPayloadActivationStatusResponseData responseData = (GetPayloadActivationStatusResponseData) getPayloadActivationStatus.getResponseData(message);


        assertEquals(expetcedInstanceCapacity, responseData.getInstanceCapacity());
        assertEquals(expectedAvailableInstances, responseData.getAvailableInstances());
    }

    @Test
    public void shouldReturnNotNullProperPayloadWhenPreparePayload() throws Exception {
        IpmiPayload payload = getPayloadActivationStatus.preparePayload(1);

        assertNotNull("Should not return null payload", payload);
        assertThat(payload, instanceOf(IpmiLanRequest.class));
    }

    @Test
    public void shouldReturnDataWithProperLengthWhenPreparePayload() throws Exception {
        IpmiPayload payload = getPayloadActivationStatus.preparePayload(1);
        byte[] rawData = payload.getData();

        assertNotNull("Data in prepared payload should not be null", rawData);
        assertEquals(1, rawData.length);
    }

    @Test
    public void shouldReturnValidPayloadTypeWhenPreparePayload() throws Exception {
        byte expectedPayloadTypeByte = TypeConverter.intToByte(payloadType.getCode());

        IpmiPayload payload = getPayloadActivationStatus.preparePayload(1);
        byte[] rawData = payload.getData();

        assertEquals(expectedPayloadTypeByte, rawData[0]);
    }
}