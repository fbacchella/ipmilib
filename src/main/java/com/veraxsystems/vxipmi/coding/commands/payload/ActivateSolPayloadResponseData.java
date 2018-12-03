/*
 * ActivateSolPayloadResponseData.java
 * Created on 18.05.2017
 *
 * Copyright (c) Sonalake 2017.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */

package com.veraxsystems.vxipmi.coding.commands.payload;

import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Concrete implementation of {@link ActivatePayloadResponseData} for {@link com.veraxsystems.vxipmi.coding.protocol.PayloadType#SOL}.
 */
public class ActivateSolPayloadResponseData extends ActivatePayloadResponseData {

    /**
     * Information whether test mode is enabled or not.
     */
    private boolean testMode;

    @Override
    public void setAuxilaryInformationData(byte[] auxilaryInformationData) {
        if (auxilaryInformationData == null || auxilaryInformationData.length < 1 || auxilaryInformationData.length > 4) {
            throw new IllegalArgumentException("Auxilary information data must consists of 1 to 4 bytes");
        }

        this.testMode = TypeConverter.isBitSetOnPosition(0, auxilaryInformationData[0]);
    }

    public boolean isTestMode() {
        return testMode;
    }
}
