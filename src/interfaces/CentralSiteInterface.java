package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.Operation;
import common.Transaction;

public interface CentralSiteInterface extends Remote {
	
	/**
	 * Returns the ID for the data site based on the site order managed by the
	 * central site.
	 * @return
	 * @throws RemoteException
	 */
	public int nextSiteId() throws RemoteException;
	
	/**
	 * 
	 * @param transaction
	 * @throws RemoteException
	 */
	public void releaseLock(Transaction transaction) throws RemoteException;
	
	/**
	 * 
	 * @param operation
	 * @return
	 * @throws RemoteException
	 */
	public boolean requestLock(Operation operation) throws RemoteException;

}
