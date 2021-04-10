package cz.kojotak.udemy.akka.racing;

import akka.actor.typed.ActorSystem;

public class AkkaMain {

	public static void main(String[] args) {
		ActorSystem<RaceController.Command> raceController = ActorSystem.create(RaceController.create(), "RaceSimulation");
		raceController.tell(new RaceController.StartCommand());
	}

}
