/*
 * IpmiCommandCoder.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands;

import com.veraxsystems.vxipmi.coding.PayloadCoder;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.PlainMessage;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

/**
 * A wrapper for IPMI command.
 * 
 * Parameterless constructors in classes derived from IpmiCommandCoder are meant
 * to be used for decoding. To avoid omitting setting an important parameter
 * when encoding message use parametered constructors rather than the
 * parameterless ones.
 * 
 */
public abstract class IpmiCommandCoder extends PayloadCoder {

    public IpmiCommandCoder() {

    }

    public IpmiCommandCoder(IpmiVersion version, CipherSuite cipherSuite,
                            AuthenticationType authenticationType) {
        super(version, cipherSuite, authenticationType);
    }

    @Override
    public PayloadType getSupportedPayloadType() {
        return PayloadType.Ipmi;
    }

    /**
     * Checks if given message contains response command specific for this
     * class.
     *
     * @param message
     * @return True if message contains response command specific for this
     *         class, false otherwise.
     */
    public boolean isCommandResponse(IpmiMessage message) {
        if (message.getPayload() instanceof IpmiPayload) {
            if (message.getPayload() instanceof IpmiLanResponse) {
                return ((IpmiLanResponse) message.getPayload()).getCommand() == getCommandCode();
            } else  {
                return message.getPayload() instanceof PlainMessage;
            }
        } else {
            return false;
        }
    }

    /**
     * Retrieves command code specific for command represented by this class
     *
     * @return command code
     */
    public abstract byte getCommandCode();

    /**
     * Retrieves network function specific for command represented by this
     * class.
     *
     * @return network function
     * @see NetworkFunction
     */
    public abstract NetworkFunction getNetworkFunction();

    /**
     * Used in several derived classes - converts {@link PrivilegeLevel} to
     * byte.
     *
     * @param privilegeLevel
     * @return privilegeLevel encoded as a byte due to {@link CommandsConstants}
     */
    protected byte encodePrivilegeLevel(PrivilegeLevel privilegeLevel) {
        switch (privilegeLevel) {
        case MaximumAvailable:
            return CommandsConstants.AL_HIGHEST_AVAILABLE;
        case Callback:
            return CommandsConstants.AL_CALLBACK;
        case User:
            return CommandsConstants.AL_USER;
        case Operator:
            return CommandsConstants.AL_OPERATOR;
        case Administrator:
            return CommandsConstants.AL_ADMINISTRATOR;
        default:
            throw new IllegalArgumentException("Invalid privilege level");
        }
    }
}
