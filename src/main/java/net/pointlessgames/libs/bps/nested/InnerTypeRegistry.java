package net.pointlessgames.libs.bps.nested;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.pointlessgames.libs.bps.IDeserializationContext;
import net.pointlessgames.libs.bps.ISerializationContext;
import net.pointlessgames.libs.bps.extracontext.IDependentSerializer;
import net.pointlessgames.libs.bps.functional.HashMapPairing;
import net.pointlessgames.libs.bps.functional.IPairing;

//TODO maybe I need a monad for the different type registries?
public class InnerTypeRegistry<Inner, Outer> implements IDependentSerializer<Inner, Outer> {
	private final int version;
	private final Map<Class<? extends Inner>, IDependentSerializer<? extends Inner, ? super Outer>> typeSerializers = new HashMap<>();
	private final IPairing<Byte, Class<? extends Inner>> classIds = new HashMapPairing<>();
	
	public InnerTypeRegistry() {
		this(0);
	}
	
	public InnerTypeRegistry(int version) {
		this.version = version;
	}

	public void registerNull(int id) {
		assertInRange(id);
		typeSerializers.put(null, new IDependentSerializer<Inner, Object>() {
			@Override
			public Inner deserialize(IDeserializationContext context, Object outerObject) throws IOException {
				return null;
			}

			@Override
			public void serialize(ISerializationContext context, Inner innerObject) throws IOException {
			}
		});
		classIds.add((byte) id, null);
	}

	public <T extends Inner> void register(int id, Class<T> type, IDependentSerializer<T, ? super Outer> serializer) {
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
	public void serialize(ISerializationContext context, Inner innerObject) throws IOException {
		Class<? extends Inner> type = (Class<? extends Inner>) (innerObject != null ? innerObject.getClass() : null);
		IDependentSerializer serializer = getSerializer(type);
		context.writeByte(getId(type));
		context.writeDependent(serializer, innerObject);
	}

	@Override
	public Inner deserialize(IDeserializationContext context, Outer outerObject) throws IOException {
		Class<? extends Inner> type = getType(context.readByte());
		IDependentSerializer<? extends Inner, ? super Outer> serializer = getSerializer(type);
		return context.readDependent(serializer, outerObject);
	}

	@SuppressWarnings("unchecked")
	private <T extends Inner> IDependentSerializer<T, ? super Outer> getSerializer(Class<T> type) {
		IDependentSerializer<T, ? super Outer> serializer = (IDependentSerializer<T, ? super Outer>) typeSerializers.get(type);
		if(serializer == null) {
			throw new IllegalArgumentException("No serializer registered for type "+(type == null ? "nullType" : type.getName()));
		}
		return serializer;
	}

	private byte getId(Class<? extends Inner> type) {
		Byte id = classIds.getBySecond(type);
		if(id == null) {
			throw new IllegalArgumentException("Type "+type.getName()+" not registered");
		}
		return id;
	}

	private Class<? extends Inner> getType(byte id) {
		Class<? extends Inner> type = classIds.getByFirst(id);
		if(type == null) {
			Byte nullId = classIds.getBySecond(null);
			if(nullId != null && nullId == id) {
				return null;
			}
			throw new IllegalArgumentException("No class registered for id "+id);
		}
		return type;
	}
	
	@Override
	public int getVersion() {
		return version;
	}
	
	public static <Inner extends IInnerType, Outer extends IOuterType> IDependentSerializer<Inner, Outer> configure(Consumer<InnerTypeRegistry<Inner, Outer>> configuration) {
		return configure(0, configuration);
	}
	
	public static <Inner extends IInnerType, Outer extends IOuterType> IDependentSerializer<Inner, Outer> configure(int version, Consumer<InnerTypeRegistry<Inner, Outer>> configuration) {
		InnerTypeRegistry<Inner, Outer> registry = new InnerTypeRegistry<>(version);
		configuration.accept(registry);
		return registry;
	}
	
}
