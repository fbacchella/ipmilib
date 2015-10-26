/*
 * OpenSession.java 
 * Created on 2011-08-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.commands.session;

import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.PlainMessage;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.security.AuthenticationAlgorithm;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityAlgorithm;
import com.veraxsystems.vxipmi.coding.security.ConfidentialityNone;
import com.veraxsystems.vxipmi.coding.security.IntegrityAlgorithm;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Wrapper class for RMCP+ Open Session request.
 */
public class OpenSession extends IpmiCommandCoder {

	private PrivilegeLevel requestedPrivilegeLevel;

	/**
	 * Remote console Session ID.
	 */
	private int sessionID;

	public void setRequestedPrivilegeLevel(
			PrivilegeLevel requestedPrivilegeLevel) {
		this.requestedPrivilegeLevel = requestedPrivilegeLevel;
	}

	public PrivilegeLevel getRequestedPrivilegeLevel() {
		return requestedPrivilegeLevel;
	}

	private byte getRequestedPrivilegeLevelEncoded() {
		switch (requestedPrivilegeLevel) {
		case MaximumAvailable:
			return 0;
		case Callback:
			return TypeConverter.intToByte(0x1);
		case User:
			return TypeConverter.intToByte(0x2);
		case Operator:
			return TypeConverter.intToByte(0x3);
		case Administrator:
			return TypeConverter.intToByte(0x4);
		default:
			throw new IllegalArgumentException("Invalid privilege level");
		}
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	public int getSessionID() {
		return sessionID;
	}

	/**
	 * Initiates class for decoding. Sets IPMI version to
	 * {@link IpmiVersion#V20} since OpenSession is a RMCP+ command. Sets
	 * authentication type to RMCP+.
	 * 
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 *            Cipher Suite might (and probably will be, because at this
	 *            point of session creation SIK is not yet known) be not
	 *            initialized.
	 * 
	 * @see IpmiVersion
	 */
	public OpenSession(CipherSuite cipherSuite) {
		super(IpmiVersion.V20, cipherSuite, AuthenticationType.RMCPPlus);
		setCipherSuite(cipherSuite);
	}

	/**
	 * Initiates class for encoding and decoding. Sets IPMI version to
	 * {@link IpmiVersion#V20} since OpenSession is a RMCP+ command. Sets
	 * authentication type to RMCP+.
	 * 
	 * @see IpmiVersion
	 * 
	 * @param sessionID
	 *            - Session ID selected by the remote console.
	 * @param privilegeLevel
	 *            - Requested privilege level for the session. Can be
	 *            {@link PrivilegeLevel#MaximumAvailable}.
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 *            Cipher Suite might (and probably will be, because at this
	 *            point of session creation SIK is not yet known) be not
	 *            initialized.
	 * @see CipherSuite
	 * @see AuthenticationAlgorithm
	 * @see IntegrityAlgorithm
	 * @see ConfidentialityAlgorithm
	 */
	public OpenSession(int sessionID, PrivilegeLevel privilegeLevel,
			CipherSuite cipherSuite) {
		super(IpmiVersion.V20, cipherSuite, AuthenticationType.RMCPPlus);

		setSessionID(sessionID);
		setRequestedPrivilegeLevel(privilegeLevel);
	}

	@Override
	public IpmiMessage encodeCommand(int sequenceNumber, int sessionId) {
		if (sessionId != 0) {
			throw new IllegalArgumentException("Session ID must be 0");
		}
		Ipmiv20Message message = new Ipmiv20Message(new ConfidentialityNone());

		message.setPayloadType(PayloadType.RmcpOpenSessionRequest);
		message.setSessionID(0);
		message.setSessionSequenceNumber(0);
		message.setAuthenticationType(getAuthenticationType());
		message.setPayloadAuthenticated(false);
		message.setPayloadEncrypted(false);
		message.setPayload(preparePayload(sequenceNumber));

		return message;
	}

	@Override
	protected IpmiPayload preparePayload(int sequenceNumber) {
		byte[] payload = new byte[32];

		payload[0] = TypeConverter.intToByte(sequenceNumber % 256);

		payload[1] = getRequestedPrivilegeLevelEncoded();

		payload[2] = 0; // reserved
		payload[3] = 0; // reserved

		// prepare requested session ID
		byte[] id = TypeConverter.intToLittleEndianByteArray(sessionID);

		System.arraycopy(id, 0, payload, 4, 4);

		// Authentication Payload

		payload[8] = 0; // payload type
		payload[9] = 0; // reserved
		payload[10] = 0; // reserved
		payload[11] = 0x08; // payload length
		payload[12] = getCipherSuite().getAuthenticationAlgorithm().getCode(); // authentication
																				// algorithm
																				// specific
																				// code
		payload[13] = 0; // reserved
		payload[14] = 0; // reserved
		payload[15] = 0; // reserved

		// Integrity Payload

		payload[16] = 0x01; // payload type
		payload[17] = 0; // reserved
		payload[18] = 0; // reserved
		payload[19] = 0x08; // payload length
		payload[20] = getCipherSuite().getIntegrityAlgorithm().getCode(); // integrity
																			// algorithm
																			// specific
																			// code
		payload[21] = 0; // reserved
		payload[22] = 0; // reserved
		payload[23] = 0; // reserved

		// Confidentiality Payload

		payload[24] = 0x02; // payload type
		payload[25] = 0; // reserved
		payload[26] = 0; // reserved
		payload[27] = 0x08; // payload length
		payload[28] = getCipherSuite().getConfidentialityAlgorithm().getCode(); // confidentiality
																				// algorithm
																				// specific
																				// code
		payload[29] = 0; // reserved
		payload[30] = 0; // reserved
		payload[31] = 0; // reserved

		return new PlainMessage(payload);
	}

	@Override
	@Deprecated
	public byte getCommandCode() {
		return 0;
	}

	@Override
	@Deprecated
	public NetworkFunction getNetworkFunction() {
		return NetworkFunction.ChassisRequest;
	}

	@Override
	public ResponseData getResponseData(IpmiMessage message)
			throws IllegalArgumentException, IPMIException {
		if (!isCommandResponse(message)) {
			throw new IllegalArgumentException(
					"This is not a response for Open Session command");
		}

		byte[] payload = message.getPayload().getPayloadData();

		if (payload[1] != 0) {
			throw new IPMIException(CompletionCode.parseInt(TypeConverter
					.byteToInt(payload[1])));
		}

		if (payload.length < 36) {
			throw new IllegalArgumentException("Invalid payload length");
		}

		OpenSessionResponseData data = new OpenSessionResponseData();

		data.setMessageTag(payload[0]);

		data.setStatusCode(payload[1]);

		data.setPrivilegeLevel(payload[2]);

		byte[] buffer = new byte[4];

		System.arraycopy(payload, 4, buffer, 0, 4);

		data.setRemoteConsoleSessionId(TypeConverter
				.littleEndianByteArrayToInt(buffer));

		System.arraycopy(payload, 8, buffer, 0, 4);

		data.setManagedSystemSessionId(TypeConverter
				.littleEndianByteArrayToInt(buffer));

		byte[] auth = new byte[8];

		System.arraycopy(payload, 12, auth, 0, 8);

		data.setAuthenticationAlgorithm(auth[4]);

		byte[] integr = new byte[8];

		System.arraycopy(payload, 20, integr, 0, 8);

		data.setIntegrityAlgorithm(integr[4]);

		byte[] conf = new byte[8];

		System.arraycopy(payload, 28, conf, 0, 8);

		data.setConfidentialityAlgorithm(conf[4]);

		return data;
	}

}
