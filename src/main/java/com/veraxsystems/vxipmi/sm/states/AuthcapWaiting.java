/*
 * AuthcapWaiting.java 
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

import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.decoder.ProtocolDecoder;
import com.veraxsystems.vxipmi.coding.protocol.decoder.Protocolv15Decoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.ErrorAction;
import com.veraxsystems.vxipmi.sm.actions.ResponseAction;
import com.veraxsystems.vxipmi.sm.events.AuthenticationCapabilitiesReceived;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;
import com.veraxsystems.vxipmi.sm.events.Timeout;

/**
 * Waiting for the {@link GetChannelAuthenticationCapabilities} response. <br>
 * Transition to: <li>{@link Ciphers} on {@link Timeout} <li>{@link Authcap} on
 * {@link AuthenticationCapabilitiesReceived}</li>.
 */
public class AuthcapWaiting extends State {

	private int tag;

	public AuthcapWaiting(int tag) {
		this.tag = tag;
	}

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof Timeout) {
			stateMachine.setCurrent(new Ciphers());
		} else if (machineEvent instanceof AuthenticationCapabilitiesReceived) {
			stateMachine.setCurrent(new Authcap());
		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition")));
		}
	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {
		if (ProtocolDecoder.decodeAuthenticationType(message) == AuthenticationType.RMCPPlus) {
			return; // this isn't IPMI v1.5 message so we ignore it
		}
		Protocolv15Decoder decoder = new Protocolv15Decoder();
		IpmiMessage ipmiMessage = null;
		try {
			ipmiMessage = decoder.decode(message);
			/*System.out.println("[AW "
					+ stateMachine.hashCode()
					+ "] Expected: "
					+ tag
					+ " encountered: "
					+ TypeConverter.byteToInt(((IpmiLanResponse) ipmiMessage
							.getPayload()).getSequenceNumber()));*/
			GetChannelAuthenticationCapabilities capabilities = new GetChannelAuthenticationCapabilities();
			if (capabilities.isCommandResponse(ipmiMessage)
					&& TypeConverter.byteToInt(((IpmiLanResponse) ipmiMessage
							.getPayload()).getSequenceNumber()) == tag) {
				stateMachine.doExternalAction(new ResponseAction(capabilities
						.getResponseData(ipmiMessage)));
			}
		} catch (Exception e) {
			//stateMachine.doTransition(new Timeout());
			stateMachine.doExternalAction(new ErrorAction(e));
		}
	}

}
