package cz.kojotak.udemy.akka.actors.bigprimes;

import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

public class AkkaBigPrimes {

	public static void main(String[] args) {
		//firstActorSystem();
		
		ActorSystem<ManagerBehavior.Command> actorManager = ActorSystem.create(ManagerBehavior.create(), "manager");
		
		//this line will be replaced, because we want to use Ask pattern to retreive the result from Akkka
		//actorManager.tell(new ManagerBehavior.InstructionCommand("start"));

		CompletionStage<SortedSet<BigInteger>> result = AskPattern.ask(actorManager, 
				(me) ->new ManagerBehavior.InstructionCommand("start", me), 
				Duration.ofSeconds(60), 
				actorManager.scheduler());
		
		result.whenComplete(
				(reply,failure)->{
					if(reply!=null) {
						reply.forEach(System.out::println);
					} else {
						System.out.println("System didn't respond in time");
					}
					actorManager.terminate();
				});
	}

	private static void firstActorSystem() {
		ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.create(), "FirstActorSystem");
		actorSystem.tell("say hello");
		actorSystem.tell("who are you");
		actorSystem.tell("create a child");
		actorSystem.tell("hi there");
	}

}
