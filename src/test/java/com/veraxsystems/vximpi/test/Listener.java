/*
 * UdpListener.java 
 * Created on 2011-09-20
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vximpi.test;

import com.veraxsystems.vxipmi.coding.Decoder;
import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.payload.lan.IPMIException;
import com.veraxsystems.vxipmi.coding.protocol.decoder.Protocolv20Decoder;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;
import com.veraxsystems.vxipmi.transport.UdpListener;
import com.veraxsystems.vxipmi.transport.UdpMessage;

/**
 * Tests utility class, implementation for {@link UdpListener}
 */
public class Listener implements UdpListener {

	private int receivedCnt;

	private boolean ccOk = true;
	private boolean ok = true;

	private CipherSuite cipherSuite;
	private IpmiCommandCoder commandCoder;

	public Listener(CipherSuite cipherSuite, IpmiCommandCoder commandCoder) {
		this.cipherSuite = cipherSuite;
		this.commandCoder = commandCoder;
	}

	@Override
	public void notifyMessage(UdpMessage message) {
		++receivedCnt;
		try {
			Decoder.decode(message.getMessage(), new Protocolv20Decoder(
					cipherSuite), commandCoder);
		} catch (IPMIException e) {
			setCcOk(false);
			setOk(false);
		} catch (Exception e) {
			setOk(false);
		}
	}

	public void setReceivedCnt(int receivedCnt) {
		this.receivedCnt = receivedCnt;
	}

	public int getReceivedCnt() {
		return receivedCnt;
	}

	public void setCcOk(boolean ccOk) {
		this.ccOk = ccOk;
	}

	public boolean isCcOk() {
		return ccOk;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public boolean isOk() {
		return ok;
	}

}
