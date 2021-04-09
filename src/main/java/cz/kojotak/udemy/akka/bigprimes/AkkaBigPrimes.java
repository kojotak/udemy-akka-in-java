package cz.kojotak.udemy.akka.bigprimes;

import akka.actor.typed.ActorSystem;

public class AkkaBigPrimes {

	public static void main(String[] args) {
		ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.create(), "FirstActorSystem");
		actorSystem.tell("hi there");
	}

}
