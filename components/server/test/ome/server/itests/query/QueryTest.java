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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosFindHierarchiesQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.QP;
import ome.services.query.Query;
import ome.services.query.StringQuerySource;
import ome.tools.lsid.LsidUtils;
import ome.util.ContextFilter;
import ome.util.Filterable;
import ome.util.RdfPrinter;

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
            AbstractManagedContextTest {

    private static Log log = LogFactory.getLog(QueryTest.class);
    

    public void testFindHierarchies() throws Exception
    {

        PojosFindHierarchiesQueryDefinition queryDef 
            = new PojosFindHierarchiesQueryDefinition(
                    QP.Class("class",Project.class),
                    QP.List("ids",Arrays.asList(9090L,9091L,9092L,9990L,9991L,9992L)),
                    QP.Map("options",null)
        );
        List result = (List) iQuery.execute(queryDef);
        walkResult(result);
    }
    
    public void testFilteredCalls(){

        PojosLoadHierarchyQueryDefinition queryDef 
            = new PojosLoadHierarchyQueryDefinition(
                    QP.Class("class",Project.class),
                    QP.List("ids",Arrays.asList(9090L,9091L,9092L,9990L,9991L,9992L)),
                    QP.Long("ownerId",10000L),
                    QP.Map("options",null)
        );
        List result = (List) iQuery.execute(queryDef);
        walkResult(result);
    }
    
    public void testCriteriaCalls(){
        PojosLoadHierarchyQueryDefinition queryDef
            = new PojosLoadHierarchyQueryDefinition(
                    QP.Class("class",Project.class),
                    QP.List("ids",
                    Arrays.asList(9090L,9091L,9092L,9990L,9991L,9992L)),
                    QP.Long("ownerId",null),
                    QP.Map("options",null));
        
        List result = (List) iQuery.execute(queryDef);
        walkResult(result);
    }
    
    protected void walkResult(List result) {
        RdfPrinter rdf = new RdfPrinter();
        rdf.filter("results are", result);
        System.out.println(rdf.getRdf());
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
		projectById.put("id",1L);
		List l1 = iQuery.getListByMap(Project.class, projectById );
		assertTrue("Can't be more than one",l1.size()<=1);
		
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
    
    public void testCounts() throws Exception
    {
        String s_dataset = LsidUtils.parseType(Dataset.ANNOTATIONS);
        String s_annotations = LsidUtils.parseField(Dataset.ANNOTATIONS);
        String works = 
            String.format("select target.id, count(collection) from %s target " +
                "join target.%s collection group by target.id",s_dataset,s_annotations);
        
        Query q = 
            new StringQuerySource()
                .lookup(works);
//                        select sum(*) from Dataset ds " +
//                        "group by ds.id having ds.id in (1L)");
        List result = (List) iQuery.execute(q);
        System.out.println(result);
    }

}

