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

public class CombiningFlows {

	public static void main(String[] args) {
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
	
		Source<String, NotUsed> sentenceSource = Source.from(List.of("The sky is blue",
				"The moon is only seen at night",
				"The planets orbits the sun"));
		
		Flow<String, Integer, NotUsed> howManyWords = Flow.of(String.class)
				.map( s -> s.split(" ").length );
		
		Source<Integer, NotUsed> howManyWordsSource = sentenceSource.via(howManyWords);
		
//		alternative
//		Source<Integer, NotUsed> sentenceSourceAlternative = Source.from(List.of("The sky is blue",
//				"The moon is only seen at night",
//				"The planets orbits the sun"))
//				.map( s -> s.split(" ").length );
		
		Sink<Integer, CompletionStage<Done>> sink = Sink.ignore();
		
		Sink<String, NotUsed> combinedSink = howManyWords.to(sink);
	}
	

}
