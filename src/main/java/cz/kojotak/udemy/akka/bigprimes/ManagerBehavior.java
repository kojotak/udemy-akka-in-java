package cz.kojotak.udemy.akka.bigprimes;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;

public class ManagerBehavior extends AbstractBehavior<String>{

	private ManagerBehavior(ActorContext<String> context) {
		super(context);
	}
	
	public static Behavior<String> create(){
		return Behaviors.setup(ManagerBehavior::new);
	}

	@Override
	public Receive<String> createReceive() {
		return newReceiveBuilder()
				.onMessageEquals("start", ()->{
					for(int i = 0; i<20; i++) {
						ActorRef<WorkerBehavior.Command> child = getContext().spawn(WorkerBehavior.create(), "worker"+i);
						child.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
					}
					return this;
				})
				.build();
	}

}
