/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import org.ome.model.FollowGroup;
import org.ome.model.LSObject;
import org.ome.model.LSID;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * responsible for the resolution of individual LSObjects
 * 
 * @author josh
 */
public interface GenericService extends Remote {

	public void setLSOjbect(LSObject obj) throws RemoteException;
	public void updateLSObject(LSObject obj) throws RemoteException;
	public LSObject getLSObject(LSID lsid) throws RemoteException;
	public LSObject getLSObjectWithFollowGroup(LSID lsid, FollowGroup fg) throws RemoteException;
	public List getLSObjectsByLSIDType(LSID type) throws RemoteException;
	public List getLSObjectsByClassType(Class klass) throws RemoteException;
	
}