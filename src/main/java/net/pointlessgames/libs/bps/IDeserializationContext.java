package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
}
