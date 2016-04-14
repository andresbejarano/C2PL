package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataSiteInterface extends Remote {
	
	/**
	 * 
	 * @throws RemoteException
	 */
	public void abort() throws RemoteException;
	
	/**
	 * 
	 * @throws RemoteException
	 */
	public void unblock() throws RemoteException;
	
	/**
	 * Writes the content of the data site
	 * @throws RemoteException
	 */
	public void write() throws RemoteException;

}
