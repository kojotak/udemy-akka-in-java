package cz.kojotak.udemy.akka.bigprimes;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {
	
	public static class Command implements Serializable {//serializable pro posilani z jednoho JVM do druheho

		private static final long serialVersionUID = 1L;
		
		private final String msg;
		private final ActorRef<ManagerBehavior.Command> sender;

		public Command(String msg, ActorRef<ManagerBehavior.Command> sender) {
			super();
			this.msg = msg;
			this.sender = sender;
		}

		public String getMsg() {
			return msg;
		}

		public ActorRef<ManagerBehavior.Command> getSender() {
			return sender;
		}
		
	}

	private WorkerBehavior(ActorContext<Command> context) {
		super(context);
	}

	public static Behavior<Command> create(){
		return Behaviors.setup(WorkerBehavior::new);
	}
	
	private BigInteger prime;
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onAnyMessage(command ->{
					if(command.getMsg().equals("start")) {
						BigInteger bi = new BigInteger(2000, new Random());
						prime = bi.nextProbablePrime();
						command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
						System.out.println(getContext().getSelf().path() + ", next: " + prime);
					}
					return subsequentHandler();
				})
				.build();
	}
	
	public Receive<Command> subsequentHandler(){
		return newReceiveBuilder()
				.onAnyMessage(command ->{
					if(command.getMsg().equals("start")) {
						System.err.println("already computed...");
						command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
						System.out.println(getContext().getSelf().path() + ", next: " + prime);
					}
					return Behaviors.same();
				})
				.build();
	}

	
}
