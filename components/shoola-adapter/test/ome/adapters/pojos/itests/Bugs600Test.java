/*
 * ome.adapters.pojos.itests.Bugs600Test
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

package ome.adapters.pojos.itests;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
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
import ome.adapters.pojos.Model2PojosMapper;
import ome.api.Pojos;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Image;
import ome.model.Project;
import ome.testing.AbstractPojosServiceTest;
import ome.testing.OMEData;
import ome.util.ModelMapper;
import ome.util.builders.PojoOptions;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;


/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.0
 */
public class Bugs600Test
        extends
            AbstractDependencyInjectionSpringContextTests {

    protected static Log log = LogFactory.getLog(Bugs600Test.class);
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
    	return new String[]{
    			"ome/client/spring.xml",
                "ome/testing/test.xml",
                "ome/testing/data.xml"};
    }    
    
    protected ModelMapper mapper;
    
    protected void onSetUp() throws Exception
    {
        mapper = new Model2PojosMapper();    
    }
    
    protected OMEData data;

    public void setData(OMEData omeData)
    {
        data = omeData;
    }

    protected Pojos psrv;
    
    public void setPojosService(Pojos service) 
    {
        psrv = service;
    }

    // Bug 627
    // ================================================================
    
    private Set getMappedSet(String query, Class klass)
    {
        Set ids = new HashSet(data.getMax(query,2));
        Set result = this.psrv.loadContainerHierarchy(klass,ids,null);
        Set mapped = (Set) mapper.map(result);
        return mapped;
    }
    
    public void testBug627_CategoryGroup_owner_group_isnull() throws Exception
    {
        Set mapped = getMappedSet("CategoryGroup.ids",CategoryGroup.class);
        for (Iterator it = mapped.iterator(); it.hasNext();)
        {
            CategoryGroupData cg = (CategoryGroupData) it.next();
            assertNotNull("Group should be there",cg.getOwner().getGroup());
        }
    }
    
    public void testBug627_Category_owner_group_isnull() throws Exception
    {
        Set mapped = getMappedSet("Category.ids",Category.class);
        for (Iterator it = mapped.iterator(); it.hasNext();)
        {
            CategoryData c = (CategoryData) it.next();
            assertNotNull("Group should be there",c.getOwner().getGroup());
        }
    }
    
    // Bug 628
    // ================================================================
    public void testBug628_UserDetails_group_isnull() throws Exception
    {
        Set names = new HashSet(data.getMax("Experimenter.names",2));
        Map results = (Map) psrv.getUserDetails(names,null);
        Map mapped = (Map) mapper.map(results);
        for (Iterator it = mapped.keySet().iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            ExperimenterData exp = (ExperimenterData) mapped.get(key);
            assertNotNull("Groups should be there", exp.getGroups());
        }
    }
    

    // Bug 629
    // ================================================================
    public void testBug629_Project_group_leader_isnull() throws Exception
    {
        PojoOptions po = new PojoOptions();
        po.noLeaves().noAnnotations().exp(new Integer(642));
        Set result = psrv.loadContainerHierarchy(Project.class,null,po.map());
        for (Iterator it = result.iterator(); it.hasNext();)
        {
            Project prj = (Project) it.next();
            assertNotNull("Leader should be filled",prj.getGroup().getLeader());
        }
    }
}

