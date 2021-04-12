package cz.kojotak.udemy.akka.actors.racing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaMain {

	public static final int RACERS = 10;
	public static int raceLength = 100;
	public static int displayLength = 160;
	static long start;
	
	private static void displayRace(Map<Integer, Integer> currentPositions) {
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
		for (int i = 0; i < RACERS; i++) {
			System.out.println(i + " : "  + new String (new char[currentPositions.get(i) * displayLength / 100]).replace('\0', '*'));
		}
	}
	 

	public static void main(String[] args) throws InterruptedException {
		
		//ConcurrentHashMap = thread safe
		Map<Integer, Integer> currentPositions = new ConcurrentHashMap<Integer, Integer>(); 
		Map<Integer, Long> results = new ConcurrentHashMap<Integer, Long>();
		
		start = System.currentTimeMillis();
		
		ExecutorService threadPool = Executors.newFixedThreadPool(RACERS);
		
		for (int i = 0; i <RACERS; i++) {
			JavaRacer h = new JavaRacer(i,raceLength, currentPositions, results);
			currentPositions.put(i, 0);
			threadPool.execute(h);
		}
		
		boolean finished = false;
		while (!finished) {
			Thread.sleep(1000);//never do Thread.sleep in Akka!
			displayRace(currentPositions);
			finished = results.size() == RACERS;
		}
		
		threadPool.shutdownNow();
				
		System.out.println("Results");
		results.values().stream().sorted().forEach(it -> {
			for (Integer key : results.keySet()) {
				if (results.get(key) == it) {
					System.out.println("Racer " + key + " finished in " + ( (double)it - start ) / 1000 + " seconds.");
				}
			}
		});
	}
}
