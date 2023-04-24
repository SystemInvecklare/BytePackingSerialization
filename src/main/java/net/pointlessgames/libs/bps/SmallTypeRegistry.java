package net.pointlessgames.libs.bps;

import net.pointlessgames.libs.bps.functional.HashMapPairing;
import net.pointlessgames.libs.bps.functional.IPairing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SmallTypeRegistry implements ISerializer<Object> {
	private static class NullClass {
	}

	private final Map<Class<?>, ISerializer<?>> typeSerializers = new HashMap<>();
	private final IPairing<Byte, Class<?>> classIds = new HashMapPairing<>();

	public void registerNull(int id) {
		assertInRange(id);
		register(id, NullClass.class, new ISerializer<NullClass>() {
			@Override
			public NullClass deserialize(IDeserializationContext context) throws IOException {
				return null;
			}

			@Override
			public void serialize(ISerializationContext context, NullClass object) throws IOException {
			}
		});
	}

	public <T> void register(int id, Class<T> type, ISerializer<T> serializer) {
		assertInRange(id);
		typeSerializers.put(type, serializer);
		classIds.add((byte) id, type);
	}

	private void assertInRange(int id) {
		if (id < 0 || id > 255) {
			throw new IllegalArgumentException("Ids must be in range [0, 255] (was " + id + ")");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void serialize(ISerializationContext context, Object object) throws IOException {
		Class<?> type = object != null ? object.getClass() : NullClass.class;
		ISerializer serializer = getSerializer(type);
		context.writeByte(getId(type));
		context.write(serializer, object);
	}

	@Override
	public Object deserialize(IDeserializationContext context) throws IOException {
		Class<?> type = getType(context.readByte());
		ISerializer<?> serializer = getSerializer(type);
		return context.read(serializer);
	}

	@SuppressWarnings("unchecked")
	private <T> ISerializer<T> getSerializer(Class<T> type) {
		ISerializer<T> serializer = (ISerializer<T>) typeSerializers.get(type);
		if(serializer == null) {
			throw new IllegalArgumentException("No serializer registered for type "+type.getName());
		}
		return serializer;
	}

	private byte getId(Class<?> type) {
		Byte id = classIds.getBySecond(type);
		if(id == null) {
			throw new IllegalArgumentException("Type "+type.getName()+" not registered");
		}
		return id;
	}

	private Class<?> getType(byte id) {
		Class<?> type = classIds.getByFirst(id);
		if(type == null) {
			throw new IllegalArgumentException("No class registered for id "+id);
		}
		return type;
	}
}
