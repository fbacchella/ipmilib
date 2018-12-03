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

import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.sol.SolAckState;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;

import java.util.Set;

/**
 * Impementation of {@link ResponseData} for {@link com.veraxsystems.vxipmi.coding.payload.sol.SolMessage}s.
 */
public class SolResponseData implements ResponseData {

    /**
     * Sequence number of corresponding request message.
     */
    private final byte requestSequenceNumber;

    /**
     * Information if corresponding message was ACKd or NACKd by remote system.
     */
    private final SolAckState acknowledgeState;

    /**
     * Set of statuses returned by the remote system in a response for corresponging message.
     */
    private final Set<SolStatus> statuses;

    /**
     * Number of characters accepted from the corresponding message.
     */
    private final byte acceptedCharactersNumber;

    /**
     * Creates new instance of {@link SolResponseData} filled with given data.
     *
     * @param acknowledgeState
     *          Acknowledge status for corresponding request message
     * @param statuses
     *          Set of statuses
     */
    public SolResponseData(byte requestSequenceNumber, SolAckState acknowledgeState, Set<SolStatus> statuses, byte acceptedCharactersNumber) {
        this.requestSequenceNumber = requestSequenceNumber;
        this.acknowledgeState = acknowledgeState;
        this.statuses = statuses;
        this.acceptedCharactersNumber = acceptedCharactersNumber;
    }

    public byte getRequestSequenceNumber() {
        return requestSequenceNumber;
    }

    public SolAckState getAcknowledgeState() {
        return acknowledgeState;
    }

    public Set<SolStatus> getStatuses() {
        return statuses;
    }

    public byte getAcceptedCharactersNumber() {
        return acceptedCharactersNumber;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SolResponseData{");
        sb.append("requestSequenceNumber=").append(requestSequenceNumber);
        sb.append(", acknowledgeState=").append(acknowledgeState);
        sb.append(", statuses=").append(statuses);
        sb.append(", acceptedCharactersNumber=").append(acceptedCharactersNumber);
        sb.append('}');
        return sb.toString();
    }
}
