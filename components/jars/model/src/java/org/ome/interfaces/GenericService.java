/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import org.ome.model.FollowGroup;
import org.ome.model.LSObject;
import org.ome.model.LSID;


import java.rmi.Remote;

/**
 * responsible for the resolution of individual LSObjects
 * 
 * @author josh
 */
public interface GenericService extends Remote {

	public void setLSOjbect(LSObject obj);
	public void updateLSObject(LSObject obj);
	public LSObject getLSObject(LSID lsid);
	public LSObject getLSObject(LSID lsid, FollowGroup pg);
	
}