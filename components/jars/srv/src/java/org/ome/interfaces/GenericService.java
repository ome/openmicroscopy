/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import org.ome.model.FollowGroup;
import org.ome.model.ILSObject;
import org.ome.model.LSID;


import java.rmi.Remote;

/**
 * responsible for the resolution of individual LSObjects
 * 
 * @author josh
 */
public interface GenericService extends Remote {

	public void setLSOjbect(ILSObject obj);
	public void updateLSObject(ILSObject obj);
	public ILSObject getLSObject(LSID lsid);
	public ILSObject getLSObject(LSID lsid, FollowGroup pg);
	
}