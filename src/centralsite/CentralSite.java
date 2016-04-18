package centralsite;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import common.Operation;
import common.Pair;
import common.Transaction;
import interfaces.CentralSiteInterface;
import interfaces.DataSiteInterface;

public class CentralSite implements CentralSiteInterface {
	
	/* The tag name of the class */
	public static final String TAG = CentralSite.class.getName();
	
	/* The verbose option */
	private static boolean verbose;
	
	/**
	 * Returns the current time stamp
	 * @return
	 */
	public static String getTimestamp()
	{
		/* Transform the date to the specified format and return it */
		return "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date()) + "]";
	}
	
	public static boolean isVerbose() {
		return verbose;
	}
	
	/**
	 * The main function.
	 * Expected arguments:
	 * 	[0]: Connection port (integer number)
	 *  [1]: Check deadlock delay (in milliseconds)
	 * 	[2]: verbose (true or false)
	 * @param argv
	 */
	public static void main (String[] args) {
		
		/* Try to start the Central Site object */
		try {
			
			/* Get the verbose option (true by default) */
			verbose = (args.length > 2) ? Boolean.parseBoolean(args[2]) : true;
			
			/* Get the port value */
			int port = Integer.parseInt(args[0]);
			
			/* Get the check deadlock delay */
			long checkDelay = Long.parseLong(args[1]);
			 
			/* Start the Central Site object */
			new CentralSite(port, checkDelay);
			println(TAG, "Central Site initiated at port " + port);
		
		}
		catch(NumberFormatException e) {
			System.out.println(TAG + " Number Format Exception: " + e.getMessage());
		}
		catch(NullPointerException e) {
			System.out.println(TAG + " Null Pointer Exception: " + e.getMessage());
		}
		catch (Exception e) {
			System.out.println(TAG + " Exception in main: " + e.getMessage());
		}
	}
	
	/**
	 * Prints the message on the console
	 * @param tag
	 * @param message
	 */
	public static void print(String tag, String message) {
		
		/* If verbose then print */
		if(verbose) {
			System.out.println(CentralSite.getTimestamp() + " " + tag + ": " + message);
		}
	}
	
	/**
	 * Prints the message on the console adding a new line before 
	 * printing the message
	 * @param tag
	 * @param message
	 */
	public static void println(String tag, String message) {
		
		/* If verbose then print */
		if(verbose) {
			System.out.println("");
			System.out.println(CentralSite.getTimestamp() + " " + tag + ": " + message);
		}
	}
	
	/**
	 * 
	 * @param port
	 */
	public static void printRegistry(int port) {
		
		try {
			Registry registry = LocateRegistry.getRegistry(port);
			String[] list = registry.list();
			int n = list.length;
			StringBuilder builder = new StringBuilder();
			builder.append("RMI Registry: <");
			for(int i = 0; i < n; i += 1) {
				if(i == 0) {
					builder.append(list[i]);
				}
				else {
					builder.append(", " + list[i]);
				}
			}
			builder.append(">");
			print(TAG, builder.toString());
		}
		catch(RemoteException e) {
			System.out.println(TAG + " Remote Exception: " + e.getMessage());
		}
		
	}
	
	public static void setVerbose(boolean verbose) {
		CentralSite.verbose = verbose;
	}

	/* The lock manager */
	private LockManager lockManager;
	
	/* The counter of registered data sites */
	private int siteCount = 0;
	
	/* The port where the central site is running */
	private int port;
	
	/* The check deadlock delay (in milliseconds) */
	private long checkDelay;
	
	/* Keep track of the number of deadlocks found */
	private int deadlockCount;
	
	/**
	 * Constructor of the class
	 * @param port
	 * @param checkDelay
	 */
	public CentralSite(int port, long checkDelay) {
		
		/* Indicates the connection port of the central site */
		this.port = port;
		
		/* Indicates the check deadlock delay (in milliseconds) */
		this.checkDelay = checkDelay;
		
		/* Initialize the deadlock count */
		deadlockCount = 0;
		
		/* Initiates the lock manager */
		lockManager = new LockManager();
		
		/* Try to set up the central site */
		try {
			
			/* Creates the registry and get its reference */
			Registry registry = LocateRegistry.createRegistry(port);
			
			/* Set the stub of the central site and register it to the RMI registry */
			CentralSiteInterface stub = (CentralSiteInterface) UnicastRemoteObject.exportObject(this, 0);
			registry.bind("c2pl", stub);
			
			/* Initiates the times for deadlocks checking */ 
			Timer checkWaitsForGraphTimer = new Timer();
			print(TAG, "Timer for the WFG checking initiated");
			
			/**/
			checkWaitsForGraphTimer.schedule(new TimerTask() {
				
				@Override
	            public void run() {
					CentralSite.printRegistry(port);
					checkDeadlocks();
					print(TAG, "Deadlock checked\n");
				}
				
			}, checkDelay, checkDelay);
			print(TAG, "Deadlock is checked every " + checkDelay + " miliseconds");
			
		}
		catch(RemoteException e) {
			System.out.println(TAG + " Remote Exception: " + e.getMessage());
		}
		catch(AlreadyBoundException e) {
			System.out.println(TAG + " Already Bound Exception: " + e.getMessage());
		}
		catch(Exception e) {
			System.out.println(TAG + " Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public synchronized void checkDeadlocks() {
		
		print(TAG, "Checking Deadlocks...");
		
		/* Get the deadlock information */
		Pair<Integer, ArrayList<Integer>> result = lockManager.checkDeadlocks();
		
		/* If there is result means there is a deadlock */
		if(result != null) {
			
			/* A deadlock was found, increment the counter by one */
			deadlockCount += 1;
			
			print(TAG, "Deadlock information obtained");
			
			/* Get the transaction to be aborted and its site */
			int transactionToAbort = result.getFirst().intValue();
			int siteId = Transaction.getSiteId(transactionToAbort);
			print(TAG, "Transaction to be aborted: " + transactionToAbort + " from site " + siteId);
			
			/* Try to abort the transaction */
			try {
				
				/* Get the stub to the data site */
				DataSiteInterface dataSiteStub = getDataSiteStub(siteId);
				
				if(dataSiteStub == null) {
					
					print(TAG, "Data site stub for site " + siteId + " is null");
					
				}
				else {
					
					/* Abort */
					dataSiteStub.abort();
					print(TAG, "Data site " + siteId + " aborted the transaction");
					
				}
				
			}
			catch(Exception e) {
				System.out.println(TAG + " Exception in checkDeadlocks: " + e.getMessage());
			}
			
			/* Get the list of unblocked sites */
			ArrayList<Integer> unblocked = result.getSecond();
			print(TAG, unblocked.size() + " sites will be unlocked");
			
			/* Traverse the unblocked sites list */
			for(Integer site: unblocked) {
				
				/* Try to unblock the site */
				try {
					
					/* Get the stub for the unblocked data site */
					DataSiteInterface dataSiteStub = getDataSiteStub(site);
					print(TAG, "Stub for data site " + site + " obtained");
					
					if(dataSiteStub != null) {
						print(TAG, "Stub says is not null");
						
						/* Unblock the site */
						dataSiteStub.unblock();
						print(TAG, "Data Site " + site + " unblocked!");
						
					}
					else {
						
						print(TAG, "Data site stub is null");
						
					}
					
				}
				catch(Exception e) {
					System.out.println(TAG + " Exception in checkDeadlocks: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		else {
			print(TAG, "Deadlock list is null = No deadlocks found");
		}
		
		/* Print the number of deadlocks so far */
		print(TAG, "Deadlocks found so far: " + deadlockCount);
	}

	/**
	 * 
	 * @return
	 */
	public long getCheckDelay() {
		return checkDelay;
	}

	/**
	 * 
	 * @param siteId
	 * @return
	 * @throws Exception
	 */
	private DataSiteInterface getDataSiteStub(int id) {
		
		println(TAG, "getDataSiteStub");
		print(TAG, "Get stub for data site " + id);
		
		/* Try to get the required data site stub */
		try {
			
			/* Access to the RMI registry */
			Registry registry = LocateRegistry.getRegistry(port);
			print(TAG, "Obtained acces to the registry");
			
			/* Get the data site stub */
			String siteStubName = "c2pl" + id;
			DataSiteInterface dataSiteStub = (DataSiteInterface) registry.lookup(siteStubName);
			
			/* Return the data site stub */
			print(TAG, "Data Site Stub for site " + id + " obtained");
			return dataSiteStub;
			
		}
		catch(RemoteException e) {
			System.out.println(TAG + " Remote Exception: " + e.getMessage());
		}
		catch(NotBoundException e) {
			System.out.println(TAG + " Not Bound Exception: " + e.getMessage());
		}
		catch(NullPointerException e) {
			System.out.println(TAG + " Null Pointer Exception: " + e.getMessage());
		}
		catch(Exception e) {
			System.out.println(TAG + " Exception in getDataSiteStub: " + e.getMessage());
			e.printStackTrace();
		}
		
		print(TAG, "Data Site Stub for site " + id + " is null");
		return null;
		
	}

	/**
	 * 
	 * @return
	 */
	public int getDeadlockCount() {
		return this.deadlockCount;
	}

	/**
	 * 
	 * @return
	 */
	public LockManager getLockManager() {
		print(TAG, "Lock Manager requested");
		return lockManager;
	}

	/**
	 * 
	 * @return
	 */
	public int getPort() {
		print(TAG, "port requested");
		return port;
	}

	/**
	 * 
	 * @return
	 */
	public int getSiteCount() {
		print(TAG, "Site count requested");
		return siteCount;
	}

	/**
	 * 
	 * @return
	 */
	public synchronized int nextSiteId() throws RemoteException {
		
		/**/
		siteCount += 1;
		print(TAG, "Next site id requested. Return " + siteCount);
		
		/**/
		return siteCount;
	}
	
	/**
	 * Releases the locks obtained by the given transaction
	 */
	public synchronized void releaseLock(Transaction transaction) throws RemoteException {
		
		println(TAG, "Transaction " + transaction.getId() + " requests to release its locks");
		
		/* Try to release the locks from the transaction */
		try {
			
			/* Get the list of unlocked sites */
			List<Integer> unblockedSites = lockManager.releaseLocks(transaction);
			print(TAG, "Number of unlocked sites by transaction " + transaction.getId() + " = " + unblockedSites.size());
			
			/* For every unblocked site in the list */
			int n = unblockedSites.size();
			for(int i = 0; i < n; i += 1) {
				
				/* Get the site id */
				int siteId = unblockedSites.get(i).intValue();
				print(TAG, "Unblocking site " + siteId);
				
				/* Obtain the stub to the site */
				print(TAG, "Requesting Data Site stub for site " + siteId);
				DataSiteInterface dataSiteStub = getDataSiteStub(siteId);
				print(TAG, "Stub obtained for data site " + siteId);
				
				if(dataSiteStub == null) {
					print(TAG, "Stub to data site " + siteId + " is null <WRONG>");
				}
				
				/* Unblock the data site */
				print(TAG, "Requesting unblocking data site " + siteId);
				dataSiteStub.write();
				print(TAG, "Information for site " + siteId + " was printed");
				dataSiteStub.unblock();
				print(TAG, "Data site " + siteId + " unblocked");
				
			}
			
			/* Print the lock manager state */
			lockManager.print();
			
		}
		catch(NullPointerException e) {
			System.out.println(TAG + " Null Pointer Exception in releaseLock: " + e.getMessage());
		}
		catch(Exception e) {
			System.out.println(TAG + " Exception in releaseLock: " + e.getMessage());
		}
	}

	/**
	 * Request a lock for the given operation
	 * @param op
	 * @return
	 */
	public synchronized boolean requestLock(Operation operation) throws RemoteException {
		
		println(TAG, "Operation from transaction " + operation.getTransactionId() + " requests a lock");
		
		/* Try to get the lock for the operation */
		try {
			
			/* Request to the lock manager a lock for the operation */
			boolean result = lockManager.requestLock(operation);
			print(TAG, "Operation from transaction " + operation.getTransactionId() + " lock request result is " + result);
			
			/* Print the lock manager state */
			lockManager.print();
			
			/* Returns the result of the lock request */
			return result;
			
		}
		catch(NullPointerException e) {
			System.out.println(TAG + " Null Pointer Exception in requestLock: " + e.getMessage());
		}
		catch(Exception e) {
			System.out.println(TAG + " Exception in requestLock: " + e.getMessage());
		}
		
		/* Since something went wrong return false */
		return false;
	}

	/**
	 * 
	 * @param checkDelay
	 */
	public void setCheckDelay(long checkDelay) {
		this.checkDelay = checkDelay;
	}
	
	/**
	 * 
	 * @param lockManager
	 */
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	
	/**
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * 
	 * @param siteCount
	 */
	public void setSiteCount(int siteCount) {
		this.siteCount = siteCount;
	}

}
