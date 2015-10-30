package com.veraxsystems.vxipmi.connection;

import com.veraxsystems.vxipmi.sm.states.State;

public class StateConnectionException extends ConnectionException {

	private final String stateName;

	public StateConnectionException(State state) {
		super("Illegal connection state: " + state.getClass().getSimpleName());
		this.stateName = state.getClass().getSimpleName();
	}

	public String getStateName() {
		return stateName;
	}

}
