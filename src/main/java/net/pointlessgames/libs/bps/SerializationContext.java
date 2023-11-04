package net.pointlessgames.libs.bps;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.pointlessgames.libs.bps.data.IDataWriter;
import net.pointlessgames.libs.bps.data.OutputStreamDataWriter;
import net.pointlessgames.libs.bps.extracontext.IDependentSerializer;
import net.pointlessgames.libs.bps.functional.UnsafeConsumer;
import net.pointlessgames.libs.bps.nested.IInnerType;
import net.pointlessgames.libs.bps.nested.IOuterType;
import net.pointlessgames.libs.bps.nested.InnerTypeMarker;

public class SerializationContext implements ISerializationContext {
	private final IDataWriter out;
	private final ISerializer<Object> objectSerializer;
	private final Map<Object, Integer> objectMap = new HashMap<Object, Integer>();
	private final Set<Object> objectsInMidSerialization = new HashSet<>();
	private final Map<IOuterType, List<UnsafeConsumer<IOuterType, IOException>>> deferredInnerTypeSerializations = new HashMap<>();
	private int nextId = 0;
	
	public SerializationContext(OutputStream stream, ISerializer<Object> objectSerializer) {
		this(new OutputStreamDataWriter(stream), objectSerializer);
	}
	
	public SerializationContext(IDataWriter out, ISerializer<Object> objectSerializer) {
		this.out = out;
		this.objectSerializer = objectSerializer;
	}
	
	@Override
	public void writeFloat(float f) throws IOException {
		out.writeFloat(f);
	}
	
	@Override
	public void writeInt(int i) throws IOException {
		if(i <= 63 && i >= -64) {
			writeByte((byte) (i & 0b01111111));
		} else if(i <= 8191 && i >= -8192) {
			writeByte((byte) (((i >> 8) & 0b00111111) | 0b10000000));
			writeByte((byte) i);
		} else if(i <= 1048575 && i >= -1048576) {
			writeByte((byte) (((i >> (2*8)) & 0b00011111) | 0b11000000));
			writeByte((byte) (i >> 8));
			writeByte((byte) i);
		} else if(i <= 134217727 && i >= -134217728) {
			writeByte((byte) (((i >> (3*8)) & 0b00001111) | 0b11100000));
			writeByte((byte) (i >> (2*8)));
			writeByte((byte) (i >> 8));
			writeByte((byte) i);
		} else {
			writeByte((byte) (0b11110000));
			writeByte((byte) (i >> (3*8)));
			writeByte((byte) (i >> (2*8)));
			writeByte((byte) (i >> 8));
			writeByte((byte) i);
		}
	}
	
	@Override
	public void writeBoolean(boolean b) throws IOException {
		out.writeBoolean(b);
	}

	@Override
	public void writeDouble(double d) throws IOException {
		out.writeDouble(d);
	}

	@Override
	public void writeString(String s) throws IOException {
		out.writeUTF(s);
	}

	@Override
	public void writeShort(short s) throws IOException {
		out.writeShort(s);
	}

	@Override
	public void writeLong(long l) throws IOException {
		out.writeLong(l);
	}

	@Override
	public void writeChar(char c) throws IOException {
		out.writeChar(c);
	}

	@Override
	public void writeByte(byte b) throws IOException {
		out.writeByte(b);
	}

	@Override
	public <T> void write(ISerializer<T> serializer, T object) throws IOException {
		writeInt(serializer.getVersion());
		serializer.serialize(this, object);
	}
	
	@Override
	public <T> void writeDependent(IDependentSerializer<T, ?> serializer, T object) throws IOException {
		writeInt(serializer.getVersion());
		serializer.serialize(this, object);
	}
	
	@Override
	public void writeObject(Object object) throws IOException {
		Integer id = objectMap.get(object);
		if(id == null) {
			//First time
			id = nextId++;
			objectMap.put(object, id);
			writeInt(id);
			objectsInMidSerialization.add(object);
			if(object instanceof IInnerType) {
				IOuterType outerType = ((IInnerType) object).getOuterObject();
				write(objectSerializer, InnerTypeMarker.INSTANCE);
				writeObject(outerType);
				if(objectsInMidSerialization.contains(outerType)) {
						List<UnsafeConsumer<IOuterType, IOException>> outerFinishedListeners = deferredInnerTypeSerializations.get(outerType);
						if(outerFinishedListeners == null) {
							outerFinishedListeners = new ArrayList<UnsafeConsumer<IOuterType,IOException>>();
							deferredInnerTypeSerializations.put(outerType, outerFinishedListeners);
						}
						outerFinishedListeners.add(outer -> { 
							outer.serializeInner(this, (IInnerType) object);
						});
				} else {
					outerType.serializeInner(this, (IInnerType) object);
				}
			} else {
				write(objectSerializer, object);
			}
			objectsInMidSerialization.remove(object);
			List<UnsafeConsumer<IOuterType, IOException>> onFinishedListeners = deferredInnerTypeSerializations.remove(object);
			if(onFinishedListeners != null) {
				IOuterType outer = (IOuterType) object;
				for(UnsafeConsumer<IOuterType, IOException> onFinishedListener : onFinishedListeners) {
					onFinishedListener.accept(outer);
				}
			}
		} else {
			writeInt(id);
		}
	}
}
