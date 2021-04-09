package cz.kojotak.udemy.akka.bigprimes;

import java.util.ArrayList;
import java.util.List;

public class MultithreadedBigPrimes {

	public static void main(String[] args) throws InterruptedException {
		long start = System.currentTimeMillis();
		Results results = new Results();
		Runnable task = new PrimeGenerator(results);
		Runnable status = new CurrentStatus(results);
		Thread statusThread = new Thread(status);
		statusThread.start();
		List<Thread> threads = new ArrayList<>();
		for(int i=0; i<SingleThreadedBigPrimes.LIMIT;i++) {
			//issue -> threads are blocking each other, because number of threads > number of CPU cores
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
