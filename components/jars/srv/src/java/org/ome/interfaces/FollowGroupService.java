/*
 * Created on Feb 10, 2005
 */
package org.ome.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.ome.model.IFollowGroup;
import org.ome.model.LSID;

/**
 * @author josh
 */
public interface FollowGroupService extends Remote {

	//public IFollowGroup createFollowGroup() throws RemoteException;;
	public IFollowGroup getFollowGroup(LSID lsid) throws RemoteException;;
	
	
}
