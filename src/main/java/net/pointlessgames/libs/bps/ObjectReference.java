package net.pointlessgames.libs.bps;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ObjectReference<T> implements IObjectReference<T> {
	private T value;
	private boolean hasValue = false;
	private List<Consumer<T>> waiting = new ArrayList<Consumer<T>>(0);

	public void setValue(T value) {
		if(hasValue) {
			throw new IllegalStateException("setValue was called twice");
		}
		this.value = value;
		this.hasValue = true;
		if(!waiting.isEmpty()) {
			for(Consumer<T> consumer : waiting) {
				consumer.accept(value);
			}
		}
		waiting = null;
	}
	
	@Override
	public void get(Consumer<T> consumer) {
		if(hasValue) {
			consumer.accept(value);
		} else {
			waiting.add(consumer);
		}
	}
}
