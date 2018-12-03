/*
 * PayloadCoder.java
 * Created on 25.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.coding;

import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv15Message;
import com.veraxsystems.vxipmi.coding.protocol.Ipmiv20Message;
import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import com.veraxsystems.vxipmi.coding.protocol.encoder.Protocolv20Encoder;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.coding.security.SecurityConstants;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Base class for {@link IpmiPayload} coders, used for encoding pyloads inside {@link IpmiMessage}s.
 */
public abstract class PayloadCoder {

    private IpmiVersion ipmiVersion;

    /**
     * Authentication type used. Default == None;
     */
    private AuthenticationType authenticationType;

    private CipherSuite cipherSuite;

    public void setIpmiVersion(IpmiVersion ipmiVersion) {
        this.ipmiVersion = ipmiVersion;
    }

    public IpmiVersion getIpmiVersion() {
        return ipmiVersion;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setCipherSuite(CipherSuite cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    public PayloadCoder() {
        setSessionParameters(IpmiVersion.V20, CipherSuite.getEmpty(),
                AuthenticationType.RMCPPlus);
    }

    public PayloadCoder(IpmiVersion version, CipherSuite cipherSuite,
                            AuthenticationType authenticationType) {
        setSessionParameters(version, cipherSuite, authenticationType);
    }

    /**
     * Sets session parameters.
     *
     * @param version
     *            IPMI version of the command.
     * @param cipherSuite
     *            {@link CipherSuite} containing authentication,
     *            confidentiality and integrity algorithms for this session.
     * @param authenticationType
     *            Type of authentication used. Must be RMCPPlus for IPMI v2.0.
     */
    public void setSessionParameters(IpmiVersion version,
                                     CipherSuite cipherSuite, AuthenticationType authenticationType) {

        if (version == IpmiVersion.V20
                && authenticationType != AuthenticationType.RMCPPlus) {
            throw new IllegalArgumentException(
                    "Authentication Type must be RMCPPlus for IPMI v2.0 messages");
        }

        setIpmiVersion(version);
        setAuthenticationType(authenticationType);
        setCipherSuite(cipherSuite);
    }

    /**
     * Prepares an IPMI request message containing class-specific payload.
     *
     * @param messageSequenceNumber
     *            A generated sequence number used for matching request and
     *            response. For all IPMI messages,
     *            messageSequenceNumber is used as a IPMI LAN Message sequence
     *            number and as an IPMI payload message tag.
     * @param sessionSequenceNumber
     *          If IPMI message is sent in a session, it is used as
     *            a Session Sequence Number
     * @param sessionId
     *            ID of the managed system's session message is being sent in.
     *            For sessionless commands should b set to 0.
     * @return IPMI message
     * @throws NoSuchAlgorithmException
     *             when authentication, confidentiality or integrity algorithm
     *             fails.
     * @throws InvalidKeyException
     *             when creating of the algorithm key fails
     */
    public IpmiMessage encodePayload(int messageSequenceNumber, int sessionSequenceNumber, int sessionId) throws NoSuchAlgorithmException, InvalidKeyException {
        if (getIpmiVersion() == IpmiVersion.V15) {
            return encodeV15Payload(messageSequenceNumber, sessionSequenceNumber, sessionId);
        } else /* IPMI version 2.0 */{
            return encodeV20Payload(messageSequenceNumber, sessionSequenceNumber, sessionId);
        }
    }

    private Ipmiv15Message encodeV15Payload(int messageSequenceNumber, int sessionSequenceNumber, int sessionId) throws NoSuchAlgorithmException, InvalidKeyException {
        Ipmiv15Message message = new Ipmiv15Message();

        message.setAuthenticationType(getAuthenticationType());

        message.setSessionID(sessionId);

        message.setSessionSequenceNumber(sessionSequenceNumber);

        message.setPayload(preparePayload(messageSequenceNumber));

        return message;
    }

    private Ipmiv20Message encodeV20Payload(int messageSequenceNumber, int sessionSequenceNumber, int sessionId) throws NoSuchAlgorithmException, InvalidKeyException {
        Ipmiv20Message message = new Ipmiv20Message(getCipherSuite()
                .getConfidentialityAlgorithm());

        message.setAuthenticationType(getAuthenticationType());

        message.setSessionID(sessionId);

        message.setSessionSequenceNumber(sessionSequenceNumber);

        message.setPayloadType(getSupportedPayloadType());

        message.setPayloadAuthenticated(getCipherSuite()
                .getIntegrityAlgorithm().getCode() != SecurityConstants.IA_NONE);

        message.setPayloadEncrypted(getCipherSuite()
                .getConfidentialityAlgorithm().getCode() != SecurityConstants.CA_NONE);

        message.setPayload(preparePayload(messageSequenceNumber));

        message.setAuthCode(getCipherSuite()
                .getIntegrityAlgorithm()
                .generateAuthCode(message.getIntegrityAlgorithmBase(new Protocolv20Encoder())));

        return message;
    }

    public abstract PayloadType getSupportedPayloadType();

    /**
     * Prepares {@link IpmiPayload} to be encoded. Called from
     * {@link #encodePayload(int, int, int)}
     *
     * @param sequenceNumber
     *            sequenceNumber is used as an IPMI payload message tag
     * @return IPMI payload
     * @throws NoSuchAlgorithmException
     *             when authentication, confidentiality or integrity algorithm
     *             fails.
     * @throws InvalidKeyException
     *             when creating of the algorithm key fails
     */
    protected abstract IpmiPayload preparePayload(int sequenceNumber) throws NoSuchAlgorithmException, InvalidKeyException;

    /**
     * Retrieves payload-specific response data from IPMI message
     *
     * @param message
     *            IPMI message
     * @return response data
     * @throws IllegalArgumentException
     *             when message is not a response for class-specific command or
     *             response has invalid length.
     * @throws IPMIException
     *             when response completion code isn't OK.
     * @throws NoSuchAlgorithmException
     *             when authentication, confidentiality or integrity algorithm
     *             fails.
     * @throws InvalidKeyException
     *             when creating of the authentication algorithm key fails
     */
    public abstract ResponseData getResponseData(IpmiMessage message) throws IPMIException, NoSuchAlgorithmException, InvalidKeyException;

}
