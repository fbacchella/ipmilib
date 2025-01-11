/*
 * .java 
 * Created on 2011-08-18
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm.states;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.veraxsystems.vxipmi.coding.Encoder;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelCipherSuites;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.IOErrorAction;
import com.veraxsystems.vxipmi.sm.actions.RuntimeErrorAction;
import com.veraxsystems.vxipmi.sm.actions.SecurityErrorAction;
import com.veraxsystems.vxipmi.sm.events.Default;
import com.veraxsystems.vxipmi.sm.events.GetChannelCipherSuitesPending;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;

/**
 * The initial state. Transits to {@link CiphersWaiting} on
 * {@link GetChannelCipherSuitesPending}.
 */
public class Uninitialized extends State {

    @Override
    public void doTransition(StateMachine stateMachine,
            StateMachineEvent machineEvent) {
        if (machineEvent instanceof GetChannelCipherSuitesPending) {
            Default event = (GetChannelCipherSuitesPending) machineEvent;
            GetChannelCipherSuites cipherSuites = new GetChannelCipherSuites(
                    TypeConverter.intToByte(0xE), (byte) 0);
            try {
                stateMachine.setCurrent(new CiphersWaiting(0, event
                        .getSequenceNumber()));

                stateMachine.sendMessage(Encoder.encode(
                        new Protocolv20Encoder(), cipherSuites,
                        event.getSequenceNumber(), 0,0));
            } catch (IOException e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new IOErrorAction(e));
            } catch (GeneralSecurityException e) {
                stateMachine.setCurrent(this);
                stateMachine.doExternalAction(new SecurityErrorAction(e));
            }
        } else {
            stateMachine.doExternalAction(new RuntimeErrorAction(
                    new IllegalArgumentException("Invalid transition")));
        }
    }

    @Override
    public void doAction(StateMachine stateMachine, RmcpMessage message) {
        // No action is needed
    }

}
