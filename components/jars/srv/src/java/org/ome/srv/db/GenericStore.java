/*
 * Created on Feb 19, 2005
 */
package org.ome.srv.db;

import java.util.List;

import org.ome.interfaces.GenericService;
import org.ome.model.FollowGroup;
import org.ome.model.LSID;
import org.ome.model.LSObject;

/**
 * @author josh
 */
public interface GenericStore extends GenericService{
	
	public List evaluateNamedQuery(NamedQuery query);
	public List evaluateNamedQuery(NamedQuery query, String modelName);
	public void setLSObject(LSObject obj,String modelName) ;
	public void updateLSObject(LSObject obj, String modelName) ;
	public LSObject getLSObject(LSID lsid, String modelName) ;
	public LSObject getLSObjectWithFollowGroup(LSID lsid, FollowGroup fg, String modelName);
	public List getLSObjectsByLSIDType(LSID type, String modelName) ;
	public List getLSObjectsByClassType(Class klass, String modelName) ;
}