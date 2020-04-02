package contour.bean;

public class Tuple2<A, B> {

	public final A _1;
	public final B _2;

	public Tuple2(A a, B b) {
		this._1 = a;
		this._2 = b;
	}

	@Override
	public String toString() {
		return "(" +
				_1 +
				","+ _2 +
				')';
	}
}
