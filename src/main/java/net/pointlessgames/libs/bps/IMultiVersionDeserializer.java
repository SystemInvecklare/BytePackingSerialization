package net.pointlessgames.libs.bps;

public interface IMultiVersionDeserializer<T> {
    IDeserializer<T> getDeserializer(int version);
}
