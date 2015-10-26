package com.veraxsystems.vxipmi.transport;

import java.util.List;

public class UdpNotifier extends Thread {

	private List<UdpListener> listeners;
	private UdpMessage message;

	public UdpNotifier(UdpMessage message, List<UdpListener> listeners) {
		this.message = message;
		this.listeners = listeners;
	}

	@Override
	public void run() {

		for (UdpListener listener : listeners) {
			if (listener != null) {
				listener.notifyMessage(message);
			}
		}

		super.run();
	}
}
