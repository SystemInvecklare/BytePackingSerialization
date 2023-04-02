package net.pointlessgames.libs.bps.functional;

import java.util.HashMap;
import java.util.Map;

public class HashMapPairing<A, B> implements IPairing<A, B> {
	private final Map<A, B> firstToSecond = new HashMap<>();
	private final Map<B, A> secondToFirst = new HashMap<>();

	@Override
	public void add(A a, B b) {
		firstToSecond.put(a, b);
		secondToFirst.put(b, a);
	}

	@Override
	public B getByFirst(A first) {
		return firstToSecond.get(first);
	}

	@Override
	public A getBySecond(B second) {
		return secondToFirst.get(second);
	}
}
