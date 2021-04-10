package cz.kojotak.udemy.akka.blockchain.multiThreadedExample;

import cz.kojotak.udemy.akka.blockchain.model.Block;
import cz.kojotak.udemy.akka.blockchain.model.HashResult;
import cz.kojotak.udemy.akka.blockchain.utils.BlockChainUtils;

public class BlockMiner implements Runnable{

	private Block block;
	private int firstNonce;
	private HashResult hashResult;
	private int difficultyLevel;
	
	public BlockMiner(Block block, int firstNonce, HashResult hashResult, int difficultyLevel) {
		this.block = block;
		this.firstNonce = firstNonce;
		this.hashResult = hashResult;
		this.difficultyLevel = difficultyLevel;
	}
	
	@Override
	public void run() {
		HashResult hashResult = BlockChainUtils.mineBlock(block, difficultyLevel, firstNonce, firstNonce + 1000);
		if (hashResult != null) {
			this.hashResult.foundAHash(hashResult.getHash(), hashResult.getNonce());
		}
			
	}
	
}
