/*
 * ome.server.itests.PojosDaoTest
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.api.IQuery;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.containers.Project;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.util.builders.PojoOptions;

/** 
 * tests for an up-and-coming pojos data access
 *  TODO rename "PojosQuerySourceTest"
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.0
 */ // FIXME
@Test(
	groups = "integration"
)
public class PojosDaoTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(PojosDaoTest.class);


    @Override protected String[] getConfigLocations() { return new String[]{}; }
    @Override protected ConfigurableApplicationContext loadContextLocations(String[] locations) {
        return OmeroContext.getManagedServerContext();
    }

    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception
    {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception
    {
        super.tearDown();
    }
    // =========================================================================
    
    @Override
    protected void onSetUp() throws Exception {
        _q = new ServiceFactory( (OmeroContext) applicationContext ).getQueryService();
        po = new PojoOptions().exp(1L);
        ids = new HashSet<Integer>(Arrays.asList(new Integer[]{1,2,3,4,5,6,250,253,249,258}));
        m = new HashMap();
        m.put("id_list",ids);
        m.put("exp",po.getExperimenter());
    }

    IQuery _q;

    Set s;
	String q;
	PojoOptions po;
	Set<Integer> ids;
	Map m = new HashMap();
	String n;
    
    private void runLoad(String name, Class c){
    	// Class, Set<Container>, options
    	//q = PojosQueryBuilder.buildLoadQuery(c,false,po.map());
    	n = name;go();
    }
    
    @Test
	public void testLoadProject(){
		runLoad("Load_p",Project.class);
    }

    @Test
	public void testLoadDataset(){
		runLoad("Load_d",Dataset.class);
    }

    @Test
	public void testLoadCG(){
		runLoad("Load_cg",CategoryGroup.class);
    }

    @Test
	public void testLoadC(){
		runLoad("Load_c",Category.class);
    }
	
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
	
    private void runFind(String name, Class c){
    	// Class, Set<Image>, options
    	//q = PojosQueryBuilder.buildFindQuery(c,po.map());
    	n = name;go();
    }
    
    @Test
    public void testFindProject(){
    	runFind("Find_p",Project.class);
    }

    @Test
    public void testFindDataset(){
    	runFind("Find_cg",CategoryGroup.class);
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    private void runAnn(String name, Class c){
    	// Class, Set<Container>, Map
    	//q = PojosQueryBuilder.buildAnnsQuery(c,po.map());
    	m.remove("exp"); // unused
    	n = name;go();
    }

    @Test
    public void testDatasetAnn(){
    	runAnn("ann_d",Dataset.class);
    }

    @Test
    public void testImageAnn(){
    	runAnn("ann_i",Image.class);
    }
   
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    private void runGet(String name, Class c){
    	// Class, Set<Container>, Map
    	//q = PojosQueryBuilder.buildGetQuery(c,po.map());
    	n = name;go();
    }
    
    @Test
    public void testGetFromProject(){
    	runGet("get_p",Project.class);
    }

    @Test
    public void testGetFromDataset(){
    	runGet("get_p",Dataset.class);
    }

    @Test
    public void testGetFromCg(){
    	runGet("get_cg",CategoryGroup.class);
    }

    @Test
    public void testGetFromCat(){
    	runGet("get_c",Category.class);
    }

    //TODO how to run getUserImages
    @Test
    public void testGetUser(){
    	m.remove("id_list");
    	runGet("get_user",Image.class); // TODO make nicer; here Image.class=>noIds in template could just po.set("noIds")
    }

    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    private void runPaths(String name, String algorithm){
    	// Set<Image>, Algorithm options
    	//q = PojosQueryBuilder.buildPathsQuery(algorithm,po.map());
    	n = name;go();
    }
    
    @Test
    public void testPathsInc() {
    	runPaths("inc_path","INCLUSIVE");
	}

    @Test
    public void testPathsExc() {
    	runPaths("exc_path","EXCLUSIVE");
	}
    
    //  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    private void go(){
    	log.info(String.format("%n1)NAME: %s%n2)QUERY: %s",n,q));
    	//s = new HashSet(_q.queryListMap(q,m));
    	log.info(String.format("%n3)RESULT: %s",s));
    }
    
}
