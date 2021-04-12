package cz.kojotak.udemy.akka.blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.PoolRouter;
import cz.kojotak.udemy.akka.blockchain.ManagerBehavior.Command;

//need router to limit how many ManagerBehaviors are running
//using same message type as managers!
public class MiningSystemBehavior extends AbstractBehavior<ManagerBehavior.Command> {

	private PoolRouter<ManagerBehavior.Command> managerPoolRouter;
	private ActorRef<ManagerBehavior.Command> managers;
	
	private MiningSystemBehavior(ActorContext<Command> context) {
		super(context);
		managerPoolRouter = Routers.pool( 3, 
				Behaviors.supervise(ManagerBehavior.create())
					.onFailure(SupervisorStrategy.restart())
				);
		managers = getContext().spawn(managerPoolRouter, "managerPool");
	}
	
	public static Behavior<ManagerBehavior.Command> create(){
		return Behaviors.setup(MiningSystemBehavior::new);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
				.onAnyMessage(msg->{
					managers.tell(msg);
					return Behaviors.same();
				})
				.build();
	}

}
