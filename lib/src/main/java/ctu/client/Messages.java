package ctu.client;

import ctu.core.abstracts.Connection;
import ctu.core.abstracts.Packet;
import ctu.core.interfaces.Listener;

public class Messages implements Listener {
	@Override
	public void connected(Connection connection) {
		System.out.println("Connected...");
	}

	@Override
	public void disconnected(Connection connection) {
		System.out.println("Disconnected...");
	}

	@Override
	public void postConnect(Connection connection) {
		System.out.println("Post Connect...");
	}

	@Override
	public void reset(Connection connection) {
		System.out.println("Reset...");
	}

	@Override
	public void timeout(Connection connection) {
		System.out.println("Timed Out...");
	}

	@Override
	public void recieved(Connection connection, Packet packet) {
	}
}