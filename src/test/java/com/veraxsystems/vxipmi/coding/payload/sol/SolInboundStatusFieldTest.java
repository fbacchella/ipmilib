/*
 * SolInboundStatusFieldTest.java
 * Created on 17.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload.sol;

import com.veraxsystems.vxipmi.common.TypeConverter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SolInboundStatusFieldTest {

    private SolInboundStatusField statusField;
    private SolAckState ackState;
    private Set<SolStatus> statuses;

    @Before
    public void setUp() throws Exception {
        this.ackState = SolAckState.ACK;
        this.statuses = new HashSet<SolStatus>();

        this.statusField = new SolInboundStatusField(ackState, statuses);
    }

    @Test
    public void createNewFromRawByte() throws Exception {
        byte raw = 0;
        raw = TypeConverter.setBitOnPosition(6, raw);

        statuses.add(SolStatus.RtsAsserted);
        statuses.add(SolStatus.CharacterTransferUnavailable);
        statuses.add(SolStatus.SolDeactivated);

        for (SolStatus status : statuses) {
            raw = TypeConverter.setBitOnPosition(status.getStatusNumber(), raw);
        }

        this.statusField = new SolInboundStatusField(raw);

        assertEquals(SolAckState.NACK, statusField.getAckState());
        assertThat(statusField.getStatuses(), containsInAnyOrder(statuses.toArray()));
    }

    @Test
    public void getAckStateReturnsPassedState() throws Exception {
        assertEquals(ackState, statusField.getAckState());
    }

    @Test
    public void getStatusesWhenEmptyStatusesReturnsEmptySet() throws Exception {
        assertThat(statusField.getStatuses(), is(empty()));
    }

    @Test
    public void getStatusesReturnsPassedStatuses() throws Exception {
        Set<SolStatus> expectedStatuses = new HashSet<SolStatus>() {{
            add(SolStatus.CharacterTransferUnavailable);
            add(SolStatus.Break);
            add(SolStatus.SolDeactivated);
            add(SolStatus.TransmitOverrun);
        }};

        statuses.addAll(expectedStatuses);

        assertThat(statusField.getStatuses(), containsInAnyOrder(expectedStatuses.toArray()));
    }

    @Test
    public void convertToByteWhenNack() throws Exception {
        byte expectedValue = TypeConverter.setBitOnPosition(6, (byte) 0);
        this.statusField = new SolInboundStatusField(SolAckState.NACK, statuses);

        assertEquals(expectedValue, statusField.convertToByte());
    }

    @Test
    public void convertToByteWhenAckAndNoStatuses() throws Exception {
        assertEquals(0, statusField.convertToByte());
    }

    @Test
    public void convertToByteWhenAckAndSomeStatuses() throws Exception {
        Set<SolStatus> newStatuses = new HashSet<SolStatus>() {{
            add(SolStatus.CharacterTransferUnavailable);
            add(SolStatus.RtsAsserted);
        }};

        statuses.addAll(newStatuses);

        byte expectedValue = 0;

        for (SolStatus status : newStatuses) {
            expectedValue = TypeConverter.setBitOnPosition(status.getStatusNumber(), expectedValue);
        }

        assertEquals(expectedValue, statusField.convertToByte());
    }
}