package com.veraxsystems.vxipmi.test;

import java.net.InetAddress;
import java.util.List;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdr;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReading;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSensorReadingResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepository;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepositoryResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.CompactSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.FullSensorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.RateUnit;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.ReadingType;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.SensorRecord;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.PropertiesManager;
import com.veraxsystems.vxipmi.common.TypeConverter;

public class GetAllSensorReadingsRunner {

    /**
     * This is the value of Last Record ID (FFFFh). In order to retrieve the full set of SDR records, client must repeat
     * reading SDR records until MAX_REPO_RECORD_ID is returned as next record ID. For further information see section
     * 33.12 of the IPMI specification ver. 2.0
     */
    private static final int MAX_REPO_RECORD_ID = 65535;

    private static final String hostname = "192.168.1.1";

    private static final String username = "user";

    private static final String password = "pass";

    /**
     * Size of the initial GetSdr message to get record header and size
     */
    private static final int INITIAL_CHUNK_SIZE = 8;

    /**
     * Chunk size depending on buffer size of the IPMI server. Bigger values will improve performance. If server is
     * returning "Cannot return number of requested data bytes." error during GetSdr command, CHUNK_SIZE should be
     * decreased.
     */
    private static final int CHUNK_SIZE = 16;

    /**
     * Size of SDR record header
     */
    private static final int HEADER_SIZE = 5;

    private int nextRecId;

    /**
     * @param args
     */
    public static void main(String[] args) {
        GetAllSensorReadingsRunner runner = new GetAllSensorReadingsRunner();
        try {
            // Change default timeout value
            PropertiesManager.getInstance().setProperty("timeout", "2500");
            runner.doRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doRun() throws Exception {
        // Id 0 indicates first record in SDR. Next IDs can be retrieved from
        // records - they are organized in a list and there is no BMC command to
        // get all of them.
        nextRecId = 0;

        // Some BMCs allow getting sensor records without reservation, so we try
        // to do it that way first
        int reservationId = 0;
        int lastReservationId = -1;

        // Create the connector
        IpmiConnector connector = new IpmiConnector(0);

        // start the session to the remote host. We assume, that two-key
        // authentication isn't enabled, so BMC key is left null (see
        // #startSession for details).
        ConnectionHandle handle = startSession(connector, InetAddress.getByName(hostname), username, password, "",
                PrivilegeLevel.User);

        // Change timeout of this particular connection (default value for further connections does not change)
        connector.setTimeout(handle, 2750);
        
        // We get sensor data until we encounter ID = 65535 which means that
        // this record is the last one.
        while (nextRecId < MAX_REPO_RECORD_ID) {

            SensorRecord record = null;

            try {
                // Populate the sensor record and get ID of the next record in
                // repository (see #getSensorData for details).
                record = getSensorData(connector, handle, reservationId);

                int recordReadingId = -1;

                // We check if the received record is either FullSensorRecord or
                // CompactSensorRecord, since these types have readings
                // associated with them (see IPMI specification for details).
                if (record instanceof FullSensorRecord) {
                    FullSensorRecord fsr = (FullSensorRecord) record;
                    recordReadingId = TypeConverter.byteToInt(fsr.getSensorNumber());
                    System.out.println(fsr.getName());

                } else if (record instanceof CompactSensorRecord) {
                    CompactSensorRecord csr = (CompactSensorRecord) record;
                    recordReadingId = TypeConverter.byteToInt(csr.getSensorNumber());
                    System.out.println(csr.getName());
                }

                // If our record has got a reading associated, we get request
                // for it
                GetSensorReadingResponseData data2 = null;
                try {
                    if (recordReadingId >= 0) {
                        data2 = (GetSensorReadingResponseData) connector
                                .sendMessage(handle, new GetSensorReading(IpmiVersion.V20, handle.getCipherSuite(),
                                        AuthenticationType.RMCPPlus, recordReadingId));
                        if (record instanceof FullSensorRecord) {
                            FullSensorRecord rec = (FullSensorRecord) record;
                            // Parse sensor reading using information retrieved
                            // from sensor record. See
                            // FullSensorRecord#calcFormula for details.
                            System.out.println(data2.getSensorReading(rec) + " " + rec.getSensorBaseUnit().toString()
                                    + (rec.getRateUnit() != RateUnit.None ? " per " + rec.getRateUnit() : ""));
                        }
                        if (record instanceof CompactSensorRecord) {
                            CompactSensorRecord rec = (CompactSensorRecord) record;
                            // Get states asserted by the sensor
                            List<ReadingType> events = data2.getStatesAsserted(rec.getSensorType(),
                                    rec.getEventReadingType());
                            String s = "";
                            for (int i = 0; i < events.size(); ++i) {
                                s += events.get(i) + ", ";
                            }
                            System.out.println(s);

                        }

                    }
                } catch (IPMIException e) {
                    if (e.getCompletionCode() == CompletionCode.DataNotPresent) {
                        e.printStackTrace();
                    } else {
                        throw e;
                    }
                }
            } catch (IPMIException e) {

                System.out.println("Getting new reservation ID");

                System.out.println("156: " + e.getMessage());

                // If getting sensor data failed, we check if it already failed
                // with this reservation ID.
                if (lastReservationId == reservationId)
                    throw e;
                lastReservationId = reservationId;

                // If the cause of the failure was canceling of the
                // reservation, we get new reservationId and retry. This can
                // happen many times during getting all sensors, since BMC can't
                // manage parallel sessions and invalidates old one if new one
                // appears.
                reservationId = ((ReserveSdrRepositoryResponseData) connector
                        .sendMessage(handle, new ReserveSdrRepository(IpmiVersion.V20, handle.getCipherSuite(),
                                AuthenticationType.RMCPPlus))).getReservationId();
            }
        }

        // Close the session
        connector.closeSession(handle);
        System.out.println("Session closed");
        // Close the connection
        connector.closeConnection(handle);
        connector.tearDown();
        System.out.println("Connection manager closed");
    }

    public ConnectionHandle startSession(IpmiConnector connector, InetAddress address, String username,
            String password, String bmcKey, PrivilegeLevel privilegeLevel) throws Exception {

        // Create the handle to the connection which will be it's identifier
        ConnectionHandle handle = connector.createConnection(address);

        CipherSuite cs;

        try {
            // Get cipher suites supported by the remote host
            List<CipherSuite> suites = connector.getAvailableCipherSuites(handle);

            if (suites.size() > 3) {
                cs = suites.get(3);
            } else if (suites.size() > 2) {
                cs = suites.get(2);
            } else if (suites.size() > 1) {
                cs = suites.get(1);
            } else {
                cs = suites.get(0);
            }
            // Pick the cipher suite and requested privilege level for the
            // session
            connector.getChannelAuthenticationCapabilities(handle, cs, privilegeLevel);

            // Open the session and authenticate
            connector.openSession(handle, username, password, bmcKey.getBytes());
        } catch (Exception e) {
            connector.closeConnection(handle);
            throw e;
        }

        return handle;
    }

    public SensorRecord getSensorData(IpmiConnector connector, ConnectionHandle handle, int reservationId)
            throws Exception {
        try {
            // BMC capabilities are limited - that means that sometimes the
            // record size exceeds maximum size of the message. Since we don't
            // know what is the size of the record, we try to get
            // whole one first
            GetSdrResponseData data = (GetSdrResponseData) connector.sendMessage(handle, new GetSdr(IpmiVersion.V20,
                    handle.getCipherSuite(), AuthenticationType.RMCPPlus, reservationId, nextRecId));
            // If getting whole record succeeded we create SensorRecord from
            // received data...
            SensorRecord sensorDataToPopulate = SensorRecord.populateSensorRecord(data.getSensorRecordData());
            // ... and update the ID of the next record
            nextRecId = data.getNextRecordId();
            return sensorDataToPopulate;
        } catch (IPMIException e) {
            // System.out.println(e.getCompletionCode() + ": " + e.getMessage());
            // The following error codes mean that record is too large to be
            // sent in one chunk. This means we need to split the data in
            // smaller parts.
            if (e.getCompletionCode() == CompletionCode.CannotRespond
                    || e.getCompletionCode() == CompletionCode.UnspecifiedError) {
                System.out.println("Getting chunks");
                // First we get the header of the record to find out its size.
                GetSdrResponseData data = (GetSdrResponseData) connector.sendMessage(handle, new GetSdr(
                        IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus, reservationId,
                        nextRecId, 0, INITIAL_CHUNK_SIZE));
                // The record size is 5th byte of the record. It does not take
                // into account the size of the header, so we need to add it.
                int recSize = TypeConverter.byteToInt(data.getSensorRecordData()[4]) + HEADER_SIZE;
                int read = INITIAL_CHUNK_SIZE;

                byte[] result = new byte[recSize];

                System.arraycopy(data.getSensorRecordData(), 0, result, 0, data.getSensorRecordData().length);

                // We get the rest of the record in chunks (watch out for
                // exceeding the record size, since this will result in BMC's
                // error.
                while (read < recSize) {
                    int bytesToRead = CHUNK_SIZE;
                    if (recSize - read < bytesToRead) {
                        bytesToRead = recSize - read;
                    }
                    GetSdrResponseData part = (GetSdrResponseData) connector.sendMessage(handle, new GetSdr(
                            IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus, reservationId,
                            nextRecId, read, bytesToRead));

                    System.arraycopy(part.getSensorRecordData(), 0, result, read, bytesToRead);

                    System.out.println("Received part");

                    read += bytesToRead;
                }

                // Finally we populate the sensor record with the gathered
                // data...
                SensorRecord sensorDataToPopulate = SensorRecord.populateSensorRecord(result);
                // ... and update the ID of the next record
                nextRecId = data.getNextRecordId();
                return sensorDataToPopulate;
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }
    }

}
