/*
 * Created on Feb 9, 2005
 */
package org.ome.interfaces;

import org.ome.model.FollowGroup;
import org.ome.model.LSObject;
import org.ome.model.LSID;

import java.util.List;

/**
 * responsible for the resolution of individual LSObjects
 * 
 * @author josh
 */
public interface GenericService {

	public void setLSObject(LSObject obj) ;
	public void updateLSObject(LSObject obj) ;
	public LSObject getLSObject(LSID lsid) ;
	public LSObject getLSObjectWithFollowGroup(LSID lsid, FollowGroup fg);
	public List getLSObjectsByLSIDType(LSID type) ;
	public List getLSObjectsByClassType(Class klass) ;
	
}