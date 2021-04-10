package cz.kojotak.udemy.akka.racing;

import java.io.Serializable;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;

public class AkkaRacer extends AbstractBehavior<AkkaRacer.Command>{
	
	public interface Command extends Serializable{}
	
	public static class StartCommand implements Command{
		private static final long serialVersionUID = 1L;
		private final int raceLength;
		public StartCommand(int raceLength) {
			super();
			this.raceLength = raceLength;
		}
		public int getRaceLength() {
			return raceLength;
		}
	}
	
	public static class PositionCommand implements Command{
		private static final long serialVersionUID = 1L;
		private final ActorRef<RaceController.Command> controller;
		public PositionCommand(ActorRef<cz.kojotak.udemy.akka.racing.RaceController.Command> controller) {
			super();
			this.controller = controller;
		}
		public ActorRef<RaceController.Command> getController() {
			return controller;
		}
	}

	private AkkaRacer(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(AkkaRacer::new);
	}

	@Override
	public Receive<Command> createReceive() {
		return notYetStarted();
	}
	
	public Receive<Command> notYetStarted(){
		return newReceiveBuilder()
				.onMessage(StartCommand.class, msg ->{
					Random random = new Random();
					this.averageSpeedAdjustmentFactor = random.nextInt(30) - 10; //-10..+20
					return running(msg.getRaceLength(), 0, random);
				})
				.onMessage(PositionCommand.class, msg ->{
					msg.getController().tell(new RaceController.RacerUpdate(getContext().getSelf(), 0));
					return Behaviors.same();
				})
				.build();
	}
	
	public Receive<Command> running(int raceLength, int currentPosition, Random random){
		return newReceiveBuilder()
				.onMessage(PositionCommand.class, msg -> {
					determineNextSpeed(raceLength, currentPosition, random);
					int newPosition = currentPosition;
					newPosition += getDistanceMovedPerSecond();
					if (newPosition > raceLength )
						newPosition  = raceLength;
					
					//old java version -> we have to send message about current position instead
					//currentPositions.put(id, (int)currentPosition);
					msg.getController().tell(new RaceController.RacerUpdate(getContext().getSelf(), (int)newPosition));
					
					if(newPosition == raceLength) {
						return completed(raceLength);
					} else {
						return running(raceLength, newPosition, random);
					}					
				})
				.build();
	}
	
	public Receive<Command> completed(int raceLength){
		return newReceiveBuilder()
				.onMessage(PositionCommand.class, msg ->{
					msg.getController().tell(new RaceController.RacerUpdate(getContext().getSelf(), raceLength));
					return Behaviors.same();
				})
				.build();
	}
	
	//below ... copy-paste from JavaRacer
	
	private final double defaultAverageSpeed = 48.2;
	private int averageSpeedAdjustmentFactor;
	private double currentSpeed = 0;
	
	private double getMaxSpeed() {
		return defaultAverageSpeed * (1+((double)averageSpeedAdjustmentFactor / 100));
	}
		
	private double getDistanceMovedPerSecond() {
		return currentSpeed * 1000 / 3600;
	}
	
	private void determineNextSpeed(int currentPosition, int raceLength, Random random) {
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
	
}
