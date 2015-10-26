package com.veraxsystems.vxipmi.transport;

import java.io.IOException;

/**
 * Low level connection handler.
 * 
 */
public interface Messenger {
	/**
	 * Sends {@link UdpMessage}.
	 * 
	 * @param message
	 *            - {@link UdpMessage} to send.
	 * @throws IOException
	 *             when sending of the message fails
	 */
	void send(UdpMessage message) throws IOException;

	/**
	 * Registers listener in the {@link Messenger} so it will be notified via
	 * {@link UdpListener#notifyMessage(UdpMessage)} when new message arrives.
	 * 
	 * @param listener
	 *            - {@link UdpListener} to register.
	 */
	void register(UdpListener listener);

	/**
	 * Unregisters listener from {@link Messenger} so it no longer will be
	 * notified.
	 * 
	 * @param listener
	 *            - {@link UdpListener} to unregister
	 */
	void unregister(UdpListener listener);

	/**
	 * Closes the connection.
	 */
	void closeConnection();
}
