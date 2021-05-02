package cz.kojotak.udemy.akka.streams.chapter18.blockchain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.stream.Attributes;
import akka.stream.FanInShape2;
import akka.stream.FlowShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.ZipWith;
import akka.stream.javadsl.GraphDSL;
import akka.stream.typed.javadsl.ActorFlow;
import akka.stream.javadsl.Sink;
import cz.kojotak.udemy.akka.actors.blockchain.model.Transaction;
import cz.kojotak.udemy.akka.streams.chapter18.model.Block;
import cz.kojotak.udemy.akka.streams.chapter18.model.BlockChain;
import cz.kojotak.udemy.akka.streams.chapter18.model.HashResult;

public class Streaming {
	
	private static int transId = -1;
	private static Random random = new Random();

	public static void main(String[] args) {
		BlockChain blockchain = new BlockChain();
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
			Block block = new Block(blockchain.getLastHash(), list);
			System.out.println("created block: " + block);
			return block;
		}).conflate( (block1, block2)->{
			block1.addTransactionToList(block2.getFirstTransaction());
			System.out.println("conflated block: " + block1);
			return block1;
		});
		Flow<Block, HashResult, NotUsed> miningProcess = ActorFlow.ask(ac, Duration.ofSeconds(30), (block,self)-> new ManagerBehavior.MineBlockCommand(block, self, 5));
		Flow<Block, Block, NotUsed> miningFlow = Flow.fromGraph(
				GraphDSL.create(builder->{
					UniformFanOutShape<Block,Block> broadcast = builder.add(Broadcast.create(2));
					FlowShape<Block,HashResult> mineBlock = builder.add(miningProcess);
					FanInShape2<Block, HashResult, Block> receivedHashResult = builder.add(ZipWith.create((block,hashResult)->{
						block.setHash(hashResult.getHash());
						block.setNonce(hashResult.getNonce());
						return block;
					}));
					builder.from(broadcast).toInlet(receivedHashResult.in0());
					builder.from(broadcast).via(mineBlock).toInlet(receivedHashResult.in1());
					return FlowShape.of(broadcast.in(), receivedHashResult.out());
				})
				);
		Sink<Block,CompletionStage<Done>> sink = Sink.foreach(block->{
			blockchain.addBlock(block);
			blockchain.printAndValidate();
		});
		transactionsSource
			.via(blockBuilder)
			.via(miningFlow.async().addAttributes(Attributes.inputBuffer(1, 1)))
			.to(sink)
			.run(ac);
		
	}

}
