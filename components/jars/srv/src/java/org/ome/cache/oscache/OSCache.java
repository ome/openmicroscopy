/*
 * Created on Feb 8, 2005
 */
package org.ome.cache.oscache;

import org.ome.ILSObject;
import org.ome.LSID;
import org.ome.LSObject;
import org.ome.cache.Cache;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * @author josh
 */
public class OSCache implements Cache {

	private boolean updated;
	private GeneralCacheAdministrator admin;
	
	/**
	 * 
	 */
	public OSCache() {
		super();
		admin=new GeneralCacheAdministrator();
	}

	public void put(LSID key, ILSObject obj){
		admin.putInCache(key.getURI(), obj);
	}
	
	public ILSObject get(LSID key) {
		 String myKey = key.getURI();
		 ILSObject myValue = null;
		 try {
		     // Get from the cache
		     myValue = (ILSObject) admin.getFromCache(myKey, -1);
		 } catch (NeedsRefreshException nre) {
		 	return null;
		 }
		 return myValue;
	}
	
}
