package ctu.client;

import ctu.core.threads.CTURunnable;

public abstract class CallBack extends CTURunnable {
	private boolean success;

	@Override
	public void run() {
		callback(success);
	}

	public abstract void callback(boolean success);

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
