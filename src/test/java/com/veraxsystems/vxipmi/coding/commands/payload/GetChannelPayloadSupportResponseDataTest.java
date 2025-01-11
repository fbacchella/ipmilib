package com.veraxsystems.vxipmi.coding.commands.payload;

import com.veraxsystems.vxipmi.coding.protocol.PayloadType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GetChannelPayloadSupportResponseDataTest {

    private GetChannelPayloadSupportResponseData responseData;

    @Before
    public void setUp() {
        this.responseData = new GetChannelPayloadSupportResponseData();
    }

    @Test
    public void getSupportedPayloadsWhenNothingSupported() {
        responseData.setStandardPayloads((byte) 0);
        responseData.setSessionSetupPayloads((byte) 0);
        responseData.setOemPayloads((byte) 0);

        assertNotNull("Set of supported payloads cannot be null", responseData.getSupportedPayloads());
        assertTrue("Set of supported payloads should be empty", responseData.getSupportedPayloads().isEmpty());
    }

    @Test
    public void getSupportedPayloadsWhenIpmiAndSOLSupported() {
        responseData.setStandardPayloads((byte) 3);
        responseData.setSessionSetupPayloads((byte) 0);
        responseData.setOemPayloads((byte) 0);

        assertThat(responseData.getSupportedPayloads(), containsInAnyOrder(PayloadType.Ipmi, PayloadType.Sol));
    }

    @Test
    public void getSupportedtPayloadsWhenAllSupported() {
        responseData.setStandardPayloads((byte) 255);
        responseData.setSessionSetupPayloads((byte) 255);
        responseData.setOemPayloads((byte) 255);

        assertThat(responseData.getSupportedPayloads(), containsInAnyOrder(PayloadType.values()));
    }

}