package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import datasite.DataManager;

public class Transaction implements Serializable {
	
	/* The tag name of the class */
	public static final String TAG = Transaction.class.getName();
	
	/* A serial version id */
	private static final long serialVersionUID = 1L;

	/* An offset for transaction id generation */
	public static final int ID_OFFSET = 100;
	
	/* The delay between operations */
	public static final int DELAY_MILLISEC = 100;
	
	/**
	 * Returns the id of the site where the given transaction's 
	 * id comes from
	 * @param id
	 * @return
	 */
	public static int getSiteId(int transactionId) {
		return transactionId / Transaction.ID_OFFSET; 
	}
	
	/**
	 * Indicates if the given string corresponds to a natural number
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {
		return str.matches("\\d+");
	}
	
	/* The id of the transaction */
	private int id;
	
	/* The list of operations of the transaction */
	private ArrayList<Operation> operations;
	
	/* The hash table for pre commits */
	private Hashtable<String, Integer> preCommit;
	
	/* The hash table for pre writes */
	private Hashtable<String, Integer> preWrite;
	
	/**
	 * Constructor of the class
	 * @param id
	 */
	public Transaction(int id) {
		this.id = id;
		operations = new ArrayList<Operation>();
		preCommit = new Hashtable<String, Integer>();
		preWrite = new Hashtable<String, Integer>();
	}
	
	/**
	 * Adds an operation to the operations list
	 * @param operation
	 * @return
	 */
	public boolean addOperation(Operation operation) {
		operation.setTransactionId(id);
		return operations.add(operation);
	}
	
	/**
	 * Writes all the operations in the pre committed table to 
	 * the database
	 * @return
	 */
	public void commit() {
		
		/* Traverse each key element in the pre commit hash table */
		for(String key: preCommit.keySet()) {
			
			/* Get the value associated with the key */
			int value = preCommit.get(key);
			
			/* Write the pair in the database */
			DataManager.write(key, value);
		}
	}
	
	/**
	 * Executes the given operation
	 * @param operation
	 */
	public void executeOperation(Operation operation) throws Exception {
		
		/* Try to execute the operation */
		try {
			
			/* Put the thread to sleep */
			Thread.sleep(Transaction.DELAY_MILLISEC);
			
			/* Get the data item from the operation */
			String item = operation.getItem();
			
			/* Process the operation based on its type */
			switch(operation.getType()) {
			
				/* Process a read operation */
				case Operation.TYPE_READ:
					
					/* Read the value associated to the item in the database */
					int value = DataManager.read(item);
					
					/* Save the item and its value in the pre commit table */
					preCommit.put(item, value);
					
				break;
				
				/* Process a write operation */
				case Operation.TYPE_WRITE:
					
					/* Check previous read value from item */
					if(preWrite.containsKey(item)) {
						
						/* Save the item and its value in the pre commit table */
						preCommit.put(item, preWrite.get(item));
					}
					else {
						throw new Exception("Write operation value not available");
					}
					
				break;
				
				/* Process a math operation */
				case Operation.TYPE_MATH:
					
					/* Get the operands and the operator */
					String operand1 = operation.getOperand1();
					String operand2 = operation.getOperand2();
					String operator = operation.getOperator();
					
					/* The numeric values of the operands */
					int operand1Value;
					int operand2Value;
					
					/* The result of the operation */
					int result;
					
					/* If the first operand is a number then convert it, otherwise read
					 * from the pre commit table its correspondent value */
					if(Transaction.isNumber(operand1)) {
						operand1Value = Integer.parseInt(operand1);
					}
					else {
						operand1Value = preCommit.get(operand1);
					}
					
					/* If the second operand is a number then convert it, otherwise read
					 * from the pre commit table its correspondent value */
					if(Transaction.isNumber(operand2)) {
						operand2Value = Integer.parseInt(operand2);
					}
					else {
						operand2Value = preCommit.get(operand2);
					}
					
					/* Calculate the result based on the operator */
					switch(operator) {
					
						case "+":
							result = operand1Value + operand2Value;
						break;
						
						case "-":
							result = operand1Value - operand2Value;
						break;
						
						case "*":
							result = operand1Value * operand2Value;
						break;
						
						case "/":
							result = operand1Value / operand2Value;
						break;
						
						default:
							throw new Exception("Undefined operator");
					
					}
					
					/* Stores the item with the result */
					preWrite.put(item, result);
					
				break;
			
			}
			
		}
		catch(InterruptedException e) {
			System.out.println("Interrupted Exception: " + e.getMessage());
		}
		
		
	}
	
	/**
	 * Executes the operations in the operations list
	 * @throws Exception
	 */
	public void executeOperations() throws Exception {
		
		/* Get the size of the operations list */
		int n = operations.size();
		
		/* Execute each operation in the list */
		for(int i = 0; i < n; i += 1) {
			executeOperation(operations.get(i));
		}
		
	}
	
	/**
	 * Returns the id of the transaction
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the id of the transaction in string format
	 * @return
	 */
	public String getName() {
		return "" + id;
	}

	/**
	 * Returns the list of operations in the transaction
	 * @return
	 */
	public ArrayList<Operation> getOperations() {
		return operations;
	}

	/**
	 * Returns the hash table with the pre committed atomic operations
	 * @return
	 */
	public Hashtable<String, Integer> getPreCommit() {
		return preCommit;
	}

	/**
	 * Returns the hash table with the pre written atomic operations
	 * @return
	 */
	public Hashtable<String, Integer> getPreWrite() {
		return preWrite;
	}

	/**
	 * Returns an Array List with all the write operations in the
	 * operation list
	 * @return
	 */
	public ArrayList<Operation> getWriteOperations() {
		
		/* Get the size of the operations list */
		int n = operations.size();
		
		/* Stop execution if there are no operations in the list */
		if(n <= 0) {
			return null;
		}
		
		/* The list of the write operations */
		ArrayList<Operation> writeOperations = new ArrayList<Operation>();
		
		/* Traverse the operations list */
		for(int i = 0; i < n; i += 1) {
			
			/* If the current operation is a write operation then add it to the
			 * write operations list */
			Operation operation = operations.get(i);
			if(operation.getType() == Operation.TYPE_WRITE) {
				writeOperations.add(operation);
			}
			
		}
		
		/* Return the write operations list */
		return writeOperations;
	}

	/**
	 * Sets the id of the transaction
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the list of operations in the transaction
	 * @param operations
	 */
	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}

	/**
	 * Sets the hash table with the pre committed atomic operations
	 * @param preCommit
	 */
	public void setPreCommit(Hashtable<String, Integer> preCommit) {
		this.preCommit = preCommit;
	}
	
	/**
	 * Sets the hash table with the pre written atomic operations
	 * @param preWrite
	 */
	public void setPreWrite(Hashtable<String, Integer> preWrite) {
		this.preWrite = preWrite;
	}
	
	/**
	 * 
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[id: " + id);
		
		int n = operations.size();
		for(int i = 0; i < n; i += 1) {
			builder.append(", [" + operations.get(i).toString() + "]");
		}
		
		builder.append("]");
		return builder.toString();
	}

}
