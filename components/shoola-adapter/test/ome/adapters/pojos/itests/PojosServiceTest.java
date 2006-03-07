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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import junit.framework.TestCase;


//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.adapters.pojos.Model2PojosMapper;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.client.ServiceFactory;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.system.OmeroContext;
import ome.testing.OMEData;
import ome.util.CBlock;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.util.builders.PojoOptions;

import omeis.providers.re.Renderer;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.metadata.PixelsStats;
import omeis.providers.re.metadata.StatsFactory;

import pojos.DatasetData;
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
public class PojosServiceTest extends TestCase {

    protected static Log log = LogFactory.getLog(PojosServiceTest.class);

    ServiceFactory factory = new ServiceFactory(OmeroContext.MANAGED_CONTEXT);
   
    OMEData data;
    Set ids, results, mapped;
    IPojos iPojos;
    IQuery iQuery;
    IUpdate iUpdate;
    ModelMapper mapper;
    ReverseModelMapper reverse;
    
    Image img;
    ImageData imgData ;
    
    Dataset ds;
    DatasetData dsData;
    
    protected void setUp() throws Exception
    {
        Properties p = System.getProperties();
        p.setProperty("omero.username","root");
        p.setProperty("omero.groupname","system");
        p.setProperty("omero.eventtype","Test");
        
        DataSource dataSource = (DataSource) factory.ctx.getBean("dataSource");
        data = new OMEData();
        data.setDataSource(dataSource);
        iPojos = factory.getPojosService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();
        mapper = new Model2PojosMapper();
        reverse = new ReverseModelMapper();
    }
    
    public void testGetSomethingThatsAlwaysThere() throws Exception
    {
        List l = iQuery.getListByExample(new Experimenter());
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
        imgData = simpleImageData();
        img = (Image) reverse.map(imgData);
        
        img = (Image) iPojos.createDataObject(img,null);
        assertNotNull("We should get something back",img);
        assertNotNull("Should have an id",img.getId());
        
        Image img2 = (Image) iQuery.getById(Image.class,img.getId().longValue());
        assertNotNull("And we should be able to find it again.",img2);
        
    }
    
    public void testAndSaveSomtheingWithParents() throws Exception
    {
        saveImage();
        ds = (Dataset) img.iterateOverDatasetLinks().next();
        Long id = ds.getId();
        
        // another copy
        Image img2 = (Image) iQuery.queryUnique(
                "select i from Image i " +
                "left outer join fetch i.datasetLinks " +
                "where i.id = ?",
                new Object[]{img.getId()});
        assertTrue("It better have a dataset link too",
                img2.sizeOfDatasetLinks()>0);
        Dataset ds2 = (Dataset) img2.iterateOverDatasetLinks().next();
        assertTrue("And the ids have to be the same",id.equals(ds2.getId()));
    }
    
    public void testNowOnToSavingAndDeleting() throws Exception
    {
        imgData = simpleImageData();
        img = (Image) reverse.map(imgData);
        
        assertNull("Image doesn't have an id.",img.getId());
        img = (Image) iPojos.createDataObject(img,null);
        assertNotNull("Presto change-o, now it does.",img.getId());
        iPojos.deleteDataObject(img,null);
        
        img = (Image) iQuery.getById(Image.class,img.getId().longValue());
        assertNull("we should have deleted it ",img);
        
    }
    
    public void testLetsTryToLinkTwoThingsTogether() throws Exception
    {
        imgData = simpleImageData();
        dsData = simpleDatasetData();
        
        img = (Image) reverse.map(imgData);
        ds = (Dataset) reverse.map(dsData);
        
        DatasetImageLink link = new DatasetImageLink();
        link.link(ds,img);
        
        ILink test = iPojos.link(new ILink[]{link},null)[0];
        assertNotNull("ILink should be there",test);
        
    }

    public void testAndHeresHowWeUnlinkThings() throws Exception
    {

        // Method 1:
        saveImage();
        List updated = unlinkImage();
        iUpdate.saveCollection( updated );
        
        test that row is gone.
        
        // Method 2:
        saveImage();
        updated = unlinkImage();
        iPojos.udpateDataObjects( 
                (IObject[]) updated.toArray(new IObject[updated.size()]), null);
        
        // Method 3:
        Dataset d = new Dataset();
        Project p = new Project();
        p = (Project) iPojos.createDataObject( p, null );
        d = (Dataset) iPojos.createDataObject( d, null ); 
        
        ProjectDatasetLink link = new ProjectDatasetLink();
        link.setParent( p );
        link.setChild( c );
    }
    
    public void test_uh_oh_duplicate_rows() throws Exception
    {
        String name = "TEST:"+System.currentTimeMillis();
        
        // Save Project.
        
        // Update it.
        service.updateDataObject(rmapper.map(object), options)
        List l = new ArrayList(1);
        l.add();
        List result = (List) mapper.map(l);
        return (DataObject) result.get(0);
    }
    

    private void saveImage()
    {
        imgData = simpleImageDataWithDatasets();
        img = (Image) reverse.map(imgData);
        
        img = (Image) iUpdate.saveAndReturnObject(img);
        assertTrue("It better have a dataset link",
                img.sizeOfDatasetLinks()>0);
    }
    
    private List unlinkImage()
    {
        List updated = img.collectFromDatasetLinks( new CBlock() {
            public Object call(IObject arg0)
            {
                img.unlinkDataset( (Dataset) arg0 );
                return arg0;
            }
        });
        updated.add( img );
        return updated;
    }
    
    //
    // READ API
    // 
    
    public final static String TESTER = "tester"; // Defined in create_pojos.sql

    public void test_loadContainerHierarchy() throws Exception
    {
        
        ids = new HashSet(data.getMax("Project.ids",2));
        results = iPojos.loadContainerHierarchy(Project.class, ids, null);

        PojoOptions po = new PojoOptions().exp( new Long(0L) );
        results = iPojos.loadContainerHierarchy(Project.class, null, po.map() );
        
    }

    
    public void test_findContainerHierarchies(){
        
        Model2PojosMapper mapper ;
        PojoOptions defaults = new PojoOptions(), empty = new PojoOptions(null);
        
        ids = new HashSet(data.getMax("Image.ids",2)); 
        results = iPojos.findContainerHierarchies(Project.class,ids,defaults.map()); 
        mapper = new Model2PojosMapper(); 
    	mapped = (Set) mapper.map(results);

        try {
        results = iPojos.findContainerHierarchies(Dataset.class,ids,empty.map());
        fail("Should fail");
        } catch (IllegalArgumentException e) {}
        
        ids = new HashSet(data.getMax("Image.ids",2)); 
        results = iPojos.findContainerHierarchies(CategoryGroup.class,ids,defaults.map()); 
        mapper = new Model2PojosMapper();
        
    }

    public void test_findAnnotations(){
        
        Map m;
        
        ids = new HashSet(data.getMax("Image.Annotated.ids",2));
    	m = new Model2PojosMapper().map(iPojos.findAnnotations(
                Image.class,ids,null,null));
        
        ids = new HashSet(data.getMax("Dataset.Annotated.ids",2));
        m = new Model2PojosMapper().map(iPojos.findAnnotations(
                Dataset.class,ids,null,null)); 

    }

    public void test_retrieveCollection() throws Exception
    {
        Image i = (Image) iQuery.getById(Image.class,5551);
        i.unload();
        Set annotations = (Set) iPojos.retrieveCollection(i,Image.ANNOTATIONS,null);
        assertTrue(annotations.size() > 0);
    }

    public void test_findCGCPaths() throws Exception
    {
        ids = new HashSet(data.getMax("Image.ids",2));
        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_ME,null);
        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_NME,null);
        results = iPojos.findCGCPaths(ids, IPojos.DECLASSIFICATION,null);
    }

    public void test_getCollectionCount() throws Exception    
    {
        Long id = new Long(5551);
        Map m = iPojos.getCollectionCount(
                Image.class.getName(),
                Image.ANNOTATIONS,
                Collections.singleton(id),
                null);
        Integer count = (Integer) m.get(id);
        assertTrue(count.intValue() > 0);
        
        id = new Long(7771);
        m = iPojos.getCollectionCount(
                Dataset.class.getName(),
                Dataset.IMAGELINKS,
                Collections.singleton( id ),
                null);
        count = (Integer) m.get(id);
        assertTrue(count.intValue() > 0);
        
    }

    public void test_getImages() throws Exception
    {
        ids = new HashSet(data.getMax("Project.ids",2));
        Set imagse = iPojos.getImages(Project.class, ids, null );
    }

    public void test_getUserDetails() throws Exception
    {
        Map m = iPojos.getUserDetails(Collections.singleton(TESTER),null);
        Experimenter e = (Experimenter) m.get(TESTER);
    }

    public void test_getUserImages() throws Exception
    {
        try {
            results = iPojos.getUserImages(null);
            fail("Illegal argument: experimenter/group option must be set.");
        } catch (IllegalArgumentException e) { }
        
        results = iPojos.getUserImages(new PojoOptions().exp(new Long(10000)).map());
        assertTrue(results.size() > 0);

    }

    //
    // Misc
    ///
    
    public void testAndForTheFunOfItLetsGetTheREWorking() throws Exception
    {

        Pixels pix = (Pixels) iQuery.getByClass(Pixels.class).get(0);
        IPixels pixDB = factory.getPixelsService();
        PixelsService pixFS = new PixelsService(
                PixelsService.ROOT_DEFAULT);
        PixelBuffer pixBF = pixFS.createPixelBuffer(pix);
        
        StatsFactory sf = new StatsFactory();
        PixelsStats pixST = sf.compute(pix,pixBF);
        // TODO RenderingEngine re = factory.getRenderingService();
        Renderer r = new Renderer(pix,null,pixBF,pixST); 
        
        PlaneDef pd = new PlaneDef(0,0);
        pd.setX(0); pd.setY(0); pd.setZ(0);
        r.render(pd);
        
    }
    
    // ~ Helpers
    // =========================================================================
    
    private ImageData simpleImageData(){
        // prepare data
        ImageData id = new ImageData();
        id.setName("My test image");
        id.setDescription("My test description");
        return id;
    }


    private DatasetData simpleDatasetData()
    {
        DatasetData dd = new DatasetData();
        dd.setName("t1");
        dd.setDescription("t1");
        return dd;
    }

    private ImageData simpleImageDataWithDatasets()
    {
        DatasetData dd = simpleDatasetData();
        Set dss = new HashSet();
        dss.add(dd);
        ImageData id = simpleImageData();
        id.setDatasets(dss);
        return id;
    }

    
}

