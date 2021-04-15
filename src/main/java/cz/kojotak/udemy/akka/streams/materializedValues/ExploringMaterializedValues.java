package cz.kojotak.udemy.akka.streams.materializedValues;

import akka.NotUsed;
import akka.stream.javadsl.Source;
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

public class ExploringMaterializedValues {

	public static void main(String[] args) {
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
		
		Random random = new Random();
		
		Source<Integer, NotUsed> source = Source.range(1, 100).map( n->random.nextInt(1000)+1);
		
		Flow<Integer, Integer, NotUsed> greaterThan200 = Flow.of(Integer.class)
				.filter(x -> x>200);

		Flow<Integer, Integer, NotUsed> evenNumber = Flow.of(Integer.class)
				.filter( x -> x%2==0);
		
		Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);
		
		//budeme delat pocitadlo jako materialized value
		Sink<Integer, CompletionStage<Integer>> sinkWithCounter = Sink
				.fold(0, (counter, value)->{
					System.out.println(value);
					return ++counter;
				});
		
		Sink<Integer, CompletionStage<Integer>> sinkWithSum = Sink
				.reduce( (first, second)->{
					System.out.println(second);
					return first + second;
				});
		
		//defaultni typ materialized value je ze source, tj. NotUsed
		CompletionStage<Integer> result = source
				.via(greaterThan200)
				.via(evenNumber)
				.toMat(sinkWithCounter, //musim pouzit toMat, abych zistal spravny typ materializovaneho value 
						Keep.right())   //Keep.right() zmeni typ toho, co je materialized value
				.run(ac);
		
		result.whenComplete(
				(value, throwable)->{
					if(throwable==null) {
						System.out.println("Materialized value is " + value);
					}else {
						System.out.println("Bad day " + throwable);
					}
//					ac.terminate();
				}
				);
		CompletionStage<Done> result2 = source.toMat(sink, Keep.right()).run(ac);
		result2.whenComplete( (value,throwable)->{
//			ac.terminate();
		});
	}

}
