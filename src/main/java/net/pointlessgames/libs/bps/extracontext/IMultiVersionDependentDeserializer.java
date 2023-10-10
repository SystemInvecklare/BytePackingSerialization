package net.pointlessgames.libs.bps.extracontext;

public interface IMultiVersionDependentDeserializer<T, C> {
    IDependentDeserializer<T, C> getDependentDeserializer(int version);
}
