package cz.kojotak.udemy.akka.streams.first;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

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
		
//		finite sources
//		Source.single(5);
//		Source.range(1, 10, 2); //ob jedno
//		Source.from( List.of(1,3,5,7) ); // z listu
		
//		infinite sources
//		Source.repeat(42);
//		Source.cycle( ()->List.of(1,2,3).iterator() ); //nekonecne se opakujici trojice
//		Source.fromIterator( ()-> Stream.iterate(0, i->i+1).iterator() ); //z java streamu
		
		Flow<Integer, String, NotUsed> flow = Flow.of(Integer.class) //of(Integer) - pro vynucenou typovou kontrolu
				.map( v -> "The next value is "+v );  //samotny obsah flow
		
//		operator
//		source.throttle(1, Duration.ofSeconds(3)); //davkuje se zpozdenim
//		source.take(2); //staci nam 2 a pak se zastavi - omezovac
		
		//string - co konzumuju
		Sink<String, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

//		alternativy pro sink
//		Sink.ignore(); //nic nedela, ignoruje co do nej prijde
		
		//vytvori graf
		RunnableGraph<NotUsed> graph = source.via(flow).to(sink);
		
		ActorSystem actor = ActorSystem.create(Behaviors.empty(), "actorSystem");
		graph.run(actor); //actor system po spusteni bude stale spusteny :/
		
//		alternativni zjednuseny zapis
//		sink.runWith(source, actor); //zkraceny zapis bez grafu
//		source.via(flow).runForeach(System.out::println, actor); //zkraceny zapis bez sinku
	}

}
