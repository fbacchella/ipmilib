/*
 * ActivateSolPayloadTest.java
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
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityNone;
import com.veraxsystems.vxipmi.coding.security.SecurityConstants;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.connection.Connection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ActivateSolPayloadTest {

    private ActivateSolPayload activatePayload;
    private int payloadInstance = 2;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.activatePayload = new ActivateSolPayload(Connection.getDefaultCipherSuite(), payloadInstance);
    }

    @Test
    public void getCommandCodeReturnsValidCode() throws Exception {
        assertEquals(CommandCodes.ACTIVATE_PAYLOAD, activatePayload.getCommandCode());
    }

    @Test
    public void getNetworkFunctionReturnsProperFunction() throws Exception {
        assertEquals(NetworkFunction.ApplicationRequest, activatePayload.getNetworkFunction());
    }

    @Test
    public void createEmptyResposeDataReturnsSolResponseData() throws Exception {
        assertThat(activatePayload.createEmptyResponse(), instanceOf(ActivateSolPayloadResponseData.class));
    }

    @Test
    public void getResponseDataReturnsNonNullValidObject() throws Exception {
        IpmiMessage ipmiMessage = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok, CommandCodes.ACTIVATE_PAYLOAD, NetworkFunction.ApplicationResponse);
        ResponseData responseData = activatePayload.getResponseData(ipmiMessage);

        Class<?> expectedResponseDataType = activatePayload.createEmptyResponse().getClass();

        assertNotNull(responseData);
        assertThat(responseData, instanceOf(expectedResponseDataType));
    }

    @Test
    public void getResponseDataThrowsExceptionWhenWrongCommandResponse() throws Exception {
        IpmiMessage ipmiMessage = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok, CommandCodes.CHASSIS_CONTROL, NetworkFunction.ApplicationResponse);

        expectedException.expect(IllegalArgumentException.class);

        activatePayload.getResponseData(ipmiMessage);
    }

    @Test
    public void getResponseDataThrowsExceptionWhenCompletionCodeNotOk() throws Exception {
        IpmiMessage ipmiMessage = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.UnspecifiedError, CommandCodes.ACTIVATE_PAYLOAD, NetworkFunction.ApplicationResponse);

        expectedException.expect(IPMIException.class);

        activatePayload.getResponseData(ipmiMessage);
    }

    @Test
    public void getResponseDataThrowsExceptionWhenPayloadOfWrongType() throws Exception {
        IpmiLanRequest payload = new IpmiLanRequest(NetworkFunction.ApplicationRequest, CommandCodes.ACTIVATE_PAYLOAD,
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0}, (byte) 1);
        IpmiMessage ipmiMessage = new Ipmiv20Message(new ConfidentialityNone());
        ipmiMessage.setPayload(payload);

        expectedException.expect(IllegalArgumentException.class);

        activatePayload.getResponseData(ipmiMessage);
    }

    @Test
    public void getResponseDataFillsActualData() throws Exception {
        IpmiMessage ipmiMessage = CommandsTestHelper.prepareTestResposeMessage(CompletionCode.Ok, CommandCodes.ACTIVATE_PAYLOAD, NetworkFunction.ApplicationResponse);
        ipmiMessage.getPayload().setData(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

        ActivatePayloadResponseData responseData = (ActivatePayloadResponseData) activatePayload.getResponseData(ipmiMessage);
        assertThat(responseData.getInboundPayloadSize(), is(greaterThan(0)));
        assertThat(responseData.getOutboundPayloadSize(), is(greaterThan(0)));
        assertThat(responseData.getPayloadUdpPortNumber(), is(greaterThan(0)));
        assertThat(responseData.getPayloadVlanNumber(), is(greaterThan(0)));
    }

    @Test
    public void preparePayloadReturnsNonNullPayload() throws Exception {
        assertNotNull("Return payload cannot be null", activatePayload.preparePayload(1));
    }

    @Test
    public void preparePayloadReturnsDataOfValidLength() throws Exception {
        IpmiPayload payload = activatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertNotNull("Prepared payload data cannot be null", rawData);
        assertEquals(6, rawData.length);
    }

    @Test
    public void getPayloadTypeReturnsPassedType() throws Exception {
        assertEquals(PayloadType.Sol, activatePayload.getPayloadType());
    }

    @Test
    public void preparePayloadReturnsValidPayloadTypeAndInstance() throws Exception {
        byte expectedPayloadTypeByte = TypeConverter.intToByte(PayloadType.Sol.getCode());
        int expectedPayloadInstanceByte = TypeConverter.intToByte(payloadInstance);

        IpmiPayload payload = activatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertEquals(expectedPayloadTypeByte, rawData[0]);
        assertEquals(expectedPayloadInstanceByte, rawData[1]);
    }

    @Test
    public void preparePayloadReservedBytesAre0() throws Exception {
        IpmiPayload payload = activatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertEquals(0, rawData[3]);
        assertEquals(0, rawData[4]);
        assertEquals(0, rawData[5]);
    }

    @Test
    public void preparePayloadEncryptionAndAuthenticationDisabled() throws Exception {
        CipherSuite cipherSuite = new CipherSuite((byte) 1, SecurityConstants.AA_RAKP_NONE, SecurityConstants.CA_NONE, SecurityConstants.IA_NONE);
        activatePayload.setCipherSuite(cipherSuite);

        IpmiPayload payload = activatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertFalse("Bit representing authentication should not be set when authentication is deactivated", TypeConverter.isBitSetOnPosition(6, rawData[2]));
        assertFalse("Bit representing encryption should not be set when encryption is deactivated", TypeConverter.isBitSetOnPosition(7, rawData[2]));
    }

    @Test
    public void preparePayloadEncryptionActive() throws Exception {
        CipherSuite cipherSuite = new CipherSuite((byte) 1, SecurityConstants.AA_RAKP_NONE, SecurityConstants.CA_AES_CBC128, SecurityConstants.IA_NONE);
        activatePayload.setCipherSuite(cipherSuite);

        IpmiPayload payload = activatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertTrue("Bit representing encryption should be set when encryption is activated", TypeConverter.isBitSetOnPosition(7, rawData[2]));
    }

    @Test
    public void preparePayloadAuthenticationActive() throws Exception {
        CipherSuite cipherSuite = new CipherSuite((byte) 1, SecurityConstants.AA_RAKP_HMAC_SHA1, SecurityConstants.CA_NONE, SecurityConstants.IA_NONE);
        activatePayload.setCipherSuite(cipherSuite);

        IpmiPayload payload = activatePayload.preparePayload(1);
        byte[] rawData = payload.getData();

        assertTrue("Bit representing authentication should be set when authentication is activated", TypeConverter.isBitSetOnPosition(6, rawData[2]));
    }

}