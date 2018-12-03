/*
 * CloseSession.java 
 * Created on 2011-08-10
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.session;

import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanRequest;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.TypeConverter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Wrapper for Close Session request
 */
public class CloseSession extends IpmiCommandCoder {

    private int sessionId;

    /**
     * Initiates CloseSession for both encoding and decoding.
     *
     * @param version
     *            - IPMI version of the command.
     * @param cipherSuite
     *            - {@link CipherSuite} containing authentication,
     *            confidentiality and integrity algorithms for this session.
     * @param authenticationType
     *            - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
     * @param sessionId
     *            - Generated by managed system ID of the session to close.
     */
    public CloseSession(IpmiVersion version, CipherSuite cipherSuite,
            AuthenticationType authenticationType, int sessionId) {
        super(version, cipherSuite, authenticationType);

        if (sessionId == 0) {
            throw new IllegalArgumentException("Cannot close session '0'");
        }

        this.sessionId = sessionId;
    }

    @Override
    public byte getCommandCode() {
        return TypeConverter.intToByte(0x3c);
    }

    @Override
    public NetworkFunction getNetworkFunction() {
        return NetworkFunction.ApplicationRequest;
    }

    @Override
    protected IpmiPayload preparePayload(int sequenceNumber)
            throws NoSuchAlgorithmException, InvalidKeyException {

        byte[] payload = TypeConverter.intToLittleEndianByteArray(sessionId);

        return new IpmiLanRequest(getNetworkFunction(), getCommandCode(),
                payload, TypeConverter.intToByte(sequenceNumber));
    }

    @Override
    public ResponseData getResponseData(IpmiMessage message) throws IPMIException, NoSuchAlgorithmException, InvalidKeyException {
        return new CloseSessionResponseData();
    }

}
