/*
 * ActivatePayloadResponseData.java
 * Created on 17.05.2017
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
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Wrapper for Activate Payload response
 */
public abstract class ActivatePayloadResponseData implements ResponseData {

    /**
     *  Maximum size of a payload data field from remote console to BMC.
     *  Excludes size of confidentiality header and trailer fields, if any.
     */
    private int inboundPayloadSize;

    /**
     *  Maximum size of a payload data field from BMC to remote console.
     *  Excludes size of confidentiality header and trailer fields, if any.
     */
    private int outboundPayloadSize;

    /**
     * UDP port number that payload can be transferred over.
     */
    private int payloadUdpPortNumber;

    /**
     * Payload VLAN number. FFFFh if VLAN addressing is not used.
     */
    private int payloadVlanNumber;

    public abstract void setAuxilaryInformationData(byte[] auxilaryInformationData);

    public int getInboundPayloadSize() {
        return inboundPayloadSize;
    }

    public void setInboundPayloadSize(byte[] inboundPayloadSizeData) {
        this.inboundPayloadSize = TypeConverter.littleEndianWordToInt(inboundPayloadSizeData);
    }

    public int getOutboundPayloadSize() {
        return outboundPayloadSize;
    }

    public void setOutboundPayloadSize(byte[] outboundPayloadSizeData) {
        this.outboundPayloadSize = TypeConverter.littleEndianWordToInt(outboundPayloadSizeData);
    }

    public int getPayloadUdpPortNumber() {
        return payloadUdpPortNumber;
    }

    public void setPayloadUdpPortNumber(byte[] payloadUdpPortNumberData) {
        this.payloadUdpPortNumber = TypeConverter.littleEndianWordToInt(payloadUdpPortNumberData);
    }

    public int getPayloadVlanNumber() {
        return payloadVlanNumber;
    }

    public void setPayloadVlanNumber(byte[] payloadVlanNumberData) {
        this.payloadVlanNumber = TypeConverter.littleEndianWordToInt(payloadVlanNumberData);
    }

}
