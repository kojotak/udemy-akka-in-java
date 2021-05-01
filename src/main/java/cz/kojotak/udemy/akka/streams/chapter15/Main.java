package cz.kojotak.udemy.akka.streams.chapter15;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.FanInShape2;
import akka.stream.FlowShape;
import akka.stream.Graph;
import akka.stream.SinkShape;
import akka.stream.SourceShape;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.ZipWith;
import akka.stream.typed.javadsl.ActorFlow;
import akka.stream.typed.javadsl.ActorSink;
import cz.kojotak.udemy.akka.streams.chapter13.Account;
import cz.kojotak.udemy.akka.streams.chapter13.Transaction;
import cz.kojotak.udemy.akka.streams.chapter13.Transfer;
import cz.kojotak.udemy.akka.streams.chapter15.AccountManager.AddTransactionCommand;
import cz.kojotak.udemy.akka.streams.chapter15.AccountManager.AddTransactionResponse;
public class Main {



    public static void main(String[] args) {
        //source to generate 1 transaction every second
        Source<Integer, NotUsed> source = Source.repeat(1).throttle(1, Duration.ofSeconds(3));

        //flow to create a random transfer
        Flow<Integer, Transfer, NotUsed> generateTransfer = Flow.of(Integer.class).map (x -> {
            Random r = new Random();
            int accountFrom = r.nextInt(9) + 1;
            int accountTo;
            do {
                 accountTo = r.nextInt(9) + 1;
            } while (accountTo == accountFrom);

            BigDecimal amount = new BigDecimal(r.nextInt(100000)).divide(new BigDecimal(100));
            Date date = new Date();

            Transaction from = new Transaction(accountFrom, BigDecimal.ZERO.subtract(amount), date);
            Transaction to = new Transaction(accountTo, amount, date);
            return new Transfer(from,to);
        });

        Flow<Transfer, Transaction, NotUsed> getTransactionFromTransfer = Flow.of(Transfer.class)
        		.mapConcat( transfer -> List.of(transfer.getFrom(), transfer.getTo()));
        
        Source<Integer,NotUsed> transactionIdSource = Source.fromIterator(()->	Stream.iterate(1, i->i++).iterator() );
        
        Sink<Transfer, CompletionStage<Done>> logger = Sink.foreach( t->{
        	System.out.println("transfer from " + t.getFrom() + " to " + t.getTo());
        });
        
        Graph<SourceShape<Transaction>, NotUsed> sourcePartialGraph = GraphDSL.create(builder->{
        	FanInShape2<Transaction, Integer, Transaction> assignTransactionId =
					builder.add(ZipWith.create(
							(trans,id)->{
								trans.setUniqueId(id);
								return trans;
							}));
        	
        	builder.from(builder.add(source))
					.via(builder.add(generateTransfer.alsoTo(logger))) //tady je pridany navic logger jako dalsi sink
					.via(builder.add(getTransactionFromTransfer))
					.toInlet(assignTransactionId.in0()
				);
	
			builder.from(builder.add(transactionIdSource))
					.toInlet(assignTransactionId.in1());
		
			return SourceShape.of(assignTransactionId.out());	
        });
        
        ActorSystem<AccountManager.AccountManagerCommand> accountManager = ActorSystem.create(AccountManager.create(), "accountManager");        

        Flow<Transaction, AccountManager.AddTransactionResponse, NotUsed> attemptToApplyTransaction = ActorFlow.ask(
        		accountManager, Duration.ofSeconds(10), (trans,self)->{
        			return new AddTransactionCommand(trans, self);
        		});
        
        Sink<AddTransactionResponse, CompletionStage<Done>> rejectedTransactions = Sink.foreach( resp ->{
        	System.out.println("REJECTED trans: " + resp.getTransaction());
        });
        
        Flow<AccountManager.AddTransactionResponse, AccountManager.AccountManagerCommand, NotUsed> receiveResult =
        		Flow.of(AccountManager.AddTransactionResponse.class).map( result->{
        			System.out.println("Receive result: " + result.getTransaction());
        			return new AccountManager.DisplayBalanceCommand(result.getTransaction().getAccountNumber());
        		});
        
        Sink<AccountManager.AccountManagerCommand, NotUsed> displayBalanceSink = ActorSink.actorRef(
        		accountManager, 
        		new AccountManager.CompleteCommand(), 
        		(throwable)->new AccountManager.FailedCommand() 
    		);

        Source.fromGraph(sourcePartialGraph)
        	.via(attemptToApplyTransaction.divertTo(rejectedTransactions, result->!result.getSucceeded()))
        	.via(receiveResult)
        	.to(displayBalanceSink)
        	.run(accountManager);
   } 
}
