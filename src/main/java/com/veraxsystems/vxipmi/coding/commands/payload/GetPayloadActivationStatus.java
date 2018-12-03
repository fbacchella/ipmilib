/*
 * GetPayloadActivationStatus.java
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
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.MessageComposer;
import com.veraxsystems.vxipmi.common.MessageReader;
import com.veraxsystems.vxipmi.common.TypeConverter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Wrapper for Get Payload Activation Status Command.
 */
public class GetPayloadActivationStatus extends IpmiCommandCoder {

    private static final int REQUEST_DATA_LENGTH = 1;
    private static final int INSTANCE_CAPACITY_FIELD_LENGTH = 1;
    private static final int AVAILABLE_INSTANCES_FIELD_LENGTH = 2;

    private final PayloadType payloadType;

    public GetPayloadActivationStatus(CipherSuite cipherSuite, PayloadType payloadType) {
        super(IpmiVersion.V20, cipherSuite, AuthenticationType.RMCPPlus);
        this.payloadType = payloadType;
    }

    public GetPayloadActivationStatus(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    @Override
    public byte getCommandCode() {
        return CommandCodes.GET_PAYLOAD_ACTIVATION_STATUS;
    }

    @Override
    public NetworkFunction getNetworkFunction() {
        return NetworkFunction.ApplicationRequest;
    }

    @Override
    protected IpmiPayload preparePayload(int sequenceNumber) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] message = MessageComposer.get(REQUEST_DATA_LENGTH)
                .appendField(TypeConverter.intToByte(payloadType.getCode()))
                .getMessage();

        return new IpmiLanRequest(getNetworkFunction(), getCommandCode(), message, TypeConverter.intToByte(sequenceNumber));
    }

    @Override
    public ResponseData getResponseData(IpmiMessage message) throws IPMIException, NoSuchAlgorithmException, InvalidKeyException {
        if (!isCommandResponse(message)) {
            throw new IllegalArgumentException("This is not a response for Get Payload Activation Status");
        }

        if (!(message.getPayload() instanceof IpmiLanResponse)) {
            throw new IllegalArgumentException("Invalid response payload");
        }

        if (((IpmiLanResponse) message.getPayload()).getCompletionCode() != CompletionCode.Ok) {
            throw new IPMIException(((IpmiLanResponse) message.getPayload()).getCompletionCode());
        }

        MessageReader messageReader = new MessageReader(message.getPayload().getData());

        GetPayloadActivationStatusResponseData responseData = new GetPayloadActivationStatusResponseData();

        byte instaceCapacity = messageReader.readNextField(INSTANCE_CAPACITY_FIELD_LENGTH)[0];
        responseData.setInstanceCapacity(instaceCapacity);

        byte[] availableInstancesData = messageReader.readNextField(AVAILABLE_INSTANCES_FIELD_LENGTH);
        responseData.setAvailableInstances(availableInstancesData);

        return responseData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetPayloadActivationStatus that = (GetPayloadActivationStatus) o;

        if (payloadType != that.payloadType) {
            return false;
        }

        return getCipherSuite().equals(that.getCipherSuite());
    }

    @Override
    public int hashCode() {
        int result = payloadType.getCode();
        result = 31 * result + (getCipherSuite() != null ? getCipherSuite().hashCode() : 0);
        return result;
    }
}
