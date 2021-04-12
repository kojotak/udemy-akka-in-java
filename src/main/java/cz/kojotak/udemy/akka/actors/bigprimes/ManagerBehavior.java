package cz.kojotak.udemy.akka.actors.bigprimes;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.TreeSet;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command >{
	
	public static interface Command extends Serializable { 	}

	public static class InstructionCommand implements Command {
		private static final long serialVersionUID = 1L;
		private final String msg;
		private final ActorRef<SortedSet<BigInteger>> sender;
		public InstructionCommand(String msg, ActorRef<SortedSet<BigInteger>> sender) {
			super();
			this.msg = msg;
			this.sender = sender;
		}
		public String getMsg() {
			return msg;
		}
		public ActorRef<SortedSet<BigInteger>> getSender() {
			return sender;
		}
	}
	
	public static class ResultCommand implements Command {
		private static final long serialVersionUID = 1L;
		private final BigInteger prime;
		public ResultCommand(BigInteger prime) {
			super();
			this.prime = prime;
		}
		public BigInteger getPrime() {
			return prime;
		}
	}
	
	private class NoReponseReceivedCommand implements Command {
		private static final long serialVersionUID = 1L;
		private ActorRef<WorkerBehavior.Command> worker;
		public NoReponseReceivedCommand(ActorRef<WorkerBehavior.Command> worker) {
			super();
			this.worker = worker;
		}
		public ActorRef<WorkerBehavior.Command> getWorker() {
			return worker;
		}
	}
	
	private ManagerBehavior(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(ManagerBehavior::new);
	}

	private SortedSet<BigInteger> primes = new TreeSet<>();
	private ActorRef<SortedSet<BigInteger>> sender;
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(InstructionCommand.class, cmd->{
					if(cmd.getMsg().equals("start")) {
						this.sender = cmd.getSender();
						for(int i = 0; i<20; i++) {
							ActorRef<WorkerBehavior.Command> child = getContext().spawn(WorkerBehavior.create(), "worker"+i);
							askWorkersForPrime(child);
						}
					}
					return Behaviors.same();
				})
				.onMessage(ResultCommand.class, cmd->{
					primes.add(cmd.getPrime());
					System.out.println("received: " + primes.size() + " numbers");
					if(primes.size()==20) {
						this.sender.tell(primes);
					}
					return Behaviors.same();
				})
				.onMessage(NoReponseReceivedCommand.class, cmd -> {
					System.out.println("Retrying with worker " + cmd.getWorker().path());
					askWorkersForPrime(cmd.getWorker());
					return Behaviors.same();
				})
				.build();
	}

	private void askWorkersForPrime(ActorRef<WorkerBehavior.Command> worker) {
		getContext().ask(Command.class, worker, Duration.ofSeconds(5), 
				(me)->  new WorkerBehavior.Command("start", me),
				(response, throwable) -> {
					if(response != null) {
						return response;
					} else {
						System.out.println("Worker " + worker.path() + " failed to respond.");
						return new NoReponseReceivedCommand(worker);
					}
				}
		);
	}
}
