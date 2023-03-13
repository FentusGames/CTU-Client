package ctu.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import ctu.core.abstracts.CTU;
import ctu.core.abstracts.Connection;

public class CTUClient extends CTU {
	private ConnectionClient connection;
	private boolean success = false;
	private CallBack callback;

	public void setCallback(CallBack callback) {
		this.callback = callback;
	}

	@Override
	public void exec() {
		System.out.println("Connecting...");

		try {
			setSocket(SocketFactory.getDefault().createSocket(getConfig().IP_ADDRESS, getConfig().PORT));
			success = true;
		} catch (final ConnectException e) {
			System.out.println("Could not connect to server, connection refused.");
		} catch (final UnknownHostException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			callback.setSuccess(success);

			if (success) {
				getExecutorService().execute(callback);

				connection = new ConnectionClient(this);

				connection.run();

				executorStop();

				System.exit(0);
			} else {
				getExecutorService().execute(callback);
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}
}
