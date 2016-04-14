package datasite;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;

import common.Operation;
import common.Transaction;
import interfaces.CentralSiteInterface;
import interfaces.DataSiteInterface;

public class DataSite implements Runnable, DataSiteInterface {
	
	/* The tag name of the class */
	public static final String TAG = DataSite.class.getName();
	
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
	
	/**
	 * The main function for the data site
	 * Expected arguments:
	 * 	[0]: Host address (string URL or IP address)
	 * 	[1]: Host connection port (integer number)
	 * 	[2]: Transaction file
	 *  [3]: Verbose (true or false)
	 * @param args
	 */
	public static void main(String[] args) {
		
		/* Get the verbose option (true by default) */
		verbose = (args.length > 3) ? Boolean.parseBoolean(args[3]) : true;
		println(TAG, "Initiating Data Site");
		
		/* Create the data manager */
		DataManager.create();
		print(TAG, "Data Manager initiated");
		
		/* The host address and the port of the Central Site */
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		System.out.println("Connecting to Central Site at " + host + ":" + port);
		
		/* Get the transactions file from the arguments */
		String transactionsFile = args[2];
		print(TAG, "Loaded transactions file " + transactionsFile);
		
		/* Initiate the data site */
		(new DataSite(host, port, transactionsFile)).run();
		
	}
	
	/**
	 * Prints the message on the console
	 * @param tag
	 * @param message
	 */
	public static void print(String tag, String message) {
		
		/* If verbose then print */
		if(verbose) {
			System.out.println(DataSite.getTimestamp() + " " + tag + ": " + message);
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
			System.out.println(DataSite.getTimestamp() + " " + tag + ": " + message);
		}
	}
	
	/* The transaction manager */
	private TransactionManager transactionManager;
	
	/* The id of the site */
	private int id;
	
	/* The stub to the central site */
	private CentralSiteInterface centralSiteStub;

	/* Indicates if the site is blocked */
	private boolean blocked;

	/* Indicates if the site has to abort something */
	public boolean abort;

	/**
	 * The constructor of the class
	 * @param id
	 * @param transactionsFile
	 */
	public DataSite(String host, int port, String transactionsFile) {
		
		blocked = false;
		abort = false;
		
		/* Try to set up the site */
		try {
			
			/* Setup the communication with the central site using RMI */
			Registry registry = LocateRegistry.getRegistry(host, port);
			centralSiteStub = (CentralSiteInterface) registry.lookup("c2pl");
			
			/* Set the ID for the site (based on the counter in the Central Site) */
			id = centralSiteStub.nextSiteId();
			
			/* Initiate the transaction manager and load the file */
			transactionManager = new TransactionManager(id);
			transactionManager.load(transactionsFile);
			
			/* Register the site using RMI */
			DataSiteInterface dataSiteStub = (DataSiteInterface) UnicastRemoteObject.exportObject(this, 0);
			String siteStubName = "c2pl" + id;
			registry.bind(siteStubName, dataSiteStub);
			print(TAG, "Registered Data Site stub as " + siteStubName);
			
			/* Indicate the data site is ready */
			System.out.println(DataSite.getTimestamp() + " " + "Data site " + id + " is ready");
			
		}
		catch(RemoteException e) {
			System.out.println(TAG + " Remote Exception: " + e.getMessage());
		}
		catch(NotBoundException e) {
			System.out.println(TAG + " Not Bound Exception: " + e.getMessage());
		}
		catch(IndexOutOfBoundsException e) {
			System.out.println(TAG + " Index Out Of Bounds Exception: " + e.getMessage());
		}
		catch(AlreadyBoundException e) {
			System.out.println(TAG + " Already Bound Exception: " + e.getMessage());
		}
		catch(Exception e) {
			System.out.println(TAG + " Exception: " + e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void abort() throws RemoteException {
		
		abort = true;
		print(TAG, "Site is now aborted");
		
		unblock();
		//notifyAll();
	}
	
	/**
	 * 
	 */
	public synchronized void blocked() {
		
		/**/
		try {
			
			/**/
			blocked = true;
			
			/**/
			while(blocked) {
				
				/**/
				System.out.println(DataSite.getTimestamp() + " " + "Site " + id + " blocked. Waiting for events...");
				
				/**/
				Thread.sleep(1000);
				//wait();
				//TODO instead of wait try sleep a given amount of time
			}
		}
		catch(InterruptedException e) {
			System.out.println(TAG + " Interrupted Exception: " + e.getMessage());
		}
		catch(NullPointerException e) {
			System.out.println(TAG + " Null Pointer Exception: " + e.getMessage());
		}
		catch(Exception e) {
			System.out.println(TAG + " Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		/* Infinite loop */
		while(true) {
			
			/* Try to process the transactions */
			try {
				
				/**/
				synchronized(this) {
					
					println(TAG, "Initiate a new cycle");
					
					/* Get the transaction */
					Transaction transaction = transactionManager.popTransaction();
					print(TAG, "Transaction obtained");
					
					/*  */
					if(transaction == null) {
						
						print(TAG, "Transaction is null");
						
						/**/
						System.out.println(DataSite.getTimestamp() + " " + "Site " + id + " waiting for next transaction");
						
						/**/
						blocked();
						print(TAG, "Site " + id + " is now blocked");
						
					}
					else {
						
						String tag = "[T" + transaction.getId() + "]";
						System.out.println(DataSite.getTimestamp() + " " + "Starting transaction " + tag);
						
						/**/
						for(Operation operation : transaction.getOperations()) {
							
							/**/
							switch(operation.getType()) {
							
								/**/
								case Operation.TYPE_READ: {
									
									/**/
									boolean result = centralSiteStub.requestLock(operation);
									
									/**/
									if(!result) {
										blocked();
									}
									
									/**/
									if(abort) {
										break;
									}
									
									/**/
									transaction.executeOperation(operation);
									break;
								}
								
								/**/
								case Operation.TYPE_WRITE: {
									
									/**/
									boolean result = centralSiteStub.requestLock(operation);
									
									/**/
									if(!result) {
										blocked();
									}
									
									/**/
									if(abort) {
										break;
									}
									
									/**/
									transaction.executeOperation(operation);
									break;
								}
								
								/**/
								case Operation.TYPE_MATH: {
									
									/**/
									transaction.executeOperation(operation);
									break;
								}
							}
							
							/**/
							if(abort) {
								break;
							}
							
						}
						
						/**/
						if(abort) {
							
							/**/
							System.out.println(DataSite.getTimestamp() + " " + "Transaction " + transaction.getId() + " is aborted");
							
							/**/
							abort = false;
						}
						else {
							
							/**/
							transaction.commit();
							
							/**/
							centralSiteStub.releaseLock(transaction);
							
							/**/
							System.out.println(DataSite.getTimestamp() + " " + "Transaction " + transaction.getId() + " completed");
							
						}
					}
				}
			}
			catch(RemoteException e) {
				System.out.println(TAG + " Remote Exception: " + e.getMessage());
			}
			catch(Exception e) {
				System.out.println(TAG + " Exception: " + e.getMessage());
			}
		}
	}
	
	@Override
	public void unblock() throws RemoteException {
		
		println(TAG, "Preparing to unblock the site");
		
		/* Indicate the site is not blocked anymore */
		blocked = false;
		print(TAG, "Site is now unblocked");
		
		/* Notify the threads */
		//notifyAll();
	}

	@Override
	public void write() throws RemoteException {
		System.out.println(DataSite.getTimestamp() + " " + TAG + " Writing the info for site " + id);
	}

}
