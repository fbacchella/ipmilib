/*
 * GetFruInfoRunner.java Created on 2011-09-20
 * 
 * Copyright (c) Verax Systems 2012. All rights reserved.
 * 
 * This software is furnished under a license. Use, duplication, disclosure
 * and all other uses are restricted to the rights specified in the written
 * license agreement.
 */
package com.veraxsystems.vxipmi.test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.fru.BaseUnit;
import com.veraxsystems.vxipmi.coding.commands.fru.GetFruInventoryAreaInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.GetFruInventoryAreaInfoResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.ReadFruData;
import com.veraxsystems.vxipmi.coding.commands.fru.ReadFruDataResponseData;
import com.veraxsystems.vxipmi.coding.commands.fru.record.BoardInfo;
import com.veraxsystems.vxipmi.coding.commands.fru.record.FruRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdr;
import com.veraxsystems.vxipmi.coding.commands.sdr.GetSdrResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepository;
import com.veraxsystems.vxipmi.coding.commands.sdr.ReserveSdrRepositoryResponseData;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.FruDeviceLocatorRecord;
import com.veraxsystems.vxipmi.coding.commands.sdr.record.SensorRecord;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.TypeConverter;

public class GetFruInfoRunner {

    /**
     * This is the value of Last Record ID (FFFFh). In order to retrieve the full set of SDR records, client must repeat
     * reading SDR records until MAX_REPO_RECORD_ID is returned as next record ID. For further information see section
     * 33.12 of the IPMI specification ver. 2.0
     */
    private static final int MAX_REPO_RECORD_ID = 65535;

    /**
     * Id of the built-in, default FRU
     */
    private static final int DEFAULT_FRU_ID = 0;

    /**
     * Size of data transmitted in single ReadFru command. Bigger values will improve performance. If server is
     * returning "Invalid data field in Request." error during ReadFru command, FRU_READ_PACKET_SIZE should be
     * decreased.
     */
    private static final int FRU_READ_PACKET_SIZE = 16;

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

    private static int nextRecId = 0;


    private static final String hostname = "192.168.1.1";

    private static final String username = "user";

    private static final String password = "pass";
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        IpmiConnector connector;

        // Create the connector, specify port that will be used to communicate
        // with the remote host. The UDP layer starts listening at this port, so
        // no 2 connectors can work at the same time on the same port.
        // The second parameter is optional - it binds the underlying socket to
        // the specific IP interface. Skipping it will result in kernel choosing
        // an IP address.

        connector = new IpmiConnector(6000);
        System.out.println("Connector created");

        // Create the connection and get the handle, specifiy IP address of the
        // remote host. The connection is being registered in ConnectionManager,
        // the handle will be needed to identify it among other connections
        // (target IP address isn't enough, since we can handle multiple
        // connections to the same host)
        ConnectionHandle handle = connector.createConnection(InetAddress.getByName(hostname));
        System.out.println("Connection created");

        // Get available cipher suites list via getAvailableCipherSuites and
        // pick one of them that will be used further in the session.
        CipherSuite cs = connector.getAvailableCipherSuites(handle).get(3);
        System.out.println("Cipher suite picked");

        // Provide chosen cipher suite and privilege level to the remote host.
        // From now on, your connection handle will contain these information.
        connector.getChannelAuthenticationCapabilities(handle, cs, PrivilegeLevel.User);
        System.out.println("Channel authentication capabilities receivied");

        // Start the session, provide username and password, and optionally the
        // BMC key (only if the remote host has two-key authentication enabled,
        // otherwise this parameter should be null)
        connector.openSession(handle, username, password, null);
        System.out.println("Session open");
        ReserveSdrRepositoryResponseData reservation = (ReserveSdrRepositoryResponseData) connector.sendMessage(handle,
                new ReserveSdrRepository(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));

        processFru(connector, handle, DEFAULT_FRU_ID);

        while (nextRecId < MAX_REPO_RECORD_ID) {

            System.out.println(">>Sending request for record " + nextRecId);

            // Search SDR repository for FruDeviceLocatorRecords, containing IDs of additional FRUs (if such exist)

            try {
                SensorRecord record = getSensorData(connector, handle, reservation.getReservationId());
                if (record instanceof FruDeviceLocatorRecord) {
                    FruDeviceLocatorRecord fruLocator = (FruDeviceLocatorRecord) record;

                    System.out.println(fruLocator.getName());
                    System.out.println(fruLocator.getDeviceType());
                    System.out.println("FRU entity ID: " + fruLocator.getFruEntityId());
                    System.out.println("FRU access address: " + fruLocator.getDeviceAccessAddress());
                    System.out.println("FRU device ID: " + fruLocator.getDeviceId());
                    System.out.println("FRU logical: " + fruLocator.isLogical());
                    System.out.println("FRU access lun: " + fruLocator.getAccessLun());
                    if (fruLocator.isLogical()) {
                        processFru(connector, handle, fruLocator.getDeviceId());
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                reservation = (ReserveSdrRepositoryResponseData) connector.sendMessage(handle,
                        new ReserveSdrRepository(IpmiVersion.V20, cs, AuthenticationType.RMCPPlus));
            }
        }

        // Close the session
        connector.closeSession(handle);
        System.out.println("Session closed");

        // Close connection manager and release the listener port.
        connector.tearDown();
        System.out.println("Connection manager closed");
    }

    private static void processFru(IpmiConnector connector, ConnectionHandle handle, int fruId) throws Exception {
        List<ReadFruDataResponseData> fruData = new ArrayList<ReadFruDataResponseData>();

        // get the FRU Inventory Area info
        GetFruInventoryAreaInfoResponseData info = (GetFruInventoryAreaInfoResponseData) connector.sendMessage(handle,
                new GetFruInventoryAreaInfo(IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus,
                        fruId));

        int size = info.getFruInventoryAreaSize();
        BaseUnit unit = info.getFruUnit();

        // since the size of single FRU entry can exceed maximum size of the
        // message sent via IPMI, it has to be read in chunks
        for (int i = 0; i < size; i += FRU_READ_PACKET_SIZE) {
            int cnt = FRU_READ_PACKET_SIZE;
            if (i + cnt > size) {
                cnt = size % FRU_READ_PACKET_SIZE;
            }
            try {
                // get single package od FRU data
                ReadFruDataResponseData data = (ReadFruDataResponseData) connector.sendMessage(handle, new ReadFruData(
                        IpmiVersion.V20, handle.getCipherSuite(), AuthenticationType.RMCPPlus, fruId, unit, i, cnt));

                fruData.add(data);

            } catch (Exception e) {
                System.out.println("Error while sending ReadFruData command : " + e.getMessage());
            }
        }

        try {
            // after collecting all the data, we can combine and parse it
            List<FruRecord> records = ReadFruData.decodeFruData(fruData);

            System.out.println("----------------------------------------------");
            System.out.println("Received FRU records");
            System.out.println("----------------------------------------------");
            for (FruRecord record : records) {
                // now we can for example display received info about board
                if (record instanceof BoardInfo) {
                    BoardInfo bi = (BoardInfo) record;
                    System.out.println(bi.getBoardSerialNumber() + " " + bi.getBoardProductName() + " "
                            + bi.getBoardPartNumber() + " " + bi.getBoardManufacturer());
                } else {
                    System.out.println("Other format: " + record.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            System.out.println("Error while parsing FRU record: " + e.getMessage());
        }
    }

    public static SensorRecord getSensorData(IpmiConnector connector, ConnectionHandle handle, int reservationId)
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
