package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.pointlessgames.libs.bps.functional.HashMapPairing;
import net.pointlessgames.libs.bps.functional.IPairing;

public class SmallTypeRegistry implements ISerializer<Object> {
	private static class NullClass {
	}
	
	private final Map<Class<?>, MultiVersionSerializer> typeSerializers = new HashMap<>();
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
	
	public <T> Registration<T> register(int id, Class<T> type, ISerializer<T> serializer) {
		assertInRange(id);
		MultiVersionSerializer multiVersionSerializer = new MultiVersionSerializer(serializer);
		typeSerializers.put(type, multiVersionSerializer);
		classIds.add((byte) id, type);
		return new Registration<T>() {
			@Override
			public Registration<T> legacy(IDeserializer<T> legacyDeserializer) {
				multiVersionSerializer.legacyDeserializers.put(legacyDeserializer.getVersion(), legacyDeserializer);
				return this;
			}
		};
	}
	
	private void assertInRange(int id) {
		if(id < 0 || id > 255) {
			throw new IllegalArgumentException("Ids must be in range [0, 255] (was "+id+")");
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void serialize(ISerializationContext context, Object object) throws IOException {
		Class<?> type = object != null ? object.getClass() : NullClass.class;
		ISerializer serializer = getMultiVersionSerializer(type).mainSerializer;
		context.writeByte(classIds.getBySecond(type));
		context.writeShort(serializer.getVersion());
		serializer.serialize(context, object);
	}
	
	@Override
	public Object deserialize(IDeserializationContext context) throws IOException {
		Class<?> type = classIds.getByFirst(context.readByte());
		short serializationVersion = context.readShort();
		return getMultiVersionSerializer(type).getDeserializer(serializationVersion).deserialize(context);
	}

	private MultiVersionSerializer getMultiVersionSerializer(Class<?> type) {
		MultiVersionSerializer multiVersionSerializer = typeSerializers.get(type);
		if(multiVersionSerializer == null) {
			throw new RuntimeException("No serializer registered for type "+type.getName());
		}
		return multiVersionSerializer;
	}
	
	private static class MultiVersionSerializer {
		public final ISerializer<?> mainSerializer;
		public final Map<Short, IDeserializer<?>> legacyDeserializers = new HashMap<>();
		
		public MultiVersionSerializer(ISerializer<?> mainSerializer) {
			this.mainSerializer = mainSerializer;
		}

		public IDeserializer<?> getDeserializer(short serializationVersion) {
			if(mainSerializer.getVersion() == serializationVersion) {
				return mainSerializer;
			} else {
				IDeserializer<?> serializer = legacyDeserializers.get(serializationVersion);
				if(serializer == null) {
					//TODO also include type in message
					throw new NullPointerException("Missing deserializer for version "+serializationVersion);
				}
				return serializer;
			}
		}
	}
	
	public abstract class Registration<T> {
		public abstract Registration<T> legacy(IDeserializer<T> legacyDeserializer);
		@SafeVarargs
		public final Registration<T> legacy(IDeserializer<T> ... legacyDeserializers) {
			for(IDeserializer<T> deserializer : legacyDeserializers) {
				legacy(deserializer);
			}
			return this;
		}
	}
}
