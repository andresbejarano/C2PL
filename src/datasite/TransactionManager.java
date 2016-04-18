package datasite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Operation;
import common.Transaction;

public class TransactionManager {
	
	/* The tag name of the class */
	public static final String TAG = TransactionManager.class.getName();
	
	/* The history of transactions */
	public ArrayList<Transaction> history;

	/* The ID of the site where the transaction manager belongs */
	private int siteId;

	/* Local transaction id counter */
	private int transactionIdCount = 1;

	/* Offset for transaction id generation */
	private int transactionIdOffset = 10000;

	/**
	 * Constructor of the class
	 * @param id
	 */
	public TransactionManager(int siteId) {
		this.siteId = siteId;
		history = new ArrayList<Transaction>();
	}

	/**
	 * Transforms the commands list into transactions and stores them in an array list
	 * @param commands
	 */
	public ArrayList<Transaction> generateTransactions(ArrayList<String> commands) throws Exception {
		
		/* The total number of commands in the list */
		int n = commands.size();
		
		/* Stop execution if the list has no commands */
		if(n <= 0) {
			return null;
		}
		
		/* The list of transactions */
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		
		/* Reference to the current transaction */
		Transaction transaction = null;
		
		/* Traverse the commands list */
		for(int i = 0; i < n; i += 1) {
			
			/* Get the current command */
			String command = commands.get(i);
			
			/* If the command has the word "transaction" then start a new one and save the 
			 * previous one if any, otherwise keep adding operations to the current 
			 * transaction*/
			if(command.toLowerCase().contains("transaction")) {
				
				/* If there is a current transaction in process then add it to the history */
				if(transaction != null) {
					transactions.add(transaction);
				}
				
				/* Initiate a new transaction */
				transaction = new Transaction(getNewTransactionId());
			}
			else {
				
				/* Get the type of the command (first character of the string) */
				char commandType = command.trim().charAt(0);
				
				/* Proceed based on the command type */
				switch(commandType) {
				
					/* read command */
					case 'r': {
						
						/* Match the command with the regex pattern */
						Pattern pattern = Pattern.compile(".*\\((.*)\\).*");
						Matcher matcher = pattern.matcher(command);
						
						/* If command matches with a read operation structure then add it
						 * to the current transaction */
						if(matcher.matches()) {
							
							/* Get the item of the operation and create it */
							String item = matcher.group(1);
							Operation operation = new Operation(
									transaction.getId(), Operation.TYPE_READ, item);
							
							/* Add the operation to the current transaction */
							transaction.addOperation(operation);
							
						}
						else {
							throw new Exception("Read operation " + command + " has wrong format");
						}
						
						break;
					}
					
					/* write command */
					case 'w': {
						
						/* Match the command with the regex pattern */
						Pattern pattern = Pattern.compile(".*\\((.*)\\).*");
						Matcher matcher = pattern.matcher(command);
						
						/* If command matches with a write operation structure then add it
						 * to the current transaction */
						if(matcher.matches()) {
							
							/* Get the item of the operation and create it */
							String item = matcher.group(1);
							Operation operation = new Operation(
									transaction.getId(), Operation.TYPE_WRITE, item);
							
							/* Add the operation to the current transaction */
							transaction.addOperation(operation);
							
						}
						else {
							throw new Exception("Write operation " + command + " has wrong format");
						}
						
						break;
					}
					
					/* math command */
					case 'm': {
						
						/* Match the command with the regex pattern for binary operations */
						Pattern pattern = Pattern.compile("m(.*)\\=(.*)(\\+|\\-|\\*|\\/)(.*);");
						Matcher matcher = pattern.matcher(command);
						
						/* If command matches with a binary operation structure then add it
						 * to the current transaction */
						if(matcher.matches()) {
							
							/* Get the item, the operands and the operator of the operation 
							 * and create it */
							String item = matcher.group(1);
							String operand1 = matcher.group(2);
							String operator = matcher.group(3);
							String operand2 = matcher.group(4);
							Operation operation = new Operation(transaction.getId(), 
									Operation.TYPE_MATH, item, operand1, operator, operand2);
							
							/* Add the operation to the current transaction */
							transaction.addOperation(operation);
							
						}
						else {
							
							/* Match the command with the regex pattern for unary operations */
							pattern = Pattern.compile("m(.*)\\=(.*);");
							matcher = pattern.matcher(command);
							
							/* If command matches with an unary operation structure then add it
							 * to the current transaction */
							if(matcher.matches()) {
								
								/* Get the item, the operands and the operator of the operation 
								 * and create it */
								String item = matcher.group(1);
								String operand1 = matcher.group(2);
								String operator = "+";
								String operand2 = "0";
								Operation operation = new Operation(transaction.getId(), 
										Operation.TYPE_MATH, item, operand1, operator, operand2);
								
								/* Add the operation to the current transaction */
								transaction.addOperation(operation);
								
							}
							else {
								throw new Exception("Math operation " + command + " has wrong format");
							}
							
						}
						
						break;
					}
					
					/* Undefined operations */
					default:
						throw new Exception("Undefined operation " + command);
				
				}
			}
		}
		
		/* Check for any left generated transaction */
		if(transaction != null) {
			transactions.add(transaction);
		}
		
		/* Return the list of transactions */
		return transactions;
	}

	public ArrayList<Transaction> getHistory() {
		return history;
	}
	
	/**
	 * Returns the id of the transaction manager
	 * @return
	 */
	public int getSiteId() {
		return siteId;
	}
	
	/**
	 * Generates a new transaction id
	 * @return
	 */
	private int getNewTransactionId() {
		
		/* Generates the new transaction id */
		int transactionId = siteId * transactionIdOffset + transactionIdCount;
		
		/* Increments the local transaction id counter */
		transactionIdCount += 1;
		
		/* Return the generated transaction id */
		return transactionId;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTransactionIdCount() {
		return transactionIdCount;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTransactionIdOffset() {
		return transactionIdOffset;
	}
	
	/**
	 * Loads the file (found in the given file path)
	 * @param filepath
	 * @throws Exception
	 */
	public void load(String filepath) throws Exception {
		
		ArrayList<String> fileData = readCommandsFile(filepath);
		ArrayList<Transaction> transactions = generateTransactions(fileData);
		setHistory(transactions);
		
	}
	
	/**
	 * Returns the first transaction in the history. The transaction is
	 * previously removed from the history. If the history is empty then
	 * it is returned null
	 * @return
	 */
	public Transaction popTransaction() {
		
		/* If there is at least one transaction in the history */
		if(history.size() > 0) {
			
			/* Return the first transaction in the history */
			return history.remove(0);
		}
		
		/* Since history is empty then return null */
		return null;
	}
	
	/**
	 * Reads the file containing the commands. Returns an array
	 * list with the commands from the file.
	 * @param filepath
	 * @return The ArrayList<String> with the commands from the file
	 */
	public ArrayList<String> readCommandsFile(String filepath) {
		
		/* The commands list */
		ArrayList<String> commands = new ArrayList<String>();
		
		/* Try to read the file */
		try {
			
			/* Open the file and initialize the reader */
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			
			/* Read the file line by line */
			String line;
			while((line = reader.readLine()) != null && !line.trim().isEmpty()) {
				
				/* Add the line to the commands list */
				commands.add(line);
			}
			
			/* Close the reader */
			reader.close();
			
		}
		catch(IOException e) {
			System.out.println("IO Exception: " + e.getMessage());
		}
		
		/* Return the commands list */
		return commands;
	}
	
	/**
	 * 
	 * @param history
	 */
	public void setHistory(ArrayList<Transaction> history) {
		this.history = history;
	}
	
	/**
	 * 
	 * @param transactionIdCount
	 */
	public void setTransactionIdCount(int transactionIdCount) {
		this.transactionIdCount = transactionIdCount;
	}
	
	/**
	 * 
	 * @param transactionIdOffset
	 */
	public void setTransactionIdOffset(int transactionIdOffset) {
		this.transactionIdOffset = transactionIdOffset;
	}

}
