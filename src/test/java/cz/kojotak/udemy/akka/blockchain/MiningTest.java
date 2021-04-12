package cz.kojotak.udemy.akka.blockchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import cz.kojotak.udemy.akka.actors.blockchain.ManagerBehavior;
import cz.kojotak.udemy.akka.actors.blockchain.WorkerBehavior;
import cz.kojotak.udemy.akka.actors.blockchain.model.Block;
import cz.kojotak.udemy.akka.actors.blockchain.model.HashResult;
import cz.kojotak.udemy.akka.actors.blockchain.utils.BlocksData;

class MiningTest {

	@Test
	void testSendMessage() {
		BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
		Block block = BlocksData.getNextBlock(0, "0");
		TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
		WorkerBehavior.Command msg = new WorkerBehavior.Command(block, 0, 5, testInbox.getRef());
		testActor.run(msg);
		//check result in log, that is in console...
		List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
		assertEquals(1, logMessages.size());
		assertEquals("null", logMessages.get(0).message());
		assertEquals("DEBUG", logMessages.get(0).level().name());
	}
	
    /**
     * It should not pass, because our first valid nonce, for the first block, having a difficulty of 3 is 2147
     * And because we are giving nonce range insufficient (0-1_000), the test of course should fail.
     */
    @Test
    void testMiningFailsIfNonceNotInRange() {
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
 
        TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
 
        int startNonce = 0;
        int difficulty = 3;
 
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, startNonce, difficulty, testInbox.getRef());
        testActor.run(message);
        List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
        assertEquals(1, logMessages.size());
        assertEquals("null", logMessages.get(0).message());
        assertEquals(Level.DEBUG, logMessages.get(0).level());
    }
 
    /**
     * Here the nonce range is between 2_000 - 3_000, and the test should pass, because our Actor, will be able to find
     * in this range, a valid nonce value, what is 2147, for this level of difficulty.
     */
    @Test
    void testMiningPassesIfNonceIsInRange() {
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
 
        TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
 
        int startNonce = 2_000;
        int difficulty = 3;
 
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, startNonce, difficulty, testInbox.getRef());
        testActor.run(message);
 
        List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();
        assertEquals(logMessages.size(), 1);
 
        String expectedResult = "2147 : 00003d81cb2882f5c6bc14248b356ba17b37c7cb0c3fdf4256f885ade10c373b";
        assertEquals(expectedResult, logMessages.get(0).message());
        assertEquals(Level.DEBUG, logMessages.get(0).level());
    }
 
    @Test
    void testMessageReceivedIfNonceInRange() {
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
 
        TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
 
        int startNonce = 2_000;
        int difficulty = 3;
 
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, startNonce, difficulty, testInbox.getRef());
        testActor.run(message);
 
        HashResult expectedHashResult = new HashResult();
        expectedHashResult.foundAHash("00003d81cb2882f5c6bc14248b356ba17b37c7cb0c3fdf4256f885ade10c373b", 2147);
 
        testInbox.expectMessage(new ManagerBehavior.HashResultCommand(expectedHashResult));
    }
 
    @Test
    void testNoMessageReceivedIfNonceNotInRange() {
        BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
 
        TestInbox<ManagerBehavior.Command> testInbox = TestInbox.create();
 
        int startNonce = 0;
        int difficulty = 3;
 
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, startNonce, difficulty, testInbox.getRef());
        testActor.run(message);
 
        assertFalse(testInbox.hasMessages());
    }

}
