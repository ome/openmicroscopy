/*
 * Created on Feb 10, 2005
 */
package org.ome.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.ome.LSID;
import org.ome.texen.interfaces.IPredicateGroup;

/**
 * @author josh
 */
public interface PredicateGroupService extends Remote {

	//public IPredicateGroup createPredicateGroup() throws RemoteException;;
	public IPredicateGroup getPredicateGroup(LSID lsid) throws RemoteException;;
	
	
}
