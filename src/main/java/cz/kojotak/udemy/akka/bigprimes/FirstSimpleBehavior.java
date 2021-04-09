package cz.kojotak.udemy.akka.bigprimes;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FirstSimpleBehavior extends AbstractBehavior<String> {
	
	//privatni, protoze nemusime mit vzdy pristup k ActorContextu
	//pro vyvoreni slouzi #create()
	private FirstSimpleBehavior(ActorContext<String> context) {
		super(context);
	}
	
	public static Behavior<String> create(){
		//tohle ve skutecnosti znamena ctx -> return new FirstSimpleBehavior(ctx)
		return Behaviors.setup(FirstSimpleBehavior::new);
	}

	@Override
	public Receive<String> createReceive() {
		return newReceiveBuilder()
				.onMessageEquals("say hello", ()->{
					System.out.println("hi there ");
					return this;
				})
				.onMessageEquals("who are you", ()->{
					System.out.println("my path is: " + getContext().getSelf().path());
					return this;
				})
				.onMessageEquals("create a child", ()->{
					ActorRef<String> secondActor = getContext().spawn(FirstSimpleBehavior.create(), "secondActor");
					secondActor.tell("who are you");
					return this;
				})
				.onAnyMessage(msg->{
					System.out.println("received: " + msg);
					return this; //musime neco vratit
				})
				.build();
	}

}
