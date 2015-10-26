/*
 * MessageListener.java 
 * Created on 2011-09-08
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package com.veraxsystems.vxipmi.api.sync;

import java.util.ArrayList;
import java.util.List;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.async.IpmiAsyncConnector;
import com.veraxsystems.vxipmi.api.async.IpmiListener;
import com.veraxsystems.vxipmi.api.async.messages.IpmiError;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponse;
import com.veraxsystems.vxipmi.api.async.messages.IpmiResponseData;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.connection.Connection;

/**
 * Listens to the {@link IpmiAsyncConnector} waiting for concrete message to
 * arrive. Must be registered via
 * {@link IpmiAsyncConnector#registerListener(IpmiListener)} to receive
 * messages.
 */
public class MessageListener implements IpmiListener {

	private ConnectionHandle handle;

	private int tag;

	private IpmiResponse response;

	/**
	 * Messages that have proper connection handle but arrived before tag was
	 * set - they need to be checked in case expected message arrived very early
	 * between sending the request and starting waiting for answer (waiting
	 * cannot be initialized before sending message since tag is not yet known
	 * then)
	 */
	private List<IpmiResponse> quickMessages;

	/**
	 * Initiates the {@link MessageListener}
	 * 
	 * @param handle
	 *            - {@link ConnectionHandle} associated with the
	 *            {@link Connection} {@link MessageListener} is expecting
	 *            message from.
	 */
	public MessageListener(ConnectionHandle handle) {
		quickMessages = new ArrayList<IpmiResponse>();
		this.handle = handle;
		tag = -1;
		response = null;
	}

	/**
	 * Blocks the invoking thread until deserved message arrives (tag and handle
	 * as specified in {@link #MessageListener(ConnectionHandle)}).
	 * 
	 * @param tag
	 *            - tag of the expected message
	 * @return {@link ResponseData} for message.
	 * @throws Exception
	 *             when message delivery fails
	 */
	public ResponseData waitForAnswer(int tag) throws Exception {
		if (tag < 0 || tag > 63) {
			throw new Exception("Corrupted message tag");
		}
		this.tag = tag;
		for (IpmiResponse response : quickMessages) {
			this.notify(response);
		}

		while (response == null) {
			Thread.sleep(1);
		}
		if (response instanceof IpmiResponseData) {
			this.tag = -1;
			quickMessages.clear();
			return ((IpmiResponseData) response).getResponseData();
		} else /* response instanceof IpmiError */{
			throw ((IpmiError) response).getException();
		}
	}

	@Override
	public synchronized void notify(IpmiResponse response) {
		if (response.getHandle().getHandle() == handle.getHandle()) {
			if (tag == -1) {
				quickMessages.add(response);
			} else if (response.getTag() == tag) {
				this.response = response;
			}
		}
	}

}
