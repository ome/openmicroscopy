/*
 * Created on Feb 10, 2005
 */
package org.ome.srv.logic;

import java.rmi.RemoteException;

import org.ome.model.FollowGroup;
import org.ome.model.IFollowGroup;
import org.ome.model.LSID;
import org.ome.interfaces.FollowGroupService;

/**
 * @author josh
 */
public class FollowGroupServiceImpl extends AbstractService implements FollowGroupService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.PredicateGroupService#getPredicateGroup(org.ome.LSID)
	 */
	public IFollowGroup getFollowGroup(LSID lsid) throws RemoteException {
		return new FollowGroup(db.getLSObject(lsid));
	}

	
}
