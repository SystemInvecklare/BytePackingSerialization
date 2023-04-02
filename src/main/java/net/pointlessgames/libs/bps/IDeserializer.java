package net.pointlessgames.libs.bps;

import java.io.IOException;

public interface IDeserializer<T> {
	T deserialize(IDeserializationContext context) throws IOException;
	
	default short getVersion() {
		return 0;
	}
}
