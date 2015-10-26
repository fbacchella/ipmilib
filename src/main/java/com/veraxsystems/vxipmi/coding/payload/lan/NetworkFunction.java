/*
 * NetworkFunction.java 
 * Created on 2011-07-21
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.coding.payload.lan;

/**
 * IPMB network functions
 */
public enum NetworkFunction {
	ChassisRequest(NetworkFunction.CHASSISREQUEST), ChassisResponse(
			NetworkFunction.CHASSISRESPONSE), StorageRequest(
			NetworkFunction.STORAGEREQUEST), StorageResponse(
			NetworkFunction.STORAGERESPONSE), BridgeRequest(
			NetworkFunction.BRIDGEREQUEST), BridgeResponse(
			NetworkFunction.BRIDGERESPONSE),
	/**
	 * Sensor/Event Request
	 */
	SensorRequest(NetworkFunction.SENSORREQUEST),
	/**
	 * Sensor/Event Response
	 */
	SensorResponse(NetworkFunction.SENSORRESPONSE), ApplicationRequest(
			NetworkFunction.APPLICATIONREQUEST), ApplicationResponse(
			NetworkFunction.APPLICATIONRESPONSE), FirmwareRequest(
			NetworkFunction.FIRMWAREREQUEST), FirmwareResponse(
			NetworkFunction.FIRMWARERESPONSE), ;
	private static final int CHASSISREQUEST = 0;
	private static final int CHASSISRESPONSE = 1;
	private static final int STORAGEREQUEST = 10;
	private static final int STORAGERESPONSE = 11;
	private static final int BRIDGEREQUEST = 2;
	private static final int BRIDGERESPONSE = 3;
	private static final int SENSORREQUEST = 4;
	private static final int SENSORRESPONSE = 5;
	private static final int APPLICATIONREQUEST = 6;
	private static final int APPLICATIONRESPONSE = 7;
	private static final int FIRMWAREREQUEST = 8;
	private static final int FIRMWARERESPONSE = 9;

	private int code;

	NetworkFunction(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static NetworkFunction parseInt(int value) {
		switch (value) {
		case CHASSISREQUEST:
			return ChassisRequest;
		case CHASSISRESPONSE:
			return ChassisResponse;
		case STORAGEREQUEST:
			return StorageRequest;
		case STORAGERESPONSE:
			return StorageResponse;
		case BRIDGEREQUEST:
			return BridgeRequest;
		case BRIDGERESPONSE:
			return BridgeResponse;
		case SENSORREQUEST:
			return SensorRequest;
		case SENSORRESPONSE:
			return SensorResponse;
		case APPLICATIONREQUEST:
			return ApplicationRequest;
		case APPLICATIONRESPONSE:
			return ApplicationResponse;
		case FIRMWAREREQUEST:
			return FirmwareRequest;
		case FIRMWARERESPONSE:
			return FirmwareResponse;
		default:
			throw new IllegalArgumentException("Invalid value: " + value);
		}
	}
}