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
	
	default void writeFloatArray(float[] array) throws IOException {
		writeInt(array.length);
		for(float val : array) {
			writeFloat(val);
		}
	}
	
	default void writeIntArray(int[] array) throws IOException {
		writeInt(array.length);
		for(int val : array) {
			writeInt(val);
		}
	}
	
	default void writeBooleanArray(boolean[] array) throws IOException {
		writeInt(array.length);
		for(boolean val : array) {
			writeBoolean(val);
		}
	}
	
	default void writeDoubleArray(double[] array) throws IOException {
		writeInt(array.length);
		for(double val : array) {
			writeDouble(val);
		}
	}
	
	default void writeStringArray(String[] array) throws IOException {
		writeInt(array.length);
		for(String val : array) {
			writeString(val);
		}
	}
	
	default void writeShortArray(short[] array) throws IOException {
		writeInt(array.length);
		for(short val : array) {
			writeShort(val);
		}
	}
	
	default void writeLongArray(long[] array) throws IOException {
		writeInt(array.length);
		for(long val : array) {
			writeLong(val);
		}
	}
	
	default void writeCharArray(char[] array) throws IOException {
		writeInt(array.length);
		for(char val : array) {
			writeChar(val);
		}
	}
	
	default void writeByteArray(byte[] array) throws IOException {
		writeInt(array.length);
		for(byte val : array) {
			writeByte(val);
		}
	}
	
	default <T> void writeArray(ISerializer<T> elementSerializer, T[] array) throws IOException {
		writeInt(array.length);
		for(T val : array) {
			write(elementSerializer, val);
		}
	}
	
	default <T> void writeObjectArray(T[] array) throws IOException {
		writeInt(array.length);
		for(T val : array) {
			writeObject(val);
		}
	}
}
