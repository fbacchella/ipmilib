/*
 * MessageHandler.java
 * Created on 23.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.connection;

import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.connection.queue.MessageQueue;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.events.Sendv20Message;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import org.apache.log4j.Logger;

/**
 * Class used for handling outgoing and incoming messages for a {@link Connection}.
 */
public abstract class MessageHandler {

    private static final Logger logger = Logger.getLogger(MessageHandler.class);

    protected int lastReceivedSequenceNumber = 0;
    protected final MessageQueue messageQueue;
    protected final Connection connection;

    public MessageHandler(Connection connection, int timeout, int minSequenceNumber, int maxSequenceNumber) {
        this.messageQueue = new MessageQueue(connection, timeout, minSequenceNumber, maxSequenceNumber);
        this.connection = connection;
    }

    /**
     * Attempts to send message encoded by given {@link PayloadCoder} to the remote system.
     *
     * @param payloadCoder
     *          instance of {@link PayloadCoder} that will produce payload for the message being sent.
     * @param stateMachine
     *          {@link StateMachine} for the currenr connection.
     * @param sessionId
     *          ID of the current session.
     * @param isOneWay
     *          flag indicating, if message is one way and we shouldn't await response,
     *          or it isn't and needs response from remote system
     * @return sequence number of the sent message
     * @throws ConnectionException when could not send message due to some problems with connection
     */
    public int sendMessage(PayloadCoder payloadCoder, StateMachine stateMachine, int sessionId, boolean isOneWay)
            throws ConnectionException {
        validateSessionState(stateMachine);

        int seq = isOneWay ? messageQueue.getSequenceNumber() : messageQueue.add(payloadCoder);
        if (seq > 0) {
            stateMachine.doTransition(new Sendv20Message(payloadCoder, sessionId, seq, connection.getNextSessionSequenceNumber()));
        }

        return seq;
    }

    /**
     * Attempts to retry sending message with given tag, assuming that this message exists in message queue.
     *
     * @param tag
     *          tag of the message that we want to resend
     * @param stateMachine
     *          {@link StateMachine} for the currenr connection.
     * @param sessionId
     *          ID of the current session.
     * @return sequence number of the retried message (should be the same as original message tag) or -1 if no message was found in the queue.
     * @throws ConnectionException when could not send message due to some problems with connection
     */
    public int retryMessage(int tag, StateMachine stateMachine, int sessionId) throws ConnectionException {
        validateSessionState(stateMachine);

        PayloadCoder payloadCoder = messageQueue.getMessageFromQueue(tag);

        if (payloadCoder == null) {
            return  -1;
        }

        stateMachine.doTransition(new Sendv20Message(payloadCoder, sessionId, tag, connection.getNextSessionSequenceNumber()));

        return tag;
    }

    private void validateSessionState(StateMachine stateMachine) throws ConnectionException {
        if (stateMachine.getCurrent().getClass() != SessionValid.class) {
            throw new ConnectionException("Illegal connection state: " + stateMachine.getCurrent().getClass().getSimpleName());
        }
    }

    /**
     * Checks if received message is inside "sliding window range", and if it is,
     * further processes the message in a cimplementation-specific way.
     *
     * @param message
     */
    public void handleIncomingMessage(Ipmiv20Message message) {

        int seq = message.getSessionSequenceNumber();

        if (seq != 0 && (seq > lastReceivedSequenceNumber + 15 || seq < lastReceivedSequenceNumber - 16)) {
            logger.debug("Dropping message " + seq);
            return; // if the message's sequence number gets out of the sliding
            // window range we need to drop it
        }

        if (seq != 0) {
            lastReceivedSequenceNumber = (seq > lastReceivedSequenceNumber ? seq : lastReceivedSequenceNumber);
        }

        handleIncomingMessageInternal(message);
    }

    public void setTimeout(int timeout) {
        messageQueue.setTimeout(timeout);
    }

    public void tearDown() {
        messageQueue.tearDown();
    }

    public int getSequenceNumber() {
        return messageQueue.getSequenceNumber();
    }

    /**
     * Abstract method for implementation-specific logic for handling incomming IPMI message.
     *
     * @param message
     *          IPMI message received from BMC
     */
    protected abstract void handleIncomingMessageInternal(Ipmiv20Message message);

}
