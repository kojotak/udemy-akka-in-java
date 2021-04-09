package cz.kojotak.udemy.akka.bigprimes;

import java.util.ArrayList;
import java.util.List;

public class MultithreadedBigPrimes {

	public static void main(String[] args) throws InterruptedException {
		long start = System.currentTimeMillis();
		Results results = new Results();
		Runnable task = new PrimeGenerator(results);
		List<Thread> threads = new ArrayList<>();
		for(int i=0; i<20;i++) {
			Thread t = new Thread(task);
			threads.add(t);
			t.start();
		}
		for(Thread t : threads) {
			t.join(); //wait for all threads to finish
		}
		System.out.println("time taken: " + (System.currentTimeMillis() - start) + " ms");
	}

}
