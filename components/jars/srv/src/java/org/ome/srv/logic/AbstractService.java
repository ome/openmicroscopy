/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.logic;

import java.rmi.RemoteException;

import org.ome.cache.Cache;
import org.ome.exceptions.ImmutableException;
import org.ome.interfaces.VersionService;
import org.ome.model.IOMEObject;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.model.OMEObject;
import org.ome.srv.db.GenericStore;

/**
 * @author josh
 */
public abstract class AbstractService {

	protected GenericStore db;

	public void setStore(GenericStore store){
	    this.db= store;
	}
	
	protected Cache cache;
	
	public void setCache(Cache cache){
	    this.cache= cache;
	}
	
	protected VersionService versions;
	
	public void setVersionService(VersionService versionService){
	    this.versions = versionService;
	}
	
	/** reponsible for update logic for all services.
     * @param data
     */
	protected LSObject retrieveObject(LSID lsid) {
	    LSObject returnObj = cache.get(lsid);
	    if (null==returnObj|| versions.retrieveVersion(lsid) > cache.getVersion(lsid)){
	        returnObj = db.getLSObject(lsid);
	        cache.put(lsid,returnObj);
	    }
	    return returnObj;
	}
	
	/** reponsible for update logic for all services.
     * @param data
     */
    protected void updateObject(IOMEObject data) {
	    OMEObject o = (OMEObject) db.getLSObject(data.getLSID());//TODO from cache!
	    if (null==o.getImmutable() || !o.getImmutable().booleanValue()){
	        db.updateLSObject(data);
	        Integer i = o.getVersion();
	        int value=0;
	        if (null!=i){
	            value=i.intValue()+1;
	        }
	        versions.updateVersion(o.getLSID(),value);
	    } else {
	        throw new ImmutableException(o.getLSID()+" is immutable.");
	    }

    }

	
}
