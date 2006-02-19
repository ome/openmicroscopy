package ome.adapters.pojos.itests;
/*
 * ome.adapters.pojos.utests.Model2PojosMapper
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

//Java imports
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import junit.framework.TestCase;


//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.adapters.pojos.Model2PojosMapper;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.client.ServiceFactory;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.testing.OMEData;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

import pojos.ExperimenterData;
import pojos.ImageData;


/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.0
 */
public class PojosServiceTest extends TestCase {

    protected static Log log = LogFactory.getLog(PojosServiceTest.class);

    ServiceFactory factory = ServiceFactory.makeLocalServiceFactory();
   
    OMEData data;
    Set ids;
    IPojos psrv;
    IQuery q;
    IUpdate u;
    ModelMapper mapper;
    ReverseModelMapper reverse;
    
    protected void setUp() throws Exception
    {
        DataSource dataSource = (DataSource) factory.ctx.getBean("dataSource");
        data = new OMEData();
        data.setDataSource(dataSource);
        psrv = factory.getPojosService();
        q = factory.getQueryService();
        u = factory.getUpdateService();
        mapper = new Model2PojosMapper();
        reverse = new ReverseModelMapper();
    }
    
    public void testGetSomethingThatsAlwaysThere() throws Exception
    {
        List l = q.getListByExample(new Experimenter());
        assertTrue("Root has to exist.",l.size()>0);
        Experimenter exp = (Experimenter) l.get(0);
        assertNotNull("Must have an id",exp.getId());
        assertNotNull("And a name",exp.getFirstName());
        
        // Now let's try to map it.
        ExperimenterData expData = 
            (ExperimenterData) mapper.map((Experimenter)l.get(0));
        assertNotNull("And something should still be there",expData);
        assertTrue("And it should have an id",expData.getId()>-1);
        assertNotNull("And various other things",expData.getFirstName());
    }
    
    public void testNowLetsTryToSaveSomething() throws Exception
    {
        // prepare data
        ImageData imgData = new ImageData();
        imgData.setName("My test image");
        imgData.setDescription("My test description");
        
        // convert it
        Image img = (Image) reverse.map(imgData);

        img = (Image) psrv.createDataObject(img,null);
        assertNotNull("We should get something back",img);
        assertNotNull("Should have an id",img.getId());
        
        Image img2 = (Image) q.getById(Image.class,img.getId().longValue());
        assertNotNull("And we should be able to find it again.",img2);
        
    }
    
    public void testMappingFindContainerHierarchies(){
        ids = new HashSet(data.getMax("Project.ids",2)); // TODO possibly convert to "Set get*"
    	Model2PojosMapper mapper = new Model2PojosMapper(); // TODO doc mem-leak
    	Set s = psrv.findContainerHierarchies(Project.class,ids,null);
    	log.info(mapper.map(s));
    }
    
    /* exaple of how to use */
    /* must pass in Image not ImageData.class */
    public void testMappingFindAnnotations(){
        ids = new HashSet(data.getMax("Image.Annotated.ids",2));
    	Map m = new Model2PojosMapper().map(psrv.findAnnotations(Image.class,ids,null)); 
    	log.info(m);
    }
   
}

