/*
 * SolStatus.java
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
 * Enumeration of all possible states, that Remote Console can receive from BMC during SOL communication.
 */
public enum SolStatus {
    /**
     * Character transfer is unavailable because system is in a powered-down or sleep state.
     */
    CharacterTransferUnavailable(5),

    /**
     * SOL is deactivated/deactivating.
     * (Remote console can use this to tell if SOL was deactivated by some other party,
     * or by local pushbutton reset or power on/off).
     */
    SolDeactivated(4),

    /**
     * Characters were dropped between transmitting this packet and the previous packet,
     * because the system did not pay attention to hardware flow control.
     */
    TransmitOverrun(3),

    /**
     * A break condition from the system has been detected.
     * The BMC will generate this only on one packet at the start of the break.
     */
    Break(2),

    /**
     * When test mode active, signals that RTS is asserted on serial port.
     * For test mode inactive, it is unused.
     * A packet with this status will be automatically sent whenever RTS changes state.
     * Note that this packet may not contain character data.
     */
    RtsAsserted(1),

    /**
     * When test mode active, signals that DTR is asserted on serial port.
     * For test mode inactive, it is unused.
     * A packet with this status will be automatically sent whenever DTR changes state.
     * Note that this packet may not contain character data.
     */
    DtrAsserted(0);

    /**
     * ID of the status (number of bit in status field byte).
     */
    private final int statusNumber;

    SolStatus(int statusNumber) {
        this.statusNumber = statusNumber;
    }

    public int getStatusNumber() {
        return statusNumber;
    }
}
