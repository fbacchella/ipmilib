/*
 * SolResponseData.java
 * Created on 26.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.sol;

import com.veraxsystems.vxipmi.coding.payload.sol.SolAckState;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SolResponseDataTest {

    @Test
    public void getRequestSequenceNumberReturnsPassedNumber() throws Exception {
        byte sequenceNumber = 12;

        SolResponseData solResponseData = new SolResponseData(sequenceNumber, SolAckState.ACK, new HashSet<SolStatus>(), (byte) 5);
        assertEquals(sequenceNumber, solResponseData.getRequestSequenceNumber());
    }

    @Test
    public void getAcknowledgeStateReturnsPassedState() throws Exception {
        SolAckState ackState = SolAckState.ACK;

        SolResponseData solResponseData = new SolResponseData((byte) 1, ackState, new HashSet<SolStatus>(), (byte) 10);
        assertEquals(ackState, solResponseData.getAcknowledgeState());
    }

    @Test
    public void getStatusesReturnsPassedStatuses() throws Exception {
        Set<SolStatus> statuses = new HashSet<SolStatus>() {{
            add(SolStatus.CharacterTransferUnavailable);
            add(SolStatus.TransmitOverrun);
            add(SolStatus.DtrAsserted);
        }};

        SolResponseData solResponseData = new SolResponseData((byte) 1, SolAckState.ACK, statuses, (byte) 1);
        assertEquals(statuses, solResponseData.getStatuses());
    }

    @Test
    public void getAcceptedCharactersReturnsPassedData() throws Exception {
        byte acceptedCharacters = (byte) 22;

        SolResponseData solResponseData = new SolResponseData((byte) 1, SolAckState.ACK, new HashSet<SolStatus>(), acceptedCharacters);
        assertEquals(acceptedCharacters, solResponseData.getAcceptedCharactersNumber());
    }
}