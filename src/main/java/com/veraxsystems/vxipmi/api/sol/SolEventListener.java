/*
 * SolEventListener.java
 * Created on 31.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.api.sol;

import com.veraxsystems.vxipmi.coding.payload.sol.SolOperation;
import com.veraxsystems.vxipmi.coding.payload.sol.SolStatus;

import java.util.Set;

/**
 * Interface for Serial over LAN (SOL) events listener.
 * Events can be sent proactively by remote server or as a response for specific request.
 */
public interface SolEventListener {

    /**
     * Process event sent proactively by remote server.
     *
     * @param statuses
     *          statuses indicated by remote server
     */
    void processRequestEvent(Set<SolStatus> statuses);

    /**
     * Process event sent by remote server as a response for some outbound message.
     *
     * @param statuses
     *          statuses indicated by remote server
     * @param correspondingRequestData
     *          data from message, that caused this event to be fired.
     *          Can be empty array if message didn't contain character data.
     * @param correspondingRequestOperations
     *          set of operations invoked on remote server's serial port that caused this event to be fired.
     *          Can be empty set if corresponding request message didn't contain any operations to be invoked
     */
    void processResponseEvent(Set<SolStatus> statuses, byte[] correspondingRequestData, Set<SolOperation> correspondingRequestOperations);

}
