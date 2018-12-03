/*
 * GetChannelPayloadSupportResponseData.java
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

import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.common.TypeConverter;

import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for Get Channel Payload Support response
 */
public class GetChannelPayloadSupportResponseData implements ResponseData {

    /**
     * Byte that carries information about supported Standard Payload Types.
     */
    private byte standardPayloads;

    /**
     * Byte that carries information about supported Session Setup Payload Types.
     */
    private byte sessionSetupPayloads;

    /**
     * Byte that carries information about supported OEM Payload Types.
     */
    private byte oemPayloads;

    /**
     * Set of supported {@link PayloadType}s.
     */
    private Set<PayloadType> supportedPayloads;

    public void setStandardPayloads(byte standardPayloads) {
        this.standardPayloads = standardPayloads;
    }

    public void setSessionSetupPayloads(byte sessionSetupPayloads) {
        this.sessionSetupPayloads = sessionSetupPayloads;
    }

    public void setOemPayloads(byte oemPayloads) {
        this.oemPayloads = oemPayloads;
    }

    public Set<PayloadType> getSupportedPayloads() {
        if (supportedPayloads == null) {
            initializeSupportedPayloads();
        }

        return supportedPayloads;
    }

    /**
     * Reads raw data from standardPayloads, sessionSetupPayloads and oemPayloads bytes and converts it to the {@link Set}
     * of supported {@link PayloadType}s
     */
    private void initializeSupportedPayloads() {
        supportedPayloads = new HashSet<PayloadType>();

        lookForGivenPayloads(standardPayloads, PayloadType.Ipmi, PayloadType.Sol, PayloadType.Oem);
        lookForGivenPayloads(sessionSetupPayloads, PayloadType.RmcpOpenSessionRequest, PayloadType.RmcpOpenSessionResponse,
                PayloadType.Rakp1, PayloadType.Rakp2, PayloadType.Rakp3, PayloadType.Rakp4);
        lookForGivenPayloads(oemPayloads, PayloadType.Oem0, PayloadType.Oem1, PayloadType.Oem2,
                PayloadType.Oem3, PayloadType.Oem4, PayloadType.Oem5, PayloadType.Oem6, PayloadType.Oem7);
    }

    /**
     * Checks if given payload types are enabled into given byte. If some of them is, adds this type to the output supportedPayloads.
     *
     * @param payloadsByte
     *          - byte carrying information about specific enabled payload types
     * @param types
     *          - array of types that we want to search in given byte
     */
    private void lookForGivenPayloads(byte payloadsByte, PayloadType... types) {
        for (int i = 0; i < types.length; i++) {
            if (TypeConverter.isBitSetOnPosition(i, payloadsByte)) {
                supportedPayloads.add(types[i]);
            }
        }
    }

}
