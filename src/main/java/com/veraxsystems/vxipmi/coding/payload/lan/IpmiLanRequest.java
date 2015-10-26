/*
 * IpmiLanRequest.java 
 * Created on 2011-08-02
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload.lan;

import com.veraxsystems.vxipmi.coding.commands.CommandCodes;
import com.veraxsystems.vxipmi.common.TypeConverter;

/**
 * A wrapper class for IPMB LAN message
 */
public class IpmiLanRequest extends IpmiLanMessage {
	/**
	 * Builds IpmiLanRequest addressed at LUN 0.
	 * 
	 * @param networkFunction
	 *            - command specific {@link NetworkFunction}
	 * @param commandCode
	 *            - command specific {@link CommandCodes}
	 * @param requestData
	 *            - command specific payload
	 * @param sequenceNumber
	 *            - used to match request and response - must be in range <0-63>
	 */
	public IpmiLanRequest(NetworkFunction networkFunction, byte commandCode,
			byte[] requestData, byte sequenceNumber) {
		this(networkFunction, commandCode, requestData, sequenceNumber,
				TypeConverter.intToByte(0));
	}

	/**
	 * Builds IpmiLanRequest.
	 * 
	 * @param networkFunction
	 *            - command specific {@link NetworkFunction}
	 * @param commandCode
	 *            - command specific {@link CommandCodes}
	 * @param requestData
	 *            - command specific payload
	 * @param sequenceNumber
	 *            - used to match request and response - must be in range <0-63>
	 * @param lun
	 *            - target Logical Unit Number. Must be in range <0-3>.
	 */
	public IpmiLanRequest(NetworkFunction networkFunction, byte commandCode,
			byte[] requestData, byte sequenceNumber, byte lun) {
		if (lun < 0 || lun > 3) {
			throw new IllegalArgumentException("Invalid LUN");
		}
		setResponderAddress(IpmiLanConstants.BMC_ADDRESS);
		setNetworkFunction(networkFunction);
		setResponderLogicalUnitNumber(TypeConverter.intToByte(lun));
		setRequesterAddress(IpmiLanConstants.REMOTE_CONSOLE_ADDRESS);
		setRequesterLogicalUnitNumber(TypeConverter.intToByte(0));
		setSequenceNumber(sequenceNumber);
		setData(requestData);
		setCommand(commandCode);
	}

	@Override
	public int getPayloadLength() {
		int length = 7;

		if (getData() != null) {
			length += getData().length;
		}
		return length;
	}

	@Override
	public byte[] getPayloadData() {
		byte[] message = new byte[getPayloadLength()];

		message[0] = getResponderAddress();
		message[1] = TypeConverter.intToByte((networkFunction << 2)
				| getResponderLogicalUnitNumber());
		message[2] = getChecksum1(message);
		message[3] = getRequesterAddress();
		message[4] = TypeConverter
				.intToByte(((getSequenceNumber() & 0x3f) << 2)
						| getResponderLogicalUnitNumber());
		message[5] = getCommand();

		if (getData() != null) {
			System.arraycopy(getData(), 0, message, 6, getData().length);
		}

		message[message.length - 1] = getChecksum2(message);

		return message;
	}

}
