package newchain;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;;

public class Wallet {

	public PrivateKey privateKey;
	public PublicKey publicKey; 
	
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //uto for this wallet only
	
	public Wallet() {
		generateKeyPair();
	}
	
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			//SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			SecureRandom random = SecureRandom.getInstanceStrong(); //more security due to md5
			//ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime256v1"); //higher bit version
			
			//init generator, generate key pair
			
			keyGen.initialize(ecSpec, random); //256byte
			KeyPair keyPair = keyGen.generateKeyPair();
			
			//set public and private keys
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
			}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//return current balance, useful after a transaction has occurred. 
	
	public float getBalance() {
		float total =0;
		for (Map.Entry<String, TransactionOutput> item: NewChain.UTXOs.entrySet()) { 
			TransactionOutput UTXO = item.getValue();
			if(UTXO.isMine(publicKey)) { //is this my coin?
				UTXOs.put(UTXO.id, UTXO); //add it
				total += UTXO.value;
			}
		}
		return total;
			}
	

//send funds, if transaction is validated.
	public Transaction sendFunds(PublicKey _recipient, float value) {
		if(getBalance() < value) { //not enough currency to complete transaction.
			System.out.println("#insufficent funds, Transaction cancelled");
			return null;
		}
	ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	
	float total = 0;
	for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()) {
		TransactionOutput UTXO = item.getValue();
		total += UTXO.value; //add value of transaction to the total
		inputs.add(new TransactionInput(UTXO.id));
		if(total > value) break;
	}
	
	//establish new transaction
	Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
	newTransaction.generateSignature(privateKey);
	
	for(TransactionInput input: inputs) {
		UTXOs.remove(input.transactionOutputId);
		}
	return newTransaction;
	}
	}

