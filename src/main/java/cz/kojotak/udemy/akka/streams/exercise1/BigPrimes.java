package cz.kojotak.udemy.akka.streams.exercise1;

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
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class BigPrimes {

	public static void main(String[] args) {
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
		
		Source<Integer, NotUsed> source = Source.range(1, 10);
		
		Flow<Integer, BigInteger, NotUsed> toBigInteger = Flow.of(Integer.class)
				.map( i -> new BigInteger(2000, new Random()) );
		
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
		
		source.via(toBigInteger).via(toProbablyPrime).via(toGroups).to(sink).run(ac);
		
	}

}
