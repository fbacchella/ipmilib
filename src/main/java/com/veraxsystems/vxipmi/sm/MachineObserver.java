/*
 * MachineObserver.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm;

import com.veraxsystems.vxipmi.sm.actions.StateMachineAction;

/**
 * An interface for listener of the {@link StateMachine}
 * 
 * @see StateMachine#register(MachineObserver)
 */
public interface MachineObserver {
	/**
	 * Notifies observer of action performed by the State Machine.
	 * 
	 * @param action
	 *            - action performed
	 */
	void notify(StateMachineAction action);
}
