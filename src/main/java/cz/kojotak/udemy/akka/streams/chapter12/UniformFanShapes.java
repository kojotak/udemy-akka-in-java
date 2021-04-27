package cz.kojotak.udemy.akka.streams.chapter12;

import java.time.Duration;
import java.util.Random;
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
import akka.stream.javadsl.Partition;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class UniformFanShapes {

	public static void main(String[] args) {
		ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");

		RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
				GraphDSL.create(
						Sink.foreach(System.out::println),(builder,out)->{
							
							SourceShape<Integer> sourceShape = builder.add(
									Source.repeat(1).throttle(1, Duration.ofSeconds(1))
									.map(x->{
										Random r = new Random();
										return 1 + r.nextInt(10);
									})
									);
							
							FlowShape<Integer, Integer> flowShape = builder.add(
									Flow.of(Integer.class).map(x->{
										System.out.println("Flowing " + x);
										return x;
									})
									); 
							
							UniformFanOutShape<Integer, Integer> partioon = builder.add(
									Partition.create(2, x-> (x==1 || x==10) ? 0 : 1 )
									);
							
							UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(2));
							
							builder.from(sourceShape).viaFanOut(partioon);
							builder.from(partioon.out(0)).via(flowShape).viaFanIn(merge).to(out);
							builder.from(partioon.out(1)).viaFanIn(merge);
							
							return ClosedShape.getInstance();
						}));

			graph.run(ac);
	}

}
