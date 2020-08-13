package newchain;
import java.security.PublicKey;

import helpers.StringUtil;

public class TransactionOutput {
	
	public String id;
	public PublicKey reciepient; //new coins for me!
	public float value;
	public String parentTransactionID; //ID for logging?
	
	//Construction!
	
public TransactionOutput(PublicKey reciepient, float value, String parentTransactionID) {
	this.reciepient = reciepient;
	this.value = value;
	this.parentTransactionID = parentTransactionID;
	//this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionID);
	//MD5 for speed!
	this.id = StringUtil.applyMD5(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionID);
}

// is this coin yours? Validate via boolean for the intended recipient's public key. 
public boolean isMine(PublicKey publicKey) {
	return (publicKey == reciepient);
}
}
