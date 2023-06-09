package net.pointlessgames.libs.bps.data;

import java.io.IOException;

public interface IDataWriter {
	void writeFloat(float value) throws IOException;
	void writeBoolean(boolean value) throws IOException;
	void writeDouble(double value) throws IOException;
	void writeUTF(String value) throws IOException;
	void writeShort(short value) throws IOException;
	void writeLong(long value) throws IOException;
	void writeChar(char value) throws IOException;
	void writeByte(byte value) throws IOException;
	void writeInt(int value) throws IOException;
	void writeBytes(byte[] value) throws IOException;
}
