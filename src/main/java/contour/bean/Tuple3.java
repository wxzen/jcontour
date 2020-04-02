package contour.bean;

public class Tuple3<A, B, C> {

	public final A _1;
	public final B _2;
	public final C _3;

	public Tuple3(A a, B b, C c) {
		this._1 = a;
		this._2 = b;
		this._3 = c;
	}

	@Override
	public String toString() {
		return "(" +
				_1 +
				","+ _2 +
				","+ _3 +
				')';
	}
}
