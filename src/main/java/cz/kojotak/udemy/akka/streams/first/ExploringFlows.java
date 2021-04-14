package cz.kojotak.udemy.akka.streams.first;

import java.util.ArrayList;
import java.util.Collections;
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
		Flow<Integer,Integer, NotUsed> groupFlow = Flow.of(Integer.class)
				.grouped(3) //obracene k mapConcat - seskupi tri elementy do jednoho (listu)
		
//		jedna z variant jak pouzit of(List<Integer>.class) - coz nejde
//		Flow<IntegerList, Integer, NotUsed> ungroupFlow = Flow.of(IntegerList.class)
//				.mapConcat( value-> {
//					return value;
//				});
		
//		taky neni dobry - nepujde namapovat na presny typ v sinku
//		Flow<List, Integer, NotUsed> ungroupFlow = Flow.of(List.class)
//				.mapConcat( value-> {
//					return value;
//				});
		
//		treti zpusob jak vyresit of(List<Integer>.class)
//		nemusim definovat nove flow, ale muzu se hned napojit metodou na predchozi flow
				.map(value->{
				//vytvorim mutable copii, aby list dal pozpatku
				List<Integer> newList = new ArrayList<>(value);
				Collections.sort(newList, Collections.reverseOrder());
				return newList;
			})
			.mapConcat( value-> value);
		
		
		Flow<Integer, Integer, NotUsed> chainedFlow = filterFlow.via(mapConcatFlow);
		
		Sink<Integer, CompletionStage<Done>> printSink = Sink.foreach(System.out::println);
		
		numbers.via(chainedFlow).via(groupFlow).to(printSink).run(ac);
	}
	

}
