package cz.kojotak.udemy.akka.streams.exercise2;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.ClosedShape;
import akka.stream.FlowShape;
import akka.stream.SinkShape;
import akka.stream.SourceShape;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Sink;

public class Main {

    public static void main(String[] args) {
    	final int MAX = 8;
    	
        Map<Integer, VehiclePositionMessage> vehicleTrackingMap = new HashMap<>();
        for (int i = 1; i <=8; i++) {
            vehicleTrackingMap.put(i, new VehiclePositionMessage(1, new Date(), 0,0));
        }

        //source - repeat some value every 10 seconds.
        Source<Integer, NotUsed> source = Source.repeat(42).throttle(1, Duration.ofSeconds(1));

        //flow 1 - transform into the ids of each van (ie 1..8) with mapConcat
        Flow<Integer, Integer, NotUsed> vehicles = Flow.of(Integer.class)
        		.mapConcat( i-> IntStream.range(1, MAX).boxed().collect(Collectors.toList()) );

        //flow 2 - get position for each van as a VPMs with a call to the lookup method (create a new instance of
        //utility functions each time). Note that this process isn't instant so should be run in parallel.
        Flow<Integer, VehiclePositionMessage, NotUsed> positions = Flow.of(Integer.class)
        		.mapAsyncUnordered(MAX, idx->{
        			System.out.println("preparing " + idx);
        			 CompletableFuture<VehiclePositionMessage> future = new CompletableFuture<>();
        			 UtilityFunctions util = new UtilityFunctions();//create instance in each completable future, do not share
        			 future.completeAsync( ()-> util.getVehiclePosition(idx));
        			 return future;
        		});
        
        //flow 3 - use previous position from the map to calculate the current speed of each vehicle. Replace the
        // position in the map with the newest position and pass the current speed downstream
        Flow<VehiclePositionMessage, VehicleSpeed, NotUsed> speeds = Flow.of(VehiclePositionMessage.class)
        		.map( pos -> {
        			UtilityFunctions util = new UtilityFunctions();
        			VehiclePositionMessage previous = vehicleTrackingMap.get(pos.getVehicleId());
        			VehicleSpeed current = util.calculateSpeed(previous, pos);
        			vehicleTrackingMap.put(pos.getVehicleId(), pos);
        			System.out.println("vehicle " + pos.getVehicleId() + " traveling at " + current);
        			return current;
        		});
        
        
        //flow 4 - filter to only keep those values with a speed > 95
        Flow<VehicleSpeed, VehicleSpeed, NotUsed> speedLimit = Flow.of(VehicleSpeed.class)
        		.filter( vehicle -> vehicle.getSpeed() > 95);
        
        //sink - as soon as 1 value is received return it as a materialized value, and terminate the stream
        Sink<VehicleSpeed, CompletionStage<VehicleSpeed>> sink = Sink.head();
        
        ActorSystem actors = ActorSystem.create(Behaviors.empty(), "actorSystem");
        
//        CompletionStage<VehicleSpeed> result = source
//        	.via(vehicles)
//        	.async()
//        	.via(positions) //slow
//        	.async()
//        	.via(speeds)
//        	.via(speedLimit)
//        	.toMat(sink, Keep.right())
//        	.run(actors);
        
//		DSL API
		RunnableGraph<CompletionStage<VehicleSpeed>> graph =RunnableGraph.fromGraph(
				GraphDSL.create(
						sink,
						(builder, out) -> {
							SourceShape<Integer> sourceShape = builder
									 .add(Source.repeat(42).throttle(1, Duration.ofSeconds(1)));
							
							FlowShape<Integer,Integer> vehicleIds = builder.add(vehicles);
							FlowShape<Integer, VehiclePositionMessage> vehiclePositions = builder.add(positions);
							FlowShape<VehiclePositionMessage, VehicleSpeed> vehicleSpeeds = builder.add(speeds);
							FlowShape<VehicleSpeed, VehicleSpeed> vehicleFilter = builder.add(speedLimit);
							
							//can do...
//							SinkShape<VehicleSpeed> vehicleSink = builder.add(sink);
							//...but we don't need to - out is our shinkshape

							builder.from(sourceShape)
								.via(vehicleIds)
								.via(vehiclePositions)
								.via(vehicleSpeeds)
								.via(vehicleFilter)
								.to(out);
							
							return ClosedShape.getInstance();
						}));
		
		CompletionStage<VehicleSpeed> result = graph.run(actors);
         
        result.whenComplete( (value, throwable)->{
        	if(throwable != null) {
        		System.err.println("bad day " + throwable);
        	} else {
        		System.out.println("first vehicle finished: " + value.getSpeed());
        	}
        });
    }

}
 