package newchain;
import java.util.ArrayList;
import helpers.StringUtil;

import java.util.Date;


public class Block {

	public String hash;
	public String previousHash;
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	private long timeStamp;
	private int nonce;
	
	
	//construct a block
	public Block(String previousHash) {
	
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		//add the hash - later in tutorial
		this.hash = calculateHash(); //Does this need to be set after other values?
	}
	
	
	//Using MD5 instead of 256. The result seems to be much faster; however research indicates that it is less secure
	
	public String calculateHash() {
	
		//String calculatedhash = StringUtil.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
		String calculatedhash = StringUtil.applyMD5(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
		return calculatedhash; //string that appends all necessary parts of the hash.
	}

	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions); //from the transaction tree, identifies the Merkel Root
		String target = StringUtil.getDifficultyString(difficulty); //New Method, uses Stringbuilder to append zeroes equal to difficulty level
		//String target = new String(new char[difficulty]).replace('\0','0'); //String of difficulty 0?
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce++;
			hash = calculateHash();
		}
		System.out.println("Successful Block Mine: " + hash);
	}
	
	@SuppressWarnings("unused")
	private void proofOfWork() {
		
		
		String nonceKey = Integer.toString(nonce);
		long nonce = 0;
		boolean nonceFound = false;
		String nonceHash = "";
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		
		String merkleRoot = StringUtil.getMerkleRoot(transactions);
		
		String message = Long.toString(timeStamp) + merkleRoot + previousHash;
		
		while (!nonceFound ) {
			nonceHash = StringUtil.applyMD5(message + nonce);
			nonceFound = nonceHash.substring(0, nonceKey.length()).equals(nonceKey);
			nonce++;
			nonceHash = calculateHash();
		}
		System.out.println("Successful Block Mine: " + nonceHash);
		
}
	
//what if a transaction didn't occur? We can add all real transactions
	public boolean addTransaction (Transaction transaction) {
		if(transaction == null) return false; //insufficient funds, other errors could cause null transaction value
		if(previousHash != "0") { 
			if((transaction.processTransaction() != true)) { //if the transaction did not process, state that it failed.
				System.out.println("Transaction failed");
				return false;
			}
		}
		transactions.add(transaction); //add transaction as it has passed all failing parameters. 
		System.out.println("Transaction Success!");
		return true;
	}

	//Getter & Setter Pair for timestamp. Tracking time to see how long it takes to run process.

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}


	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}


	/**
	 * @return the nonce
	 */
	public int getNonce() {
		return nonce;
	}


	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(int nonce) {
		this.nonce = nonce;
	}


	/**
	 * @return the previousHash
	 */
	public String getPreviousHash() {
		return previousHash;
	}


	/**
	 * @param previousHash the previousHash to set
	 */
	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}
}

