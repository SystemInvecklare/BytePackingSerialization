package net.pointlessgames.libs.bps.extracontext;

import java.io.IOException;

import net.pointlessgames.libs.bps.IDeserializationContext;

@FunctionalInterface
public interface IDependentDeserializationFunction<T, C> {
    T deserialize(IDeserializationContext context, C extraContext) throws IOException;
}
