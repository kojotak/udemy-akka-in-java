package cz.kojotak.udemy.akka.bigprimes;

public class CurrentStatus implements Runnable {

	private Results results;
	
	public CurrentStatus(Results results) {
		super();
		this.results = results;
	}

	@Override
	public void run() {
		while(results.getSize() < 20) {
			System.out.println("got " + results.getSize() + " so far");
			results.print();
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException ignore) {
			}
		}
	}

}
