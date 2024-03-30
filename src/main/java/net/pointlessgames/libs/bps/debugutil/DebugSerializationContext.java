package net.pointlessgames.libs.bps.debugutil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

import net.pointlessgames.libs.bps.ISerializer;
import net.pointlessgames.libs.bps.SerializationContext;
import net.pointlessgames.libs.bps.data.IDataWriter;
import net.pointlessgames.libs.bps.extracontext.IDependentSerializer;

public class DebugSerializationContext extends SerializationContext {
	private final DebuggerPrinter debuggerPrinter = new DebuggerPrinter();
	
	public DebugSerializationContext(IDataWriter out, ISerializer<Object> objectSerializer) {
		super(out, objectSerializer);
	}

	public DebugSerializationContext(OutputStream stream, ISerializer<Object> objectSerializer) {
		super(stream, objectSerializer);
	}
	
	protected String objectToString(Object object) {
		return String.valueOf(object);
	}
	
	public String getLog() {
		return debuggerPrinter.getString();
	}

	@Override
	public void writeString(String S) throws IOException {
		debuggerPrinter.println("STRING: "+S);
		debuggerPrinter.pause();
		super.writeString(S);
		debuggerPrinter.unpause();
	}
	
	@Override
	public void writeShort(short s) throws IOException {
		debuggerPrinter.println("SHORT: "+s);
		debuggerPrinter.pause();
		super.writeShort(s);
		debuggerPrinter.unpause();
	}
	
	@Override
	public void writeObject(Object object) throws IOException {
		debuggerPrinter.println("OBJECT: "+objectToString(object)+" {");
		debuggerPrinter.indent();
		super.writeObject(object);
		debuggerPrinter.unindent();
		debuggerPrinter.println("}");
	}
	
	@Override
	public void writeLong(long l) throws IOException {
		debuggerPrinter.println("LONG: "+l);
		debuggerPrinter.pause();
		super.writeLong(l);
		debuggerPrinter.unpause();
	}
	
	@Override
	public void writeInt(int i) throws IOException {
		debuggerPrinter.println("INT: "+i);
		debuggerPrinter.pause();
		super.writeInt(i);
		debuggerPrinter.unpause();
	}
	
	@Override
	public void writeFloat(float f) throws IOException {
		debuggerPrinter.println("FLOAT: "+f);
		debuggerPrinter.pause();
		super.writeFloat(f);
		debuggerPrinter.unpause();
	}
	
	@Override
	public void writeDouble(double d) throws IOException {
		debuggerPrinter.println("DOUBLE: "+d);
		debuggerPrinter.pause();
		super.writeDouble(d);
		debuggerPrinter.unpause();
	}
	
	@Override
	public <T> void writeDependent(IDependentSerializer<T, ?> serializer, T object) throws IOException {
		debuggerPrinter.println("SERIALIZE DEPENDENT: "+objectToString(object)+" {");
		debuggerPrinter.indent();
		super.writeDependent(serializer, object);
		debuggerPrinter.unindent();
		debuggerPrinter.println("}");
	}
	
	@Override
	public void writeChar(char c) throws IOException {
		debuggerPrinter.println("CHAR: "+c);
		debuggerPrinter.pause();
		super.writeChar(c);
		debuggerPrinter.unpause();
	}
	
	@Override
	public void writeByte(byte b) throws IOException {
		debuggerPrinter.println("BYTE: "+b);
		debuggerPrinter.pause();
		super.writeByte(b);
		debuggerPrinter.unpause();
	}
	
	@Override
	public void writeBoolean(boolean b) throws IOException {
		debuggerPrinter.println("BOOLEAN: "+b);
		debuggerPrinter.pause();
		super.writeBoolean(b);
		debuggerPrinter.unpause();
	}
	
	@Override
	public <T> void write(ISerializer<T> serializer, T object) throws IOException {
		debuggerPrinter.println("SERIALIZE: "+objectToString(object)+"{");
		debuggerPrinter.indent();
		super.write(serializer, object);
		debuggerPrinter.unindent();
		debuggerPrinter.println("}");
	}

	private static class DebuggerPrinter {
		private final StringBuilder builder = new StringBuilder();
		private Stack<String> indent = new Stack<>();
		private int paused = 0;
		
		public DebuggerPrinter() {
			indent.push("");
		}
		
		public void println(String line) {
			if(paused <= 0) {
				builder.append(indent.peek()).append(line).append("\n");
			}
		}
		
		public void indent() {
			indent.push(indent.peek()+"  ");
		}
		
		public void unindent() {
			indent.pop();
		}
		
		public void pause() {
			paused++;
		}
		
		public void unpause() {
			paused--;
		}
		
		public String getString() {
			return builder.toString();
		}
	}
}
