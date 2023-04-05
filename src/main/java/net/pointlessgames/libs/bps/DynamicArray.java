package net.pointlessgames.libs.bps;

import net.pointlessgames.libs.bps.functional.UnsafeBiConsumer;
import net.pointlessgames.libs.bps.functional.UnsafeFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DynamicArray {
	public static final ISerializer<DynamicArray> SERIALIZER = new ISerializer<DynamicArray>() {
		@Override
		public DynamicArray deserialize(IDeserializationContext context) throws IOException {
			DynamicArray dynamicArray = new DynamicArray();
			context.readList(HOLDER_SERIALIZER, dynamicArray.values);
			return dynamicArray;
		}
		
		@Override
		public void serialize(ISerializationContext context, DynamicArray object) throws IOException {
			context.writeList(HOLDER_SERIALIZER, object.values);
		}
	};
	private final List<IHolder> values = new ArrayList<IHolder>();
	
	public DynamicArray addInt(int value) {
		values.add(new IntHolder(value));
		return this;
	}
	
	public DynamicArray addBoolean(boolean value) {
		values.add(new BooleanHolder(value));
		return this;
	}
	
	public DynamicArray addFloat(float value) {
		values.add(new FloatHolder(value));
		return this;
	}

	public DynamicArray addLong(long value) {
		values.add(new LongHolder(value));
		return this;
	}

	public DynamicArray addDouble(double value) {
		values.add(new DoubleHolder(value));
		return this;
	}

	public DynamicArray addShort(short value) {
		values.add(new ShortHolder(value));
		return this;
	}

	public DynamicArray addChar(char value) {
		values.add(new CharHolder(value));
		return this;
	}

	public DynamicArray addString(String value) {
		values.add(new StringHolder(value));
		return this;
	}

	public DynamicArray addObject(Object value) {
		values.add(new ObjectHolder(value));
		return this;
	}
	
	public int getInt(int index) {
		return values.get(index).getInt();
	}
	
	public boolean getBoolean(int index) {
		return values.get(index).getBoolean();
	}

	public float getFloat(int index) {
		return values.get(index).getFloat();
	}

	public long getLong(int index) {
		return values.get(index).getLong();
	}

	public double getDouble(int index) {
		return values.get(index).getDouble();
	}

	public short getShort(int index) {
		return values.get(index).getShort();
	}

	public char getChar(int index) {
		return values.get(index).getChar();
	}

	public String getString(int index) {
		return values.get(index).getString();
	}

	public IObjectReference<Object> getObject(int index) {
		return values.get(index).getObject();
	}
	
	@SuppressWarnings("unchecked")
	public <T> IObjectReference<T> get(Class<T> type, int index) {
		return (IObjectReference<T>) getObject(index);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DynamicArray) {
			DynamicArray other = (DynamicArray) obj;
			return this.values.equals(other.values);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}

	private static final ISerializer<IHolder> HOLDER_SERIALIZER = new ISerializer<IHolder>() {
		@Override
		public IHolder deserialize(IDeserializationContext context) throws IOException {
			byte holderTypeId = context.readByte();
			HolderType holderType = IHolder.getType(holderTypeId);
			if(holderType != null) {
				return (IHolder) holderType.serialization.load(context);
			} else {
				throw new IllegalArgumentException("Unknown HolderType "+holderTypeId);
			}
		}

		@Override
		public void serialize(ISerializationContext context, IHolder object) throws IOException {
			HolderType holderType = object.getType();
			context.writeByte(holderType.id);
			holderType.serialization.save(context, object);
		}
	};
	
	private static class HolderType {
		private final byte id;
		private final Serialization<?> serialization;

		public <T> HolderType(int id, Class<T> type, UnsafeBiConsumer<ISerializationContext, T, IOException> saver, UnsafeFunction<IDeserializationContext, T, IOException> loader) {
			this.id = (byte) id;
			this.serialization = new Serialization<>(saver, loader);
		}
		
		private static class Serialization<T> {
			private final UnsafeBiConsumer<ISerializationContext, T, IOException> saver;
			private final UnsafeFunction<IDeserializationContext, T, IOException> loader;

			public Serialization(UnsafeBiConsumer<ISerializationContext, T, IOException> saver, UnsafeFunction<IDeserializationContext, T, IOException> loader) {
				this.saver = saver;
				this.loader = loader;
			}
			
			@SuppressWarnings("unchecked")
			public void save(ISerializationContext context, Object object) throws IOException {
				saver.accept(context, (T) object);
			}
			
			public T load(IDeserializationContext context) throws IOException {
				return loader.eval(context);
			}
		}
	}
	
	private interface IHolder {
		public static final HolderType INT = new HolderType(0, IntHolder.class, (context, o) -> context.writeInt(o.value), context -> new IntHolder(context.readInt()));
		public static final HolderType BOOLEAN = new HolderType(1, BooleanHolder.class, (context, o) -> context.writeBoolean(o.value), context -> new BooleanHolder(context.readBoolean()));
		public static final HolderType FLOAT = new HolderType(2, FloatHolder.class, (context, o) -> context.writeFloat(o.value), context -> new FloatHolder(context.readFloat()));
		public static final HolderType LONG = new HolderType(3, LongHolder.class, (context, o) -> context.writeLong(o.value), context -> new LongHolder(context.readLong()));
		public static final HolderType DOUBLE = new HolderType(4, DoubleHolder.class, (context, o) -> context.writeDouble(o.value), context -> new DoubleHolder(context.readDouble()));
		public static final HolderType SHORT = new HolderType(5, ShortHolder.class, (context, o) -> context.writeShort(o.value), context -> new ShortHolder(context.readShort()));
		public static final HolderType CHAR = new HolderType(6, CharHolder.class, (context, o) -> context.writeChar(o.value), context -> new CharHolder(context.readChar()));
		public static final HolderType STRING = new HolderType(7, StringHolder.class, (context, o) -> context.writeString(o.value), context -> new StringHolder(context.readString()));
		public static final HolderType OBJECT = new HolderType(8, ObjectHolder.class, (context, o) -> context.writeObject(o.value), context -> ObjectHolder.load(context));
		
		static HolderType getType(byte id) {
			if(id == INT.id) {
				return INT;
			} else if(id == BOOLEAN.id) {
				return BOOLEAN;
			} else if(id == FLOAT.id) {
				return FLOAT;
			} else if(id == LONG.id) {
				return LONG;
			} else if(id == DOUBLE.id) {
				return DOUBLE;
			} else if(id == SHORT.id) {
				return SHORT;
			} else if(id == CHAR.id) {
				return CHAR;
			} else if(id == STRING.id) {
				return STRING;
			} else if(id == OBJECT.id) {
				return OBJECT;
			} else {
				return null;
			}
		}
		
		default int getInt() {
			throw new ClassCastException("Element is not int");
		}
		
		default boolean getBoolean() {
			throw new ClassCastException("Element is not boolean");
		}

		default float getFloat() {
			throw new ClassCastException("Element is not float");
		}

		default long getLong() {
			throw new ClassCastException("Element is not long");
		}

		default double getDouble() {
			throw new ClassCastException("Element is not double");
		}

		default short getShort() {
			throw new ClassCastException("Element is not short");
		}

		default char getChar() {
			throw new ClassCastException("Element is not char");
		}

		default String getString() {
			throw new ClassCastException("Element is not String");
		}

		default IObjectReference<Object> getObject() {
			throw new ClassCastException("Element is not Object");
		}
		
		HolderType getType();
	}

	private static class IntHolder implements IHolder {
		private final int value;

		public IntHolder(int value) {
			this.value = value;
		}

		@Override
		public int getInt() {
			return value;
		}

		@Override
		public HolderType getType() {
			return INT;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof IntHolder) {
				return this.value == ((IntHolder) obj).value;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}

	private static class BooleanHolder implements IHolder {
		private final boolean value;

		public BooleanHolder(boolean value) {
			this.value = value;
		}

		@Override
		public boolean getBoolean() {
			return value;
		}

		@Override
		public HolderType getType() {
			return BOOLEAN;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof BooleanHolder) {
				return this.value == ((BooleanHolder) obj).value;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}

	private static class FloatHolder implements IHolder {
		private final float value;

		public FloatHolder(float value) {
			this.value = value;
		}

		@Override
		public float getFloat() {
			return value;
		}

		@Override
		public HolderType getType() {
			return FLOAT;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof FloatHolder) {
				return this.value == ((FloatHolder) obj).value;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}

	private static class LongHolder implements IHolder {
		private final long value;

		public LongHolder(long value) {
			this.value = value;
		}

		@Override
		public long getLong() {
			return value;
		}

		@Override
		public HolderType getType() {
			return LONG;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof LongHolder) {
				return this.value == ((LongHolder) obj).value;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}

	private static class DoubleHolder implements IHolder {
		private final double value;

		public DoubleHolder(double value) {
			this.value = value;
		}

		@Override
		public double getDouble() {
			return value;
		}

		@Override
		public HolderType getType() {
			return DOUBLE;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof DoubleHolder) {
				return this.value == ((DoubleHolder) obj).value;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}

	private static class ShortHolder implements IHolder {
		private final short value;

		public ShortHolder(short value) {
			this.value = value;
		}

		@Override
		public short getShort() {
			return value;
		}

		@Override
		public HolderType getType() {
			return SHORT;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ShortHolder) {
				return this.value == ((ShortHolder) obj).value;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}

	private static class CharHolder implements IHolder {
		private final char value;

		public CharHolder(char value) {
			this.value = value;
		}

		@Override
		public char getChar() {
			return value;
		}

		@Override
		public HolderType getType() {
			return CHAR;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof CharHolder) {
				return this.value == ((CharHolder) obj).value;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}

	private static class StringHolder implements IHolder {
		private final String value;

		public StringHolder(String value) {
			this.value = value;
		}

		@Override
		public String getString() {
			return value;
		}

		@Override
		public HolderType getType() {
			return STRING;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof StringHolder) {
				return Objects.equals(this.value, ((StringHolder) obj).value);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}
	
	private static class ObjectHolder implements IHolder { 
		private Object value;
		private final ObjectReference<Object> reference = new ObjectReference<>();

		public ObjectHolder(Object value) {
			this.value = value;
		}
		
		public static ObjectHolder load(IDeserializationContext context) throws IOException {
			ObjectHolder holder = new ObjectHolder(null);
			context.readObject(Object.class, holder::setValue);
			return holder;
		}
		
		private void setValue(Object value) {
			this.value = value;
			this.reference.setValue(value);
		}

		@Override
		public IObjectReference<Object> getObject() {
			return reference;
		}
		
		@Override
		public HolderType getType() {
			return OBJECT;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ObjectHolder) {
				return Objects.equals(this.value, ((ObjectHolder) obj).value);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}
	}
}
