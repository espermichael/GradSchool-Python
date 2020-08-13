package newchain;
import java.security.*;
import java.util.ArrayList;

import helpers.StringUtil;

public class Transaction {

	public String transactionId; //transactional hash
	public PublicKey sender; //senders public key
	public PublicKey reciepient; //receiver public key
	public float value;
	public byte[] signature; //digitally sign this!
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; //count the transactions
	
	//Constructing Additional Pylons. 
	public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	/* Using MD5 instead of SHA256.
	 * Faster, less secure. (128 bit rather than 256) 
	 */
	
	private String calculateHash() {
		sequence++; //iterate sequence for each transaction success. 
		//return StringUtil.applySha256(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value) + sequence);
		return StringUtil.applyMD5(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value) + sequence);
	}
	
	//use ECDAS and BC to create and verify a signature. 
	
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
		signature = StringUtil.applyECDSASig(privateKey, data);
	}
	
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value);
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	//If the transaction is possible, return true
	
	public boolean processTransaction() {
		if(verifySignature() == false) { //fail to verify
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
		
		//gotta catch them all - the outputs that is
		for(TransactionInput i : inputs) {
			i.UTXO = NewChain.UTXOs.get(i.transactionOutputId);
		}
		
		//check and validate
		
		if(getInputsValue() < NewChain.minimumTransaction) { //make sure everything is above .1, the minimum transaction.
			System.out.println("#transaction Inputs to small: " + getInputsValue());
			return false;
		}
		
		//generate transaction output:
		float leftOver = getInputsValue() - value;
		transactionId = calculateHash();
		outputs.add(new TransactionOutput(this.reciepient, value, transactionId));
		outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));
		
		//add outputs to unspent
		for(TransactionOutput o : outputs) {
			NewChain.UTXOs.put(o.id, o);
		}
		
		//remove transaction inputs from UTXO list
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue;
			NewChain.UTXOs.remove(i.UTXO.id);
		}
		return true;
		}
	
	//return the value of input per transaction
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue;
			total += i.UTXO.value;
		}
		return total;
		}

		
	// output sum
	//return the value of output per transaction
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
		}
	}
