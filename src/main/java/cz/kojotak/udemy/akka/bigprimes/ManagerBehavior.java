package cz.kojotak.udemy.akka.bigprimes;

import java.io.Serializable;
import java.math.BigInteger;
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

		public InstructionCommand(String msg) {
			super();
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
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
	
	private ManagerBehavior(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create(){
		return Behaviors.setup(ManagerBehavior::new);
	}

	private SortedSet<BigInteger> primes = new TreeSet<>();
	
	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onMessage(InstructionCommand.class, cmd->{
					if(cmd.getMsg().equals("start")) {
						for(int i = 0; i<20; i++) {
							ActorRef<WorkerBehavior.Command> child = getContext().spawn(WorkerBehavior.create(), "worker"+i);
							child.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
						}
					}
					return this;
				})
				.onMessage(ResultCommand.class, cmd->{
					primes.add(cmd.getPrime());
					System.out.println("received: " + primes.size() + " numbers");
					if(primes.size()==20) {
						primes.forEach(System.out::println);
					}
					return this;
				})
				.build();
	}

}
