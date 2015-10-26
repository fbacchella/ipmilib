/*
 * StateMachine.java 
 * Created on 2011-08-18
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.sm;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.veraxsystems.vxipmi.coding.rmcp.RmcpDecoder;
import com.veraxsystems.vxipmi.common.Constants;
import com.veraxsystems.vxipmi.sm.actions.StateMachineAction;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;
import com.veraxsystems.vxipmi.sm.states.SessionValid;
import com.veraxsystems.vxipmi.sm.states.State;
import com.veraxsystems.vxipmi.sm.states.Uninitialized;
import com.veraxsystems.vxipmi.transport.Messenger;
import com.veraxsystems.vxipmi.transport.UdpListener;
import com.veraxsystems.vxipmi.transport.UdpMessage;

/**
 * State machine for connecting and acquiring session with the remote host via
 * IPMI v.2.0.
 */
public class StateMachine implements UdpListener {

	private List<MachineObserver> observers;

	private State current;

	private Messenger messenger;
	private InetAddress remoteMachineAddress;

	private boolean initialized;

	public State getCurrent() {
		return current;
	}

	public void setCurrent(State current) {
		this.current = current;
		current.onEnter(this);
	}

	/**
	 * Initializes the State Machine
	 * 
	 * @param messenger
	 *            - {@link Messenger} connected to the
	 *            {@link Constants#IPMI_PORT}
	 */
	public StateMachine(Messenger messenger) {
		this.messenger = messenger;
		observers = new ArrayList<MachineObserver>();
		initialized = false;
	}

	/**
	 * Sends message via {@link #messenger} to the managed system.
	 * 
	 * @param message
	 *            - the encoded message
	 * @throws IOException
	 *             - when sending of the message fails
	 */
	public void sendMessage(byte[] message) throws IOException {
		UdpMessage udpMessage = new UdpMessage();
		udpMessage.setAddress(getRemoteMachineAddress());
		udpMessage.setPort(Constants.IPMI_PORT);
		udpMessage.setMessage(message);
		messenger.send(udpMessage);
	}

	public InetAddress getRemoteMachineAddress() {
		return remoteMachineAddress;
	}

	/**
	 * Sends a notification of an action to all {@link MachineObserver}s
	 * 
	 * @param action
	 *            - a {@link StateMachineAction} to perform
	 */
	public void doExternalAction(StateMachineAction action) {
		for (MachineObserver observer : observers) {
			if (observer != null) {
				observer.notify(action);
			}
		}
	}

	/**
	 * Sets the State Machine in the initial state.
	 * 
	 * @param address
	 *            - IP address of the remote machine.
	 * @see #stop()
	 */
	public void start(InetAddress address) {
		messenger.register(this);
		remoteMachineAddress = address;
		setCurrent(new Uninitialized());
		initialized = true;
	}

	/**
	 * Cleans up the machine resources.
	 * 
	 * @see #start(InetAddress)
	 */
	public void stop() {
		messenger.unregister(this);
		initialized = false;
	}

	/**
	 * @return true if {@link StateMachine} is initialized, false otherwise.
	 * @see #start(InetAddress)
	 * @see #stop()
	 */
	public boolean isActive() {
		return initialized;
	}

	/**
	 * Performs a {@link State} transition according to the event and
	 * {@link #current} state
	 * 
	 * @param event
	 *            - {@link StateMachineEvent} invoking the transition
	 * @throws NullPointerException
	 *             - when machine was not yet started
	 * @see #start(InetAddress)
	 */
	public void doTransition(StateMachineEvent event) {
		if (!initialized) {
			throw new NullPointerException("State machine not started");
		}
		current.doTransition(this, event);
	}

	@Override
	public void notifyMessage(UdpMessage message) {
		if (message.getAddress().equals(getRemoteMachineAddress())) {
			current.doAction(this, RmcpDecoder.decode(message.getMessage()));
		}
	}

	/**
	 * Registers the listener in the {@link StateMachine} so it will be notified
	 * of the {@link StateMachineAction}s performed via
	 * {@link #doExternalAction(StateMachineAction)}
	 * 
	 * @param observer
	 *            - {@link MachineObserver} to register
	 */
	public void register(MachineObserver observer) {
		observers.add(observer);
	}

	/**
	 * @return true if {@link StateMachine} is at the point when it acquires
	 *         session and will send sessionless messages
	 */
	public boolean isSessionChallenging() {
		return !initialized || getCurrent().getClass() == SessionValid.class;
	}
}
