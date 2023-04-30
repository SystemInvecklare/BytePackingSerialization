package net.pointlessgames.libs.bps.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteBufferDataReader implements IDataReader {
	private final ByteBuffer buffer;
	
	public ByteBufferDataReader(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public float readFloat() throws IOException {
		return buffer.getFloat();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return buffer.get() == 1;
	}

	@Override
	public double readDouble() throws IOException {
		return buffer.getDouble();
	}

	@Override
	public String readUTF() throws IOException {
		int byteLength = readInt();
		byte[] bytes = new byte[byteLength];
		readBytes(bytes, 0, byteLength);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public short readShort() throws IOException {
		return buffer.getShort();
	}

	@Override
	public long readLong() throws IOException {
		return buffer.getLong();
	}

	@Override
	public char readChar() throws IOException {
		return buffer.getChar();
	}

	@Override
	public byte readByte() throws IOException {
		return buffer.get();
	}

	@Override
	public int readInt() throws IOException {
		return buffer.getInt();
	}

	@Override
	public void readBytes(byte[] result, int offset, int length) throws IOException {
		buffer.get(result, offset, length);
	}
}
