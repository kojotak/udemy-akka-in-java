package cz.kojotak.udemy.akka.bigprimes;

import akka.actor.typed.ActorSystem;

public class AkkaBigPrimes {

	public static void main(String[] args) {
		//firstActorSystem();
		
		ActorSystem<String> actorManager = ActorSystem.create(ManagerBehavior.create(), "manager");
		actorManager.tell("start");
	}

	private static void firstActorSystem() {
		ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.create(), "FirstActorSystem");
		actorSystem.tell("say hello");
		actorSystem.tell("who are you");
		actorSystem.tell("create a child");
		actorSystem.tell("hi there");
	}

}
