package net.pointlessgames.libs.bps.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamDataReader implements IDataReader {
	private final DataInputStream inputStream;

	public InputStreamDataReader(InputStream inputStream) {
		this.inputStream = inputStream instanceof DataInputStream ? (DataInputStream) inputStream : new DataInputStream(inputStream);
	}

	@Override
	public float readFloat() throws IOException {
		return inputStream.readFloat();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return inputStream.readBoolean();
	}

	@Override
	public double readDouble() throws IOException {
		return inputStream.readDouble();
	}

	@Override
	public String readUTF() throws IOException {
		return inputStream.readUTF();
	}

	@Override
	public short readShort() throws IOException {
		return inputStream.readShort();
	}

	@Override
	public long readLong() throws IOException {
		return inputStream.readLong();
	}

	@Override
	public char readChar() throws IOException {
		return inputStream.readChar();
	}

	@Override
	public byte readByte() throws IOException {
		return inputStream.readByte();
	}

	@Override
	public int readInt() throws IOException {
		return inputStream.readInt();
	}

	@Override
	public void readBytes(byte[] result, int offset, int length) throws IOException {
		int remaining = length;
		while(remaining > 0) {
			int read = inputStream.read(result, offset + length - remaining, remaining);
			if(read == -1) {
				throw new IOException("Unexpected end of stream");
			}
			remaining -= read;
		}
	}
}
