/*
 * Created on Feb 19, 2005
 */
package org.ome.srv.db;

import java.util.List;

import org.ome.interfaces.GenericService;

/**
 * @author josh
 */
public interface GenericStore extends GenericService{
	
	public List evaluateNamedQuery(NamedQuery query);

}