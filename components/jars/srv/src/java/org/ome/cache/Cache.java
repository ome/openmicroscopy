/*
 * Created on Feb 13, 2005
 */
package org.ome.cache;

import org.ome.model.ILSObject;
import org.ome.model.LSID;
import org.ome.model.LSObject;

/** 
 * @author josh
 */
public interface Cache {
	public ILSObject get(LSID key);
	public void put(LSID key, ILSObject obj);
}