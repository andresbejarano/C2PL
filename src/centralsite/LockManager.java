package centralsite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import common.Lock;
import common.Operation;
import common.Pair;
import common.Transaction;
import waitforgraph.WaitForGraph;

public class LockManager {
	
	/* The tag name of the class */
	public static final String TAG = LockManager.class.getName();
	
	/* The lock table */
	private Hashtable<String, ArrayList<Lock>> lockTable;
	
	/* The queue table */
	private Hashtable<String, ArrayList<Operation>> queueTable;
	
	/**
	 * Constructor of the class
	 */
	public LockManager() {
		lockTable = new Hashtable<String, ArrayList<Lock>>();
		queueTable = new Hashtable<String, ArrayList<Operation>>();
	}
	
	/**
	 * Aborts the transaction with the given id. Returns the list
	 * of unblocked sites
	 * @param transactionId
	 * @return
	 */
	public ArrayList<Integer> abortTransaction(int transactionId) {
		
		/*  */
		Transaction transaction = new Transaction(transactionId);
		/*  */
		try {
			
			/*  */
			print();
			
			/*  */
			removeBlockedOperations(transaction);
			
			/*  */
			print();
			
			/*  */
			ArrayList<Integer> unblocked = releaseLocks(transaction);
			
			/*  */
			print();
			
			/*  */
			return unblocked;
		}
		catch(Exception e) {
			System.out.println(TAG + " Exception while aborting transaction " 
					+ transactionId + ": " + e.getMessage());
			
			/*  */
			return new ArrayList<Integer>();
		}
		
	}
	
	/**
	 * Check deadlocks with the current information in the lock table 
	 * and the queue table
	 * @return
	 */
	public Pair<Integer, ArrayList<Integer>> checkDeadlocks() {
		
		/* Initiate the Wait For Graph */
		WaitForGraph wfg = new WaitForGraph();
		
		/* Traverse the key table (data items) from the lock table
		 * for finding transaction dependencies */
		for(String item : lockTable.keySet()) {
			
			/* The list of locks associated with the current item */
			ArrayList<Lock> locks = lockTable.get(item);
			
			/* The list of blocked operations over the current item */
			ArrayList<Operation> blockedOperations = queueTable.get(item);
			
			/* If there is at least one blocked operation then build the
			 * dependency */
			if(blockedOperations != null && blockedOperations.size() != 0) {
				
				/* The size of the locks list */
				int n = locks.size();
				
				/* Traverse the locks list */
				for(int i = 0; i < n; i += 1) {
					
					/* The id of the first transaction (node) of the dependency */
					Integer transaction1 = locks.get(i).getTransactionId();
					
					/* The size of the blocked operations list */
					int m = blockedOperations.size();
					
					/* Traverse the blocked operations list */
					for(int j = 0; j < m; j += 1) {
						
						/* The id of the second transaction (node) of the dependency */
						Integer transaction2 = blockedOperations.get(j).getTransactionId();
						
						/* If both transaction id's are different then add the dependency to
						 * the wait for graph */
						if(!transaction1.equals(transaction2)) {
							wfg.addDependency(transaction1, transaction2);
						}
					}
				}
			}
		}
		
		/* Check if the wait for graph has cycles on it */
		List<Integer> edgesToRemove = wfg.checkCycles();
		
		/* If there are edges in the list then there is a deadlock, otherwise
		 * return null */
		if(edgesToRemove != null) {
			
			/* Get the deadlocked transaction id */
			Integer abortingTransaction = edgesToRemove.get(1);
			
			/* Display deadlock information */
			System.out.println("Deadlock Detected!!!!!!!");
			System.out.println("Deadlock in " + edgesToRemove.get(0) + 
					" to " + edgesToRemove.get(1));
			System.out.println("Transaction to be aborted: " + abortingTransaction.intValue());
			
			/**/
			ArrayList<Integer> unblocked = abortTransaction(abortingTransaction.intValue());
			
			/**/
			return new Pair<Integer, ArrayList<Integer>>(abortingTransaction, unblocked);
		}
		else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param item
	 * @return
	 */
	public Operation dequeueOperation(String item) {
		
		/* If the queue table doesn't have the given item 
		 * then return null */
		if(!queueTable.containsKey(item)) {
			return null;
		}
		
		/* Get the list of queued operations */
		ArrayList<Operation> operations = queueTable.get(item);
		
		/* The size of the queued operations list */
		int n = operations.size();
		
		/* If the list has no operations then do a sanity 
		 * check and return null, otherwise remove operation
		 * from the queue table */
		if(n <= 0) {
			queueTable.remove(item);
			return null;
		}
		else {
			
			/* If the first element in the list is compatible with
			 * the lock table then remove it from the queue table
			 * and put it in the lock table, otherwise return null */
			if(isLockCompatible(operations.get(0).getLock())) {
				
				/* Get the operations */
				Operation operation = operations.remove(0);
				
				/* Sanity check */
				if(operations.size() == 0) {
					queueTable.remove(item);
				}
				
				/* Return the dequeued operation */
				return operation;
				
			}
			else {
				return null;
			}
			
		}
		
	}
	
	/**
	 * 
	 * @param transactionId
	 * @param item
	 * @return
	 */
	public Lock findLock(int transactionId, String item) {
		
		/* Get the locks holding the item */
		ArrayList<Lock> locks = lockTable.get(item);
		
		/* If there is no locks list then return null */
		if(locks == null) {
			return null;
		}
		
		/* The size of the locks list */
		int n = locks.size();
		
		/* Traverse the locks list */
		for(int i = 0; i < n; i += 1) {
			
			/* Get the current lock */
			Lock currentLock = locks.get(i);
			
			/* If the current lock has the given transaction id 
			 * then return it */
			if(currentLock.getTransactionId() == transactionId) {
				return currentLock;
			}
		}
		
		/* Since no lock was found then return null */
		return null;
		
	}
	
	/**
	 * Returns the list of locks held by the given transaction id
	 * @param transactionId
	 * @return
	 */
	public ArrayList<Lock> findLocks(int transactionId) {
		
		/* The list of locks */
		ArrayList<Lock> locks = new ArrayList<Lock>();
		
		/* The set of keys (items) from the lock table */
		Set<String> keys = lockTable.keySet();
		
		/* Traverse the keys set */
		for(String key : keys) {
			
			/* The list of locks locked by the current key (item) */
			ArrayList<Lock> lockedLocks = lockTable.get(key);
			
			/* The size of the locked locks list */
			int n = lockedLocks.size();
			
			/* Traverse the locked locks list */
			for(int i = 0; i < n; i += 1) {
				
				/* If the current locked lock has the same transaction id 
				 * then add it to the locks list */
				if(lockedLocks.get(i).getTransactionId() == transactionId) {
					locks.add(lockedLocks.get(i));
				}
			}
			
		}
		
		/* Returns the lock list */
		return locks;
		
	}
	
	/**
	 * 
	 * @return
	 */
	public Hashtable<String, ArrayList<Lock>> getLockTable() {
		return lockTable;
	}
	
	/**
	 * 
	 * @return
	 */
	public Hashtable<String, ArrayList<Operation>> getQueueTable() {
		return queueTable;
	}
	
	/**
	 * Inserts the given lock into the lock table
	 * @param lock
	 * @return
	 */
	public boolean insertLock(Lock lock) {
		
		/* The item where the lock will be placed */
		String item = lock.getItem();
		
		/* Generate the field for the item if the lock 
		 * table doesn't have it yet */
		if(!lockTable.containsKey(item)) {
			lockTable.put(item, new ArrayList<Lock>());
		}
		
		/* Add the lock */
		return lockTable.get(item).add(lock);
		
	}

	/**
	 * Checks if the given lock is compatible with the lock table
	 * @param lock
	 * @return
	 * @throws Exception
	 */
	public boolean isLockCompatible(Lock lock) {
		
		/* Get the locked item */
		String item = lock.getItem();
		
		/* Get the list of locks holding the given item */
		ArrayList<Lock> locks = lockTable.get(item);
		
		/* If no lock is holding the item then return true */
		if(locks == null) {
			return true;
		}
		
		/* Proceed based on the lock type */
		switch(lock.getType()) {
		
			/* read lock */
			case Lock.TYPE_READ: {
				
				/* Get the size of the locks list */
				int n = locks.size();
				
				/* Traverse all the locks holding the item */
				for(int i = 0; i < n; i += 1) {
					
					/* Get the current lock */
					Lock currentLock = locks.get(i);
					
					/* Both locks cannot come from the same transaction. The
					 * current lock cannot be greater than a read lock */
					if(currentLock.getTransactionId() != lock.getTransactionId() && 
							currentLock.getType() >= Lock.TYPE_WRITE) {
						return false;
					}
				}
				
				break;
			}
			
			/* write lock */
			case Lock.TYPE_WRITE: {
				
				/* Get the size of the locks list */
				int n = locks.size();
				
				/* Traverse all the locks holding the item */
				for(int i = 0; i < n; i += 1) {
					
					/* Get the current lock */
					Lock currentLock = locks.get(i);
					
					/* Only one transaction must hold a lock over the item */
					if(currentLock.getTransactionId() != lock.getTransactionId()) {
						return false;
					}
				}
				
				break;
				
			}
			
			/* Invalid lock type */
			default:
				System.out.println(TAG + " Lock is imcompatible");
				return false;
		
		}
		
		/* Since validation passed then return true */
		return true;
		
	}

	/**
	 * 
	 */
	public void print() {
		
		/* Print the current timestamp */
		printTimestamp();
		
		/* Print the lock table */
		printLockTable();
		
		/* Print the queue (wait table */
		printQueueTable();
		
		System.out.println("\n");
		
	}

	/**
	 * Prints the lock table
	 */
	public void printLockTable() {
		
		/* The title of the table */
		System.out.println("Lock Table");
		
		/* Print something if the lock table is empty */
		if(lockTable.isEmpty()) {
			System.out.println("[]");
			return;
		}
		
		/* Traverse the keys (items) of the lock table */
		for(String item : lockTable.keySet()) {
			
			/* Get the locks list associated with the current item */
			ArrayList<Lock> locks = lockTable.get(item);
			
			/* The size of the locks list */
			int n = locks.size();
			
			/* Traverse the locks list */
			for(int i = 0; i < n; i += 1) {
				
				/* Print the information of the lock */
				System.out.println(locks.get(i).toString());
				
			}
			
			System.out.println();
			
		}
	}
	
	/**
	 * Prints the queue (wait) table
	 */
	public void printQueueTable() {
		
		/* The title of the table */
		System.out.println("Queue Table");
		
		/* Print something if the queue table is empty */
		if(queueTable.isEmpty()) {
			System.out.println("[]");
			return;
		}
		
		/* Traverse the keys (items) of the queue table */
		for(String item : queueTable.keySet()) {
			
			/* Get the operations list waiting for the current item */
			ArrayList<Operation> operations = queueTable.get(item);
			
			/* The size of the operations list */
			int n = operations.size();
			
			/* Traverse the operations list */
			for(int i = 0; i < n; i += 1) {
				
				/* Print the information of the operation */
				System.out.println(operations.get(i).toString());
				
			}
			
			System.out.println();
			
		}
	}
	
	/**
	 * Prints the current timestamp
	 */
	public void printTimestamp() {
		
		/* Get the current date and transform its format */
		Date currentDate = new Date();
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(currentDate);
		
		/* Write the timestamp */
		System.out.println("Timestamp: " + timestamp);
		
	}

	/**
	 * 
	 * @param item
	 * @param operation
	 * @return
	 */
	public boolean queueOperation(String item, Operation operation) {
		
		/* If the queue table doesn't have the item in it then 
		 * generate the field */
		if(!queueTable.containsKey(item)) {
			queueTable.put(item, new ArrayList<Operation>());
		}
		
		/* Return whether the operation could be added to the table */
		return queueTable.get(item).add(operation);
		
	}
	
	/**
	 * Releases the locks of the given transaction. Returns a list with the sites
	 * that where unlocked
	 * @param transaction
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Integer> releaseLocks(Transaction transaction) throws Exception {
		
		/* The list of unlocked sites */
		ArrayList<Integer> unlockedSites = new ArrayList<Integer>();
		
		/* Get the locks from the transaction */
		ArrayList<Lock> locks = findLocks(transaction.getId());
		
		/* The size of the locks list */
		int n = locks.size();
		
		/* Traverse the locks list */
		for(int i = 0; i < n; i += 1) {
			
			/* Get the current lock */
			Lock currentLock = locks.get(i);
			
			/* Remove the lock from the lock table */
			removeLock(currentLock);
			
			/* Get the next operation (first in queue) */
			Operation operation = dequeueOperation(currentLock.getItem());
			
			/* If the next operation is not null then lock it for the
			 * respective transaction */
			if(operation != null) {
				
				/* Get the lock for the next operation */
				Lock nextLock = operation.getLock();
				
				/* If the next lock is compatible then process is */
				if(isLockCompatible(nextLock)) {
					
					/* Get any previous lock of the transaction with the specific item */
					Lock oldLock = findLock(nextLock.getTransactionId(), nextLock.getItem());
					
					/* If there was not lock then insert the new one to the lock table,
					 * otherwise upgrade the previous lock */
					if(oldLock == null) {
						
						/* Insert the new lock to the lock table */
						insertLock(nextLock);
					}
					else {
						
						/* Upgrade the current lock */
						oldLock.upgradeType(nextLock.getType());
					}
					
					/* Get the id of the site with the transaction that got the lock */
					int siteId = Transaction.getSiteId(operation.getTransactionId());
					
					/* Add the site id to the unlocked sites list */
					unlockedSites.add(siteId);
				}
			}
		}
		
		/* Print the current timestamp */
		printTimestamp();
		
		System.out.println("Transaction " + transaction.getId() + " releases all of its locks");
		
		/* Returns the list of the unlockes sites */
		return unlockedSites;
		
	}
	
	/**
	 * Removes blocked operations from the given transaction
	 * @param transaction
	 */
	public void removeBlockedOperations(Transaction transaction) {
		
		/* The transaction id */
		int transactionId = transaction.getId();
		
		/* The set of keys from the queue table */
		Set<String> keys = queueTable.keySet();
		
		/* Traverse the keys set */
		for(String key : keys) {
			
			/* The list of... */
			//TODO
			ArrayList<Integer> remove = new ArrayList<Integer>();
			ArrayList<Operation> operations = queueTable.get(key);
			int n = operations.size();
			
			for(int i = n - 1; i >= 0; i -= 1) {
				Operation operation = operations.get(i);
				if(operation.getTransactionId() == transactionId) {
					remove.add(i);
				}
			}
			
			for(int i : remove) {
				queueTable.get(key).remove(i);
			}
			
		}
		
	}
	
	/**
	 * Removes the given lock from the lock table
	 * @param lock
	 * @return
	 */
	public boolean removeLock(Lock lock) {
		
		/* The item where the lock will be removed */
		String item = lock.getItem();
		
		/* The list of locks over the item */
		ArrayList<Lock> locks = lockTable.get(item);
		
		/* The size of the locks list */
		int n = locks.size();
		
		/* Traverse the locks list */
		for(int i = 0; i < n; i += 1) {
			
			/* Get the current lock */
			Lock currentLock = locks.get(i);
			
			/* If both locks comes from the same transaction the remove it */
			if(currentLock.getTransactionId() == lock.getTransactionId()) {
				
				/* Remove the current lock */
				lockTable.get(item).remove(currentLock);
				
				/* If no more locks over the item then remove it from the 
				 * lock table */
				if(lockTable.get(item).size() == 0) {
					lockTable.remove(item);
				}
				
				/* Since the lock was removed then return true */
				return true;
				
			}
		}
		
		/* Return false since the lock was not removed */
		return false;
		
	}
	
	/**
	 * 
	 * @param operation
	 * @return
	 */
	public boolean requestLock(Operation operation) throws Exception {
		
		/* Get the lock of the operation */
		Lock lock = operation.getLock();
		
		/* If the lock from the operation is compatible with the
		 * current lock information in the lock table. Otherwise
		 * put the operation in the queue */
		if(isLockCompatible(lock)) {
			
			/* Get the current lock holding the item */
			Lock oldLock = findLock(operation.getTransactionId(), operation.getItem());
			
			/* If the lock doesn't exist then insert it, otherwise update the current
			 * holding lock */
			if(oldLock == null) {
				insertLock(lock);
			}
			else {
				oldLock.upgradeType(lock.getType());
			}
			
			/* Indicate the lock was granted */
			return true;
		}
		else {
			
			/* Queue the operation */
			queueOperation(operation.getItem(), operation);
			
			/* Indicate the lock was not granted */
			return false;
		}
		
	}
	
	/**
	 * 
	 * @param lockTable
	 */
	public void setLockTable(Hashtable<String, ArrayList<Lock>> lockTable) {
		this.lockTable = lockTable;
	}
	
	/**
	 * 
	 * @param queueTable
	 */
	public void setQueueTable(Hashtable<String, ArrayList<Operation>> queueTable) {
		this.queueTable = queueTable;
	}

}
