/*
 * Created on Feb 13, 2005
 */
package org.ome.cache;

import org.ome.ILSObject;
import org.ome.LSID;
import org.ome.LSObject;

/** 
 * @author josh
 */
public interface Cache {
	public ILSObject get(LSID key);
	public void put(LSID key, ILSObject obj);
}