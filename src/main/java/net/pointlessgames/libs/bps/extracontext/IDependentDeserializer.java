package net.pointlessgames.libs.bps.extracontext;

import java.io.IOException;

import net.pointlessgames.libs.bps.IDeserializationContext;

public interface IDependentDeserializer<T, C> {
	T deserialize(IDeserializationContext context, C extraContext) throws IOException;
	default int getVersion() {
		return 0;
	}
}
