# CTU Client

CTU-Client is the Java client application that allows for simple communication to CTU Server. This application comes with advanced encryption methods, compression, and other features.

```java
public static void main(String[] args) throws InterruptedException {
	Config config = new Config();

	config.IP_ADDRESS = "10.89.0.6";
	config.PORT = 9999;
	config.PACKET_SIZE = 1400;
	config.TIMEOUT = 10000;
	config.DEFAULT_ITERATIONS = 150000;
	config.ALGORITHM = "pbkdf2_sha256";

	CTUClient client = new CTUClient();
	
	// Register packets.
	// E.G. client.register(PACKETNAME.class);

	client.addListener(new Listener() {
		@Override
		public void timeout(Connection connection) {}

		@Override
		public void reset(Connection connection) {}

		@Override
		public void recieved(Connection connection, Packet packet) {}

		@Override
		public void postConnect(Connection connection) {}

		@Override
		public void disconnected(Connection connection) {}

		@Override
		public void connected(Connection connection) {}
	});

	client.setConfig(config);

	client.addListener(new Messages());
	client.addListener(new Security(KeyPairAlgorithms.RSA, KeyPairLenght.L2048, KeyAlgorithms.AES, KeyLength.L128));
	client.addListener(new HeartBeat());
	client.addListener(new Compressor(Algorithms.GZIP));

	// Some sort of loop to keep the client open.
}
```
