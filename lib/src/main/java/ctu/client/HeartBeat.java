package ctu.client;

import ctu.core.abstracts.Connection;
import ctu.core.abstracts.Packet;
import ctu.core.interfaces.Listener;
import ctu.core.packets.PacketPing;

public class HeartBeat implements Listener {
	@Override
	public void postConnect(Connection connection) {
	}

	@Override
	public void connected(Connection connection) {
		connection.getCtu().getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep((long) (connection.getCtu().getConfig().TIMEOUT * 0.9F));
					} catch (final InterruptedException e) {
					}

					connection.sendTCP(new PacketPing());
				}
			}
		});
	}

	@Override
	public void recieved(Connection connection, Packet packet) {
	}

	@Override
	public void disconnected(Connection connection) {
	}

	@Override
	public void reset(Connection connection) {
	}

	@Override
	public void timeout(Connection connection) {
	}
}