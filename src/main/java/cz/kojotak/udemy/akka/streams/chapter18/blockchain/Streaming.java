package cz.kojotak.udemy.akka.streams.chapter18.blockchain;

import java.time.Duration;
import java.util.Random;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.Source;
import cz.kojotak.udemy.akka.streams.chapter18.model.Transaction;

public class Streaming {
	
	private static int transId = -1;
	private static Random random = new Random();

	public static void main(String[] args) {
		ActorSystem<ManagerBehavior.Command> ac = ActorSystem.create(MiningSystemBehavior.create(), "BlockChainMiner"); 
		Source<Transaction, NotUsed> transactionsSource = Source.repeat(1).throttle(1, Duration.ofSeconds(1))
				.map(x->{
					transId++;
					System.out.println("received transaction " + transId);
					return new Transaction(transId, System.currentTimeMillis(), random.nextInt(1000), random.nextDouble() * 100);
				});
	}

}