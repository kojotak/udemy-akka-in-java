package cz.kojotak.udemy.akka.streams.chapter18.blockchain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.stream.Attributes;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Sink;
import cz.kojotak.udemy.akka.streams.chapter18.model.Block;
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
		Flow<Transaction,Block,NotUsed> blockBuilder = Flow.of(Transaction.class).map(trans->{
			List<Transaction> list = new ArrayList<>();
			list.add(trans);
			Block block = new Block("0", list);
			System.out.println("created block: " + block);
			return block;
		}).conflate( (block1, block2)->{
			block1.addTransactionToList(block2.getFirstTransaction());
			System.out.println("conflated block: " + block1);
			return block1;
		});
		Flow<Block, Block, NotUsed> miningProcess = Flow.of(Block.class).map(block->{
			System.out.println("starting to mine "  + block.toString() );
			Thread.sleep(10000);
			System.out.println("finished to mine "  + block.toString() );
			return block;
		});
		transactionsSource
			.via(blockBuilder)
			.via(miningProcess.async().addAttributes(Attributes.inputBuffer(1, 1)))
			.to(Sink.foreach(System.out::println))
			.run(ac);
		
	}

}
