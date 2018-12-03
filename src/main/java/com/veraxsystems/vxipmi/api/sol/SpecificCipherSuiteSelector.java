/*
 * SpecificCipherSuiteSelector.java
 * Created on 13.06.2017
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
 * Implementation of {@link CipherSuiteSelectionHandler} that always returns cipher suite provided in the constructor.
 */
public class SpecificCipherSuiteSelector implements CipherSuiteSelectionHandler {

    private final CipherSuite cipherSuite;

    public SpecificCipherSuiteSelector(CipherSuite cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    @Override
    public CipherSuite choose(List<CipherSuite> availableCipherSuites) {
        return cipherSuite;
    }

}
