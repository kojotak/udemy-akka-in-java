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
		
		source.via(greaterThan200).via(evenNumber).to(sink).run(ac);
	}

}
