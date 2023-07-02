package net.pointlessgames.libs.bps;

import java.io.IOException;

public class SimplePointer<T> {
	public static final ISerializer<SimplePointer<?>> SERIALIZER = new ISerializer<SimplePointer<?>>() {
		@Override
		public SimplePointer<?> deserialize(IDeserializationContext context) throws IOException {
			SimplePointer<Object> pointer = new SimplePointer<>();
			context.readObject(Object.class, pointer::init);
			return pointer;
		}
		
		@Override
		public void serialize(ISerializationContext context, SimplePointer<?> object) throws IOException {
			context.writeObject(object.value);
		}
	};
	
	public static <T> ISerializer<SimplePointer<T>> typedSerializer(Class<T> objectType) {
		return new ISerializer<SimplePointer<T>>() {
			@Override
			public SimplePointer<T> deserialize(IDeserializationContext context) throws IOException {
				SimplePointer<T> pointer = new SimplePointer<>();
				context.readObject(objectType, pointer::init);
				return pointer;
			}

			@Override
			public void serialize(ISerializationContext context, SimplePointer<T> object) throws IOException {
				context.writeObject(object.value);
			}
		};
	}
	
	private T value;
	private boolean initialized = false;
	
	private SimplePointer() {
	}

	public SimplePointer(IObjectReference<T> objectReference) {
		objectReference.get(this::init);
	}
	
	public SimplePointer(T value) {
		init(value);
	}
	
	private void init(T value) {
		this.value = value;
		initialized = true;
	}

	public T get() {
		if(!initialized) {
			throw new IllegalStateException("Uninitialized pointer access! Was pointer accessed during deserialization pass?");
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public <U> SimplePointer<U> cast(Class<U> type) {
		return (SimplePointer<U>) this;
	}
}
