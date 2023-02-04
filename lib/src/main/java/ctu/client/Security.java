package ctu.client;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ctu.core.abstracts.Connection;
import ctu.core.abstracts.Packet;
import ctu.core.interfaces.Crypt;
import ctu.core.interfaces.Listener;
import ctu.core.packets.PacketClientSecretKey;
import ctu.core.packets.PacketServerPublicKey;

public class Security implements Listener {
	public enum KeyAlgorithms {
		AES
	}

	public enum KeyLength {
		L128, L192, L256
	}

	public enum KeyPairAlgorithms {
		DIFFIE_HELLMAN, DSA, RSA, EC
	}

	public enum KeyPairLenght {
		L1024, L2048, L4096
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		final char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			final int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	private String serverAlgorithm;
	@SuppressWarnings("unused")
	private int serverKeySize; // Server key size is not used on the client.
	private String clientAlgorithm;
	private int padding;

	private int clientKeySize;

	public Security(KeyPairAlgorithms serverAlgorithm, KeyPairLenght serverKeySize, KeyAlgorithms clientAlgorithm, KeyLength clientKeySize) {
		switch (serverAlgorithm) {
		case DIFFIE_HELLMAN:
			this.serverAlgorithm = "DiffieHellman";
			break;
		case DSA:
			this.serverAlgorithm = "DSA";
			break;
		case EC:
			this.serverAlgorithm = "EC";
			break;
		case RSA:
			this.serverAlgorithm = "RSA";
			break;
		}

		switch (serverKeySize) {
		case L1024:
			this.serverKeySize = 1024;
			break;
		case L2048:
			this.serverKeySize = 2048;
			break;
		case L4096:
			this.serverKeySize = 4096;
			break;
		}

		switch (clientAlgorithm) {
		case AES:
			this.clientAlgorithm = "AES";
			this.padding = 16;
			break;
		}

		switch (clientKeySize) {
		case L128:
			this.clientKeySize = 128;
			break;
		case L192:
			this.clientKeySize = 192;
			break;
		case L256:
			this.clientKeySize = 256;
			break;
		}
	}

	public byte[] encryptSecretKey(byte[] b, PublicKey publicKey) {
		byte[] bytes = null;

		try {
			final Cipher cipher = Cipher.getInstance(serverAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			bytes = cipher.doFinal(b);
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (final NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (final InvalidKeyException e) {
			e.printStackTrace();
		} catch (final IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (final BadPaddingException e) {
			e.printStackTrace();
		}

		return bytes;
	}

	@Override
	public void postConnect(Connection connection) {
		System.out.println("Security module enabled");

		KeyGenerator keyGenerator = null;

		try {
			keyGenerator = KeyGenerator.getInstance(this.clientAlgorithm);
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		keyGenerator.init(this.clientKeySize);
		final SecretKey secretKey = keyGenerator.generateKey();

		final byte[] bytes = connection.recvTCP();

		System.out.println("[RECEIVING] ServerPublicKey [" + bytes.length + "] from server.");

		final Packet packet = connection.bytesToPacket(bytes);

		if (packet instanceof PacketServerPublicKey) {
			final PacketServerPublicKey serverPublicKey = (PacketServerPublicKey) packet;
			final PublicKey publicKey = X509(serverPublicKey.getServerPublicKey());

			final PacketClientSecretKey clientSecretKey = new PacketClientSecretKey();
			System.out.println("Encrypting Secret via public key.");
			clientSecretKey.clientSecretKey = encryptSecretKey(secretKey.getEncoded(), publicKey);
			connection.sendTCP(clientSecretKey);

			connection.setCrypt(new Crypt() {
				@Override
				public byte[] decrypt(byte[] bytes) {
					return secDecrypt(bytes, secretKey);
				}

				@Override
				public byte[] encrypt(byte[] bytes) {
					return secEncrypt(bytes, secretKey);
				}
			});
			connection.setPadding(padding);

			System.out.println("Secret: " + bytesToHex(secretKey.getEncoded()));
			System.out.println("Security Functioning");
		}
	}

	public byte[] secDecrypt(byte[] b, SecretKey secretKey) {
		byte[] bytes = null;

		try {
			final IvParameterSpec iv = new IvParameterSpec("RandomInitVector".getBytes("UTF-8"));
			final SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			b = Arrays.copyOf(b, 16 * (Math.round(b.length / 16)));

			bytes = cipher.doFinal(b);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return bytes;
	}

	public byte[] secEncrypt(byte[] b, SecretKey secretKey) {
		byte[] bytes = null;

		try {
			final IvParameterSpec iv = new IvParameterSpec("RandomInitVector".getBytes("UTF-8"));
			final SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			bytes = cipher.doFinal(b);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return bytes;
	}

	public PublicKey X509(byte[] b) {
		PublicKey publicKey = null;
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(serverAlgorithm);
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(b));
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (final InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return publicKey;
	}

	@Override
	public void connected(Connection connection) {
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