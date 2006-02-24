/*
 * ome.server.itests.query.QueryTest
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
package ome.server.itests.query;

//Java imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies
import ome.api.IQuery;
import ome.model.ILink;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.server.itests.ConfigHelper;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.system.OmeroContext;

/** 
 * tests for a generic data access
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class QueryTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(QueryTest.class);

    IQuery _q;
    HibernateTemplate ht;
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
        _q = (IQuery) applicationContext.getBean("queryService");
        ht = (HibernateTemplate) applicationContext.getBean("hibernateTemplate");
    }
    
    protected String[] getConfigLocations() { return new String[]{}; }
    protected ConfigurableApplicationContext getContext(Object key)
    {
        return OmeroContext.getManagedServerContext();
    }

    public void testCriteriaCalls(){
        PojosLoadHierarchyQueryDefinition queryDef
            = new PojosLoadHierarchyQueryDefinition();
        
        List result = (List) ht.execute(queryDef);
        System.out.println(result);
        Set d_links = ((Project)result.get(0)).getDatasetLinks();
        System.out.println(d_links);
        ILink d_link = (ILink) d_links.iterator().next();
        System.out.println(d_link);
        Dataset ds = (Dataset) d_link.getChild();
        System.out.println(ds);
        Set i_links = ds.getImageLinks();
        System.out.println(i_links);
        ILink i_link = (ILink) i_links.iterator().next();
        System.out.println(i_link);
        Image i = (Image) i_link.getChild();
        System.out.println(i);
        Set anns = i.getAnnotations();
        System.out.println(anns);
        ImageAnnotation iann = (ImageAnnotation) anns.iterator().next();
        System.out.println(iann);
        
    }
    
	public void testGetById() {
	}

	public void testGetByName() {
	}

	public void testGetListByExample() {
	}

	public void testGetListByMap() {
		
		//Project by id
		Map projectById = new HashMap();
		projectById.put("projectId",1);
		List l1 = _q.getListByMap(Project.class, projectById );
		assertTrue("Can only be one",l1.size()==1);
		
		//Dataset by locked
		Map unlockedDatasets = new HashMap();
		unlockedDatasets.put("locked",Boolean.FALSE);
		List l2 = _q.getListByMap(Dataset.class, unlockedDatasets);
		assertTrue("At least one unlocked D", l2.size()>0);
		
		//Sending wrong parameter
		Map stringRatherThanBoolean = new HashMap();
		stringRatherThanBoolean.put("locked","f");
		try {
			List l3 = _q.getListByMap(Dataset.class, stringRatherThanBoolean);
			fail("Shouldn't suceed");
		} catch (ClassCastException cce){
			// good
		} catch (Throwable t) {
			fail("Expected class cast exception");
		}
		
	}

	public void testGetUniqueByExample() {
	}

	public void testGetUniqueByMap() {
	}

	public void testPersist() {
	}

	public void testQueryList() {
	}

	public void testQueryUnique() {
	}
   
    
}
