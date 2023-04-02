package net.pointlessgames.libs.bps.functional;

public interface IPairing<A, B> {
	void add(A a, B b);
	B getByFirst(A first);
	A getBySecond(B second);
}
