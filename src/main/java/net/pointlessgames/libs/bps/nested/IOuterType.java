package net.pointlessgames.libs.bps.nested;

import java.io.IOException;

import net.pointlessgames.libs.bps.IDeserializationContext;
import net.pointlessgames.libs.bps.ISerializationContext;

public interface IOuterType {
	void serializeInner(ISerializationContext context, IInnerType inner) throws IOException;
	IInnerType deserializeInner(IDeserializationContext context) throws IOException;
}
