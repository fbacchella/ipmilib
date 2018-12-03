/*
 * Encoder.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding;

import com.veraxsystems.vxipmi.coding.protocol.decoder.Protocolv15Decoder;
import com.veraxsystems.vxipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.veraxsystems.vxipmi.coding.protocol.encoder.IpmiEncoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpEncoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpIpmiMessage;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Creates RMCP packet containing encrypted IPMI command from IPMICommand
 * wrapper class or raw byte data.
 */
public final class Encoder {

    /**
     * Encodes IPMI command specified by payloadCoder into byte array raw data.
     *
     * @param protcolEncoder
     *            - instance of {@link IpmiEncoder} class for encoding of the
     *            IPMI session header. {@link Protocolv15Decoder} or
     *            {@link Protocolv20Decoder} should be used (depending on IPMI
     *            protocol version used).
     * @param payloadCoder
     *            - instance of {@link PayloadCoder} class used for building
     *            IPMI message payload.
     * @param messageSequenceNumber
     *            - A generated sequence number used for matching request and
     *            response. If IPMI message is sent in a session, it is used as
     *            a Session Sequence Number. For all IPMI messages,
     *            messageSequenceNumber is used as a IPMI LAN Message sequence
     *            number and as an IPMI payload message tag.
     * @param sessionId
     *            - ID of the managed system's session message is being sent in.
     *            For sessionless commands should be set to 0.
     * @return encoded IPMI command
     * @throws NoSuchAlgorithmException
     *             - when authentication, confidentiality or integrity algorithm
     *             fails.
     * @throws InvalidKeyException
     *             - when creating of the algorithm key fails
     */
    public static byte[] encode(IpmiEncoder protcolEncoder, PayloadCoder payloadCoder,
                                int messageSequenceNumber, int sessionSequenceNumber, int sessionId)
            throws NoSuchAlgorithmException, InvalidKeyException {
        return RmcpEncoder
                .encode(new RmcpIpmiMessage(protcolEncoder.encode(payloadCoder
                        .encodePayload(messageSequenceNumber, sessionSequenceNumber, sessionId))));
    }

    private Encoder() {
    }
}
