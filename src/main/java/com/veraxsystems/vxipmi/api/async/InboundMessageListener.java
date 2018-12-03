/*
 * IncomingMessageListener.java
 * Created on 25.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.api.async;

import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;

/**
 * Interface for incoming payloads.
 */
public interface InboundMessageListener {

    /**
     * Checks if given payload is supported by this {@link InboundMessageListener} instance.
     * This method should be called prior to invoking {@link InboundMessageListener#notify(IpmiPayload)}.
     *
     * @param payload
     *          {@link IpmiPayload} instance to check
     * @return true if payload is supported by this object, false otherwise
     */
    boolean isPayloadSupported(IpmiPayload payload);

    /**
     * Notify listener about received inbound message.
     * This method should be invoked only with payload for which {@link InboundMessageListener#isPayloadSupported(IpmiPayload)} returned true.
     *
     * @param payload
     *          payload extracted from inbound message
     */
    void notify(IpmiPayload payload);

}
