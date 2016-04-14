package common;

public class Lock {
	
	/* The tag name of the class */
	public static final String TAG = Lock.class.getName();
	
	/* Constants for lock types */
	public static final int TYPE_READ = 1;
	public static final int TYPE_WRITE = 2;
	public static final int TYPE_READ_AND_WRITE = 3;
	
	/* The data item where the lock is placed */
	private String item;
	
	/* The lock type */
	private int type;
	
	/* The id of the transaction that holds the lock */
	private int transactionId;
	
	/**
	 * Constructor of the class
	 * @param type
	 * @param item
	 * @param transactionId
	 */
	public Lock(int type, String item, int transactionId) {
		this.type = type;
		this.item = item;
		this.transactionId = transactionId;
	}

	/**
	 * 
	 * @return
	 */
	public String getItem() {
		return item;
	}

	/**
	 * 
	 * @return
	 */
	public int getTransactionId() {
		return transactionId;
	}

	/**
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * 
	 * @param item
	 */
	public void setItem(String item) {
		this.item = item;
	}

	/**
	 * 
	 * @param transactionId
	 */
	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * 
	 */
	public String toString() {
		
		/* Initiates the String builder */
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(item + ", ");
		
		switch(type) {
		
			case Lock.TYPE_READ:
				builder.append("read");
			break;
			
			case Lock.TYPE_WRITE:
				builder.append("write");
			break;
			
			case Lock.TYPE_READ_AND_WRITE:
				builder.append("read/write");
			break;
			
		}
		
		/* Complete the lock information */
		builder.append(", transaction " + transactionId + 
				", siteId " + Transaction.getSiteId(transactionId) + " ]");
		
		/* Return the generated string */
		return builder.toString();
	}
	
	/**
	 * Upgrades the lock type
	 * @param newType
	 */
	public void upgradeType(int newType) {
		if(type == Lock.TYPE_READ && newType == Lock.TYPE_WRITE) {
			type = Lock.TYPE_READ_AND_WRITE;
		}
		else if(type == Lock.TYPE_WRITE && newType == Lock.TYPE_READ) {
			type = Lock.TYPE_READ_AND_WRITE;
		}
	}

}
