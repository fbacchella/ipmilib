/*
 * SessionValid.java 
 * Created on 2011-08-23
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
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.session.CloseSession;
import com.veraxsystems.vxipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.protocol.decoder.ProtocolDecoder;
import com.veraxsystems.vxipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.sm.StateMachine;
import com.veraxsystems.vxipmi.sm.actions.ErrorAction;
import com.veraxsystems.vxipmi.sm.actions.MessageAction;
import com.veraxsystems.vxipmi.sm.events.Sendv20Message;
import com.veraxsystems.vxipmi.sm.events.SessionUpkeep;
import com.veraxsystems.vxipmi.sm.events.StateMachineEvent;
import com.veraxsystems.vxipmi.sm.events.Timeout;

/**
 * {@link State} in which the session is valid and sending IPMI commands to the
 * remote machine is enabled. <li>Sends an IPMI v2.0 message on
 * {@link Sendv20Message} <li>Sends {@link GetChannelAuthenticationCapabilities}
 * message to keep the session form timing out on {@link SessionUpkeep} <li>
 * Transits to {@link Authcap} on {@link Timeout} <li>Sends {@link CloseSession}
 * and transits to {@link Authcap} on
 * {@link com.veraxsystems.vxipmi.sm.events.CloseSession}
 */
public class SessionValid extends State {

	private CipherSuite cipherSuite;

	private int sessionId;

	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}

	/**
	 * Initiates the state.
	 * 
	 * @param cipherSuite
	 *            - {@link CipherSuite} used during the session.
	 */
	public SessionValid(CipherSuite cipherSuite, int sessionId) {
		this.cipherSuite = cipherSuite;
		this.sessionId = sessionId;
	}

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof Sendv20Message) {
			Sendv20Message event = (Sendv20Message) machineEvent;
			// System.out.println("[SM] >>>> " +
			// event.getCommandCoder().getClass().getSimpleName());
			try {
				stateMachine.sendMessage(Encoder.encode(
						new Protocolv20Encoder(), event.getCommandCoder(),
						event.getSequenceNumber(), event.getSessionId()));
			} catch (Exception e) {
				stateMachine.doExternalAction(new ErrorAction(e));
			}
		} else if (machineEvent instanceof SessionUpkeep) {
			SessionUpkeep event = (SessionUpkeep) machineEvent;
			try {
				stateMachine.sendMessage(Encoder.encode(
						new Protocolv20Encoder(),
						new GetChannelAuthenticationCapabilities(
								IpmiVersion.V20, IpmiVersion.V20, cipherSuite,
								PrivilegeLevel.Callback, TypeConverter
										.intToByte(0xe)), event
								.getSequenceNumber(), event.getSessionId()));
			} catch (Exception e) {
				stateMachine.doExternalAction(new ErrorAction(e));
			}
		} else if (machineEvent instanceof Timeout) {
			stateMachine.setCurrent(new Authcap());
		} else if (machineEvent instanceof com.veraxsystems.vxipmi.sm.events.CloseSession) {
			com.veraxsystems.vxipmi.sm.events.CloseSession event = (com.veraxsystems.vxipmi.sm.events.CloseSession) machineEvent;

			try {
				stateMachine.setCurrent(new Authcap());
				stateMachine.sendMessage(Encoder.encode(
						new Protocolv20Encoder(),
						new CloseSession(IpmiVersion.V20, cipherSuite,
								AuthenticationType.RMCPPlus, event
										.getSessionId()), event
								.getSequenceNumber(), event.getSessionId()));
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
		if (ProtocolDecoder.decodeAuthenticationType(message) != AuthenticationType.RMCPPlus) {
			return; // this isn't IPMI v2.0 message so we ignore it
		}
		if (Protocolv20Decoder.decodeSessionID(message) == 0) {
			return; // this is a sessionless message so we drop it
		}
		Protocolv20Decoder decoder = new Protocolv20Decoder(cipherSuite);
		if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.Ipmi) {
			return;
        }
        if (Protocolv20Decoder.decodeSessionID(message) != sessionId) {
            return; // this message belongs to other session so we ignore it
        }
		try {
			Ipmiv20Message message20 = (Ipmiv20Message) decoder.decode(message);
			if (message20.getSessionID() == sessionId) {
				stateMachine.doExternalAction(new MessageAction(message20));
			}
		} catch (Exception e) {
			stateMachine.doExternalAction(new ErrorAction(e));
		}
	}

}
