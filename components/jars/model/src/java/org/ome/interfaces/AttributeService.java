/*
 * Created on Feb 10, 2005
 */
package org.ome.interfaces;

import java.rmi.RemoteException;
import java.util.List;

import org.ome.model.IAttribute;
import org.ome.model.LSID;

/**
 * responsible for all instances of SemanticType attributes.
 * 
 * @author josh
 */
public interface AttributeService {

	// annotations and categories
	// no ST/Attrs for Shoola !

	public boolean addAttributeToLSObject(IAttribute attr, LSID lsid)
			throws RemoteException;

	public List getAttributesForLSObject(LSID lsid) throws RemoteException;

	public List getAttributesForLSObjectWithType(LSID obj, LSID type)
			throws RemoteException;

}