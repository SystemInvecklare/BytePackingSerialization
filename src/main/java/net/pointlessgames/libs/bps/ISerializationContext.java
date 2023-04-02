package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ISerializationContext {
	void writeFloat(float f) throws IOException;
	void writeInt(int i) throws IOException;
	void writeBoolean(boolean b) throws IOException;
	void writeDouble(double d) throws IOException;
	void writeString(String S) throws IOException;
	void writeShort(short s) throws IOException;
	void writeLong(long l) throws IOException;
	void writeChar(char c) throws IOException;
	void writeByte(byte b) throws IOException;
	<T> void write(ISerializer<T> serializer, T object) throws IOException;
	void writeObject(Object object) throws IOException;
	
	default void writeObjectList(List<? extends Object> list) throws IOException {
		writeInt(list.size());
		for(Object object : list) {
			writeObject(object);
		}
	}
	
	default <T> void writeList(ISerializer<T> elementSerializer, List<T> list) throws IOException {
		writeInt(list.size());
		for(T element : list) {
			write(elementSerializer, element);
		}
	}
	
	default <T> void writeOptionalObject(Optional<T> optional) throws IOException {
		writeBoolean(optional.isPresent());
		if(optional.isPresent()) {
			writeObject(optional.get());
		}
	}
	
	default <T> void writeOptional(ISerializer<T> serializer, Optional<T> optional) throws IOException {
		writeBoolean(optional.isPresent());
		if(optional.isPresent()) {
			write(serializer, optional.get());
		}
	}
}
