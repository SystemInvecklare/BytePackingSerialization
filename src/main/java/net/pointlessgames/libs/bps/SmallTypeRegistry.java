package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.pointlessgames.libs.bps.functional.HashMapPairing;
import net.pointlessgames.libs.bps.functional.IPairing;

public class SmallTypeRegistry<S> implements ISerializer<S> {
	private final Map<Class<? extends S>, ISerializer<? extends S>> typeSerializers = new HashMap<>();
	private final IPairing<Byte, Class<? extends S>> classIds = new HashMapPairing<>();

	public void registerNull(int id) {
		assertInRange(id);
		typeSerializers.put(null, new ISerializer<S>() {
			@Override
			public S deserialize(IDeserializationContext context) throws IOException {
				return null;
			}

			@Override
			public void serialize(ISerializationContext context, S object) throws IOException {
			}
		});
		classIds.add((byte) id, null);
	}

	public <T extends S> void register(int id, Class<T> type, ISerializer<T> serializer) {
		assertInRange(id);
		if(type == null) {
			throw new NullPointerException("type is null");
		}
		if(serializer == null) {
			throw new NullPointerException("serializer is null");
		}
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
	public void serialize(ISerializationContext context, S object) throws IOException {
		Class<? extends S> type = (Class<? extends S>) (object != null ? object.getClass() : null);
		ISerializer serializer = getSerializer(type);
		context.writeByte(getId(type));
		context.write(serializer, object);
	}

	@Override
	public S deserialize(IDeserializationContext context) throws IOException {
		Class<? extends S> type = getType(context.readByte());
		ISerializer<? extends S> serializer = getSerializer(type);
		return context.read(serializer);
	}

	@SuppressWarnings("unchecked")
	private <T extends S> ISerializer<T> getSerializer(Class<T> type) {
		ISerializer<T> serializer = (ISerializer<T>) typeSerializers.get(type);
		if(serializer == null) {
			throw new IllegalArgumentException("No serializer registered for type "+(type == null ? "nullType" : type.getName()));
		}
		return serializer;
	}

	private byte getId(Class<? extends S> type) {
		Byte id = classIds.getBySecond(type);
		if(id == null) {
			throw new IllegalArgumentException("Type "+type.getName()+" not registered");
		}
		return id;
	}

	private Class<? extends S> getType(byte id) {
		Class<? extends S> type = classIds.getByFirst(id);
		if(type == null) {
			Byte nullId = classIds.getBySecond(null);
			if(nullId != null && nullId == id) {
				return null;
			}
			throw new IllegalArgumentException("No class registered for id "+id);
		}
		return type;
	}
	
	public static <T> ISerializer<T> configure(Consumer<SmallTypeRegistry<T>> configuration) {
		SmallTypeRegistry<T> registry = new SmallTypeRegistry<>();
		configuration.accept(registry);
		return registry;
	}
}
