/*
 * Created on May 8, 2005
*/
package org.ome.omero.client;

import org.ome.omero.interfaces.HierarchyBrowsing;

/**
 * @author josh
 */
public class ServiceFactory {

    public HierarchyBrowsing getHierarchyBrowsingService(){
        return (HierarchyBrowsing) SpringHarness.ctx.getBean("hierarchyBrowsingFacade");
    }
    
}
