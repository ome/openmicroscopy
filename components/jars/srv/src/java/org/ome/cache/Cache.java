/*
 * Created on Feb 13, 2005
 */
package org.ome.cache;

import org.ome.model.LSObject;
import org.ome.model.LSID;

/** 
 * @author josh
 */
public interface Cache {
	public LSObject get(LSID key);
	public void put(LSID key, LSObject obj);
	/** @return the version _in_ cache of this LSID or 0 of not available. */ 
	public int getVersion(LSID key);
	public void remove(LSID key);
}