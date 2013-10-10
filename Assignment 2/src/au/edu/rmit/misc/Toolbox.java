package au.edu.rmit.misc;

import java.math.BigInteger;

public class Toolbox
{

	/**
	 * This method was adapted from polygenelubricants's solution found at
	 * http://stackoverflow.com/questions/2201113/combinatoric-n-choose-r-in-java-math
	 * @param n
	 * @param k
	 * @return
	 */
	public static long choose(final int N, final int K) {
	    BigInteger ret = BigInteger.ONE;
	    for (int k = 0; k < K; k++) {
	        ret = ret.multiply(BigInteger.valueOf(N-k))
	                 .divide(BigInteger.valueOf(k+1));
	    }
	    return ret.longValue();
	}
}
