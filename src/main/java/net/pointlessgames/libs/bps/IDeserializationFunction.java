package net.pointlessgames.libs.bps;

import java.io.IOException;

@FunctionalInterface
public interface IDeserializationFunction<T> {
    T deserialize(IDeserializationContext context) throws IOException;
}
