/*
 * Rakp1Complete.java 
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
import com.veraxsystems.vxipmi.coding.commands.session.Rakp1;
import com.veraxsystems.vxipmi.coding.commands.session.Rakp3;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.ErrorAction;
import com.veraxsystems.vxipmi.sm.actions.GetSikAction;
import com.veraxsystems.vxipmi.sm.events.Rakp2Ack;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;

/**
 * At this state RAKP Message 2 was received - waiting for the confirmation to
 * send RAKP Message 3. Transition to {@link Rakp3Waiting} on {@link Rakp2Ack}.
 */
public class Rakp1Complete extends State {

	private Rakp1 rakp1;

	/**
	 * Initiates state.
	 * 
	 * @param rakp1
	 *            - the {@link Rakp1} message that was sent earlier in the
	 *            authentification process.
	 */
	public Rakp1Complete(Rakp1 rakp1) {
		this.rakp1 = rakp1;
	}

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof Rakp2Ack) {
			Rakp2Ack event = (Rakp2Ack) machineEvent;

			Rakp3 rakp3 = new Rakp3(event.getStatusCode(),
					event.getManagedSystemSessionId(), event.getCipherSuite(),
					rakp1, event.getRakp1ResponseData());

			try {
				stateMachine.setCurrent(new Rakp3Waiting(event
						.getSequenceNumber(), rakp1, event
						.getRakp1ResponseData(), event.getCipherSuite()));
				stateMachine.sendMessage(Encoder.encode(
						new Protocolv20Encoder(), rakp3,
						event.getSequenceNumber(), 0));
				stateMachine.doExternalAction(new GetSikAction(rakp1
						.calculateSik(event.getRakp1ResponseData())));
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
