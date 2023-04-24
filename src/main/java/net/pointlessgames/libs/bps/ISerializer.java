package net.pointlessgames.libs.bps;

import java.io.IOException;

public interface ISerializer<T> extends IDeserializer<T> {
	void serialize(ISerializationContext context, T object) throws IOException;

	default ISerializer<T> with(int deserializerVersion, IDeserializationFunction<T> deserialization) {
		return with(Helpers.deserializer(deserializerVersion, deserialization));
	}
	default ISerializer<T> with(IDeserializer<T> deserializer) {
		return Helpers.multiVersionDeserializer(this, deserializer);
	}
}
