/*
 * IpmiMessageHandler.java
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
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Default implementation of {@link MessageHandler} for {@link IpmiLanMessage}s.
 */
public class IpmiMessageHandler extends MessageHandler {

    private static final Logger logger = Logger.getLogger(IpmiMessageHandler.class);

    public IpmiMessageHandler(Connection connection, int timeout) throws IOException {
        super(connection, timeout, IpmiLanMessage.MIN_SEQUENCE_NUMBER, IpmiLanMessage.MAX_SEQUENCE_NUMBER);
    }

    /**
     * If message is of type {@link IpmiLanMessage}, finds corresponding request message and extracts response data from it,
     * using data carried in this message.
     *
     * @param message
     *          received IPMI message
     */
    @Override
    protected void handleIncomingMessageInternal(Ipmiv20Message message) {
        if (message.getPayload() instanceof IpmiLanMessage) {
            IpmiLanMessage lanMessagePayload = (IpmiLanMessage) message.getPayload();

            PayloadCoder coder = messageQueue.getMessageFromQueue(lanMessagePayload.getSequenceNumber());
            int tag = lanMessagePayload.getSequenceNumber();

            logger.debug("Received message with tag " + tag);

            if (coder == null) {
                logger.debug("No message tagged with " + tag
                        + " in queue. Dropping orphan message.");
                return;
            }

            if (coder.getClass() == GetChannelAuthenticationCapabilities.class) {
                messageQueue.remove(tag);
            } else {

                try {
                    ResponseData responseData = coder.getResponseData(message);
                    connection.notifyResponseListeners(connection.getHandle(), tag, responseData, null);
                } catch (Exception e) {
                    connection.notifyResponseListeners(connection.getHandle(), tag, null, e);
                }
                messageQueue.remove(lanMessagePayload.getSequenceNumber());
            }
        }
    }

}
