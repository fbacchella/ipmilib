/*
 * SolOutboundOperationFieldTest.java
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

public class SolOutboundOperationFieldTest {

    private SolOutboundOperationField operationField;
    private SolAckState ackState;
    private Set<SolOperation> operations;

    @Before
    public void setUp() throws Exception {
        this.ackState = SolAckState.ACK;
        this.operations = new HashSet<SolOperation>();

        this.operationField = new SolOutboundOperationField(ackState, operations);
    }

    @Test
    public void createNewFromRawByte() throws Exception {
        byte raw = 0;
        raw = TypeConverter.setBitOnPosition(6, raw);

        operations.add(SolOperation.DCD_DSR);
        operations.add(SolOperation.Break);

        for (SolOperation operation : operations) {
            raw = TypeConverter.setBitOnPosition(operation.getOperationNumber(), raw);
        }

        this.operationField = new SolOutboundOperationField(raw);

        assertEquals(SolAckState.NACK, operationField.getAckState());
        assertThat(operationField.getOperations(), containsInAnyOrder(operations.toArray()));
    }

    @Test
    public void getAckStateReturnsPassedState() throws Exception {
        assertEquals(ackState, operationField.getAckState());
    }

    @Test
    public void getOperationsWhenEmptyOperationsReturnsEmptySet() throws Exception {
        assertThat(operationField.getOperations(), is(empty()));
    }

    @Test
    public void getOperationsReturnsPassedOperations() throws Exception {
        Set<SolOperation> expectedOperations = new HashSet<SolOperation>() {{
            add(SolOperation.CTS);
            add(SolOperation.Break);
            add(SolOperation.FlushInbound);
        }};

        operations.addAll(expectedOperations);

        assertThat(operationField.getOperations(), containsInAnyOrder(expectedOperations.toArray()));
    }

    @Test
    public void convertToByteWhenAckAndNoOperations() throws Exception {
        assertEquals(0, operationField.convertToByte());
    }

    @Test
    public void convertToByteWhenNack() throws Exception {
        byte expectedValue = TypeConverter.setBitOnPosition(6, (byte) 0);
        this.operationField = new SolOutboundOperationField(SolAckState.NACK, operations);

        assertEquals(expectedValue, operationField.convertToByte());
    }

    @Test
    public void convertToByteWhenAckAndSomeOperations() throws Exception {
        Set<SolOperation> newOperation = new HashSet<SolOperation>() {{
            add(SolOperation.Break);
            add(SolOperation.DCD_DSR);
        }};

        operations.addAll(newOperation);

        byte expectedValue = 0;

        for (SolOperation operation : newOperation) {
            expectedValue = TypeConverter.setBitOnPosition(operation.getOperationNumber(), expectedValue);
        }

        assertEquals(expectedValue, operationField.convertToByte());
    }
}