/*
 * GetPayloadActivationStatusResponseData.java
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

import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.common.TypeConverter;

import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper for Get Payload Activation Status response.
 */
public class GetPayloadActivationStatusResponseData implements ResponseData {

    /**
     * Number of instances of given payload type that can be simultaneously activated on BMC.
     */
    private byte instanceCapacity;

    /**
     * List of instance ID's that are still available (not activated).
     */
    private List<Byte> availableInstances;

    public byte getInstanceCapacity() {
        return instanceCapacity;
    }

    public void setInstanceCapacity(byte instanceCapacity) {
        this.instanceCapacity = instanceCapacity;
    }

    public List<Byte> getAvailableInstances() {
        List<Byte> actuallyAvailableInstances = new LinkedList<Byte>();

        for (Byte instanceId : availableInstances) {
            if (instanceId <= instanceCapacity) {
                actuallyAvailableInstances.add(instanceId);
            }
        }

        return actuallyAvailableInstances;
    }

    public void setAvailableInstances(byte[] availableInstancesData) {
        this.availableInstances = getAvailableInstancesFromBytes(availableInstancesData);
    }

    private List<Byte> getAvailableInstancesFromBytes(byte[] availableInstancesData) {
        List<Byte> result = new LinkedList<Byte>();

        List<Byte> instancesFromFirstByte = checkForAvailableInstancesInByte(availableInstancesData[0], 0);
        List<Byte> instancesFromSecondByte = checkForAvailableInstancesInByte(availableInstancesData[1], 8);

        result.addAll(instancesFromFirstByte);
        result.addAll(instancesFromSecondByte);

        return result;
    }

    private List<Byte> checkForAvailableInstancesInByte(byte availableInstancesByte, int instanceIdOffset) {
        List<Byte> result = new LinkedList<Byte>();

        for (int i = 0; i < 8; ++i) {
            if (!TypeConverter.isBitSetOnPosition(i, availableInstancesByte)) {
                result.add((byte) (i + 1 + instanceIdOffset));
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "GetPayloadActivationStatusResponseData{" +
                "instanceCapacity=" + getInstanceCapacity() +
                ", availableInstances=" + getAvailableInstances() +
                '}';
    }
}
