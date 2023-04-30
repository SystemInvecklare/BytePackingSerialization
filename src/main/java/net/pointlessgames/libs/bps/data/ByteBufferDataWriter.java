package net.pointlessgames.libs.bps.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteBufferDataWriter implements IDataWriter {
	private static final byte ZERO = 0;
	private static final byte ONE = 1;

	private final ByteBuffer buffer;

	public ByteBufferDataWriter(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void writeFloat(float value) throws IOException {
		buffer.putFloat(value);
	}

	@Override
	public void writeBoolean(boolean value) throws IOException {
		buffer.put(value ? ONE : ZERO);
	}

	@Override
	public void writeDouble(double value) throws IOException {
		buffer.putDouble(value);
	}

	@Override
	public void writeUTF(String value) throws IOException {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		writeInt(bytes.length);
		writeBytes(bytes);
	}

	@Override
	public void writeShort(short value) throws IOException {
		buffer.putShort(value);
	}

	@Override
	public void writeLong(long value) throws IOException {
		buffer.putLong(value);
	}

	@Override
	public void writeChar(char value) throws IOException {
		buffer.putChar(value);
	}

	@Override
	public void writeByte(byte value) throws IOException {
		buffer.put(value);
	}

	@Override
	public void writeInt(int value) throws IOException {
		buffer.putInt(value);
	}

	@Override
	public void writeBytes(byte[] value) throws IOException {
		buffer.put(value);
	}
}
