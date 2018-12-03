/*
 * Sendv20Message.java 
 * Created on 2011-08-23
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm.events;

import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import com.veraxsystems.vxipmi.sm.states.State;

/**
 * Performed in {@link SessionValid} {@link State} will cause {@link #message}
 * to be sent.
 * 
 * @see StateMachine
 */
public class Sendv20Message extends StateMachineEvent {
    private PayloadCoder message;
    private int sessionId;
    private int messageSequenceNumber;
    private int sessionSequenceNumber;

    /**
     * Prepares an event for {@link StateMachine} that will perform sending an
     * IPMI command in v2.0 format. Only possible in {@link SessionValid}
     * {@link State}
     *
     * @param payloadCoder
     *            - The payload to send.
     * @param sessionId
     *            - managed system session ID
     * @param messageSequenceNumber
     *            - generated sequence number for the message to send
     */
    public Sendv20Message(PayloadCoder payloadCoder, int sessionId,
                          int messageSequenceNumber, int sessionSequenceNumber) {
        message = payloadCoder;
        this.sessionId = sessionId;
        this.messageSequenceNumber = messageSequenceNumber;
        this.sessionSequenceNumber = sessionSequenceNumber;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getMessageSequenceNumber() {
        return messageSequenceNumber;
    }

    public int getSessionSequenceNumber() {
        return sessionSequenceNumber;
    }

    public PayloadCoder getPayloadCoder() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sendv20Message that = (Sendv20Message) o;

        if (sessionId != that.sessionId) return false;
        if (messageSequenceNumber != that.messageSequenceNumber) return false;
        if (sessionSequenceNumber != that.sessionSequenceNumber) return false;

        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + sessionId;
        result = 31 * result + messageSequenceNumber;
        result = 31 * result + sessionSequenceNumber;
        return result;
    }
}
