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
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class BigPrimesBackPreassure {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
		
		Source<Integer, NotUsed> source = Source.range(1, 10);
		
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
		
		CompletionStage<Done> result = source
			.via(toBigInteger)
			.async()
			.via(toProbablyPrime) //tohle je pomala cast, takze pred a po dam async, abych to nebrzdil
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
