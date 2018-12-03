/*
 * DeactivatePayload.java
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
import com.veraxsystems.vxipmi.common.TypeConverter;
import org.apache.log4j.Logger;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * A wrapper class for Deactivate Payload Command.
 */
public class DeactivatePayload extends IpmiCommandCoder {

    private static final Logger logger = Logger.getLogger(DeactivatePayload.class);

    private static final int REQUEST_DATA_LENGTH = 6;

    /**
     * Payload type to be deactivated by this command.
     */
    private final PayloadType payloadType;

    /**
     * Number of payload instance to use when activating.
     */
    private final int payloadInstance;

    public DeactivatePayload(CipherSuite cipherSuite, PayloadType payloadType, int payloadInstance) {
        super(IpmiVersion.V20, cipherSuite, AuthenticationType.RMCPPlus);
        this.payloadType = payloadType;
        this.payloadInstance = payloadInstance;
    }

    @Override
    public byte getCommandCode() {
        return CommandCodes.DEACTIVATE_PAYLOAD;
    }

    /**
     * Creates new instance of the {@link DeactivatePayload} command for deactivating given {@link PayloadType}.
     *
     * @param payloadType
     *          payload to be deactivated.
     */
    public DeactivatePayload(PayloadType payloadType, int payloadInstance) {
        this.payloadType = payloadType;
        this.payloadInstance = payloadInstance;
    }

    @Override
    public NetworkFunction getNetworkFunction() {
        return NetworkFunction.ApplicationRequest;
    }

    @Override
    protected IpmiPayload preparePayload(int sequenceNumber) throws NoSuchAlgorithmException, InvalidKeyException {
        //We put just 2 bytes into the 6-bytes array, as specification for this command says to leave the rest 4 bytes as zeros
        byte[] message = MessageComposer.get(REQUEST_DATA_LENGTH)
                .appendField(TypeConverter.intToByte(payloadType.getCode()))
                .appendField(TypeConverter.intToByte(payloadInstance))
                .getMessage();

        return new IpmiLanRequest(getNetworkFunction(), getCommandCode(), message, TypeConverter.intToByte(sequenceNumber));
    }

    @Override
    public ResponseData getResponseData(IpmiMessage message) throws IPMIException, NoSuchAlgorithmException, InvalidKeyException {
        if (!isCommandResponse(message)) {
            throw new IllegalArgumentException("This is not a response for Deactivate Payload command");
        }

        if (!(message.getPayload() instanceof IpmiLanResponse)) {
            throw new IllegalArgumentException("Invalid response payload");
        }

        CompletionCode completionCode = ((IpmiLanResponse) message.getPayload()).getCompletionCode();

        if (completionCode != CompletionCode.Ok) {
            DeactivatePayloadCompletionCode specificCompletionCode = DeactivatePayloadCompletionCode.parseInt(completionCode.getCode());

            if (specificCompletionCode == DeactivatePayloadCompletionCode.PAYLOAD_ALREADY_DEACTIVATED) {
                logger.warn(specificCompletionCode.getMessage());
            } else {
                throw new IPMIException(((IpmiLanResponse) message.getPayload()).getCompletionCode());
            }
        }

        return new DeactivatePayloadResponseData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeactivatePayload that = (DeactivatePayload) o;

        if (payloadInstance != that.payloadInstance) {
            return false;
        }

        if (payloadType != that.payloadType) {
            return false;
        }

        return getCipherSuite().equals(that.getCipherSuite());
    }

    @Override
    public int hashCode() {
        int result = payloadInstance;
        result = 31 * result + payloadType.getCode();
        result = 31 * result + (getCipherSuite() == null ? 0 : getCipherSuite().hashCode());
        return result;
    }
}
