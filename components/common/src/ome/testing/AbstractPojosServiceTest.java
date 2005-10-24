/*
 * ome.testing.AbstractPojosServiceTest
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
package ome.testing;

//Java imports
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies
import ome.api.Pojos;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;
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
public abstract class AbstractPojosServiceTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    protected static Log log = LogFactory.getLog(AbstractPojosServiceTest.class);
    
    protected Pojos psrv;
    
    public void setPojos(Pojos service){
    	psrv = service;
    }

    protected Set s;
	protected PojoOptions po;
	protected Set ids;

    protected void onSetUp() throws Exception {
        po = new PojoOptions().exp(new Integer(1));
    	ids = new HashSet(Arrays.asList(new Integer[]{
    			Integer.valueOf(1),
    			Integer.valueOf(2),
    			Integer.valueOf(3),
    			Integer.valueOf(4),
    			Integer.valueOf(5),
    			Integer.valueOf(6),
    			Integer.valueOf(250),
    			Integer.valueOf(253),
    			Integer.valueOf(249),
    			Integer.valueOf(258)}));
    }

	public void testLoadProject(){
		log("LOADP",psrv.loadContainerHierarchy(Project.class,ids,po.map()));
    }

	public void testLoadDataset(){
		log("LOADD",psrv.loadContainerHierarchy(Dataset.class,ids,po.map()));
    }

	public void testLoadCG(){
		log("LOADCG",psrv.loadContainerHierarchy(CategoryGroup.class,ids,po.map()));
    }

	public void testLoadC(){
		log("Load_c",psrv.loadContainerHierarchy(Category.class,ids,po.map()));
    }
	
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
	
    public void testFindProject(){
		log("find_p",psrv.findContainerHierarchies(Project.class,ids,po.map()));
    }

    public void testFindCG(){
    	log("find_cg",psrv.findContainerHierarchies(CategoryGroup.class,ids,po.map()));
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    public void testDatasetAnn(){
    	log("d_ann",psrv.findAnnotations(Dataset.class,ids,po.map()));    
    }

    public void testImageAnn(){
    	log("i_ann",psrv.findAnnotations(Image.class,ids,po.map()));
    }
   
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

    public void testGetFromProject(){
    	log("get_p",psrv.getImages(Project.class,ids,po.map()));
    }

    public void testGetFromDataset(){
    	log("get_d",psrv.getImages(Dataset.class,ids,po.map()));
    }

    public void testGetFromCg(){
    	log("get_cg",psrv.getImages(CategoryGroup.class,ids,po.map()));
    }

    public void testGetFromCat(){
    	log("get_c",psrv.getImages(Category.class,ids,po.map()));
    }

    //TODO how to run getUserImages
    public void testGetUser(){
    	log("get_image",psrv.getUserImages(po.map()));    
    }

    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    public void testPathsInc() {
    	log("path_inc",psrv.findCGCPaths(ids,0,po.map()));
	}

    public void testPathsExc() {
    	log("path_exc",psrv.findCGCPaths(ids,1,po.map()));
	}
    
    public void testPathsFAIL() {
    	try {
    		log("path_exc",psrv.findCGCPaths(ids,2,po.map()));
    		fail(" no algorithm 2 !!!");
    	} catch (IllegalArgumentException iae){
    		// do nothing.
    	}
	}
    
    //  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    private void log(String name, Object result){
    	log.info("1)NAME: "+name+"2)RESULT: "+result);
    }
    
}