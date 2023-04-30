package net.pointlessgames.libs.bps.data;

import java.io.IOException;

public interface IDataReader {
	float readFloat() throws IOException;
	boolean readBoolean() throws IOException;
	double readDouble() throws IOException;
	String readUTF() throws IOException;
	short readShort() throws IOException;
	long readLong() throws IOException;
	char readChar() throws IOException;
	byte readByte() throws IOException;
	int readInt() throws IOException;
	void readBytes(byte[] result, int offset, int length) throws IOException;
}
