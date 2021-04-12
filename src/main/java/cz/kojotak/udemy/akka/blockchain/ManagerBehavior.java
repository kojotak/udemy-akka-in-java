package cz.kojotak.udemy.akka.blockchain;

import java.io.Serializable;
import java.util.Objects;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import cz.kojotak.udemy.akka.blockchain.model.Block;
import cz.kojotak.udemy.akka.blockchain.model.HashResult;
import akka.actor.typed.javadsl.Behaviors;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

	public interface Command extends Serializable {}

	public static class MineBlockCommand implements Command {
		private static final long serialVersionUID = 1L;
		private final Block block;
		private final ActorRef<HashResult> sender;
		private final int difficulty;
		public MineBlockCommand(Block block, ActorRef<HashResult> sender, int difficulty) {
			super();
			this.block = block;
			this.sender = sender;
			this.difficulty = difficulty;
		}
		public Block getBlock() {
			return block;
		}
		public ActorRef<HashResult> getSender() {
			return sender;
		}
		public int getDifficulty() {
			return difficulty;
		}
	}
	
	public static class HashResultCommand implements Command {
		private static final long serialVersionUID = 1L;
		private final HashResult hashResult;
		public HashResultCommand(HashResult hashResult) {
			super();
			this.hashResult = hashResult;
		}
		public HashResult getHashResult() {
			return hashResult;
		}
		@Override
		public int hashCode() {
			return Objects.hash(hashResult);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HashResultCommand other = (HashResultCommand) obj;
			return Objects.equals(hashResult, other.hashResult);
		}
	}
	
	private ManagerBehavior (ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command>  create(){
		return Behaviors.setup(ManagerBehavior::new);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onSignal(Terminated.class, handler->{
					startNextWorker();
					return Behaviors.same();
				})
				.onMessage(MineBlockCommand.class, msg->{
					this.sender = msg.getSender();
					this.block = msg.getBlock();
					this.difficulty = msg.getDifficulty();
					for(int i=0; i<10; i++) {
						startNextWorker();
					}
					return Behaviors.same();
				})
				.build();
	}
	
	private ActorRef<HashResult> sender;
	private Block block;
	private int difficulty;
	private int currentNonce = 0;
	
	private void startNextWorker() {
		System.out.println("about to start mining with nonces starting at " + currentNonce);

		Behavior<WorkerBehavior.Command> workerBehavior = Behaviors.supervise(WorkerBehavior.create())
				.onFailure(SupervisorStrategy.restart());
		
		ActorRef<WorkerBehavior.Command> worker = getContext().spawn(workerBehavior, "worker"+currentNonce);
		getContext().watch(worker);
		worker.tell(new WorkerBehavior.Command(block, currentNonce*1000, difficulty, getContext().getSelf()));
		currentNonce++;
	}
}
