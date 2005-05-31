/*
 * Created on May 31, 2005
*/
package org.openmicroscopy.omero.shoolaadapter;

import org.openmicroscopy.omero.shoolaadapter.HierarchyBrowsingView;

/**
 * @author josh
 */
public class ServiceFactory {

    public HierarchyBrowsingView getHierarchyBrowsingService(){
        return new HierarchyBrowsingAdapter();
    }
    
}
