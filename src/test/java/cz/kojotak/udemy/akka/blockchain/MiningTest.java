package cz.kojotak.udemy.akka.blockchain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import cz.kojotak.udemy.akka.blockchain.WorkerBehavior.Command;
import cz.kojotak.udemy.akka.blockchain.model.Block;
import cz.kojotak.udemy.akka.blockchain.model.HashResult;
import cz.kojotak.udemy.akka.blockchain.utils.BlocksData;

class MiningTest {

	@Test
	void testSendMessage() {
		BehaviorTestKit<Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
		Block block = BlocksData.getNextBlock(0, "0");
		TestInbox<HashResult> testInbox = TestInbox.create();
		Command msg = new WorkerBehavior.Command(block, 0, 5, testInbox.getRef());
		testActor.run(msg);
		//check result in log, that is in console...
		List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
		assertEquals(1, logMessages.size());
		assertEquals("null", logMessages.get(0).message());
		assertEquals("DEBUG", logMessages.get(0).level().name());
	}
	
	@Test
	void testMiningPassesIfNonceIsInRange() {
		BehaviorTestKit<Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
		Block block = BlocksData.getNextBlock(0, "0");
		TestInbox<HashResult> testInbox = TestInbox.create();
		Command msg = new WorkerBehavior.Command(block, 82700, 5, testInbox.getRef());
		testActor.run(msg);
		List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
		assertEquals(1, logMessages.size());
		String expected = "82741 : 0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b";  
		assertEquals(expected, logMessages.get(0).message());
		assertEquals("DEBUG", logMessages.get(0).level().name());
	}
	
	@Test
	void testMessageReceivedIfNonceInRange() {
		BehaviorTestKit<Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
		Block block = BlocksData.getNextBlock(0, "0");
		TestInbox<HashResult> testInbox = TestInbox.create();
		Command msg = new WorkerBehavior.Command(block, 82700, 5, testInbox.getRef());
		testActor.run(msg);
		HashResult expected = new HashResult();
		expected.foundAHash("0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b", 82741);
		testInbox.expectMessage(expected);
		
	}

	@Test
	void testNoMessageReceivedIfNonceNotInRange() {
		BehaviorTestKit<Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
		Block block = BlocksData.getNextBlock(0, "0");
		TestInbox<HashResult> testInbox = TestInbox.create();
		Command msg = new WorkerBehavior.Command(block, 42, 5, testInbox.getRef());
		testActor.run(msg);
		assertFalse(testInbox.hasMessages());
	}

}
