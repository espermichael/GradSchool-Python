package helpers;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;

import newchain.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.*;


//Helper utility - for encryption, e-signatures, and defining the merkle root

public class StringUtil {
	
	//NEW METHOD

	public static String generateHash(String value) {
		String hash = null;
		try {
			//MessageDigest md = MessageDigest.getInstance("SHA-256");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = md.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(); 
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1)); //For loop goes character by character and converts to hash.
			}
			hash = sb.toString(); //stringbuilder is not a string! convert to string
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash;
	}

	//CLASSIC METHODS

//Secure a string
	/*
	public static String applySha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			//makes an input SHA256?
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) { //for every character in the hash, change to hex.
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0'); //append 0 to the length if it is assigned to a value of 1
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	*/
	public static String applyMD5(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) { //for every character in the hash, change to hex.
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0'); //append 0 to the length if it is assigned to a value of 1
				hexString.append(hex);
			}
			return hexString.toString(); //convert from stringbuilder to string
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	//apply e-sig and return result
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa; //DSA = digital signature algorithm
		byte[] output = new byte[0];
		//try catch block, uses digital signature via the private key (so only one person has it) 
		try {
			
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey); //initialize object to be signed
			byte[] strByte = input.getBytes(); //captures input, stores as an array
			dsa.update(strByte); //upates the DSA to the array that has already been captured. 
			byte[] realSig = dsa.sign(); //return signature bytes of all data updated
			output = realSig;
			}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}
	
	//verification - true/false signature using public key
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		//try catch block. 
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes()); 
			return ecdsaVerify.verify(signature); //after verification of public key and updating the data, return the true false result of the signature.
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//capture encoded key and change it to a string value
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	//calculate merkle root from the array - you must loop through every previous layer of the tree.

	public static String getMerkleRoot(ArrayList<Transaction> transactions) {
		int count = transactions.size();
		ArrayList<String> previousTreeLayer = new ArrayList<String>(); //get the tree layers as an array
		for(Transaction transaction : transactions) {
			previousTreeLayer.add(transaction.transactionId); //append the transaction ID to distinguish between layers
		}
		ArrayList<String> treeLayer = previousTreeLayer;
		while(count > 1) {
			treeLayer = new ArrayList<String>();
			for(int i = 1; i < previousTreeLayer.size(); i++) {
				//treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
				treeLayer.add(applyMD5(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
			}
		
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}
		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
			}
	
		

	
	//new algorithm to get the difficulty
	//For all difficulties, this forms a counter and appends an equivalent number of leading zeros. 
public static String getDifficultyString(int difficulty) {
	
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < difficulty; i++) {
		sb.append('0');
	}
	String leadZero = sb.toString();
	return leadZero;
	
	//return new String(new char[difficulty]).replace('\0', '0');
}

}

	