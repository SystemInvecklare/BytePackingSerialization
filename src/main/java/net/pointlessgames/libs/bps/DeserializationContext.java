package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.pointlessgames.libs.bps.data.IDataReader;
import net.pointlessgames.libs.bps.data.InputStreamDataReader;

public class DeserializationContext implements IDeserializationContext {
	private final IDataReader in;
	private final IDeserializer<Object> objectDeserializer;
	private final Map<Integer, Object> objectMap = new HashMap<Integer, Object>();
	
	public DeserializationContext(InputStream in, IDeserializer<Object> objectDeserializer) {
		this(new InputStreamDataReader(in), objectDeserializer);
	}
	
	public DeserializationContext(IDataReader in, IDeserializer<Object> objectDeserializer) {
		this.in = in;
		this.objectDeserializer = objectDeserializer;
	}

	@Override
	public float readFloat() throws IOException {
		return in.readFloat();
	}

	private static int toInt(byte b) {
		return 0b11111111 & (b + 512);
	}

	@Override
	public int readInt() throws IOException {
		byte b1 = readByte();
		if((b1 & 0b10000000) == 0) {
			// 7 bytes
			int val = b1 & 0b00111111;
			if((b1 & 0b01000000) != 0) {
				val = 0b11111111111111111111111110000000 | b1;
			}
			return val;
		} else if((b1 & 0b01000000) == 0) {
			// 14 bytes
			byte b2 = readByte();
			int val = toInt(b2) | ((b1 & 0b00011111) << 8);
			if((b1 & 0b00100000) != 0) {
				val = 0b11111111111111111110000000000000 | val;
			}
			return val;
		} else if((b1 & 0b00100000) == 0) {
			// 21 bytes
			byte b2 = readByte();
			byte b3 = readByte();
			int val = toInt(b3) | (toInt(b2) << 8) | ((b1 & 0b00001111) << 16);
			if((b1 & 0b00010000) != 0) {
				val = 0b11111111111100000000000000000000 | val;
			}
			return val;
		} else if((b1 & 0b00010000) == 0) {
			// 28 bytes
			byte b2 = readByte();
			byte b3 = readByte();
			byte b4 = readByte();
			int val = toInt(b4) | (toInt(b3) << 8) | (toInt(b2) << 16) | ((b1 & 0b00000111) << 24);
			if((b1 & 0b00001000) != 0) {
				val = 0b11111000000000000000000000000000 | val;
			}
			return val;
		} else {
			byte b2 = readByte();
			byte b3 = readByte();
			byte b4 = readByte();
			byte b5 = readByte();
			return toInt(b5) | (toInt(b4) << 8) | (toInt(b3) << 16) | (toInt(b2) << 24);
		}
	}

	@Override
	public boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	@Override
	public double readDouble() throws IOException {
		return in.readDouble();
	}

	@Override
	public String readString() throws IOException {
		return in.readUTF();
	}

	@Override
	public short readShort() throws IOException {
		return in.readShort();
	}

	@Override
	public long readLong() throws IOException {
		return in.readLong();
	}

	@Override
	public char readChar() throws IOException {
		return in.readChar();
	}
	
	@Override
	public byte readByte() throws IOException {
		return in.readByte();
	}

	@Override
	public <T> T read(IDeserializer<T> serializer) throws IOException {
		int serializerVersion = readInt();
		if(serializer.getVersion() != serializerVersion) {
			if(serializer instanceof IMultiVersionDeserializer) {
				@SuppressWarnings("unchecked")
				IDeserializer<T> correctVersionSerializer = ((IMultiVersionDeserializer<T>) serializer).getDeserializer(serializerVersion);
				if(correctVersionSerializer != null) {
					return correctVersionSerializer.deserialize(this);
				}
			}
			throw new IOException("Can not deserialize object serialization version "+serializerVersion);
		}
		return serializer.deserialize(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void readObject(Class<T> type, Consumer<T> consumer) throws IOException {
		int id = readInt();
		Object object = objectMap.get(id);
		if(object == null && !objectMap.containsKey(id)) {
			//First time read
			UnfinishedObject<T> unfinishedObject = new UnfinishedObject<>();
			objectMap.put(id, unfinishedObject);
			object = read(objectDeserializer);
			objectMap.put(id, object);
			unfinishedObject.onFinished((T) object);
			consumer.accept((T) object);
		} else if(object instanceof UnfinishedObject) {
			((UnfinishedObject<T>) object).consumers.add(consumer);
		} else {
			consumer.accept((T) object);
		}
	}
	
	private static class UnfinishedObject<T> {
		private final List<Consumer<T>> consumers = new ArrayList<Consumer<T>>();

		public void onFinished(T object) {
			RuntimeException exception = null;
			for(Consumer<T> consumer : consumers) {
				try {
					consumer.accept(object);
				} catch(RuntimeException e) {
					if(exception == null) {
						exception = e;
					} else {
						exception.addSuppressed(e);
					}
				}
			}
			if(exception != null) {
				throw exception;
			}
		}
	}
}
