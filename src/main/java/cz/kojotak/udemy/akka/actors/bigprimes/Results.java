package cz.kojotak.udemy.akka.actors.bigprimes;

import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

public class Results {

	private final SortedSet<BigInteger> primes = new TreeSet<>();

	public int getSize() {
		synchronized (primes) {
			return primes.size();
		}
	}
	
	public void addPrime(BigInteger bi) {
		synchronized(primes) {
			primes.add(bi);
		}
	}
	
	public void print() {
		synchronized(primes) {
			primes.forEach(System.out::println);
		}
	}
}
