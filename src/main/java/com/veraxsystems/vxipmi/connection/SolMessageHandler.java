/*
 * SolMessageHandler.java
 * Created on 25.05.2017
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
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.sol.SolAckState;
import com.veraxsystems.vxipmi.coding.payload.sol.SolInboundMessage;
import com.veraxsystems.vxipmi.coding.payload.sol.SolMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Implementation of {@link MessageHandler} for {@link com.veraxsystems.vxipmi.coding.protocol.PayloadType#Sol}.
 */
public class SolMessageHandler extends MessageHandler {

    private static final Logger logger = Logger.getLogger(SolMessageHandler.class);

    public SolMessageHandler(Connection connection, int timeout) throws IOException {
        super(connection, timeout, SolMessage.MIN_SEQUENCE_NUMBER, SolMessage.MAX_SEQUENCE_NUMBER);
    }

    /**
     * Assuming that given message is SOL message, reads both data and acknowledge information from it,
     * notifying registered listeners about incoming data.
     *
     * @param message
     *          received IPMI message.
     */
    @Override
    protected void handleIncomingMessageInternal(Ipmiv20Message message) {
        SolInboundMessage payload = (SolInboundMessage) message.getPayload();

        if (payload.isAcknowledgeMessage()) {
            handleIncomingAcknowledgeMessage(message, payload);
        }

        if (payload.isDataCarrier()) {
            handleIncomingDataMessage(payload);
        }
    }

    private void handleIncomingAcknowledgeMessage(Ipmiv20Message message, SolInboundMessage payload) {
        PayloadCoder coder = messageQueue.getMessageFromQueue(payload.getAckNackSequenceNumber());
        int tag = payload.getAckNackSequenceNumber();

        logger.debug("Received message with tag " + tag);

        if (coder == null) {
            logger.debug("No message tagged with " + tag + " in queue. Dropping orphan message.");
            return;
        }

        try {
            ResponseData responseData = coder.getResponseData(message);
            connection.notifyResponseListeners(connection.getHandle(), tag, responseData, null);
        } catch (Exception e) {
            connection.notifyResponseListeners(connection.getHandle(), tag, null, e);
        }

        // Remove message from queue only when it was fully or partially acknowledged. Otherwise, we want to keep it in queue for further retries
        if (payload.getStatusField().getAckState() == SolAckState.ACK || payload.getAcceptedCharacterCount() > 0) {
            messageQueue.remove(payload.getAckNackSequenceNumber());
        }
    }

    private void handleIncomingDataMessage(SolInboundMessage payload) {
        connection.notifyRequestListeners(payload);
    }

}
