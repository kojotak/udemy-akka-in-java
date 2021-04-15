package cz.kojotak.udemy.akka.streams.backpressure;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.scaladsl.Behaviors;
import akka.stream.Attributes;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class BigPrimesBackPressure {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
		
		Source<Integer, NotUsed> source = Source.range(1, 100);
		
		Flow<Integer, BigInteger, NotUsed> toBigInteger = Flow.of(Integer.class)
				.map( i -> {
					BigInteger result = new BigInteger(3000, new Random());
					System.out.println(">>> on " + i);
					return result;
				});
		
		Flow<BigInteger, BigInteger, NotUsed> toProbablyPrime = Flow.of(BigInteger.class)
				.map( bi -> bi.nextProbablePrime());
		
		Flow<BigInteger, List<BigInteger>, NotUsed> toGroups = Flow.of(BigInteger.class)
				.grouped(10)
				.map( l -> {
					List<BigInteger> newList = new ArrayList<>(l);
					Collections.sort(newList);
					return newList;
				});
		
		Sink<List<BigInteger>, CompletionStage<Done>> sink = Sink.foreach(System.out::println);
		
		
		//backpressure -> rika upstream casti grafu (=vsechno pred), aby zpomalila
		
		CompletionStage<Done> result = source
			.via(toBigInteger) //hodne rychla cast, ktera u pomale casti zpusobi bottleneck
//			.buffer(16, OverflowStrategy.backpressure()) //zvysi defaultni buffer o dalsich 16 -> jednoduchy zpusob backpressure
			.async()
//			.buffer(...) //pokud bych dal buffer ZA async.boundary, tak to nebude mit zadny effekt
			//tohle je pomala cast, takze pred a po dam async (=async.boundary), abych to nebrzdil
			.via(toProbablyPrime.addAttributes(Attributes.inputBuffer(16, 32))) 
			.async()
			.via(toGroups)
			.toMat(sink, Keep.right())
			.run(ac);
		
		result.whenComplete((value, throwable)->{
			long end = System.currentTimeMillis();
			System.out.println("finished in " + (end-start) + " ms");
			ac.terminate();
		});
	}

}
