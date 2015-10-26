/*
 * OpenSessionWaiting.java 
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

import com.veraxsystems.vxipmi.coding.commands.session.OpenSession;
import com.veraxsystems.vxipmi.coding.payload.PlainMessage;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.protocol.decoder.PlainCommandv20Decoder;
import com.veraxsystems.vxipmi.coding.protocol.decoder.ProtocolDecoder;
import com.veraxsystems.vxipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.ErrorAction;
import com.veraxsystems.vxipmi.sm.actions.ResponseAction;
import com.veraxsystems.vxipmi.sm.events.DefaultAck;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;
import com.veraxsystems.vxipmi.sm.events.Timeout;

/**
 * Waiting for the {@link OpenSession} response.<br>
 * <li>Transition to {@link OpenSessionComplete} on {@link DefaultAck} <li>
 * Transition to {@link Authcap} on {@link Timeout}
 */
public class OpenSessionWaiting extends State {

	private int tag;

	public OpenSessionWaiting(int tag) {
		this.tag = tag;
	}

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof DefaultAck) {
			stateMachine.setCurrent(new OpenSessionComplete());
		} else if (machineEvent instanceof Timeout) {
			stateMachine.setCurrent(new Authcap());
		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition")));
		}
	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {
		if (ProtocolDecoder.decodeAuthenticationType(message) != AuthenticationType.RMCPPlus) {
			return; // this isn't IPMI v2.0 message so we ignore it
		}
		PlainCommandv20Decoder decoder = new PlainCommandv20Decoder(
				CipherSuite.getEmpty());
		if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.RmcpOpenSessionResponse) {
			return;
		}
		IpmiMessage ipmiMessage = null;
		try {
			ipmiMessage = decoder.decode(message);
			/*System.out.println("[OSW "
					+ stateMachine.hashCode()
					+ "] Expected: "
					+ tag
					+ " encountered: "
					+ TypeConverter.byteToInt(((PlainMessage) ipmiMessage
							.getPayload()).getPayloadData()[0]));*/
			OpenSession openSession = new OpenSession(CipherSuite.getEmpty());
			if (openSession.isCommandResponse(ipmiMessage)
					&& TypeConverter.byteToInt(((PlainMessage) ipmiMessage
							.getPayload()).getPayloadData()[0]) == tag) {
				stateMachine.doExternalAction(new ResponseAction(openSession
						.getResponseData(ipmiMessage)));
			}
		} catch (Exception e) {
			// stateMachine.doTransition(new Timeout());
			stateMachine.doExternalAction(new ErrorAction(e));
		}
	}
}
