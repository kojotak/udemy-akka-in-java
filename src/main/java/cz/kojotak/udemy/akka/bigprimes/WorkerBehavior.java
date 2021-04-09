package cz.kojotak.udemy.akka.bigprimes;

import java.math.BigInteger;
import java.util.Random;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;

public class WorkerBehavior extends AbstractBehavior<String> {

	private WorkerBehavior(ActorContext<String> context) {
		super(context);
	}

	public static Behavior<String> create(){
		return Behaviors.setup(WorkerBehavior::new);
	}
	
	@Override
	public Receive<String> createReceive() {
		return newReceiveBuilder()
				.onMessageEquals("start", ()->{
					BigInteger bi = new BigInteger(2000, new Random());
					System.out.println(getContext().getSelf().path() + ", next: " + bi.nextProbablePrime());
					return this;
				})
				.build();
	}

	
}
