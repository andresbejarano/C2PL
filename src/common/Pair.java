package common;

public class Pair<T, S> {
	
	/* The tag name of the class */
	public static final String TAG = Pair.class.getName();
	
	/* The first element of the pair  */
	private T first;
	
	/* The second element of the pair */
	private S second;
	
	/**
	 * Constructor of the class
	 * @param first
	 * @param second
	 */
	public Pair(T first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * 
	 * @return
	 */
	public T getFirst() {
		return first;
	}

	/**
	 * 
	 * @return
	 */
	public S getSecond() {
		return second;
	}

	/**
	 * 
	 * @param first
	 */
	public void setFirst(T first) {
		this.first = first;
	}

	/**
	 * 
	 * @param second
	 */
	public void setSecond(S second) {
		this.second = second;
	}

}
