/*
 * IpmiLanMessage.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload.lan;

import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * A wrapper class for IPMI LAN message
 */
public abstract class IpmiLanMessage extends IpmiPayload {
    private byte responderAddress;

    protected byte networkFunction;

    private byte responderLogicalUnitNumber;

    private byte requesterAddress;

    private byte requesterLogicalUnitNumber;
    
    private byte sequenceNumber;

    private byte command;

    public void setResponderAddress(byte responderAddress) {
        this.responderAddress = responderAddress;
    }

    public byte getResponderAddress() {
        return responderAddress;
    }

    public void setNetworkFunction(NetworkFunction networkFunction) {
    	this.networkFunction = TypeConverter.intToByte(networkFunction.getCode());
    }

    public NetworkFunction getNetworkFunction() throws IllegalArgumentException {
        return NetworkFunction.parseInt(TypeConverter.byteToInt(networkFunction));
    }

    public void setResponderLogicalUnitNumber(byte responderLogicalUnitNumber) {
        this.responderLogicalUnitNumber = responderLogicalUnitNumber;
    }

    public byte getResponderLogicalUnitNumber() {
        return responderLogicalUnitNumber;
    }

    public void setSequenceNumber(byte sequenceAddress) {
        this.sequenceNumber = sequenceAddress;
    }

    public byte getSequenceNumber() {
        return sequenceNumber;
    }

    public void setRequesterAddress(byte requesterAddress) {
        this.requesterAddress = requesterAddress;
    }

    public byte getRequesterAddress() {
        return requesterAddress;
    }

    public void setRequesterLogicalUnitNumber(byte requesterLogicalUnitNumber) {
        this.requesterLogicalUnitNumber = requesterLogicalUnitNumber;
    }

    public byte getRequesterLogicalUnitNumber() {
        return requesterLogicalUnitNumber;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    public byte getCommand() {
        return command;
    }
    
    /**
     * Gets expected size of LAN message in bytes.
     */
    @Override
    public abstract int getPayloadLength();
    
    /**
     * Converts IpmiLanMessage to byte array. 
     */
    @Override
    public abstract byte[] getPayloadData();
        
    protected byte getChecksum1(byte[] message) {
        int checksum = 0;
        for(int i = 0; i < 2; ++i) {
            checksum = (checksum + TypeConverter.byteToInt(message[i])) % 256;
        }
        return (byte) -TypeConverter.intToByte(checksum);
    }
    
    protected byte getChecksum2(byte[] message) {
        int checksum = 0;
        for(int i = 3; i < message.length-1; ++i) {
            checksum = ((checksum + TypeConverter.byteToInt(message[i])) % 256);
        }
        return (byte)-TypeConverter.intToByte(checksum);
    }
    
    @Override
    public byte[] getIpmiCommandData() {
    	return getData();
    }
}
