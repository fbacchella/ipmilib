/*
 * ActivateSolPayload.java
 * Created on 18.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.coding.commands.payload;

import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.SecurityConstants;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Concrete implementation of {@link ActivatePayload} command for {@link PayloadType#SOL}.
 */
public class ActivateSolPayload extends ActivatePayload {

    public ActivateSolPayload(CipherSuite cipherSuite, int payloadInstance) {
        super(IpmiVersion.V20, cipherSuite, AuthenticationType.RMCPPlus, payloadInstance);
    }

    @Override
    public PayloadType getPayloadType() {
        return PayloadType.Sol;
    }

    @Override
    protected byte[] prepareAuxilaryRequestData() {
        byte[] result = new byte[4];

        boolean isAuthenticationEnabled = getCipherSuite().getAuthenticationAlgorithm().getCode() != SecurityConstants.AA_RAKP_NONE;
        boolean isEncryptionEnabled = getCipherSuite().getConfidentialityAlgorithm().getCode() != SecurityConstants.CA_NONE;

        if (isEncryptionEnabled) {
            //encryption bit
            result[0] = TypeConverter.setBitOnPosition(7, result[0]);
        }

        if (isAuthenticationEnabled) {
            //authentication bit
            result[0] = TypeConverter.setBitOnPosition(6, result[0]);
        }

        /*The following settings determine what happens to serial alerts
        if IPMI over Serial and SOL are sharing the same baseboard serial controller.
        Bit 2 set and bit 3 reset mean, that serial/modem alerts are deferred while SOL active*/
        result[0] = TypeConverter.setBitOnPosition(2, result[0]);

        return result;
    }

    @Override
    protected ActivatePayloadResponseData createEmptyResponse() {
        return new ActivateSolPayloadResponseData();
    }
}
