/*
 * State.java 
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

import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;

/**
 * The abstract for state of the {@link StateMachine}.
 */
public abstract class State {
	/**
	 * Defines the action performed when the state is entered.
	 * 
	 * @param stateMachine
	 *            - the context
	 */
	public void onEnter(StateMachine stateMachine) {
		//System.out.println("[SM " + stateMachine.hashCode() + "] Entering state "
		//		+ stateMachine.getCurrent().getClass().getSimpleName());
	}

	/**
	 * Performs the state transition
	 * 
	 * @param stateMachine
	 *            - the context
	 * @param machineEvent
	 *            - the {@link StateMachineEvent} that was the cause of the
	 *            transition
	 */
	public abstract void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent);

	/**
	 * Defines the action that should be performed when a response form the
	 * remote system arrives in the current state.
	 * 
	 * @param stateMachine
	 *            - the context
	 * @param message
	 *            - the message that appeared
	 */
	public abstract void doAction(StateMachine stateMachine, RmcpMessage message);
}
