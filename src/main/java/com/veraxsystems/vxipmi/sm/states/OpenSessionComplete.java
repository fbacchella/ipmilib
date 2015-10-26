/*
 * OpenSessionComplete.java 
 * Created on 2011-08-22
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm.states;

import com.veraxsystems.vxipmi.coding.Encoder;
import com.veraxsystems.vxipmi.coding.commands.session.OpenSession;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp1;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.ErrorAction;
import com.veraxsystems.vxipmi.sm.events.OpenSessionAck;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;

/**
 * Indicates that {@link OpenSession} response was received. Transition to
 * {@link Rakp1Waiting} on {@link OpenSessionAck}
 */
public class OpenSessionComplete extends State {

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof OpenSessionAck) {
			OpenSessionAck event = (OpenSessionAck) machineEvent;

			Rakp1 rakp1 = new Rakp1(event.getManagedSystemSessionId(),
					event.getPrivilegeLevel(), event.getUsername(),
					event.getPassword(), event.getBmcKey(),
					event.getCipherSuite());

			try {
				stateMachine.setCurrent(new Rakp1Waiting(event.getSequenceNumber(), rakp1));
				stateMachine.sendMessage(Encoder.encode(
						new Protocolv20Encoder(), rakp1,
						event.getSequenceNumber(), 0));
			} catch (Exception e) {
				stateMachine.setCurrent(this);
				stateMachine.doExternalAction(new ErrorAction(e));
			}
		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition")));
		}

	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {
	}

}
