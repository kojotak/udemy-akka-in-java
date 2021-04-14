package cz.kojotak.udemy.akka.streams.first;

import java.util.List;
import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.scaladsl.Behaviors;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;

public class ExploringFlows {

	public static void main(String[] args) {
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
		Source<Integer, NotUsed> numbers = Source.range(1, 200);
		Flow<Integer,Integer, NotUsed> filterFlow = Flow.of(Integer.class)
				.filter( value-> 
					value % 17 == 0
				);
		Flow<Integer, Integer, NotUsed> mapConcatFlow = Flow.of(Integer.class)
				.mapConcat( value -> { //trochu jako flatmap - nahradi element seznamem
					List<Integer> result = List.of(value, value +1, value +2);
					return result;
				});
		Flow<Integer, List<Integer>, NotUsed> groupFlow = Flow.of(Integer.class)
				.grouped(3); //obracene k mapConcat - seskupi tri elementy do jednoho (listu)
		Sink<List<Integer>, CompletionStage<Done>> printSink = Sink.foreach(System.out::println);
		
		numbers.via(filterFlow).via(mapConcatFlow).via(groupFlow).to(printSink).run(ac);
	}
	

}
