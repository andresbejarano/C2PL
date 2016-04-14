package common;

import java.io.Serializable;

public class Operation implements Serializable {
	
	/* The tag name of the class */
	public static final String TAG = Operation.class.getName();
	
	/* Constants for operation types */
	public static final int TYPE_READ = 1;
	public static final int TYPE_WRITE = 2;
	public static final int TYPE_MATH = 3;

	/* Some serial version value */
	private static final long serialVersionUID = 1L;
	
	/* The Id of the transaction where the operation belongs */
	private int transactionId;
	
	/* The type of the operation */
	private int type;
	
	/* The data item referenced in the operation */
	private String item;
	
	/* The first operand of the operation (for math operations only) */
	private String operand1;
	
	/* The second operand of the operation (for math operations only) */
	private String operand2;
	
	/* The operator of the operation (for math operations only) */
	private String operator;
	
	/**
	 * Constructor of the class for commit and abort operations
	 * @param transactionId
	 * @param type
	 */
	public Operation(int transactionId, int type) {
		this.transactionId = transactionId;
		this.type = type;
	}
	
	/**
	 * Constructor of the class for read and write operations
	 * @param transactionId
	 * @param type
	 * @param item
	 */
	public Operation(int transactionId, int type, String item) {
		this.transactionId = transactionId;
		this.type = type;
		this.item = item;
	}
	
	/**
	 * Constructor of the class for math operations
	 * @param transactionId
	 * @param type
	 * @param vid
	 * @param operand1
	 * @param operator
	 * @param operand2
	 */
	public Operation(int transactionId, int type, String item, 
			String operand1, String operator, String operand2) {
		this.transactionId = transactionId;
		this.type = type;
		this.item = item;
		this.operand1 = operand1;
		this.operator = operator;
		this.operand2 = operand2;
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
	 * @throws Exception
	 */
	public Lock getLock() {
		
		/* The lock type */
		int lockType;
		
		/* Select the lock type based on the type of the operation */
		switch(type) {
		
			/* The operation is of type read */
			case TYPE_READ:
				lockType = Lock.TYPE_READ;
			break;
			
			/* The operation is of type write */
			case TYPE_WRITE:
				lockType = Lock.TYPE_WRITE;
			break;
			
			/* Other types of operations cannot get locks */
			default:
				System.out.println(TAG + " This operation cannot obtain a lock!");
				return null;
		
		}
		
		/* Generate the new lock and return it */
		Lock lock = new Lock(lockType, item, transactionId);
		return lock;
	}

	/**
	 * 
	 * @return
	 */
	public String getOperand1() {
		return operand1;
	}

	/**
	 * 
	 * @return
	 */
	public String getOperand2() {
		return operand2;
	}

	/**
	 * 
	 * @return
	 */
	public String getOperator() {
		return operator;
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
	 * @param operand1
	 */
	public void setOperand1(String operand1) {
		this.operand1 = operand1;
	}

	/**
	 * 
	 * @param operand2
	 */
	public void setOperand2(String operand2) {
		this.operand2 = operand2;
	}

	/**
	 * 
	 * @param operator
	 */
	public void setOperator(String operator) {
		this.operator = operator;
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
		
		/* Initiate the string builder */
		StringBuilder builder = new StringBuilder();
		
		switch(type) {
			
			case Operation.TYPE_READ:
				builder.append("[READ(" + item + "), ");
			break;
			
			case Operation.TYPE_WRITE:
				builder.append("[WRITE(" + item + "), ");
			break;
			
			case Operation.TYPE_MATH:
				builder.append("[MATH(" + item + "=" + operand1 + operator + operand2 + "), ");
			break;
			
		}
		
		/* Complete the operation information */
		builder.append("transaction " + transactionId + 
				", siteID " + Transaction.getSiteId(transactionId) + " ]");
		
		/* Return the string */
		return builder.toString();
	}

}
