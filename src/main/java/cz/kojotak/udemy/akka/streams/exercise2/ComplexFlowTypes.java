package cz.kojotak.udemy.akka.streams.exercise2;

import java.util.concurrent.CompletionStage;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.ClosedShape;
import akka.stream.FlowShape;
import akka.stream.SourceShape;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Broadcast;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.Sink;

public class ComplexFlowTypes {

	public static void main(String...args) {
		
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
		
		Source<Integer,NotUsed> source = Source.range(1, 10);
		Flow<Integer, Integer, NotUsed> flowMultiply2 = Flow.of(Integer.class)
				.map( x->{
					System.out.println("Flow *2 is processing: " + x);
					return x * 2;
				});
		Flow<Integer, Integer, NotUsed> flowAdd1 = Flow.of(Integer.class)
				.map( x->{
					System.out.println("Flow +1 is processing: " + x);
					return x + 1;
				});
		Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);
		RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
				GraphDSL.create(sink, (builder, out)->{
					//vytvoreni grafu
					SourceShape<Integer> sourceShape = builder.add(source);
					FlowShape<Integer,Integer> flow1shape = builder.add(flowMultiply2);
					FlowShape<Integer,Integer> flow2shape = builder.add(flowAdd1);
					UniformFanOutShape<Integer, Integer> broadcast //rozdeli flow na nekolik streamu
									  = builder.add(Broadcast.create(2));
					UniformFanInShape<Integer, Integer> merge //slouci dve rozdelene flow dohromady
									= builder.add(Merge.create(2));
					
					//ted vse sloucit dohromady
//					builder.from(sourceShape).viaFanOut(broadcast);
//					builder.from(broadcast.out(0)).via(flow1shape);
//					builder.from(broadcast.out(1)).via(flow2shape);
//					builder.from(flow1shape).toInlet(merge.in(0));
//					builder.from(flow2shape).toInlet(merge.in(1));
//					builder.from(merge).to(out);
					
					//muzu zjednodusit na
					builder.from(sourceShape).viaFanOut(broadcast).via(flow1shape);
					builder.from(broadcast).via(flow2shape);
					builder.from(flow1shape).viaFanIn(merge).to(out);
					builder.from(flow2shape).viaFanIn(merge);
					
					return ClosedShape.getInstance();
				})
				);

		graph.run(ac);
	}
}
