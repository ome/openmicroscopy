/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import org.ome.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * responsible for all user and group adminisistration as well as security
 * privielges and ownership.
 * 
 * @author josh
 */
public interface AdministrationService extends Remote {

	public String getSessionKey() throws RemoteException;

	public IExperimenter createExperimenter() throws RemoteException; // password
																	  // TODO
	public int createExperimenter(IExperimenter data) throws RemoteException;

	public IExperimenter getExperimenter(LSID experimenterId)
			throws RemoteException;

	public IExperimenter getExperimenter(LSID experimenterId,
			LSID predicateGroupId) throws RemoteException; // TODO use enums

}