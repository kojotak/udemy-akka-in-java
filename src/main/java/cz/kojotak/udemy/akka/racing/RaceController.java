package cz.kojotak.udemy.akka.racing;

import java.io.Serializable;
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

	private RaceController(ActorContext context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(RaceController::new);
	}
	
	private Map<ActorRef<AkkaRacer.Command>, Integer> currentPositions;
	private long start;
	private int raceLength = 100;
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(StartCommand.class, msg->{
					start = System.currentTimeMillis();
					currentPositions = new HashMap<>();
					for(int i = 0; i<10; i++) {
						ActorRef<AkkaRacer.Command> racer = getContext().spawn(AkkaRacer.create(), "racer"+i);
						currentPositions.put(racer,0);
						racer.tell(new AkkaRacer.StartCommand(raceLength));
					}
					return this;
				})
				.onMessage(RacerUpdate.class, msg->{
					currentPositions.put(msg.getRacer(), msg.getPosition());
					return this;
				})
				.build();
	}
	
}
