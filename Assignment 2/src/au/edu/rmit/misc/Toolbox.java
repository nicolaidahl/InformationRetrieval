package au.edu.rmit.misc;

import java.math.BigInteger;

public class Toolbox
{

	/**
	 * This method was adapted from 280z80's solution found at
	 * http://stackoverflow.com/questions/1678690/what-is-a-good-way-to-implement-choose-notation-in-java
	 * @param n
	 * @param k
	 * @return
	 */
	public static long choose(long n, long k)
	{
	    if (n / 2 < k)
	        return choose(n, n - k);

	    if (k > n)
	        return 0;

	    if (k == 0)
	        return 1;

	    long result = n;
	    for (long d = 2; d <= k; d++)
	    {
	    	BigInteger bigd = new BigInteger(""+d);
	    	BigInteger bign = new BigInteger(""+n);
	        long gcd = bigd.gcd(bign).longValue();
	        result *= (n / gcd);
	        result /= (d / gcd);
	        n++;
	    }

	    return result;
	}
}
