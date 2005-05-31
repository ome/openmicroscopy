/*
 * Created on May 8, 2005
*/
package org.openmicroscopy.omero.client;

import org.openmicroscopy.omero.interfaces.HierarchyBrowsing;

/**
 * @author josh
 */
public class ServiceFactory {

    public HierarchyBrowsing getHierarchyBrowsingService(){
        return (HierarchyBrowsing) SpringHarness.ctx.getBean("hierarchyBrowsingFacade");
    }
    
}
