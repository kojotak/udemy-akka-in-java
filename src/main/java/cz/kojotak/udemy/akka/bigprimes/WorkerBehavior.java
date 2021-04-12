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
	
	@Override
	public Receive<Command> createReceive() {
		return handleMessageWithoutPrimeNumber();
	}
	
	public Receive<Command> handleMessageWithoutPrimeNumber() {
		return newReceiveBuilder()
				.onAnyMessage(command ->{
					BigInteger bi = new BigInteger(2000, new Random());
					BigInteger prime = bi.nextProbablePrime();
					Random r = new Random();
					if(r.nextInt(5) < 2 ) {
						command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
					}
					//doporuceni - nemit ve tride zadny stav ani prepinac,
					//ale misto toho zmenit chovani
					return handleMessageWithPrimeNumber(prime);
				})
				.build();
	}
	
	public Receive<Command> handleMessageWithPrimeNumber(BigInteger prime){
		return newReceiveBuilder()
				.onAnyMessage(command ->{
					Random r = new Random();
					if(r.nextInt(5) < 2) {
						if(command.getMsg().equals("start")) {
							System.out.println(getContext().getSelf().path() + " already computed...");
							command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
						}
					}
					return Behaviors.same();
				})
				.build();
	}

	
}
