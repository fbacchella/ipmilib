package com.veraxsystems.vxipmi.coding.commands.sdr.record;

import java.util.function.Function;

public class OpaqueRecord extends SensorRecord {
    /**
     * Decodes record data which depends on record type
     *
     * @param recordData - raw data containing whole record
     * @param record     - {@link SensorRecord} being populated
     */

    private byte[] data = new byte[]{};

    @Override
    protected void populateTypeSpecficValues(byte[] recordData, SensorRecord record) {
        data = new byte[recordData.length - 5];
        System.arraycopy(recordData, 5, data, 0, data.length);
    }

    public <T> T parse(Function<byte[], T> parser) {
        return parser.apply(data);
    }
}
