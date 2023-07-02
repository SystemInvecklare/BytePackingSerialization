package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.pointlessgames.libs.bps.functional.HashMapPairing;
import net.pointlessgames.libs.bps.functional.IPairing;

public class TypeRegistry implements ISerializer<Object> {
	private static class NullClass {
	}
	
	private final Map<Class<?>, ISerializer<?>> typeSerializers = new HashMap<>();
	private final IPairing<Integer, Class<?>> classIds = new HashMapPairing<>();
	
	public void registerNull(int id) {
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
		typeSerializers.put(type, serializer);
		classIds.add(id, type);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void serialize(ISerializationContext context, Object object) throws IOException {
		Class<?> type = object != null ? object.getClass() : NullClass.class;
		ISerializer serializer = getSerializer(type);
		context.writeInt(getId(type));
		context.write(serializer, object);
	}
	
	@Override
	public Object deserialize(IDeserializationContext context) throws IOException {
		Class<?> type = getType(context.readInt());
		ISerializer<?> serializer = getSerializer(type);
		Object value = context.read(serializer);
		if(value == null && type != NullClass.class) {
			// Typically, object-deserializers are not permitted to deserialize to null.
			// If the original object serialized was actually null, then we would not 
			// have found the correct serializer in the first place (since we would not
			// have known the type), thus, null from an object-deserializer always
			// indicates an error.
			throw new IOException(serializer.toString() + " for type "+type.getName()+" deserialized to null!");
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private <T> ISerializer<T> getSerializer(Class<T> type) {
		ISerializer<T> serializer = (ISerializer<T>) typeSerializers.get(type);
		if(serializer == null) {
			throw new IllegalArgumentException("No serializer registered for type "+type.getName());
		}
		return serializer;
	}
	
	private int getId(Class<?> type) {
		Integer id = classIds.getBySecond(type);
		if(id == null) {
			throw new IllegalArgumentException("Type "+type.getName()+" not registered");
		}
		return id;
	}

	private Class<?> getType(int id) {
		Class<?> type = classIds.getByFirst(id);
		if(type == null) {
			throw new IllegalArgumentException("No class registered for id "+id);
		}
		return type;
	}
}
