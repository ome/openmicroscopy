/*
 * Created on Mar 1, 2005
 */
package org.ome.srv.logic;

import org.ome.interfaces.VersionService;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.model.OMEObject;
import org.ome.srv.db.jena.JenaProperties;

/**
 * it is assumed that the version service has its own model which cannot corrupt
 * or be corrupted by statements in the main model.
 * 
 * @author josh
 */
public class VersionServiceImpl extends AbstractService implements
        VersionService {
    
    protected final static String modelName = "versions";//FIXME
    
    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.VersionService#retrieveVersion(org.ome.model.LSID)
     */
    public int retrieveVersion(LSID key) {
        LSObject obj = db.getLSObject(key,modelName);
        if (null != obj) {
            Integer i = ((OMEObject) obj).getVersion();
            if (null != i) {
                return i.intValue();
            }
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ome.interfaces.VersionService#updateVersion(org.ome.model.LSID,
     *      int)
     */
    public void updateVersion(LSID key, int version) {
        boolean update = false;
        LSObject obj = db.getLSObject(key,modelName);
        if (null == obj) {
            update = true;
            obj = new OMEObject(key);
        }

        Integer i = ((OMEObject) obj).getVersion();
        if (null == i || i.intValue() < version) {
            update = true;
        }

        if (update) {
            ((OMEObject) obj).setVersion(new Integer(version));
            db.setLSObject(obj,modelName);
        }

    }

}