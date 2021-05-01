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
import cz.kojotak.udemy.akka.streams.chapter13.Account;
import cz.kojotak.udemy.akka.streams.chapter13.Transaction;
import cz.kojotak.udemy.akka.streams.chapter13.Transfer;
public class Main {



    public static void main(String[] args) {

        Map<Integer, Account> accounts = new HashMap<>();

        //set up accounts
        for (int i  = 1; i <= 10; i++) {
            accounts.put(i, new Account(i, new BigDecimal(1000)));
        }

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
        
        Flow<Transaction, Transaction, NotUsed> applyTransactionToAccounts = Flow.of(Transaction.class)
        		.map( trans -> {
        			Account acc = accounts.get(trans.getAccountNumber());
        			acc.addTransaction(trans);
        			System.out.println("added " + trans + " to " + acc.getId());
        			return trans;
        		});
        
        Sink<Transaction, CompletionStage<Done>> rejectedTransactions = Sink.foreach( trans ->{
        	System.out.println("REJECTED trans: " + trans);
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
        
        Graph<SinkShape<Transaction>, CompletionStage<Done>> sinkPartialGraph = GraphDSL.create(
        	Sink.foreach(System.out::println), 
        	(builder,out)->{
        		FlowShape<Transaction, Transaction> entryFlow = builder.add(Flow.of(Transaction.class) //prida flow pro "divert to"
						.divertTo(rejectedTransactions, trans->{
							Account account = accounts.get(trans.getAccountNumber());
							BigDecimal forcastBalance = account.getBalance().add(trans.getAmount());
							return forcastBalance.compareTo(BigDecimal.ZERO) < 0;
							//true=chci divert, false=chci pokracovat
						}));
        		
        		builder
        			.from(entryFlow)
        			.via(builder.add(applyTransactionToAccounts))
        			.to(out);
			    return SinkShape.of(entryFlow.in());
        	}
		);
        
//        RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
//        		GraphDSL.create(sinkPartialGraph, (builder,out)->{
//        			builder.from(builder.add(sourcePartialGraph)).to(out);
//        			return ClosedShape.getInstance();
//        		})
//        		);
				        
        ActorSystem ac = ActorSystem.create(Behaviors.empty(), "actorSystem");        
//        graph.run(ac);
        Source<Transaction,NotUsed> newSource = Source.fromGraph(sourcePartialGraph);
        newSource.to(sinkPartialGraph).run(ac);
   } 
}
