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

//Third-party libraries
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.testng.annotations.Test;

//Application-internal dependencies
import ome.api.IPojos;
import ome.model.annotations.DatasetAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.testing.OMEData;

/** 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class PojosServiceTest
        extends
            AbstractManagedContextTest {

    protected IPojos iPojos;

    protected OMEData data;
    
    @Override
    protected void onSetUp() throws Exception {
    	super.onSetUp(); 
        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");
        data = new OMEData();
        data.setDataSource(dataSource);
    	iPojos = (IPojos) applicationContext.getBean("pojosService");
    }
    
    @Test
    public void test_unannotated_Event_version() throws Exception
    {
        DatasetAnnotation da = createLinkedDatasetAnnotation();
        DatasetAnnotation da_test = new DatasetAnnotation( da.getId(), false );
        iPojos.deleteDataObject( da_test, null );
        
    }
    
    @Test
    public void test_cgc_Event_version() throws Exception
    {
            Set results = 
                iPojos.findCGCPaths(
                        new HashSet(data.getMax("Image.ids",2)), 
                        IPojos.CLASSIFICATION_ME,
                        null);
    }

    // ~ Helpers
    // =========================================================================

    private Project reloadProject(DatasetAnnotation da)
    {
        Dataset ds;
        Project p;
        ds = da.getDataset();
        p = (Project) ds.linkedProjectList().get(0);

        Project p_test = (Project) iPojos.loadContainerHierarchy(
                Project.class,
                Collections.singleton(p.getId()),
                null).iterator().next();
        return p_test;
    }

    private DatasetAnnotation createLinkedDatasetAnnotation()
    {
        DatasetAnnotation da = new DatasetAnnotation();
        Dataset ds = new Dataset();
        Project p = new Project();
        
        p.setName("uEv");
        p.linkDataset(ds);
        ds.setName("uEv");
        da.setContent("uEv");
        da.setDataset(ds);
        da = (DatasetAnnotation) iPojos.createDataObject( da, null );
        return da;
    }
    
}
