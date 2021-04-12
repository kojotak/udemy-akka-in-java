package cz.kojotak.udemy.akka.actors.blockchain.model;

public class HashResult {

	private int nonce;
	private String hash;
	private boolean complete = false;
	
	public HashResult() {}
	
	public int getNonce() {
		return nonce;
	}

	public String getHash() {
		return hash;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public synchronized void foundAHash(String hash, int nonce) {
		this.hash = hash;
		this.nonce = nonce;
		this.complete = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + nonce;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashResult other = (HashResult) obj;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (nonce != other.nonce)
			return false;
		return true;
	}
	
}
