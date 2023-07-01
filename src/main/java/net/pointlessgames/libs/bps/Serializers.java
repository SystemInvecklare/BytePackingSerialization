package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Serializers {
	public static final ISerializer<Integer> INT = new ISerializer<Integer>() {
		@Override
		public Integer deserialize(IDeserializationContext context) throws IOException {
			return context.readInt();
		}

		@Override
		public void serialize(ISerializationContext context, Integer object) throws IOException {
			context.writeInt(object);
		}
	};


	public static final ISerializer<Byte> BYTE = new ISerializer<Byte>() {
		@Override
		public Byte deserialize(IDeserializationContext context) throws IOException {
			return context.readByte();
		}

		@Override
		public void serialize(ISerializationContext context, Byte object) throws IOException {
			context.writeByte(object);
		}
	};


	public static final ISerializer<Short> SHORT = new ISerializer<Short>() {
		@Override
		public Short deserialize(IDeserializationContext context) throws IOException {
			return context.readShort();
		}

		@Override
		public void serialize(ISerializationContext context, Short object) throws IOException {
			context.writeShort(object);
		}
	};


	public static final ISerializer<Long> LONG = new ISerializer<Long>() {
		@Override
		public Long deserialize(IDeserializationContext context) throws IOException {
			return context.readLong();
		}

		@Override
		public void serialize(ISerializationContext context, Long object) throws IOException {
			context.writeLong(object);
		}
	};


	public static final ISerializer<Float> FLOAT = new ISerializer<Float>() {
		@Override
		public Float deserialize(IDeserializationContext context) throws IOException {
			return context.readFloat();
		}

		@Override
		public void serialize(ISerializationContext context, Float object) throws IOException {
			context.writeFloat(object);
		}
	};


	public static final ISerializer<Double> DOUBLE = new ISerializer<Double>() {
		@Override
		public Double deserialize(IDeserializationContext context) throws IOException {
			return context.readDouble();
		}

		@Override
		public void serialize(ISerializationContext context, Double object) throws IOException {
			context.writeDouble(object);
		}
	};


	public static final ISerializer<Boolean> BOOLEAN = new ISerializer<Boolean>() {
		@Override
		public Boolean deserialize(IDeserializationContext context) throws IOException {
			return context.readBoolean();
		}

		@Override
		public void serialize(ISerializationContext context, Boolean object) throws IOException {
			context.writeBoolean(object);
		}
	};


	public static final ISerializer<Character> CHAR = new ISerializer<Character>() {
		@Override
		public Character deserialize(IDeserializationContext context) throws IOException {
			return context.readChar();
		}

		@Override
		public void serialize(ISerializationContext context, Character object) throws IOException {
			context.writeChar(object);
		}
	};


	public static final ISerializer<String> STRING = new ISerializer<String>() {
		@Override
		public String deserialize(IDeserializationContext context) throws IOException {
			return context.readString();
		}
		
		@Override
		public void serialize(ISerializationContext context, String object) throws IOException {
			context.writeString(object);
		}
	};
	
	
	public static final ISerializer<int[]> INT_ARRAY = new ISerializer<int[]>() {
		@Override
		public int[] deserialize(IDeserializationContext context) throws IOException {
			return context.readIntArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, int[] object) throws IOException {
			context.writeIntArray(object);
		}
	};


	public static final ISerializer<byte[]> BYTE_ARRAY = new ISerializer<byte[]>() {
		@Override
		public byte[] deserialize(IDeserializationContext context) throws IOException {
			return context.readByteArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, byte[] object) throws IOException {
			context.writeByteArray(object);
		}
	};


	public static final ISerializer<short[]> SHORT_ARRAY = new ISerializer<short[]>() {
		@Override
		public short[] deserialize(IDeserializationContext context) throws IOException {
			return context.readShortArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, short[] object) throws IOException {
			context.writeShortArray(object);
		}
	};


	public static final ISerializer<long[]> LONG_ARRAY = new ISerializer<long[]>() {
		@Override
		public long[] deserialize(IDeserializationContext context) throws IOException {
			return context.readLongArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, long[] object) throws IOException {
			context.writeLongArray(object);
		}
	};


	public static final ISerializer<float[]> FLOAT_ARRAY = new ISerializer<float[]>() {
		@Override
		public float[] deserialize(IDeserializationContext context) throws IOException {
			return context.readFloatArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, float[] object) throws IOException {
			context.writeFloatArray(object);
		}
	};


	public static final ISerializer<double[]> DOUBLE_ARRAY = new ISerializer<double[]>() {
		@Override
		public double[] deserialize(IDeserializationContext context) throws IOException {
			return context.readDoubleArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, double[] object) throws IOException {
			context.writeDoubleArray(object);
		}
	};


	public static final ISerializer<boolean[]> BOOLEAN_ARRAY = new ISerializer<boolean[]>() {
		@Override
		public boolean[] deserialize(IDeserializationContext context) throws IOException {
			return context.readBooleanArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, boolean[] object) throws IOException {
			context.writeBooleanArray(object);
		}
	};


	public static final ISerializer<char[]> CHAR_ARRAY = new ISerializer<char[]>() {
		@Override
		public char[] deserialize(IDeserializationContext context) throws IOException {
			return context.readCharArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, char[] object) throws IOException {
			context.writeCharArray(object);
		}
	};


	public static final ISerializer<String[]> STRING_ARRAY = new ISerializer<String[]>() {
		@Override
		public String[] deserialize(IDeserializationContext context) throws IOException {
			return context.readStringArray();
		}
		
		@Override
		public void serialize(ISerializationContext context, String[] object) throws IOException {
			context.writeStringArray(object);
		}
	};
	

	public static <T> ISerializer<T> empty(Supplier<T> factory) {
		return new ISerializer<T>() {
			@Override
			public T deserialize(IDeserializationContext context) throws IOException {
				return factory.get();
			}

			@Override
			public void serialize(ISerializationContext context, T object) throws IOException {
			}
		};
	}
	
	public static <T> ISerializer<T> empty(Supplier<T> factory, int version) {
		return new ISerializer<T>() {
			@Override
			public T deserialize(IDeserializationContext context) throws IOException {
				return factory.get();
			}

			@Override
			public void serialize(ISerializationContext context, T object) throws IOException {
			}
			
			@Override
			public int getVersion() {
				return version;
			}
		};
	}

	public static <F, T> ISerializer<T> map(ISerializer<F> serializer, Function<F, T> mapping, Function<T, F> comapping) {
		return new ISerializer<T>() {
			@Override
			public void serialize(ISerializationContext context, T object) throws IOException {
				serializer.serialize(context, comapping.apply(object));
			}
			
			@Override
			public T deserialize(IDeserializationContext context) throws IOException {
				return mapping.apply(serializer.deserialize(context));
			}
		};
	}
	
	public static <T> ISerializer<Optional<T>> optional(ISerializer<T> serializer) {
		return new ISerializer<Optional<T>>() {
			@Override
			public Optional<T> deserialize(IDeserializationContext context) throws IOException {
				boolean isPresent = context.readBoolean();
				if(isPresent) {
					return Optional.of(context.read(serializer));
				} else {
					return Optional.empty();
				}
			}

			@Override
			public void serialize(ISerializationContext context, Optional<T> object) throws IOException {
				context.writeBoolean(object.isPresent());
				if(object.isPresent()) {
					context.write(serializer, object.get());
				}
			}
		};
	}
}
