package com.veraxsystems.vximpi.test;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.commands.chassis.GetChassisStatus;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.connection.Connection;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class SkipAuthCapTest {
    private static IpmiConnector connector;

    private static Properties properties;

    private ConnectionHandle connection;

    private static Logger logger = Logger.getLogger(SkipAuthCapTest.class);

    private static final int PORT = 6666;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        connector = new IpmiConnector(PORT);
        properties = new Properties();
        properties.load(new FileInputStream("src/test/resources/test.properties"));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        connector.tearDown();
    }

    @Test
    public void testMessage() throws Exception {
        connection = connector.createConnection(InetAddress.getByName(properties.getProperty("testIp")),
                Connection.getDefaultCipherSuite(), PrivilegeLevel.User);

        connector.openSession(connection, properties.getProperty("username"), properties.getProperty("password"), null);

        ResponseData data = connector.sendMessage(connection,
                new GetChassisStatus(IpmiVersion.V20, connection.getCipherSuite(), AuthenticationType.RMCPPlus));

        assertNotNull(data);
    }
}
