/*
 * Created on Feb 15, 2005
*/
package org.ome.srv.db.jena;

import java.rmi.RemoteException;

import org.ome.interfaces.FollowGroupService;
import org.ome.model.LSID;
import org.ome.model.IFollowGroup;

/**
 * @author josh
 */
public class JenaFollowGroupService implements FollowGroupService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.PredicateGroupService#getPredicateGroup(org.ome.LSID)
	 */
	public IFollowGroup getFollowGroup(LSID lsid) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

}
