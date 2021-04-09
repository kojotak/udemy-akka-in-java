package cz.kojotak.udemy.akka.bigprimes;

import java.math.BigInteger;
import java.util.Random;

public class PrimeGenerator implements Runnable {
	
	private Results results;

	public PrimeGenerator(Results results) {
		super();
		this.results = results;
	}

	@Override
	public void run() {
		BigInteger bigInteger = new BigInteger(2000, new Random());
		BigInteger prime = bigInteger.nextProbablePrime();
		results.addPrime(prime);
		System.out.println("finished " + results.getSize());
	}

}
