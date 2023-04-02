package net.pointlessgames.libs.bps;

import java.io.IOException;

public interface ISerializer<T> extends IDeserializer<T> {
	void serialize(ISerializationContext context, T object) throws IOException;
}
