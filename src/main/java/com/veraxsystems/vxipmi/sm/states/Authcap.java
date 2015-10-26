/*
 * Authcap.java 
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
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.veraxsystems.vxipmi.coding.commands.session.OpenSession;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.ErrorAction;
import com.veraxsystems.vxipmi.sm.events.Authorize;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;

/**
 * {@link GetChannelAuthenticationCapabilities} response was received. At this
 * point the Session Challenge is going to start. Transits to
 * {@link OpenSessionWaiting} on {@link Authorize}.
 */
public class Authcap extends State {

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof Authorize) {
			Authorize event = (Authorize) machineEvent;

			OpenSession openSession = new OpenSession(event.getSessionId(),
					event.getPrivilegeLevel(), event.getCipherSuite());

			try {
				stateMachine.setCurrent(new OpenSessionWaiting(event.getSequenceNumber()));
				stateMachine.sendMessage(Encoder.encode(
						new Protocolv20Encoder(), openSession,
						event.getSequenceNumber(), 0));
			} catch (Exception e) {
				stateMachine.setCurrent(this);
				stateMachine.doExternalAction(new ErrorAction(e));
			}
		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition: "
							+ machineEvent.getClass().getSimpleName())));
		}

	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {

	}

}
