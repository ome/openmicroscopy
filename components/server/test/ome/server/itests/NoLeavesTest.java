/*
 * ome.server.itests.PojosServiceTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.server.itests;

//Java imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies
import ome.api.Pojos;
import ome.dao.DaoFactory;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.ImagePixel;
import ome.model.Project;
import ome.model.Repository;
import ome.security.Utils;
import ome.testing.AbstractPojosServiceTest;
import ome.util.ContextFilter;
import ome.util.Filterable;
import ome.util.builders.PojoOptions;

/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.0
 */
public class NoLeavesTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    protected static Log log = LogFactory.getLog(NoLeavesTest.class);
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getConfigLocations(); 
    }

    DaoFactory daos;
    public void setDaoFactory(DaoFactory factory){
        this.daos = factory;
    }
    
    Pojos psrv;
    public void setPojosService(Pojos service){
        psrv = service;
    }
    
    PojoOptions po;
    Set ids = new HashSet(Arrays.asList(1));
    
    @Override
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	po = new PojoOptions();
        Utils.setUserAuth();
    }

    public void testPojoServicesLoadContainerNoLeaves(){
        po.noLeaves();   
        Set<Project> s = psrv.loadContainerHierarchy(Project.class,ids,po.map());
        imageSetExists(s,false);
    }
    
    public void testPojoServicesLoadContainerWithLeaves(){
        po.leaves();
        Set<Project> s = psrv.loadContainerHierarchy(Project.class,ids,po.map());
        imageSetExists(s,true);
    }
    
    public void testPojoServiceContainerNoLeavesWithExperimenter(){
        Set<Project> s;
        po.noLeaves().exp(1).allAnnotations();
        s = psrv.loadContainerHierarchy(Project.class,null,po.map());
        s = psrv.loadContainerHierarchy(Project.class,ids,po.map()); 
        po.noLeaves().exp(1).annotationsFor(1);
        s = psrv.loadContainerHierarchy(Project.class,null,po.map());
        s = psrv.loadContainerHierarchy(Project.class,ids,po.map());
        
        fail("There should have been a boom");
    }
    
    void imageSetExists(Set<Project> s, boolean exists)
    {
        for (Project p : s)
        {
            Set<Dataset> ds = p.getDatasets();
            for (Dataset dataset : ds)
            {
                if (exists) assertNotNull(dataset.getImages());
                if (!exists) assertNull(dataset.getImages());
            }
        }
    }
    
}
