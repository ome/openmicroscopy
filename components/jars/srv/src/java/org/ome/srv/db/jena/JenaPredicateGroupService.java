/*
 * Created on Feb 15, 2005
*/
package org.ome.srv.db.jena;

import java.rmi.RemoteException;

import org.ome.LSID;
import org.ome.interfaces.PredicateGroupService;
import org.ome.texen.interfaces.IPredicateGroup;

/**
 * @author josh
 */
public class JenaPredicateGroupService implements PredicateGroupService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.PredicateGroupService#getPredicateGroup(org.ome.LSID)
	 */
	public IPredicateGroup getPredicateGroup(LSID lsid) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

}
