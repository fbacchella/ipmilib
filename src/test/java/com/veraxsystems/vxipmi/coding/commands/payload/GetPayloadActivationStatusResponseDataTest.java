/*
 * GetPayloadActivationStatusResponseDataTest.java
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

import com.veraxsystems.vxipmi.common.TypeConverter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GetPayloadActivationStatusResponseDataTest {

    private GetPayloadActivationStatusResponseData responseData;

    @Before
    public void setUp() throws Exception {
        this.responseData = new GetPayloadActivationStatusResponseData();
    }

    @Test
    public void shouldReturnInstanceCapacityThatWasPreviouslySet() throws Exception {
        byte expectedInstanceCapacity = 10;
        responseData.setInstanceCapacity(expectedInstanceCapacity);

        assertEquals(expectedInstanceCapacity, responseData.getInstanceCapacity());
    }

    @Test
    public void shouldReturnEmptyListWhenAllInstancesActivated() throws Exception {
        responseData.setAvailableInstances(new byte[] {TypeConverter.intToByte(255), TypeConverter.intToByte(255)});

        List<Byte> availableInstances = responseData.getAvailableInstances();
        assertNotNull("Should not return null available instances", availableInstances);
        assertTrue("Should return empty list", availableInstances.isEmpty());
    }

    @Test
    public void shouldReturnAllAvailableInstancesWhenMaxCapacityAndNoneIsActivated() throws Exception {
        byte instancesCapacity = 16;
        responseData.setInstanceCapacity(instancesCapacity);
        responseData.setAvailableInstances(new byte[2]);

        List<Byte> expectedAvailableInstances = new LinkedList<Byte>();

        for (int i = 0; i < instancesCapacity; ++i) {
            expectedAvailableInstances.add((byte) (i + 1));
        }

        assertEquals(expectedAvailableInstances, responseData.getAvailableInstances());
    }

    @Test
    public void shouldNotReturnAvailableInstancesGreaterThanCapacity() throws Exception {
        byte instancesCapacity = 6;
        responseData.setInstanceCapacity(instancesCapacity);
        responseData.setAvailableInstances(new byte[2]);

        List<Byte> expectedAvailableInstances = new LinkedList<Byte>();

        for (int i = 0; i < instancesCapacity; ++i) {
            expectedAvailableInstances.add((byte) (i + 1));
        }

        assertEquals(expectedAvailableInstances, responseData.getAvailableInstances());
    }

    @Test
    public void shouldReturnJustInstancesThatWasDeclaredAvailable() throws Exception {
        byte instancesCapacity = 13;
        responseData.setInstanceCapacity(instancesCapacity);
        responseData.setAvailableInstances(new byte[] {TypeConverter.intToByte(237), TypeConverter.intToByte(169)});

        List<Byte> expectedAvailableInstances = Arrays.asList((byte) 2, (byte) 5, (byte) 10, (byte) 11, (byte) 13);

        assertEquals(expectedAvailableInstances, responseData.getAvailableInstances());
    }
}