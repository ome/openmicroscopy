/*
 * Created on Feb 14, 2005
*/
package org.ome.srv.logic;

import java.rmi.RemoteException;
import java.util.List;

import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.interfaces.GenericService;
import org.ome.model.FollowGroup;

/**
 * @author josh
 */
public class GenericServiceImpl extends AbstractService implements GenericService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID)
	 */
	public LSObject getLSObject(LSID lsid) throws RemoteException {
		return db.getLSObject(lsid);
	}
	
	//TODO =============================
	
	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#setLSOjbect(org.ome.LSObject)
	 */
	public void setLSOjbect(LSObject obj) {
		// TODO Auto-generated method stub
		throw new RuntimeException("implement me");

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#updateLSObject(org.ome.LSObject)
	 */
	public void updateLSObject(LSObject obj) {
		// TODO Auto-generated method stub
		throw new RuntimeException("implement me");

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID, org.ome.texen.srv.PredicateGroup)
	 */
	public LSObject getLSObject(LSID lsid, FollowGroup fg) {
		// TODO Auto-generated method stub
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObjectWithFollowGroup(org.ome.model.LSID, org.ome.model.FollowGroup)
	 */
	public LSObject getLSObjectWithFollowGroup(LSID arg0, FollowGroup arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObjectsByLSIDType(org.ome.model.LSID)
	 */
	public List getLSObjectsByLSIDType(LSID arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObjectsByClassType(java.lang.Class)
	 */
	public List getLSObjectsByClassType(Class arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

}
