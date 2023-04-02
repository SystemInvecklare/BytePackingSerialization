package net.pointlessgames.libs.bps.functional;

@FunctionalInterface
public interface UnsafeBiConsumer<T, U, E extends Throwable> {
	void accept(T arg1, U arg2) throws E;
}
