/*
 * Created on Feb 10, 2005
 */
package org.ome.interfaces;


import org.ome.model.IFollowGroup;
import org.ome.model.LSID;

/**
 * @author josh
 */
public interface FollowGroupService  {

	//public IFollowGroup createFollowGroup() throws RemoteException;;
	public IFollowGroup getFollowGroup(LSID lsid) ;
	
	
}
