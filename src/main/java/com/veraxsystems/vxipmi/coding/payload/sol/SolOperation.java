/*
 * SolOperation.java
 * Created on 19.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload.sol;

/**
 * Enumeration of all possible operations, that Remote Console can invoke on BMC during SOL communication.
 */
public enum SolOperation {
    /**
     * Assert RI (may not be supported on all implementations) - Goal is to allow this to be used for generating a WOR.
     */
    RingWOR(5),

    /**
     * Generate BREAK (300 ms, nominal)
     */
    Break(4),

    /**
     * Deassert CTS (clear to send) to the baseboard serial controller.
     * (This is the default state when SOL is deactivated.)
     */
    CTS(3),

    /**
     * When test mode inactive, deassert DCD/DSR to baseboard serial controller.
     * For test mode active, deassert just DCD to baseboard serial controller.
     */
    DCD_DSR(2),

    /**
     * When test mode inactive, drop (flush) data from remote console to BMC [not including data carried in this packet, if any].
     * For test mode active, deassert DSR to baseboard serial controller.
     */
    FlushInbound(1),

    /**
     * When test mode inactive, flush Outbound Character Data (flush data from BMC to remote console).
     * When test mode active, won't have any effect.
     */
    FlushOutbound(0);

    /**
     * ID of the operation (number of bit in operation field byte).
     */
    private final int operationNumber;

    SolOperation(int operationNumber) {
        this.operationNumber = operationNumber;
    }

    public int getOperationNumber() {
        return operationNumber;
    }
}
