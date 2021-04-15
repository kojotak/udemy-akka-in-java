package cz.kojotak.udemy.akka.streams.logging;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.scaladsl.Behaviors;
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
public class ExploringLogging {

	public static void main(String...args) {
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
		Source<Integer,NotUsed> source = Source.range(1,10);
		Flow<Integer,Integer,NotUsed> flow = Flow.of(Integer.class)
				.log("flow input")
				.map( x -> x*2)
				.log("flow output");//old style
		
		Flow<Integer,Integer,NotUsed> loggedFlow = Flow.of(Integer.class)
				.map( x -> {
					ac.log().info("in flow " + x);//new style
					return x*2;
				});
		
		Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);
		source.via(loggedFlow).to(sink).run(ac);
	}
}
