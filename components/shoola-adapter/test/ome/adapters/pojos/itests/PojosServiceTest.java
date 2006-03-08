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
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

//Application-internal dependencies
import ome.adapters.pojos.Model2PojosMapper;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.client.ServiceFactory;
import ome.conditions.RootException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.ILink;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
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

import pojos.DataObject;
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
        ds = (Dataset) img.linkedDatasetIterator().next();
        Long id = ds.getId();
        
        // another copy
        Image img2 = (Image) iQuery.queryUnique(
                "select i from Image i " +
                "left outer join fetch i.datasetLinks " +
                "where i.id = ?",
                new Object[]{img.getId()});
        assertTrue("It better have a dataset link too",
                img2.sizeOfDatasetLinks()>0);
        Dataset ds2 = (Dataset) img2.linkedDatasetIterator().next();
        assertTrue("And the ids have to be the same",id.equals(ds2.getId()));
    }
 
    public void testButWeHaveToHandleTheVersions() throws Exception
    {
        Image img = new Image();
        img.setName( "version handling" );
        Image sent = (Image) iUpdate.saveAndReturnObject( img );
        
        sent.setDescription( " veresion handling update" );
        Image sent2 = (Image) iUpdate.saveAndReturnObject( sent );
        
        assertTrue( ! sent.getVersion().equals( sent2.getVersion() ) );
        
        ImageAnnotation iann = new ImageAnnotation();
        iann.setContent( " version handling ");
        iann.setImage( sent );
        
        try {
            iUpdate.saveAndReturnObject( iann );
            fail("Need optmistic lock exception.");
        } catch (RootException e) {
            // TODO should be a more specific exception.
        }
        
        // Now it should work.
        sent.setVersion( sent2.getVersion() );
        iUpdate.saveAndReturnObject( iann );
        
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

        // Make sure it's not linked.
        List list = 
        iQuery.getListByFieldEq( DatasetImageLink.class, "child.id", img.getId() );
        assertTrue( list.size() == 0 );
        
        // Method 2:
        saveImage();
        updated = unlinkImage();
        iPojos.udpateDataObjects( 
                (IObject[]) updated.toArray(new IObject[updated.size()]), null);

        List list2 = 
            iQuery.getListByFieldEq( DatasetImageLink.class, "child.id", img.getId() );
            assertTrue( list.size() == 0 );
        
        // Method 3:
        saveImage();
        Dataset target = (Dataset) img.linkedDatasetIterator().next();
        // For querying
        DatasetImageLink dslink = 
            (DatasetImageLink) img.findDatasetImageLink( target ).iterator().next();
        
        img.unlinkDataset( target );
        img = (Image) iPojos.updateDataObject( img, null );
        
        ILink test = (ILink) iQuery.getById( 
                DatasetImageLink.class, dslink.getId().longValue() );
        assertNull( test );
            
        // Method 4;
        Dataset d = new Dataset(); d.setName( "unlinking");
        Project p = new Project(); p.setName( "unlinking");
        p = (Project) iPojos.createDataObject( p, null );
        d = (Dataset) iPojos.createDataObject( d, null ); 
        
        ProjectDatasetLink link = new ProjectDatasetLink();
        link.setParent( p );
        link.setChild( d );
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
        List updated = img.eachLinkedDataset( new CBlock() {
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
    //
    
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

    /// ========================================================================
    /// ~ Versions
    /// ========================================================================

    public void test_version_doesnt_increase_on_non_change() throws Exception
    {
        Image img = new Image();
        img.setName( " no vers. increment ");
        img = (Image) iUpdate.saveAndReturnObject( img );
        
        Image test = (Image) iUpdate.saveAndReturnObject( img );
        
        assertTrue( img.getVersion().equals( test.getVersion() ));
        
    }
    
    public void test_version_doesnt_increase_on_linked_update() throws Exception
    {
        ImageAnnotation ann = new ImageAnnotation();
        Image img = new Image();
        
        img.setName( "version_test" );
        ann.setContent( "version_test" );
        img.addToAnnotations( ann );
        
        img = (Image) iUpdate.saveAndReturnObject( img );
        ann = (ImageAnnotation) img.iterateAnnotations().next();
        
        assertNotNull( img.getId() );
        assertNotNull( ann.getId() );
        
        int orig_img_version = img.getVersion().intValue();
        int orig_ann_version = ann.getVersion().intValue();
        
        ann.setContent( "updated version_test" ) ;
        
        ann = (ImageAnnotation) iUpdate.saveAndReturnObject( ann );
        img = ann.getImage();
        
        int new_ann_version = ann.getVersion().intValue();
        int new_img_version = img.getVersion().intValue();
        
        assertTrue( orig_ann_version < new_ann_version );
        assertTrue( orig_img_version == new_img_version );
        
    }
    
    /// ========================================================================
    /// ~ Counts
    /// ========================================================================

    public void test_counts() throws Exception
    {
        Map counts;
        
        counts = getCounts( Dataset.class, new Long(7770L), null );
        assertNull( counts );

        PojoOptions po = new PojoOptions().leaves();
        counts = getCounts( Dataset.class, new Long(7770L), po.map() );
        assertTrue( counts == null || null == counts.get( Image.ANNOTATIONS ));
        assertTrue( counts == null || null == counts.get( Dataset.ANNOTATIONS ) );
        
        counts = getCounts( Dataset.class, new Long(7771L), null );
        assertNull( counts.get( Image.ANNOTATIONS ));
        assertTrue( counts.containsKey( Dataset.ANNOTATIONS ));
        assertTrue( ( (Integer) counts.get( Dataset.ANNOTATIONS) ).intValue() == 1 );
                
    }

    private Map getCounts(Class klass, Long id, Map options )
    {
        IObject obj = (IObject)
            iPojos.loadContainerHierarchy( klass, Collections.singleton( id ), options )
            .iterator().next();
        
        return obj.getDetails().getCounts();
    }
    
    /// ========================================================================
    /// ~ Various bug-like checks
    /// ========================================================================
 
    public void test_no_duplicate_rows() throws Exception
    {
        String name = "TEST:"+System.currentTimeMillis();
        
        // Save Project.
        Project p = new Project();
        p.setName( name );
        p = (Project) iUpdate.saveAndReturnObject( p );
        
        // Check only one
        List list = iQuery.getListByFieldILike( Project.class, "name", name);
        assertTrue(list.size() == 1);
        assertTrue( 
                ((Project)list.get(0)).getId()
                .equals( p.getId() ));
        
        
        // Update it.
        ProjectData pd = (ProjectData) mapper.map( p );
        pd.setDescription( "....testnodups...." );
        Project send = (Project) reverse.map( pd ); 
        assertEquals( p.getId().intValue(), pd.getId() );
        assertEquals( send.getId().intValue(), pd.getId() );

        List l = new ArrayList(1);
        l.add( iPojos.updateDataObject( send, null ));
        List result = (List) mapper.map(l);
        ProjectData test = (ProjectData) result.get(0);
        assertEquals( test.getId(), p.getId().intValue() );
        
        // Check again.
        List list2 = iQuery.getListByFieldILike( Project.class, "name", name);
        assertTrue(list2.size() == 1);
        assertTrue( 
                ((Project)list.get(0)).getId()
                .equals( ((Project)list2.get(0)).getId() ));
        
    }
    
    public void test_no_duplicate_links() throws Exception
    {
        Image img = new Image();
        img.setName( "duplinks");
        
        Dataset ds = new Dataset();
        ds.setName( "duplinks" );
        
        img.linkDataset( ds );

        img = (Image) iUpdate.saveAndReturnObject( img );
        ds = (Dataset) img.linkedDatasetIterator().next();
        
        List imgLinks = 
            iQuery.getListByFieldEq( 
                    DatasetImageLink.class, "child.id", img.getId() );
        
        List dsLinks = 
            iQuery.getListByFieldEq( 
                    DatasetImageLink.class, "parent.id", ds.getId() );
        
        assertTrue( imgLinks.size() == 1 );
        assertTrue( dsLinks.size() == 1 );
        
        assertTrue( 
                ((DatasetImageLink)imgLinks.get(0)).getId()
                .equals( ((DatasetImageLink) dsLinks.get(0)).getId()));
        
    }
    
    public void test_no_duplicates_on_save_array() throws Exception
    {
        Image img = new Image();
        img.setName( "duplinks");
        
        Dataset ds = new Dataset();
        ds.setName( "duplinks" );
        
        img.linkDataset( ds );
        
        IObject[] retVal = iUpdate.saveAndReturnArray( new IObject[]{img,ds});
        img = (Image) retVal[0];
        ds = (Dataset) retVal[1];
        
        List imgLinks = 
            iQuery.getListByFieldEq( 
                    DatasetImageLink.class, "child.id", img.getId() );
        
        List dsLinks = 
            iQuery.getListByFieldEq( 
                    DatasetImageLink.class, "parent.id", ds.getId() );
        
        assertTrue( imgLinks.size() == 1 );
        assertTrue( dsLinks.size() == 1 );
        
        assertTrue( 
                ((DatasetImageLink)imgLinks.get(0)).getId()
                .equals( ((DatasetImageLink) dsLinks.get(0)).getId()));
        
    }

    public void test_annotating_a_dataset_one() throws Exception
    {

        // Setup: original is our in-memory, used every where object.
        Dataset original = new Dataset();
        original.setName( " two rows " );
        original = (Dataset) iUpdate.saveAndReturnObject( original );
        DatasetData annotatedObject = (DatasetData) mapper.map( original );
        Dataset annotated = (Dataset) iPojos.updateDataObject( 
                reverse.map( annotatedObject), null);
        // Dataset m = new Dataset( original.getId(), false);
        DatasetAnnotation annotation = new DatasetAnnotation();
        annotation.setContent( " two rows content " );
        annotation.setDataset( annotated );
        
        // CGLIB
        DatasetAnnotation object 
            = (DatasetAnnotation) iPojos.createDataObject( annotation , null );
        DataObject returnedToUser = (DataObject) mapper.map( object );
        
        // Now working but iPojos is still returning a CGLIB class.
        assertTrue( original.getClass().equals( annotation.getClass() ));
    }

    public void test_annotating_a_dataset_two() throws Exception
    {

        String name = " two rows "+System.currentTimeMillis();
        String text = " two rows content "+System.currentTimeMillis();
        
        // Setup: original is our in-memory, used every where object.
        Dataset original = new Dataset();
        original.setName( name );
        original = (Dataset) iUpdate.saveAndReturnObject( original );
        DatasetData annotatedObject = (DatasetData) mapper.map( original );

        // Dataset m = new Dataset( original.getId(), false);
        DatasetAnnotation annotation = new DatasetAnnotation();
        annotation.setContent( text );
        annotation.setDataset( original );

        // Two Rows error
        DatasetAnnotation object 
        = (DatasetAnnotation) iPojos.createDataObject( annotation , null );
        Dataset annotated = (Dataset) reverse.map( annotatedObject );
        
        annotated.setVersion( object.getDataset().getVersion() );
        DatasetData returnedToUser = (DatasetData) mapper.map(
                iPojos.updateDataObject( annotated, null )
                );

        // Test
        List ds = iQuery.getListByFieldILike( Dataset.class, "name", name);
        List as = iQuery.getListByFieldILike( DatasetAnnotation.class, "content", text);
        
        assertTrue( ds.size() == 1 );
        assertTrue( as.size() == 1 );
        
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

