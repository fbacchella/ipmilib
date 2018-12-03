/*
 * CipherSuiteSelectionHandler.java
 * Created on 06.06.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.api.sol;

import com.veraxsystems.vxipmi.coding.security.CipherSuite;

import java.util.List;

/**
 * Interface for {@link CipherSuite} selection handler to choose among available {@link CipherSuite}s returned by the server.
 */
public interface CipherSuiteSelectionHandler {

    /**
     * Chooses one {@link CipherSuite} among list of available {@link CipherSuite}s, to be used during IPMI connection.
     *
     * @param availableCipherSuites
     *          {@link CipherSuite}s returned by the server as avaialble to use.
     * @return chosen cipher suite
     */
    CipherSuite choose(List<CipherSuite> availableCipherSuites);

}
