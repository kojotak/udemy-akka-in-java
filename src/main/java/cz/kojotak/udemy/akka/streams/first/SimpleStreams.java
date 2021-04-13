package cz.kojotak.udemy.akka.streams.first;

import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

//create integer -> convert to string -> println
public class SimpleStreams {

	public static void main(String[] args) {
		//integer - co produkuju, NotUsed - materialized value
		Source<Integer, NotUsed> source = Source.range(1, 10);
		
		Flow<Integer, String, NotUsed> flow = Flow.of(Integer.class) //of(Integer) - pro vynucenou typovou kontrolu
				.map( v -> "The next value is "+v );  //samotny obsah flow
		
		//string - co konzumuju
		Sink<String, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

		//vytvori graf
		RunnableGraph<NotUsed> graph = source.via(flow).to(sink);
		
		ActorSystem actor = ActorSystem.create(Behaviors.empty(), "actorSystem");
		graph.run(actor);
		
		//actor system po spusteni bude stale spusteny :/
	}

}
