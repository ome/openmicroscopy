/*
 * Created on Mar 1, 2005
*/
package org.ome.interfaces;

import org.ome.model.LSID;

/**
 * @author josh
 */
public interface VersionService {

    public int retrieveVersion(LSID key);
    public void updateVersion(LSID key, int version);
    
}
