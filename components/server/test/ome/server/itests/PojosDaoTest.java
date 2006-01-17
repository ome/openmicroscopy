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
import java.util.ArrayList;
import java.util.Arrays;
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
import ome.dao.GenericDao;
import ome.dao.hibernate.queries.PojosQueryBuilder;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;
import ome.util.builders.PojoOptions;

/** 
 * tests for an up-and-coming pojos data access
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.0
 */
public class PojosDaoTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(PojosDaoTest.class);
    private GenericDao gdao;
    
    public void setGDao(GenericDao dao){
    	gdao = dao;
    }
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getDaoConfigLocations(); 
    }

    Set s;
	String q;
	PojoOptions po;
	Set<Integer> ids;
	Map m = new HashMap();
	String n;
    
    @Override
    protected void onSetUp() throws Exception {
        po = new PojoOptions().exp(1);
    	ids = new HashSet<Integer>(Arrays.asList(new Integer[]{1,2,3,4,5,6,250,253,249,258}));
    	m = new HashMap();
    	m.put("id_list",ids);
    	m.put("exp",po.getExperimenter());
    }

    // ----------------------------------------------------
    // Checking values
    // ----------------------------------------------------
    
    public void testClassifications(){
        // determine target category group
        List l = gdao.queryList("select cg from CategoryGroup cg where cg.categories.classifications.image.id = 1430",new Object[]{});
        
        // use Query Builder to get selected
        String q_NME = PojosQueryBuilder.buildPathsQuery(Pojos.CLASSIFICATION_NME, po.map());
        String q_ME = PojosQueryBuilder.buildPathsQuery(Pojos.CLASSIFICATION_ME, po.map());
        
        // run query
        List cgc_ids = new ArrayList();
        cgc_ids.add(1430);
        Map params = new HashMap();
        params.put("id_list",cgc_ids);
        List<List> results_NME = gdao.queryListMap(q_NME,params);
        List<List> results_ME = gdao.queryListMap(q_ME,params);
        
        // parse
        Map<CategoryGroup, Set<Category>> parsed_NME = parse_cgc_paths(results_NME);
        Map<CategoryGroup, Set<Category>> parsed_ME = parse_cgc_paths(results_ME);
        System.out.println(parsed_NME);
        System.out.println(parsed_ME);

        CategoryGroup cg = (CategoryGroup) results_NME.get(0).get(0);
        System.out.println(cg);
        assertTrue(parsed_NME.containsKey(cg));
        assertTrue(parsed_ME.containsKey(cg));
    }

    private Map<CategoryGroup, Set<Category>> parse_cgc_paths(List results_NME)
    {
        Map<CategoryGroup,Set<Category>> parsed_NME = new HashMap<CategoryGroup,Set<Category>>();
        for (Iterator it = results_NME.iterator(); it.hasNext();)
        {
            List element = (List) it.next();
            CategoryGroup cg = (CategoryGroup) element.get(0);
            Category c = (Category) element.get(1);
            if (!parsed_NME.containsKey(cg))
                parsed_NME.put(cg,new HashSet<Category>());
            parsed_NME.get(cg).add(c);
        }
        return parsed_NME;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    public void runLoad(String name, Class c){
    	// Class, Set<Container>, options
    	q = PojosQueryBuilder.buildLoadQuery(c,false,po.map());
    	n = name;go();
    }
    
	public void testLoadProject(){
		runLoad("Load_p",Project.class);
    }

	public void testLoadDataset(){
		runLoad("Load_d",Dataset.class);
    }

	public void testLoadCG(){
		runLoad("Load_cg",CategoryGroup.class);
    }

	public void testLoadC(){
		runLoad("Load_c",Category.class);
    }
	
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
	
    public void runFind(String name, Class c){
    	// Class, Set<Image>, options
    	q = PojosQueryBuilder.buildFindQuery(c,po.map());
    	n = name;go();
    }
    
    public void testFindProject(){
    	runFind("Find_p",Project.class);
    }

    public void testFindDataset(){
    	runFind("Find_cg",CategoryGroup.class);
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    public void runAnn(String name, Class c){
    	// Class, Set<Container>, Map
    	q = PojosQueryBuilder.buildAnnsQuery(c,po.map());
    	m.remove("exp"); // unused
    	n = name;go();
    }
    
    public void testDatasetAnn(){
    	runAnn("ann_d",Dataset.class);
    }

    public void testImageAnn(){
    	runAnn("ann_i",Image.class);
    }
   
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    public void runGet(String name, Class c){
    	// Class, Set<Container>, Map
    	q = PojosQueryBuilder.buildGetQuery(c,po.map());
    	n = name;go();
    }
    
    public void testGetFromProject(){
    	runGet("get_p",Project.class);
    }

    public void testGetFromDataset(){
    	runGet("get_p",Dataset.class);
    }

    public void testGetFromCg(){
    	runGet("get_cg",CategoryGroup.class);
    }

    public void testGetFromCat(){
    	runGet("get_c",Category.class);
    }

    //TODO how to run getUserImages
    public void testGetUser(){
    	m.remove("id_list");
    	runGet("get_user",Image.class); // TODO make nicer; here Image.class=>noIds in template could just po.set("noIds")
    }

    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    public void runPaths(String name, String algorithm){
    	// Set<Image>, Algorithm options
    	q = PojosQueryBuilder.buildPathsQuery(algorithm,po.map());
    	n = name;go();
    }
    
    public void testPathsClassME() {
    	runPaths("class_me_path",Pojos.CLASSIFICATION_ME);
	}

    public void testPathsClassNME() {
        runPaths("class_nme_path",Pojos.CLASSIFICATION_NME);
    }
    
    public void testPathsDeClass() {
    	runPaths("declass_path",Pojos.DECLASSIFICATION);
	}
    
    //  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    private void go(){
    	log.info(String.format("%n1)NAME: %s%n2)QUERY: %s",n,q));
    	s = new HashSet(gdao.queryListMap(q,m));
    	log.info(String.format("%n3)RESULT: %s",s));
    }
    
}