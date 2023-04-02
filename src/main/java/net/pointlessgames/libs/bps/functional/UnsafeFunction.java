package net.pointlessgames.libs.bps.functional;

@FunctionalInterface
public interface UnsafeFunction<T,R, E extends Throwable> {
	R eval(T arg) throws E;
}
