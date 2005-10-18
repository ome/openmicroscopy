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
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.orm.hibernate3.HibernateTemplate;
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
import ome.security.Utils;
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
public class PojosServiceTest
        extends
            AbstractDependencyInjectionSpringContextTests {

    private static Log log = LogFactory.getLog(PojosServiceTest.class);
    
    private Pojos psrv;
    
    public void setPojos(Pojos service){
    	psrv = service;
    }
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getConfigLocations(); 
    }

    Set s;
	PojoOptions po;
	Set ids;
    
    @Override
    protected void onSetUp() throws Exception {
    	Utils.setUserAuth();
        po = new PojoOptions().exp(1);
    	ids = new HashSet<Integer>(Arrays.asList(new Integer[]{1,2,3,4,5,6,250,253,249,258}));
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
    	log.info(String.format("%n1)NAME: %s%n2)RESULT: %s",name,result));
    }
    
}