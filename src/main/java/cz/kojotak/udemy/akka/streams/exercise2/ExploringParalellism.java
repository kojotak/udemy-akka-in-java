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
import akka.stream.javadsl.Balance;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class ExploringParalellism {

	public static void main(String[] args) {
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");
		Source<Integer,NotUsed> source = Source.range(1, 10);
		Flow<Integer, Integer, NotUsed> flow = Flow.of(Integer.class)
				.map( x->{
					System.out.println("flow with: " + x + " started");
					Thread.sleep(3000);
					System.out.println("flow with: " + x + " finished");
					return x;
				});
		Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);
		long start = System.currentTimeMillis();
		RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
				GraphDSL.create(sink, (builder, out)->{
					SourceShape<Integer> sourceShape = builder.add(source);
					UniformFanOutShape<Integer, Integer> balancer = builder.add(Balance.create(4, true));//musi byt true
					UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(4));
					builder.from(sourceShape).viaFanOut(balancer);
					for(int i=1; i<=4;i++) {
						builder.from(balancer).via( builder.add(flow.async()) ).toFanIn(merge);
					}
					builder.from(merge).to(out);
					return ClosedShape.getInstance();
				}));
		CompletionStage<Done> result = graph.run(ac);
		result.whenComplete((value,throwable)->{
			System.out.println("time taken " + (System.currentTimeMillis() - start));
			ac.terminate();
		});
	}

}
