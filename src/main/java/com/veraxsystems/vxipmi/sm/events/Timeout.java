/*
 * Timeout.java 
 * Created on 2011-08-18
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm.events;

import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.states.Authcap;
import com.veraxsystems.vxipmi.sm.states.State;

/**
 * {@link StateMachineEvent} indicating that response for one of the messages in
 * the process of the session challenge did not arrive in time. In most of the cases (if
 * {@link Authcap} {@link State} was reached earlier) transits
 * {@link StateMachine} to that {@link State}.
 */
public class Timeout extends StateMachineEvent {

}
