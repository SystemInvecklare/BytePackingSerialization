package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.pointlessgames.libs.bps.extracontext.IDependentDeserializer;

public interface IDeserializationContext {
	float readFloat() throws IOException;
	int readInt() throws IOException;
	boolean readBoolean() throws IOException;
	double readDouble() throws IOException;
	String readString() throws IOException;
	short readShort() throws IOException;
	long readLong() throws IOException;
	char readChar() throws IOException;
	byte readByte() throws IOException;
	<T> T read(IDeserializer<T> deserializer) throws IOException;
	<T> void readObject(Class<T> type, Consumer<T> consumer) throws IOException;
	<T, C> T readDependent(IDependentDeserializer<T, C> deserializer, C extraContext) throws IOException;
	
	default <T, L extends List<? super T>> L readObjectList(Class<T> elementType, L list) throws IOException {
		int listSize = readInt();
		for(int i = 0; i < listSize; ++i) {
			final int index = list.size();
			list.add(null);
			readObject(elementType, (elem) -> list.set(index, elem));
		}
		return list;
	}
	
	default <T, L extends List<? super T>> L readList(IDeserializer<T> elementDeserializer, L list) throws IOException {
		int listSize = readInt();
		for(int i = 0; i < listSize; ++i) {
			list.add(read(elementDeserializer));
		}
		return list;
	}
	
	default <T> void readOptionalObject(Class<T> type, Consumer<Optional<T>> consumer) throws IOException {
		boolean isPresent = readBoolean();
		if(isPresent) {
			readObject(type, obj -> consumer.accept(Optional.of(obj)));
		} else {
			consumer.accept(Optional.empty());
		}
	}
	
	default <T> Optional<T> readOptional(IDeserializer<T> deserializer) throws IOException {
		boolean isPresent = readBoolean();
		if(isPresent) {
			return Optional.of(read(deserializer));
		} else {
			return Optional.empty();
		}
	}
	
	default <T> IObjectReference<T> readObject(Class<T> type) throws IOException {
		ObjectReference<T> reference = new ObjectReference<>();
		readObject(type, reference::setValue);
		return reference;
	}
	
	default float[] readFloatArray() throws IOException {
		float[] array = new float[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readFloat();
		}
		return array;
	}
	
	default int[] readIntArray() throws IOException {
		int[] array = new int[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readInt();
		}
		return array;
	}

	default boolean[] readBooleanArray() throws IOException {
		boolean[] array = new boolean[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readBoolean();
		}
		return array;
	}

	default double[] readDoubleArray() throws IOException {
		double[] array = new double[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readDouble();
		}
		return array;
	}

	default String[] readStringArray() throws IOException {
		String[] array = new String[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readString();
		}
		return array;
	}

	default short[] readShortArray() throws IOException {
		short[] array = new short[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readShort();
		}
		return array;
	}

	default long[] readLongArray() throws IOException {
		long[] array = new long[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readLong();
		}
		return array;
	}

	default char[] readCharArray() throws IOException {
		char[] array = new char[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readChar();
		}
		return array;
	}

	default byte[] readByteArray() throws IOException {
		byte[] array = new byte[readInt()];
		for(int i = 0; i < array.length; ++i) {
			array[i] = readByte();
		}
		return array;
	}

	default <T> T[] readArray(IDeserializer<T> elementDeserializer, Class<T> componentType) throws IOException {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(componentType, readInt());
		for(int i = 0; i < array.length; ++i) {
			array[i] = read(elementDeserializer);
		}
		return array;
	}

	default <T> T[] readObjectArray(Class<T> componentType) throws IOException {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(componentType, readInt());
		for(int i = 0; i < array.length; ++i) {
			final int finalI = i;
			readObject(componentType, v -> array[finalI] = v);
		}
		return array;
	}
}
