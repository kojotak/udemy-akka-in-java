package cz.kojotak.udemy.akka.racing;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RaceController extends AbstractBehavior<RaceController.Command>{

	public interface Command extends Serializable {}
	
	public static class StartCommand implements Command {
		private static final long serialVersionUID = 1L;
	}
	
	public static class RacerFinished implements Command{
		private static final long serialVersionUID = 1L;
		private final ActorRef<AkkaRacer.Command> racer;
		public RacerFinished(ActorRef<cz.kojotak.udemy.akka.racing.AkkaRacer.Command> racer) {
			super();
			this.racer = racer;
		}
		public ActorRef<AkkaRacer.Command> getRacer() {
			return racer;
		}
	}
	
	public static class RacerUpdate implements Command {
		private static final long serialVersionUID = 1L;
		private final ActorRef<AkkaRacer.Command> racer;
		private final int position;
		public RacerUpdate(ActorRef<cz.kojotak.udemy.akka.racing.AkkaRacer.Command> racer, int position) {
			super();
			this.racer = racer;
			this.position = position;
		}
		public ActorRef<AkkaRacer.Command> getRacer() {
			return racer;
		}
		public int getPosition() {
			return position;
		}
	}
	
	private class GetPosition implements Command {
		private static final long serialVersionUID = 1L;
	}

	private RaceController(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(RaceController::new);
	}
	
	private Map<ActorRef<AkkaRacer.Command>, Integer> currentPositions;
	private Map<ActorRef<AkkaRacer.Command>, Long> finishingTimes;
	private long start;
	private Object TIMER_KEY;
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(StartCommand.class,this::onStartCommand)
				.onMessage(GetPosition.class, this::onPositionCommand)
				.onMessage(RacerUpdate.class, this::onRacerUpdate)
				.onMessage(RacerFinished.class, this::onRacerFinished)
				.build();
	}

	public Receive<Command> raceCompleted(){
		return newReceiveBuilder()
				//i kdyz nastane RacerFinished, tak stale mame timer, ktery kazdou vterinu posila GetPosition
				.onMessage(GetPosition.class, msg->{
					for(ActorRef<AkkaRacer.Command> racer : currentPositions.keySet()) {
						getContext().stop(racer);//will terminate racer
					}
					displayResults();
					return Behaviors.withTimers(timers->{
						timers.cancelAll();
						return Behaviors.stopped();
					});
				})
				.build();
	}
	
	private Behavior<Command> onRacerFinished(RacerFinished msg){
		finishingTimes.put(msg.getRacer(), System.currentTimeMillis());
		if(finishingTimes.size() ==10) {
			return raceCompleted();
		}else {
			return Behaviors.same();
		}
	}
	
	private Behavior<Command> onRacerUpdate(RacerUpdate msg){
		currentPositions.put(msg.getRacer(), msg.getPosition());
		return Behaviors.same();
	}
	
	private Behavior<Command> onPositionCommand(GetPosition msg){
		for(ActorRef<AkkaRacer.Command> racer : currentPositions.keySet()) {
			racer.tell(new AkkaRacer.PositionCommand(getContext().getSelf()));
			displayRace();
		}
		return Behaviors.same();
	}
	
	private Behavior<Command> onStartCommand(StartCommand msg){
		start = System.currentTimeMillis();
		
		//not shared to other actors, no need for synchronization
		currentPositions = new HashMap<>();
		finishingTimes = new HashMap<>();
		for(int i = 0; i<10; i++) {
			ActorRef<AkkaRacer.Command> racer = getContext().spawn(AkkaRacer.create(), "racer"+i);
			currentPositions.put(racer,0);
			racer.tell(new AkkaRacer.StartCommand(JavaMain.raceLength));
		}
		
		//chci periodicky kontrolovat stav
		//proto nastartuju timer, ktery periodicky posle zpravu s dotazem na stav
		//toto je nahrada Thread.sleep v JavaRaceru
		return Behaviors.withTimers(timer -> {
			timer.startTimerAtFixedRate(TIMER_KEY, new GetPosition(), Duration.ofSeconds(1) );
			return Behaviors.same();
		});
	}
	
	//almost copy paste from Main.java
	private void displayRace() {
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[JavaMain.displayLength]).replace('\0', '='));
		int i = 0;
		for (ActorRef<AkkaRacer.Command> racer : currentPositions.keySet()) {
			System.out.println(i + " : "  + new String (new char[currentPositions.get(racer) * JavaMain.displayLength / 100]).replace('\0', '*'));
			i++;
		}
	}
	
	private void displayResults() {
		System.out.println("Results");
		finishingTimes.values().stream().sorted().forEach(it -> {
			for (ActorRef<AkkaRacer.Command> key : finishingTimes.keySet()) {
				if (finishingTimes.get(key) == it) {
					String racerId = key.path().toString().substring(key.path().toString().length()-1);
					System.out.println("Racer " + racerId + " finished in " + ( (double)it - start ) / 1000 + " seconds.");
				}
			}
		});
	}
	
}
