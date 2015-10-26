package com.veraxsystems.vxipmi.coding.commands.session;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.veraxsystems.vxipmi.coding.commands.CommandCodes;
import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiVersion;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.payload.CompletionCode;
import com.veraxsystems.vxipmi.coding.payload.IpmiPayload;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanRequest;
import com.veraxsystems.vxipmi.coding.payload.lan.IpmiLanResponse;
import com.veraxsystems.vxipmi.coding.payload.lan.NetworkFunction;
import com.veraxsystems.vxipmi.coding.protocol.AuthenticationType;
import com.veraxsystems.vxipmi.coding.protocol.IpmiMessage;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * Wrapper class for Set Session Privilege Level command
 */
public class SetSessionPrivilegeLevel extends IpmiCommandCoder {

    private PrivilegeLevel privilegeLevel;

    /**
     * Initiates {@link SetSessionPrivilegeLevel} for encoding and decoding
     * @param version
     * - IPMI version of the command.
     * @param cipherSuite
     * - {@link CipherSuite} containing authentication, confidentiality and integrity algorithms for this session.
     * @param authenticationType
     * - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
     * @param privilegeLevel
     * - Requested {@link PrivilegeLevel} to acquire. Can not be higher than level declared during starting session.
     */
    public SetSessionPrivilegeLevel(IpmiVersion version, CipherSuite cipherSuite,
            AuthenticationType authenticationType, PrivilegeLevel privilegeLevel) {
        super(version, cipherSuite, authenticationType);
        this.privilegeLevel = privilegeLevel;
    }

    @Override
    public byte getCommandCode() {
        return CommandCodes.SET_SESSION_PRIVILEGE_LEVEL;
    }

    @Override
    public NetworkFunction getNetworkFunction() {
        return NetworkFunction.ApplicationRequest;
    }

    @Override
    protected IpmiPayload preparePayload(int sequenceNumber) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] requestData = new byte[1];

        requestData[0] = TypeConverter.intToByte(getRequestedPrivilegeLevelEncoded());

        return new IpmiLanRequest(getNetworkFunction(), getCommandCode(), requestData,
                TypeConverter.intToByte(sequenceNumber % 64));
    }

    @Override
    public ResponseData getResponseData(IpmiMessage message) throws IllegalArgumentException, IPMIException,
            NoSuchAlgorithmException, InvalidKeyException {
        if (!isCommandResponse(message)) {
            throw new IllegalArgumentException("This is not a response for Get SEL Entry command");
        }
        if (!(message.getPayload() instanceof IpmiLanResponse)) {
            throw new IllegalArgumentException("Invalid response payload");
        }
        if (((IpmiLanResponse) message.getPayload()).getCompletionCode() != CompletionCode.Ok) {
            throw new IPMIException(((IpmiLanResponse) message.getPayload()).getCompletionCode());
        }

        return new SetSessionPrivilegeLevelResponseData();
    }

    private byte getRequestedPrivilegeLevelEncoded() {
        switch (privilegeLevel) {
        case MaximumAvailable:
            return 0;
        case Callback:
            return TypeConverter.intToByte(0x1);
        case User:
            return TypeConverter.intToByte(0x2);
        case Operator:
            return TypeConverter.intToByte(0x3);
        case Administrator:
            return TypeConverter.intToByte(0x4);
        default:
            throw new IllegalArgumentException("Invalid privilege level");
        }
    }
}
