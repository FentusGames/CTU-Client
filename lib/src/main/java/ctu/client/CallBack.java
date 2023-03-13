package ctu.client;

public abstract class CallBack implements Runnable {
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
