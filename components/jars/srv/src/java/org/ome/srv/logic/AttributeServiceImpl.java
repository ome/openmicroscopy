/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.logic;

import java.rmi.RemoteException;
import java.util.List;

import org.ome.interfaces.AttributeService;
import org.ome.model.IAttribute;
import org.ome.model.LSID;

/**
 * @author josh
 */
public class AttributeServiceImpl extends AbstractService implements AttributeService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AttributeService#addAttributeToLSObject(org.ome.model.IAttribute, org.ome.model.LSID)
	 */
	public boolean addAttributeToLSObject(IAttribute arg0, LSID arg1)
			throws RemoteException {
		// TODO Auto-generated method stub
		/* return false; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AttributeService#getAttributesForLSObject(org.ome.model.LSID)
	 */
	public List getAttributesForLSObject(LSID arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.AttributeService#getAttributesForLSObjectWithType(org.ome.model.LSID, org.ome.model.LSID)
	 */
	public List getAttributesForLSObjectWithType(LSID arg0, LSID arg1)
			throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

}
