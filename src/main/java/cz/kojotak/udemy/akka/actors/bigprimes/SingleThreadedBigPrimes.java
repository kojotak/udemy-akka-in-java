package cz.kojotak.udemy.akka.actors.bigprimes;

import java.math.BigInteger;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class SingleThreadedBigPrimes {
	
	public static final int LIMIT = 20;

	public static void main(String...args) {
		
		long start = System.currentTimeMillis();
		
		SortedSet<BigInteger> set = new TreeSet<>();
		
		while(set.size() < LIMIT) {
			BigInteger bigInteger = new BigInteger(2000, new Random());
			BigInteger prime = bigInteger.nextProbablePrime();
			set.add(prime);
			System.out.println("finished " + set.size());
		}

		long end = System.currentTimeMillis();
		System.out.println("time taken: " + (end-start) + " ms");
	}
}
