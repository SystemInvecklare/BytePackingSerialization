package net.pointlessgames.libs.bps.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamDataWriter implements IDataWriter {
	private final DataOutputStream outputStream;
	
	public OutputStreamDataWriter(OutputStream outputStream) {
		this.outputStream = outputStream instanceof DataOutputStream ? (DataOutputStream) outputStream : new DataOutputStream(outputStream);
	}

	@Override
	public void writeFloat(float value) throws IOException {
		outputStream.writeFloat(value);
	}

	@Override
	public void writeBoolean(boolean value) throws IOException {
		outputStream.writeBoolean(value);
	}

	@Override
	public void writeDouble(double value) throws IOException {
		outputStream.writeDouble(value);
	}

	@Override
	public void writeUTF(String value) throws IOException {
		outputStream.writeUTF(value);
	}

	@Override
	public void writeShort(short value) throws IOException {
		outputStream.writeShort(value);
	}

	@Override
	public void writeLong(long value) throws IOException {
		outputStream.writeLong(value);
	}

	@Override
	public void writeChar(char value) throws IOException {
		outputStream.writeChar(value);
	}

	@Override
	public void writeByte(byte value) throws IOException {
		outputStream.writeByte(value);
	}

	@Override
	public void writeInt(int value) throws IOException {
		outputStream.writeInt(value);
	}

	@Override
	public void writeBytes(byte[] value) throws IOException {
		outputStream.write(value);
	}
}
