/*
 * Created on Feb 8, 2005
 */
package org.ome.cache.oscache;

import java.util.HashMap;
import java.util.Map;

import org.ome.model.LSObject;
import org.ome.model.LSID;
import org.ome.model.OMEObject;
import org.ome.cache.Cache;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * @author josh
 */
public class OSCache implements Cache {

    private Map versions;
	private GeneralCacheAdministrator admin;
	
	/**
	 * 
	 */
	public OSCache() {
		super();
		admin=new GeneralCacheAdministrator();
		versions = new HashMap();
	}

	/** adds an object to the cache. It is assumed that 
	 * all values (**especially** <code>version</code>)
	 * can be trusted.
	 */
	public void put(LSID key, LSObject obj){
	    if (null!= obj){
	    String myKey = key.getURI();
		admin.putInCache(myKey, obj);
		versions.put(myKey,((OMEObject)obj).getVersion());
	    }
	}
	
	public LSObject get(LSID key) {
		 String myKey = key.getURI();
		 LSObject myValue = null;
		 try {
		     // Get from the cache
		     myValue = (LSObject) admin.getFromCache(myKey, -1);
		 } catch (NeedsRefreshException nre) {
		 	return null;
		 }
		 return myValue;
	}
	
	public void remove(LSID key) {
		String myKey = key.getURI();	    
        admin.flushEntry(myKey);
        versions.remove(myKey);
    }
	
	public int getVersion(LSID key){
	    String myKey = key.getURI();
	    Integer version = (Integer)versions.get(myKey);
	    if (null==version){
	        return 0;
	    } 
        return version.intValue();
        
	}

	
}
