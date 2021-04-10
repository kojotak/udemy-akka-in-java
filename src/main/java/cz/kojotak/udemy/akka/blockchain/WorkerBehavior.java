package cz.kojotak.udemy.akka.blockchain;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import cz.kojotak.udemy.akka.blockchain.model.Block;
import cz.kojotak.udemy.akka.blockchain.model.HashResult;
import cz.kojotak.udemy.akka.blockchain.utils.BlockChainUtils;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {
	
	public static class Command {
		private Block block;
		private int startNonce;
		private int difficulty;
		
		public Command(Block block, int startNonce, int difficulty) {
			this.block = block;
			this.startNonce = startNonce;
			this.difficulty = difficulty;
		}
		
		public Block getBlock() {
			return block;
		}
		public int getStartNonce() {
			return startNonce;
		}
		public int getDifficulty() {
			return difficulty;
		}
		
	}

	private WorkerBehavior(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create() {
		return Behaviors.setup(WorkerBehavior::new);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onAnyMessage(msg->{
					String hash = new String(new char[msg.getDifficulty()]).replace("\0", "X");
					String target = new String(new char[msg.getDifficulty()]).replace("\0", "0");
							
					int nonce = msg.getStartNonce();
					while (!hash.substring(0,msg.getDifficulty()).equals(target) && nonce < msg.getStartNonce() + 1000) {
						nonce++;
						String dataToEncode = msg.getBlock().getPreviousHash() 
								+ Long.toString(msg.getBlock().getTransaction().getTimestamp()) 
								+ Integer.toString(nonce) 
								+ msg.getBlock().getTransaction();
						hash = BlockChainUtils.calculateHash(dataToEncode);
					}
					if (hash.substring(0,msg.getDifficulty()).equals(target)) {
						HashResult hashResult = new HashResult();
						hashResult.foundAHash(hash, nonce);
						//return hashResult; //TODO send the hash result to the controller
						return Behaviors.same();
					} else {
						return Behaviors.same();
					}
				})
				.build();
	}

}
