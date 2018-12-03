package com.veraxsystems.vxipmi.test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import com.veraxsystems.vxipmi.coding.rmcp.RmcpDecoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpEncoder;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpMessage;
import com.veraxsystems.vxipmi.coding.rmcp.RmcpPingMessage;
import com.veraxsystems.vxipmi.common.Constants;
import com.veraxsystems.vxipmi.common.TypeConverter;
import com.veraxsystems.vxipmi.transport.UdpListener;
import com.veraxsystems.vxipmi.transport.UdpMessage;
import com.veraxsystems.vxipmi.transport.UdpMessenger;

public class RmcpPing {

    private static final String ADDRESS = "192.168.1.1";

    public static void main(String[] args) throws IOException, InterruptedException {
        UdpListener listener = new UdpListener() {
            
            @Override
            public void notifyMessage(UdpMessage message) {
                RmcpMessage rmcpMessage = RmcpDecoder.decode(message.getMessage());
                System.out.println(rmcpMessage.getClassOfMessage());
                System.out.println(Arrays.toString(rmcpMessage.getData()));
                //read bit showing if remote machine supports IPMI
                if((TypeConverter.byteToInt(rmcpMessage.getData()[16]) & 0x80) != 0) {
                    System.out.println("IPMI supported");
                } else {
                    System.out.println("IPMI not supported");
                }
            }
        };
        
        UdpMessenger messenger = new UdpMessenger(6666);
        messenger.register(listener);
        
        UdpMessage message = new UdpMessage();
        
        message.setAddress(InetAddress.getByName(ADDRESS));
        message.setPort(Constants.IPMI_PORT);
        message.setMessage(RmcpEncoder.encode(new RmcpPingMessage(1)));
        
        messenger.send(message);
        
        Thread.sleep(2000);
        
        messenger.closeConnection();
    }
}
