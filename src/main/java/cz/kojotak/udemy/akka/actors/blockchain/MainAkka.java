package cz.kojotak.udemy.akka.actors.blockchain;

public class MainAkka {
		
	public static void main(String[] args) {
			
		BlockChainMiner miner = new BlockChainMiner();
		miner.mineBlocks();
		
	}
	
}
