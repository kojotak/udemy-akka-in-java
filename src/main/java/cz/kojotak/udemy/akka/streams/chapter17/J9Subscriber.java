package cz.kojotak.udemy.akka.streams.chapter17;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

//do not use this
public class J9Subscriber implements Subscriber<String>{

	Subscription subscription;
	
	@Override
	public void onSubscribe(Subscription subscription) {
		System.out.println("j9 onSubscribe");
		this.subscription = subscription;
		subscription.request(1);
	}

	@Override
	public void onNext(String item) {
		System.out.println("j9 onNext " + item);
		subscription.request(1);
	}

	@Override
	public void onError(Throwable throwable) {
		System.out.println("j9 onError " + throwable);
		subscription.cancel();
	}

	@Override
	public void onComplete() {
		System.out.println("j9 onComplete");
		subscription.cancel();
	}

}
