package newchain;

public class TransactionInput {

	public String transactionOutputId; //references outputs
	public TransactionOutput UTXO; //Unspent Transaction output
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
