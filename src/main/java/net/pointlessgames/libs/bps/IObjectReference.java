package net.pointlessgames.libs.bps;

import java.util.function.Consumer;

public interface IObjectReference<T> {
	void get(Consumer<T> consumer);
}
