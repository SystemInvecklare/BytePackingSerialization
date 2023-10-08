package net.pointlessgames.libs.bps.functional;

import java.util.function.Consumer;

@FunctionalInterface
public interface UnsafeConsumer<T, E extends Throwable> {
	void accept(T arg) throws E;
	
	default Consumer<T> runtimeException(Class<? super E> caught) {
		return new Consumer<T>() {
			@Override
			public void accept(T t) {
				try {
					UnsafeConsumer.this.accept(t);
				} catch (Throwable e) {
					if(caught.isInstance(e) || !(e instanceof RuntimeException)) {
						throw new RuntimeException(e);
					} else {
						throw (RuntimeException) e;
					}
				}
			}
		};
	}
}
