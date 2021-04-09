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
		private final ActorRef<String> sender;

		public Command(String msg, ActorRef<String> sender) {
			super();
			this.msg = msg;
			this.sender = sender;
		}

		public String getMsg() {
			return msg;
		}

		public ActorRef<String> getSender() {
			return sender;
		}
		
	}

	private WorkerBehavior(ActorContext<Command> context) {
		super(context);
	}

	public static Behavior<Command> create(){
		return Behaviors.setup(WorkerBehavior::new);
	}
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onAnyMessage(command ->{
					if(command.getMsg().equals("start")) {
						BigInteger bi = new BigInteger(2000, new Random());
						System.out.println(getContext().getSelf().path() + ", next: " + bi.nextProbablePrime());
					}
					return this;
				})
				.build();
	}

	
}
