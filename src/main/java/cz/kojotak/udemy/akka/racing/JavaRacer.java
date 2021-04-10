package cz.kojotak.udemy.akka.racing;

import java.util.Map;
import java.util.Random;

public class JavaRacer implements Runnable{

	private int id; // 0..9
	
	private int raceLength; //100 ~ 20-30s
	
	//shared state - not the best idea though...
	private Map<Integer, Integer> currentPositions;
	private Map<Integer, Long> results;

	private final double defaultAverageSpeed = 48.2;
	private int averageSpeedAdjustmentFactor;
	private Random random;	
	
	private double currentSpeed = 0;
	private double currentPosition = 0;
	
	
	public JavaRacer(int id, int raceLength, Map<Integer, Integer> currentPositions, Map<Integer, Long> results) {
		this.id = id;
		this.raceLength = raceLength;
		this.currentPositions = currentPositions;
		this.results = results;
		random = new Random();
		averageSpeedAdjustmentFactor = random.nextInt(30) - 10; //-10..+20
	}
	
	private double getMaxSpeed() {
		return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
	}
		
	private double getDistanceMovedPerSecond() {
		return currentSpeed * 1000 / 3600;
	}
	
	private void determineNextSpeed() {
		if (currentPosition < (raceLength / 4)) {
			currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
		}
		else {
			currentSpeed = currentSpeed * (0.5 + random.nextDouble());
		}
	
		if (currentSpeed > getMaxSpeed()) 
			currentSpeed = getMaxSpeed();
		
		if (currentSpeed < 5)
			currentSpeed = 5;
		
		if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
			currentSpeed = getMaxSpeed() / 2;
		}
	}
		

	@Override
	public void run() {
		
		while (currentPosition < raceLength) {
			determineNextSpeed();
			currentPosition += getDistanceMovedPerSecond();
			if (currentPosition > raceLength )
				currentPosition  = raceLength;
			currentPositions.put(id, (int)currentPosition);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		results.put(id, System.currentTimeMillis());
		
	}
	
}
