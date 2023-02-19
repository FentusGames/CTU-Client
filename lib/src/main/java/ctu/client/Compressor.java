package ctu.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import ctu.core.abstracts.Connection;
import ctu.core.abstracts.Packet;
import ctu.core.interfaces.Compression;
import ctu.core.interfaces.Listener;

public class Compressor implements Listener {
	private Compression zip = new Compression() {
		@Override
		public byte[] compress(byte[] bytes) throws IOException {
			return compressByteArray(bytes);
		}

		@Override
		public byte[] decompress(byte[] bytes) throws IOException {
			return decompressByteArray(bytes);
		}
	};

	private Compression gzip = new Compression() {
		@Override
		public byte[] compress(byte[] bytes) throws IOException {
			return gzipCompress(bytes);
		}

		@Override
		public byte[] decompress(byte[] bytes) throws IOException {
			return gzipDecompress(bytes);
		}
	};

	private Compression none = new Compression() {
		@Override
		public byte[] compress(byte[] bytes) throws IOException {
			return bytes;
		}

		@Override
		public byte[] decompress(byte[] bytes) throws IOException {
			return bytes;
		}
	};

	private Algorithms algorithm;
	private int level = Deflater.DEFAULT_COMPRESSION;
	private int packetSize;

	public enum Algorithms {
		ZIP, GZIP, NONE
	}

	public Compressor(Algorithms algorithm, int level) {
		this.algorithm = algorithm;
		this.level = level;
	}

	public Compressor(Algorithms algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public void postConnect(Connection connection) {
		switch (algorithm) {
		case GZIP:
			connection.setCompression(gzip);
			break;
		case ZIP:
			connection.setCompression(zip);
			break;

		case NONE:
		default:
			connection.setCompression(none);
			break;
		}

		packetSize = connection.getCtu().getConfig().PACKET_SIZE;
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

	public byte[] compressByteArray(byte[] bytes) {
		ByteArrayOutputStream baos = null;
		Deflater dfl = new Deflater();

		dfl.setLevel(level);
		dfl.setInput(bytes);
		dfl.finish();

		baos = new ByteArrayOutputStream();

		byte[] tmp = new byte[packetSize];

		try {
			while (!dfl.finished()) {
				int size = dfl.deflate(tmp);
				baos.write(tmp, 0, size);
			}
		} catch (Exception ex) {

		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (Exception ex) {
			}
		}

		return baos.toByteArray();
	}

	public byte[] decompressByteArray(byte[] bytes) {
		ByteArrayOutputStream baos = null;
		Inflater iflr = new Inflater();

		iflr.setInput(bytes);

		baos = new ByteArrayOutputStream();

		byte[] tmp = new byte[packetSize];

		try {
			while (!iflr.finished()) {
				int size = iflr.inflate(tmp);
				baos.write(tmp, 0, size);
			}
		} catch (Exception ex) {

		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (Exception ex) {
			}
		}

		return baos.toByteArray();
	}

	private static byte[] gzipCompress(byte[] bytes) {
		byte[] result = new byte[] {};
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length); GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
			gzipOS.write(bytes);
			gzipOS.close();
			result = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static byte[] gzipDecompress(byte[] bytes) {
		byte[] result = new byte[] {};
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ByteArrayOutputStream bos = new ByteArrayOutputStream(); GZIPInputStream gzipIS = new GZIPInputStream(bis)) {
			byte[] buffer = new byte[8 * 1024];
			int len;
			while ((len = gzipIS.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			result = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
