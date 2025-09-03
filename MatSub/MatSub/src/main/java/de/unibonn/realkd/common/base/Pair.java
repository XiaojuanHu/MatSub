package de.unibonn.realkd.common.base;

/**
 * 
 * @author Pavel Tokmakov
 * 
 * @since 0.1.0
 * 
 * @since 0.1.1
 * 
 * 
 */
public class Pair<K, V> {

	public static <K, V> Pair<K, V> pair(K lhs, V rhs) {
		return new Pair<K, V>(lhs, rhs);
	}

	private final K _1;

	private final V _2;

	private Pair(K lhs, V rhs) {
		this._1 = lhs;
		this._2 = rhs;
	}

	public K _1() {
		return _1;
	}

	public V _2() {
		return _2;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair))
			return false;
		if (this == obj)
			return true;
		return equal(_1, ((Pair<?, ?>) obj)._1())
				&& equal(_2, ((Pair<?, ?>) obj)._2());
	}

	@Override
	public int hashCode() {
		return 31 * (_1 == null ? 0 : _1.hashCode())
				+ (_2 == null ? 0 : _2.hashCode());
	}

	private boolean equal(Object o1, Object o2) {
		return o1 == null ? o2 == null : (o1 == o2 || o1.equals(o2));
	}

	@Override
	public String toString() {
		return "(" + _1 + ", " + _2 + ")";
	}
	
}
