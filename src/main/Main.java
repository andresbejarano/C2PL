package main;

import java.util.ArrayList;

import common.Transaction;
import datasite.DataManager;
import datasite.TransactionManager;

/**
 * This class is for testing purposes
 * @author AndresMauricio
 *
 */
public class Main {
	
	/* The tag name of the class */
	public static final String TAG = Main.class.getName();

	/**
	 * The main function
	 * @param args
	 */
	public static void main(String[] args) {	
		try {
			TransactionManager tm = new TransactionManager(1);
			tm.setHistory(tm.generateTransactions(tm.readCommandsFile("transactions/transactions_few_1.txt")));
			System.out.println("YAY");
			ArrayList<Transaction> history = tm.getHistory();
			
			int n = history.size();
			System.out.println("Number of transactions: " + n);
			
			DataManager.create();
			System.out.println("Database created");
			
			for(int i = 0; i < n; i += 1) {
				Transaction t = history.get(i);
				System.out.println(t.toString());
				t.executeOperations();
				System.out.println("Operations executed");
				t.commit();
				System.out.println("Operations committed. Check database");
			}
			
			DataManager.show();
		}
		catch(Exception e) {
			System.out.println("Something went wrong. " + e.getMessage());
		}
	}

}
