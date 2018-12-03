/*
 * ActivatePayloadResponseDataTest.java
 * Created on 2017-05-16
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.coding.commands.payload;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ActivateSolPayloadResponseDataTest {

    private ActivateSolPayloadResponseData responseData;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.responseData = new ActivateSolPayloadResponseData();
    }

    @Test
    public void setAuxilaryInformationNull() throws Exception {
        byte[] auxilaryInformation = null;

        expectedException.expect(IllegalArgumentException.class);

        responseData.setAuxilaryInformationData(auxilaryInformation);
    }

    @Test
    public void setAuxilaryInformationEmpty() throws Exception {
        byte[] auxilaryInformation = new byte[0];

        expectedException.expect(IllegalArgumentException.class);

        responseData.setAuxilaryInformationData(auxilaryInformation);
    }

    @Test
    public void setAuxilaryInformationTooLarge() throws Exception {
        byte[] auxilaryInformation = new byte[] {0, 0, 0, 0, 0};

        expectedException.expect(IllegalArgumentException.class);

        responseData.setAuxilaryInformationData(auxilaryInformation);
    }

    @Test
    public void isTestModeTrueWhenTestModeSet() throws Exception {
        byte[] auxilaryInformation = new byte[] {1, 0};
        responseData.setAuxilaryInformationData(auxilaryInformation);
        assertTrue("Test mode should be true when set", responseData.isTestMode());
    }

    @Test
    public void isTestModeFalseWhenTestModeNotSet() throws Exception {
        byte[] auxilaryInformation = new byte[] {0, 0};
        responseData.setAuxilaryInformationData(auxilaryInformation);
        assertFalse("Test mode should be false when not set", responseData.isTestMode());
    }

    @Test
    public void setInboundPayloadSizeNull() throws Exception {
        byte[] inboundPayloadSize = null;

        expectedException.expect(IllegalArgumentException.class);

        responseData.setInboundPayloadSize(inboundPayloadSize);
    }

    @Test
    public void setInboundPayloadSizeEmpty() throws Exception {
        byte[] inboundPayloadSize = new byte[0];

        expectedException.expect(IllegalArgumentException.class);

        responseData.setInboundPayloadSize(inboundPayloadSize);
    }

    @Test
    public void setInboundPayloadSizeTooLarge() throws Exception {
        byte[] inboundPayloadSize = new byte[] {0, 0, 0};

        expectedException.expect(IllegalArgumentException.class);

        responseData.setInboundPayloadSize(inboundPayloadSize);
    }

    @Test
    public void setInboundPayloadSizeWhen0() throws Exception {
        byte[] inboundPayloadSize = new byte[] {0, 0};
        responseData.setInboundPayloadSize(inboundPayloadSize);

        assertEquals(0, responseData.getInboundPayloadSize());
    }

    @Test
    public void setInboundPayloadSizeWhenFirstByte0() throws Exception {
        byte[] inboundPayloadSize = new byte[] {0, 5};
        responseData.setInboundPayloadSize(inboundPayloadSize);

        assertEquals(1280, responseData.getInboundPayloadSize());
    }

    @Test
    public void setInboundPayloadSizeWhenBothBytesNon0() throws Exception {
        byte[] inboundPayloadSize = new byte[] {2, 7};
        responseData.setInboundPayloadSize(inboundPayloadSize);

        assertEquals(1794, responseData.getInboundPayloadSize());
    }

    @Test
    public void setOutboundPayloadSizeNull() throws Exception {
        byte[] inboundPayloadSize = null;

        expectedException.expect(IllegalArgumentException.class);

        responseData.setOutboundPayloadSize(inboundPayloadSize);
    }

    @Test
    public void setOutboundPayloadSizeEmpty() throws Exception {
        byte[] inboundPayloadSize = new byte[0];

        expectedException.expect(IllegalArgumentException.class);

        responseData.setOutboundPayloadSize(inboundPayloadSize);
    }

    @Test
    public void setOutboundPayloadSizeTooLarge() throws Exception {
        byte[] inboundPayloadSize = new byte[] {0, 0, 0};

        expectedException.expect(IllegalArgumentException.class);

        responseData.setOutboundPayloadSize(inboundPayloadSize);
    }

    @Test
    public void setOutboundPayloadSizeWhen0() throws Exception {
        byte[] inboundPayloadSize = new byte[] {0, 0};
        responseData.setOutboundPayloadSize(inboundPayloadSize);

        assertEquals(0, responseData.getOutboundPayloadSize());
    }

    @Test
    public void setOutboundPayloadSizeWhenFirstByte0() throws Exception {
        byte[] inboundPayloadSize = new byte[] {0, 27};
        responseData.setOutboundPayloadSize(inboundPayloadSize);

        assertEquals(6912, responseData.getOutboundPayloadSize());
    }

    @Test
    public void setOutboundPayloadSizeWhenBothBytesNon0() throws Exception {
        byte[] inboundPayloadSize = new byte[] {4, 1};
        responseData.setOutboundPayloadSize(inboundPayloadSize);

        assertEquals(260, responseData.getOutboundPayloadSize());
    }

    @Test
    public void setPayloadUdpPortNumberNull() throws Exception {
        byte[] payloadUdpPortNumber = null;

        expectedException.expect(IllegalArgumentException.class);

        responseData.setPayloadUdpPortNumber(payloadUdpPortNumber);
    }

    @Test
    public void setPayloadUdpPortNumberEmpty() throws Exception {
        byte[] payloadUdpPortNumber = new byte[0];

        expectedException.expect(IllegalArgumentException.class);

        responseData.setPayloadUdpPortNumber(payloadUdpPortNumber);
    }

    @Test
    public void setPayloadUdpPortNumberTooLarge() throws Exception {
        byte[] payloadUdpPortNumber = new byte[] {0, 0, 0};

        expectedException.expect(IllegalArgumentException.class);

        responseData.setPayloadUdpPortNumber(payloadUdpPortNumber);
    }

    @Test
    public void setPayloadUdpPortNumberWhen0() throws Exception {
        byte[] payloadUdpPortNumber = new byte[] {0, 0};
        responseData.setPayloadUdpPortNumber(payloadUdpPortNumber);

        assertEquals(0, responseData.getPayloadUdpPortNumber());
    }

    @Test
    public void setPayloadUdpPortNumberWhenFirstByte0() throws Exception {
        byte[] payloadUdpPortNumber = new byte[] {0, 127};
        responseData.setPayloadUdpPortNumber(payloadUdpPortNumber);

        assertEquals(32512, responseData.getPayloadUdpPortNumber());
    }

    @Test
    public void setPayloadUdpPortNumberWhenBothBytesNon0() throws Exception {
        byte[] payloadUdpPortNumber = new byte[] {14, 84};
        responseData.setPayloadUdpPortNumber(payloadUdpPortNumber);

        assertEquals(21518, responseData.getPayloadUdpPortNumber());
    }

    @Test
    public void setPayloadVlanNumberNull() throws Exception {
        byte[] payloadVlanNumber = null;

        expectedException.expect(IllegalArgumentException.class);

        responseData.setPayloadVlanNumber(payloadVlanNumber);
    }

    @Test
    public void setPayloadVlanNumberEmpty() throws Exception {
        byte[] payloadVlanNumber = new byte[0];

        expectedException.expect(IllegalArgumentException.class);

        responseData.setPayloadVlanNumber(payloadVlanNumber);
    }

    @Test
    public void setPayloadVlanNumberTooLarge() throws Exception {
        byte[] payloadVlanNumber = new byte[] {0, 0, 0};

        expectedException.expect(IllegalArgumentException.class);

        responseData.setPayloadVlanNumber(payloadVlanNumber);
    }

    @Test
    public void setPayloadVlanNumberWhen0() throws Exception {
        byte[] payloadVlanNumber = new byte[] {0, 0};
        responseData.setPayloadVlanNumber(payloadVlanNumber);

        assertEquals(0, responseData.getPayloadVlanNumber());
    }

    @Test
    public void setPayloadVlanNumberWhenFirstByte0() throws Exception {
        byte[] payloadVlanNumber = new byte[] {0, 44};
        responseData.setPayloadVlanNumber(payloadVlanNumber);

        assertEquals(11264, responseData.getPayloadVlanNumber());
    }

    @Test
    public void setPayloadVlanNumberWhenBothBytesNon0() throws Exception {
        byte[] payloadVlanNumber = new byte[] {111, 111};
        responseData.setPayloadVlanNumber(payloadVlanNumber);

        assertEquals(28527, responseData.getPayloadVlanNumber());
    }

}