package ctu.client;

public abstract class CallBack implements Runnable {
	private boolean success;

	@Override
	public void run() {
		if (success) {
			pass();
		} else {
			fail();
		}
	}

	public abstract void fail();

	public abstract void pass();

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
