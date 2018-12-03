/*
 * GetChannelPayloadSupportCommand.java
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

import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanRequest;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.TypeConverter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.veraxsystems.vxipmi.coding.commands.CommandCodes.GET_CHANNEL_PAYLOAD_SUPPORT;

/**
 * A wrapper class for Get Channel Payload Support Command.
 */
public class GetChannelPayloadSupport extends IpmiCommandCoder {

    private byte channelNumber;

    /**
     * Initiates class for decoding.
     */
    public GetChannelPayloadSupport() {
        super(IpmiVersion.V20, new CipherSuite((byte) 0, (byte) 0, (byte) 0, (byte) 0), AuthenticationType.RMCPPlus);
    }

    /**
     * Initiates class for both encoding and decoding.
     *
     * @param channelNumber
     *            - must be 0h-Bh or Eh-Fh <br>
     *            Eh = retrieve information for channel this request was issued
     *            on
     */
    public GetChannelPayloadSupport(byte channelNumber) {
        super(IpmiVersion.V20, new CipherSuite((byte) 0, (byte) 0, (byte) 0, (byte) 0), AuthenticationType.RMCPPlus);
        setChannelNumber(channelNumber);
    }

    /**
     * Initiates class for both encoding and decoding.
     *
     * @param channelNumber
     *            must be 0h-Bh or Eh-Fh <br>
     *            Eh = retrieve information for channel this request was issued
     *            on
     * @param cipherSuite
     *            {@link CipherSuite} containing authentication,
     *            confidentiality and integrity algorithms for this session.
     * @param authenticationType
     *            Type of authentication used. Must be RMCPPlus for IPMI v2.0.
     */
    public GetChannelPayloadSupport(byte channelNumber, CipherSuite cipherSuite, AuthenticationType authenticationType) {
        super(IpmiVersion.V20, cipherSuite, authenticationType);
        setChannelNumber(channelNumber);
    }

    public byte getChannelNumber() {
        return channelNumber;
    }

    /**
     * Sets the channel number that will be put into IPMI command.
     *
     * @param channelNumber
     *            must be 0h-Bh or Eh-Fh <br>
     *            Eh = retrieve information for channel this request was issued
     *            on
     * @throws IllegalArgumentException
     */
    public void setChannelNumber(int channelNumber) {
        if (channelNumber < 0 || channelNumber > 0xF || channelNumber == 0xC
                || channelNumber == 0xD) {
            throw new IllegalArgumentException("Invalid channel number");
        }
        this.channelNumber = TypeConverter.intToByte(channelNumber);
    }

    @Override
    public byte getCommandCode() {
        return GET_CHANNEL_PAYLOAD_SUPPORT;
    }

    @Override
    public NetworkFunction getNetworkFunction() {
        return NetworkFunction.ApplicationRequest;
    }

    @Override
    protected IpmiPayload preparePayload(int sequenceNumber) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] requestData = new byte[1];

        requestData[0] = channelNumber;

        return new IpmiLanRequest(getNetworkFunction(), getCommandCode(), requestData, TypeConverter.intToByte(sequenceNumber));
    }

    @Override
    public ResponseData getResponseData(IpmiMessage message) throws IPMIException, NoSuchAlgorithmException, InvalidKeyException {
        if (!isCommandResponse(message)) {
            throw new IllegalArgumentException("This is not a response for Get Payload Info command");
        }

        if (!(message.getPayload() instanceof IpmiLanResponse)) {
            throw new IllegalArgumentException("Invalid response payload");
        }

        if (((IpmiLanResponse) message.getPayload()).getCompletionCode() != CompletionCode.Ok) {
            throw new IPMIException(((IpmiLanResponse) message.getPayload()).getCompletionCode());
        }

        GetChannelPayloadSupportResponseData data = new GetChannelPayloadSupportResponseData();

        byte[] responseData = message.getPayload().getData();

        data.setStandardPayloads(responseData[0]);
        data.setSessionSetupPayloads(responseData[1]);
        data.setOemPayloads(responseData[2]);

        return data;
    }

}
