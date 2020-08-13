package newchain;
import java.util.ArrayList;



import java.util.HashMap;

import helpers.StringUtil;

import java.security.Security;



public class NewChain {
	
	
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	
	//new hash map!
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //all unspent transactions.
	
	public static int difficulty = 5; //five zeros 
	public static float minimumTransaction = 0.1f; //transaction can be no less that .1 
	public static Wallet walletA; 
	public static Wallet walletB;
	public static Transaction firstTransaction;
	
	public static void main(String[] args) {
		
		//Bouncy Castle? those are fun!
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Jar file, 3rd party, adds cryptography resources.
		
		//What's in your wallet(s) - establish accounts/wallets to transfer funds to and between.
		walletA = new Wallet();
		walletB = new Wallet();
		Wallet coinbase = new Wallet();
		
		//send some coins via first transaction
		firstTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 10f, null); //10 points to walletA
		firstTransaction.generateSignature(coinbase.privateKey);
		firstTransaction.transactionId = "0"; //manual set
		firstTransaction.outputs.add(new TransactionOutput(firstTransaction.reciepient, firstTransaction.value, firstTransaction.transactionId));
		UTXOs.put(firstTransaction.outputs.get(0).id, firstTransaction.outputs.get(0));// store first transaction
		
		System.out.println("Creating and Mining Initial block..");
		Block first = new Block("0");
		first.addTransaction(firstTransaction);
		
		//addBlock(first);
		proofOfWork(first);
		
		//testing
		Block block1 = new Block(first.hash); //creates hash for this block
		System.out.println("\nWalletA's Starting Balance is: " + walletA.getBalance());
		System.out.println("\nWalletB's Starting Balance is: " + walletB.getBalance());
		
		//collects starting balances for wallet - to be benchmarked against final numbers for a daily ledger report
		float startA = walletA.getBalance();
		float startB = walletB.getBalance();
		
		
		System.out.println("\nWalletA wants to send (7) to WalletB... ");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 7f)); //sufficient funds, send to 7 to wallet b
		
		//addBlock(block1); //send the block to the mines.
		//proofOfWork is new method. to test speed versus difficulty - I suggest commenting out proofOfWork and un-comment the addBlock above.
		proofOfWork(block1);
		
		//print the new totals
		System.out.println("\nWalletA's Balance is: " + walletA.getBalance());
		System.out.println("\nWalletB's Balance is: " + walletB.getBalance());
		
		//Store the initial timestamp
		long start = block1.getTimeStamp();
		//testing getTimeStamp()
		//System.out.println("\nTime to complete: " + block1.getTimeStamp());

		
		Block block2 = new Block(block1.hash); //links to the previous block. recursively, for block N this will be block(n-1).hash
		System.out.println("\nWalletA wants to send (9001)... doesn't have over 9000 funds"); //test to see if failing parameters work.
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 9001f));
		
		//addBlock(block2); //send the block to the mines.
		proofOfWork(block2);
		
		System.out.println("\nWalletA's Balance is: " + walletA.getBalance());
		System.out.println("\nWalletB's Balance is: " + walletB.getBalance());

		
		Block block3 = new Block(block2.hash);//links to the previous block. recursively, for block N this will be block(n-1).hash
		System.out.println("\nWalletB wants to send (5) to WalletA"); //Now the B has money, send some to A. 
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 5f));
		
		//addBlock(block3);//send the block to the mines.
		proofOfWork(block3);
		
		System.out.println("\nWalletA's Balance is: " + walletA.getBalance());
		System.out.println("\nWalletB's Balance is: " + walletB.getBalance());
		
		long end = block3.getTimeStamp();
		long totalTime = end - start;
		
		isChainValid(); //runs validation by testing if hashes are equal
		
		
		//daily ledger report - takes starting values from ending values, winds up with a transaction difference. 
		System.out.println("\n WalletA's daily total transaction value: " + (walletA.getBalance() - startA));
		System.out.println("\n WalletB's daily total transaction value: " + (walletB.getBalance() - startB));
		System.out.println("\n Total Blockchain time: " + totalTime);
		System.out.println("\n Notes: For every difficulty, I notice roughly an order of magnitude increase in time");
		System.out.println("\n Using proofOfWork doesn't have difficulty, so it is near instant. Going back to addblock would be a great use case for testing time.");

/*
		//let's test this! 
		System.out.println("Private and Public Keys: ");
		System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
		System.out.println(StringUtil.getStringFromKey(walletA.publicKey));
		
		//sample transaction from A to B
		Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
		transaction.generateSignature(walletA.privateKey);
		
		//Verify
		System.out.println("Verified Signature: ");
		System.out.println(transaction.verifySignature());
		*/
		
		
		
		/*old code from part 1
		//add blocks to the blockchain ArrayList
		
		blockchain.add(new Block("Hello World, I am the first block", "0"));
		System.out.println("First Block Mining...");
		blockchain.get(0).mineBlock(difficulty);
		
		blockchain.add(new Block("Block 2.0 Tokyo Drift",blockchain.get(blockchain.size()-1).hash));
		System.out.println("Second Block Mining...");
		blockchain.get(1).mineBlock(difficulty);
		
		blockchain.add(new Block("Block Hard with a vengance 3",blockchain.get(blockchain.size()-1).hash));
		System.out.println("Third Block Mining...");
		blockchain.get(2).mineBlock(difficulty);
		
		System.out.println("\nValid Blockchain? " + isChainValid());

		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
		System.out.println("\nThe Block Chain: ");
		System.out.println(blockchainJson);
		*/
	}
	
	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0'); //could use string builder here, but this way is fewer lines of code.
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>(); //puts things in temp
		tempUTXOs.put(firstTransaction.outputs.get(0).id, firstTransaction.outputs.get(0));
		
		//loop around block(s) checking for hash
		
		for (int i = 1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculate hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ) { //if the current hashes don't match, we need to invalidate the blockchain.
				System.out.println("#Current Hashes not equal");
				return false;
			}
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {//if the previous hashes don't match, we need to invalidate the blockchain.
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//NEW - check if hash is solved?
			if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) { //this means it hasn't gone against any difficulty. 
				System.out.println("#Hasn't been mined");
				return false;
			}
			
			//looping through blockchains
			TransactionOutput tempOutput;
			for (int t=0; t <currentBlock.transactions.size(); t++) { //loop through total number of transactions.
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifySignature()) { //if signature fails to validate.
					System.out.println("#Invalid Signature on: " + t);
					return false;
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) { //the money in should equal the money out. if new money is introduced, there is a problem.
					System.out.println("#unequal inputs on transaction: " + t);
					return false;
				}
				
				for(TransactionInput input: currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) { //a null output id would mean no transaction succeeded.
						System.out.println("#Reference input is missing on: " + t);
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) { //error handling, what if the transacation value and the output value are not equal.
						System.out.println("#Reference input is invalid on: " + t);
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId); //clears temp
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if(currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) { //error handling - invalid recipient (example: walletA to walletC) 
					System.out.println("#Incorrect reciepient on transaction: " + t);	
					return false;
				}
				
				if(currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {//error handling - invalid sender (example: walletC to walletA) 
					System.out.println("#Incorrect sender on transaction: " + t);	
					return false;
				}
			}
		}
		System.out.println("Blockchain Validated!");	//everything works as planned.
		return true;
	}

	
		public static void addBlock(Block newBlock) {
			newBlock.mineBlock(difficulty); //mine result based on difficulty setting. 
			
			blockchain.add(newBlock);
		}
		
		
		//NEW proof of work concept. 
		//This would be more of a unit tester as it doesn't include difficulty, so modern computers should do this rather quickly.
		public static void proofOfWork(Block newBlock) {
			
			//pull values 
			int nonceInt = newBlock.getNonce();
			String nonceKey = Integer.toString(nonceInt);
			long nonce = 0;
			boolean nonceFound = false;
			String nonceHash = "";
			
			//transaction log, get Merkle Root
			String merkleRoot = StringUtil.getMerkleRoot(transactions);
			//similar to other concept - you need to generate a message/target to benchmark against. 
			String message = Long.toString(newBlock.getTimeStamp()) + merkleRoot + newBlock.getPreviousHash();
			
			//While loop that applies a hash, increments nonce, then calculates new hash. 
			while (!nonceFound ) {
				nonceHash = StringUtil.applyMD5(message + nonce);
				nonceFound = nonceHash.substring(0, nonceKey.length()).equals(nonceKey);
				nonce++;
				nonceHash = newBlock.calculateHash();
			}
			System.out.println("Successful Block Mine: " + nonceHash);
			
	}
		
		
	
}
