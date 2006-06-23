/*
 * ome.adapters.pojos.itests.PojosServiceTest
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
import org.testng.annotations.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBException;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import junit.framework.TestCase;


//Application-internal dependencies
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.parameters.Parameters;
import ome.system.ServiceFactory;
import ome.conditions.ApiUsageException;
import ome.conditions.OptimisticLockException;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.testing.OMEData;
import ome.testing.Paths;
import ome.util.CBlock;
import ome.util.IdBlock;
import ome.util.builders.PojoOptions;

import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;

import pojos.AnnotationData;
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
@Test(
	groups = {"client","integration"}
)
public class PojosServiceTest extends TestCase {

    protected static Log log = LogFactory.getLog(PojosServiceTest.class);

    ServiceFactory factory = new ServiceFactory("ome.client.test");
   
    OMEData data;
    Set ids, results, mapped;
    IPojos iPojos;
    IQuery iQuery;
    IUpdate iUpdate;
    
    Image img;
    ImageData imgData ;
    
    Dataset ds;
    DatasetData dsData;
    
  @Configuration(beforeTestClass = true)
    protected void setUp() throws Exception
    {
        data = (OMEData) factory.getContext().getBean("data");
        iPojos = factory.getPojosService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();

        try 
            {
                iQuery.get(Experimenter.class,0l);
            } catch (Throwable t){
                // TODO no, no, really. This is ok. (And temporary) 
            }
  
      iQuery.get(Experimenter.class,0l);
      // if this one fails, skip rest.

    }
  
  @Test  
    public void testGetSomethingThatsAlwaysThere() throws Exception
    {
        List l = iQuery.findAllByExample(new Experimenter(),null);
        assertTrue("Root has to exist.",l.size()>0);
        Experimenter exp = (Experimenter) l.get(0);
        assertNotNull("Must have an id",exp.getId());
        assertNotNull("And a name",exp.getFirstName());
        
        // Now let's try to map it.
        ExperimenterData expData = 
            new ExperimenterData((Experimenter)l.get(0));
        assertNotNull("And something should still be there",expData);
        assertTrue("And it should have an id",expData.getId()>-1);
        assertNotNull("And various other things",expData.getFirstName());
    }
    
  @Test
    public void testNowLetsTryToSaveSomething() throws Exception
    {
        imgData = simpleImageData();
        img = (Image) imgData.asIObject();
        
        img = (Image) iPojos.createDataObject(img,null);
        assertNotNull("We should get something back",img);
        assertNotNull("Should have an id",img.getId());
        
        Image img2 = (Image) iQuery.get(Image.class,img.getId().longValue());
        assertNotNull("And we should be able to find it again.",img2);
        
    }
    
  @Test
    public void testAndSaveSomtheingWithParents() throws Exception
    {
        saveImage();
        ds = (Dataset) img.linkedDatasetIterator().next();
        Long id = ds.getId();
        
        // another copy
        Image img2 = (Image) iQuery.findAllByQuery(
                "select i from Image i " +
                "left outer join fetch i.datasetLinks " +
                "where i.id = :id",
                new Parameters().addId(img.getId())).get(0);
        assertTrue("It better have a dataset link too",
                img2.sizeOfDatasetLinks()>0);
        Dataset ds2 = (Dataset) img2.linkedDatasetIterator().next();
        assertTrue("And the ids have to be the same",id.equals(ds2.getId()));
    }
 
  @Test( groups = {"versions","broken"} )
    public void testButWeHaveToHandleTheVersions() throws Exception
    {
        Image img = new Image();
        img.setName( "version handling" );
        Image sent = (Image) iUpdate.saveAndReturnObject( img );
        
        sent.setDescription( " veresion handling update" );
        Integer version = sent.getVersion();
        
        // Version incremented
        Image sent2 = (Image) iUpdate.saveAndReturnObject( sent );
        Integer version2 = sent2.getVersion();
        assertTrue( ! version.equals( version2 ) );
        
        // Resetting; should get error
        sent2.setVersion( version );
        ImageAnnotation iann = new ImageAnnotation();
        iann.setContent( " version handling ");
        iann.setImage( sent2 );
        
        try {
            iUpdate.saveAndReturnObject( iann );
            fail("Need optmistic lock exception.");
        } catch (OptimisticLockException e) {
            // ok.
        }
        
        // Fixing the change;
        // now it should work.
        sent2.setVersion( version2 );
        iUpdate.saveAndReturnObject( iann );
        
    }
    
  @Test
    public void testNowOnToSavingAndDeleting() throws Exception
    {
        imgData = simpleImageData();
        img = (Image) imgData.asIObject();
        
        assertNull("Image doesn't have an id.",img.getId());
        img = (Image) iPojos.createDataObject(img,null);
        assertNotNull("Presto change-o, now it does.",img.getId());
        iPojos.deleteDataObject(img,null);
        
        img = (Image) iQuery.find(Image.class,img.getId().longValue());
        assertNull("we should have deleted it ",img);
        
    }
    
  @Test
    public void testLetsTryToLinkTwoThingsTogether() throws Exception
    {
        imgData = simpleImageData();
        dsData = simpleDatasetData();
        
        img = (Image) imgData.asIObject();
        ds = (Dataset) dsData.asIObject();
        
        DatasetImageLink link = new DatasetImageLink();
        link.link(ds,img);
        
        ILink test = iPojos.link(new ILink[]{link},null)[0];
        assertNotNull("ILink should be there",test);
        
    }

  @Test
    public void testAndHeresHowWeUnlinkThings() throws Exception
    {

        // Method 1:
        saveImage();
        List updated = unlinkImage();
        iUpdate.saveCollection( updated );

        // Make sure it's not linked.
        List list = 
        iQuery.findAllByQuery( DatasetImageLink.class.getName(), 
                new Parameters().addLong("child.id", img.getId()));
        assertTrue( list.size() == 0 );
        
        // Method 2:
        saveImage();
        updated = unlinkImage();
        iPojos.updateDataObjects( 
                (IObject[]) updated.toArray(new IObject[updated.size()]), null);

        List list2 = 
            iQuery.findAllByQuery( DatasetImageLink.class.getName(),
                    new Parameters().addLong("child.id", img.getId() ));
            assertTrue( list.size() == 0 );
        
        // Method 3:
        saveImage();
        Dataset target = (Dataset) img.linkedDatasetIterator().next();
        // For querying
        DatasetImageLink dslink = 
            (DatasetImageLink) img.findDatasetImageLink( target ).iterator().next();
        
        img.unlinkDataset( target );
        img = (Image) iPojos.updateDataObject( img, null );
        
        ILink test = (ILink) iQuery.find( 
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
    
  @Test
    public void testHeresHowWeUnlinkFromJustOneSide() throws Exception
    {
        saveImage();
        DatasetImageLink link 
            = (DatasetImageLink) img.iterateDatasetLinks().next();
        img.removeDatasetImageLink( link, false );
        
        iPojos.updateDataObject( img, null );
        
        DatasetImageLink test = (DatasetImageLink)
        iQuery.find( DatasetImageLink.class, link.getId().longValue() );
        
        assertNull( test );
        
    }
 
    private void saveImage()
    {
        imgData = simpleImageDataWithDatasets();
        img = (Image) imgData.asIObject();
        
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

  @Test
    public void test_loadContainerHierarchy() throws Exception
    {
        
        ids = new HashSet(data.getMax("Project.ids",2));
        results = iPojos.loadContainerHierarchy(Project.class, ids, null);

        PojoOptions po = new PojoOptions().exp( new Long(0L) );
        results = iPojos.loadContainerHierarchy(Project.class, null, po.map() );
        
    }

    
  @Test( groups = "EJBExceptions" )
    public void test_findContainerHierarchies(){
        
        PojoOptions defaults = new PojoOptions(), empty = new PojoOptions(null);
        
        ids = new HashSet(data.getMax("Image.ids",2)); 
        results = iPojos.findContainerHierarchies(Project.class,ids,defaults.map()); 

        try {
        results = iPojos.findContainerHierarchies(Dataset.class,ids,empty.map());
        fail("Should fail");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }
        
        ids = new HashSet(data.getMax("Image.ids",2)); 
        results = iPojos.findContainerHierarchies(CategoryGroup.class,ids,defaults.map()); 
        
    }

  @Test
    public void test_findAnnotations(){
        
        Map m;
        
        ids = new HashSet(data.getMax("Image.Annotated.ids",2));
    	iPojos.findAnnotations( Image.class,ids,null,null );
        
        ids = new HashSet(data.getMax("Dataset.Annotated.ids",2));
        iPojos.findAnnotations( Dataset.class,ids,null,null ); 

    }

  @Test
    public void test_retrieveCollection() throws Exception
    {
        Image i = (Image) iQuery.get(Image.class,5551);
        i.unload();
        Set annotations = (Set) iPojos.retrieveCollection(i,Image.ANNOTATIONS,null);
        assertTrue(annotations.size() > 0);
    }

  @Test
    public void test_findCGCPaths() throws Exception
    {
        ids = new HashSet(data.getMax("Image.ids",2));
        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_ME,null);
        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_NME,null);
        results = iPojos.findCGCPaths(ids, IPojos.DECLASSIFICATION,null);
    }
    
  @Test
    public void test_findCGCPaths_declass() throws Exception
    {
        Paths paths = new Paths(data.get("CGCPaths.all"));
        Set de = iPojos.findCGCPaths(paths.uniqueImages(),IPojos.DECLASSIFICATION,null);
        assertTrue(de.size() == paths.unique(Paths.CG, Paths.EXISTS,Paths.EXISTS,Paths.EXISTS).size());
        
        for ( Iterator it = de.iterator( ); it.hasNext( ); )
        {
            CategoryGroup cg = (CategoryGroup) it.next( );
            Iterator it2 = cg.linkedCategoryIterator();
            while ( it2.hasNext() )
            {
                Category c = (Category) it2.next();
                Iterator it3 = c.linkedImageIterator();
                while ( it3.hasNext() )
                {
                    Image i = (Image) it3.next();
                    Set found = paths.find
                        (cg.getId(),
                         c.getId(),
                         i.getId());
                    assertTrue( found.size() == 1);

                }
            }
        }

        Long single_i = paths.singlePath()[Paths.I.intValue()];
        Set one_de = iPojos.findCGCPaths
            (Collections.singleton
                 (single_i),
                 IPojos.DECLASSIFICATION,
                 null);
        assertTrue(one_de.size() == paths.unique
                   (Paths.CG, Paths.WILDCARD,Paths.WILDCARD,single_i).size());

    }

  @Test
    public void test_findCGCPaths_class() throws Exception
    {
        // Finding a good test
        Long[] targetPath = null;
        Paths paths = new Paths(data.get("CGCPaths.all"));
        Set withNoImages = paths.find(Paths.WILDCARD,Paths.WILDCARD,Paths.NULL_IMAGE);
        
        for ( Iterator it = withNoImages.iterator( ); it.hasNext( ); )
        {
            Long n = (Long) it.next( );
            
            // Must be at least two Categories in one CG since we're only 
            // examining the Categories in this CategoryGroup w/o an image.
            // Now need one with an image.
            Long[] values = paths.get(n);
            Set target = paths.find(values[Paths.CG.intValue()],Paths.WILDCARD,Paths.EXISTS);
            if (target.size() > 0) {
                targetPath = paths.get((Long) target.iterator().next());
                break;
            }
        }
        
        if (targetPath == null) fail("No valid category group found for classification test.");
        
        Set single = Collections.singleton(targetPath[Paths.I.intValue()]);
        Set me =  iPojos.findCGCPaths(single,IPojos.CLASSIFICATION_ME,null);
        Set nme = iPojos.findCGCPaths(single,IPojos.CLASSIFICATION_NME,null);
        
        for ( Iterator it = nme.iterator( ); it.hasNext( ); )
        {
            CategoryGroup group = (CategoryGroup) it.next( );
            if (group.getId().equals(targetPath[Paths.CG.intValue()] )){
                for (Iterator it2 = group.linkedCategoryIterator(); it.hasNext();)
                {
                    Category c = (Category) it2.next();
                    if (c.getId().equals(targetPath[Paths.C.intValue()]))
                        fail("Own category should not be included.");
                }
            }   
                
        }
        
        for ( Iterator it3 = nme.iterator( ); it3.hasNext( ); )
        {
            CategoryGroup group = (CategoryGroup) it3.next( );
            if (group.getId().equals(targetPath[Paths.CG.intValue()]))
                fail("Should not be in mutually-exclusive set.");
        }

    }
    
  @Test( groups = "EJBExceptions")
    public void testCountingApiExceptions(){

        Set ids = Collections.singleton(new Long(1));

        // Does not exist
        try {
            iPojos.getCollectionCount("DoesNotExist","meNeither",ids,null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }

        // Missing plural on dataset
        try {
            iPojos.getCollectionCount("ome.model.containers.Project","dataset",ids,null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }

        // Null ids
        try {
            iPojos.getCollectionCount("ome.model.containers.Project","datasets",null,null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }

        // Poorly formed
        try {
            iPojos.getCollectionCount("hackers.rock!!!","",ids,null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }

        // Empty Class string
        try {
            iPojos.getCollectionCount("","datasets",ids,null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }

        // Empty Class string
        try {
            iPojos.getCollectionCount(null,"datasets",ids,null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }

        // Empty property string
        try {
            iPojos.getCollectionCount("ome.model.core.Image","",ids,null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }

        // Null property string
        try {
            iPojos.getCollectionCount("ome.model.core.Image",null,ids,null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }

    }

    
  @Test
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

  @Test
    public void test_getImages() throws Exception
    {
        ids = new HashSet(data.getMax("Project.ids",2));
        Set imagse = iPojos.getImages(Project.class, ids, null );
    }

  @Test
    public void test_getUserDetails() throws Exception
    {
        Map m = iPojos.getUserDetails(Collections.singleton(TESTER),null);
        Experimenter e = (Experimenter) m.get(TESTER);
    }

  @Test( groups = "EJBExceptions" )
    public void test_getUserImages() throws Exception
    {
        try {
            results = iPojos.getUserImages(null);
            fail("APIUsage: experimenter/group option must be set.");
        } catch (ApiUsageException e) {
            // ok.
        } catch (EJBException ejbe) {
            log.warn("Should not be here."); //TODO
        }
        
        results = iPojos.getUserImages(new PojoOptions().exp(new Long(10000)).map());
        assertTrue(results.size() > 0);

    }

    //
    // Misc
    //
    
  @Test
    public void testAndForTheFunOfItLetsGetTheREWorking() throws Exception
    {

        Pixels pix = (Pixels) iQuery.findAll(Pixels.class,null).get(0);
        IPixels pixDB = factory.getPixelsService();
        RenderingEngine re = factory.createRenderingEngine(); 
        
        PlaneDef pd = new PlaneDef(0,0);
        pd.setX(0); pd.setY(0); pd.setZ(0);
        re.render(pd);
        
    }

    /// ========================================================================
    /// ~ Versions
    /// ========================================================================

  @Test( groups = {"versions","broken"} )
    public void test_version_doesnt_increase_on_non_change() throws Exception
    {
        Image img = new Image();
        img.setName( " no vers. increment ");
        img = (Image) iUpdate.saveAndReturnObject( img );
        
        Image test = (Image) iUpdate.saveAndReturnObject( img );

        fail( "must move details correction to the merge event listener " +
                "or version will always be incremented. ");
        
        assertTrue( img.getVersion().equals( test.getVersion() ));
        
    }
    
  @Test( groups = {"versions","broken"} )
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

  @Test
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
        assertTrue( ( (Long) counts.get( Dataset.ANNOTATIONS) ).intValue() == 1 );
                
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
 
  @Test
    public void test_no_duplicate_rows() throws Exception
    {
        String name = "TEST:"+System.currentTimeMillis();
        
        // Save Project.
        Project p = new Project();
        p.setName( name );
        p = (Project) iUpdate.saveAndReturnObject( p );
        
        // Check only one
        List list = iQuery.findAllByString( Project.class, "name", name, true, null);
        assertTrue(list.size() == 1);
        assertTrue( 
                ((Project)list.get(0)).getId()
                .equals( p.getId() ));
        
        
        // Update it.
        ProjectData pd = new ProjectData( p );
        pd.setDescription( "....testnodups...." );
        Project send = (Project) pd.asIObject(); 
        assertEquals( p.getId().intValue(), pd.getId() );
        assertEquals( send.getId().intValue(), pd.getId() );

        Project result = (Project) iPojos.updateDataObject( send, null );
        ProjectData test = new ProjectData( result );
        assertEquals( test.getId(), p.getId().intValue() );
        
        // Check again.
        List list2 = iQuery.findAllByString( Project.class, "name", name, true, null);
        assertTrue(list2.size() == 1);
        assertTrue( 
                ((Project)list.get(0)).getId()
                .equals( ((Project)list2.get(0)).getId() ));
        
    }
    
  @Test
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
            iQuery.findAllByQuery( 
                    DatasetImageLink.class.getName(), 
                    new Parameters().addLong("child.id", img.getId() ));
        
        List dsLinks = 
            iQuery.findAllByQuery( 
                    DatasetImageLink.class.getName(), 
                    new Parameters().addLong("parent.id", ds.getId() ));
        
        assertTrue( imgLinks.size() == 1 );
        assertTrue( dsLinks.size() == 1 );
        
        assertTrue( 
                ((DatasetImageLink)imgLinks.get(0)).getId()
                .equals( ((DatasetImageLink) dsLinks.get(0)).getId()));
        
    }
    
  @Test
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
            iQuery.findAllByQuery( 
                    DatasetImageLink.class.getName(),
                    new Parameters().addLong("child.id", img.getId() ));
        
        List dsLinks = 
            iQuery.findAllByQuery( 
                    DatasetImageLink.class.getName(),
                    new Parameters().addLong("parent.id", ds.getId() ));
        
        assertTrue( imgLinks.size() == 1 );
        assertTrue( dsLinks.size() == 1 );
        
        assertTrue( 
                ((DatasetImageLink)imgLinks.get(0)).getId()
                .equals( ((DatasetImageLink) dsLinks.get(0)).getId()));
        
    }

  @Test
    public void test_annotating_a_dataset_cglib_issue() throws Exception
    {

        // Setup: original is our in-memory, used every where object.
        Dataset original = new Dataset();
        original.setName( " two rows " );
        original = (Dataset) iPojos.createDataObject( original, null );
        DatasetData annotatedObject = new DatasetData( original );
        Dataset annotated = (Dataset) iPojos.updateDataObject( 
                annotatedObject.asIObject(), null);
        // Dataset m = new Dataset( original.getId(), false);
        DatasetAnnotation annotation = new DatasetAnnotation();
        annotation.setContent( " two rows content " );
        annotation.setDataset( annotated );
        
        // CGLIB
        DatasetAnnotation object 
            = (DatasetAnnotation) iPojos.createDataObject( annotation , null );
        DataObject returnedToUser = new AnnotationData( object );
        
        // Now working but iPojos is still returning a CGLIB class.
        assertTrue( original.getClass().equals( annotation.getClass() ));
    }

  @Test
    public void test_annotating_a_dataset() throws Exception
    {
        String name = " two rows "+System.currentTimeMillis();
        String text = " two rows content "+System.currentTimeMillis();
        String desc = " new description "+System.currentTimeMillis();
        
        // Setup: original is our in-memory, used every where object.
        Dataset original = new Dataset();
        original.setName( name );
        original = (Dataset) iPojos.createDataObject( original, null );

        assertTrue(
                original.getDetails().getCounts() == null 
                || original.getDetails().getCounts().get( Dataset.ANNOTATIONS ) == null
                );
        
        original.setDescription( desc );
        
        DatasetAnnotation annotation = new DatasetAnnotation();
        annotation.setContent( text );
        annotation.setDataset( original );

        annotation = (DatasetAnnotation) iPojos.createDataObject(
                annotation, null );
        original = annotation.getDataset(); // TODO is this okay?
        
        assertUniqueAnnotationCreation(name, text);
        
        Dataset test = 
            (Dataset) iQuery.get( Dataset.class, original.getId().longValue() );
        
        assertTrue( desc.equals( test.getDescription() ));

        assertNotNull(original.getDetails().getCounts());
        assertNotNull(original.getDetails().getCounts().get( Dataset.ANNOTATIONS ));
        assertTrue(
                ((Integer) original.getDetails().getCounts().get( Dataset.ANNOTATIONS)).intValue() > 0
                );

        System.out.println( original.getDetails().getCounts());
        
    }
    
  @Test
    public void test_two_datasets_and_a_project() throws Exception
    {
        String name = " 2&1 "+System.currentTimeMillis();
        Project p = new Project();
        p.setName( name );
        
        p = (Project) iPojos.createDataObject( p, null );
        
        Dataset d1 = new Dataset();
        d1.setName( name );
        
        Dataset d2 = new Dataset();
        d2.setName( name );
        
        ProjectDatasetLink l1 = new ProjectDatasetLink();
        ProjectDatasetLink l2 = new ProjectDatasetLink();
        
        l1.setParent( p );
        l1.setChild( d1 );
        
        l2.setParent( p );
        l2.setChild( d2 );
        
        p.addProjectDatasetLink( l1, true );
        p.addProjectDatasetLink( l2, true );
        
        p = (Project) iPojos.updateDataObject( p, null );
        
        Iterator it = p.iterateDatasetLinks();
        while ( it.hasNext() ) 
        {
            ProjectDatasetLink link = (ProjectDatasetLink) it.next();
            if ( link.child().getId().equals( d1.getId() ))
            {
                l1 = link;
                d1 = link.child();
            } else if ( link.child().getId().equals( d2.getId() )) {
                l2 = link;
                d2 = link.child();
            } else {
                fail( " Links aren't set up propertly");
            }
            
        }                    
        
        d1.setDescription( name );
        
        Dataset test = (Dataset) iPojos.updateDataObject( d1, null );
        
        ProjectDatasetLink link1 = 
                (ProjectDatasetLink) iQuery.get( 
                        ProjectDatasetLink.class, l1.getId().longValue() );
            
        assertNotNull( link1 );
        assertTrue( link1.parent().getId().equals( p.getId()));
        assertTrue( link1.child().getId().equals( d1.getId() ));

        ProjectDatasetLink link2 = 
            (ProjectDatasetLink) iQuery.get( 
                    ProjectDatasetLink.class, l2.getId().longValue() );
        
        assertNotNull( link2 );
        assertTrue( link2.parent().getId().equals( p.getId()));
        assertTrue( link2.child().getId().equals( d2.getId() ));

    }
    
  @Test
    public void test_delete_annotation() throws Exception
    {
        String string = "delete_annotation"+System.currentTimeMillis();
        
        Dataset d = new Dataset();
        d.setName( string );
        
        DatasetAnnotation a = new DatasetAnnotation();
        a.setDataset( d );
        a.setContent( string );
        
        a = (DatasetAnnotation) iPojos.createDataObject( a, null );
        
        iPojos.deleteDataObject( a, null );
        
        Object o = iQuery.find( DatasetAnnotation.class, a.getId().longValue() );
        assertNull( o );
        
    }
    
  @Test
    public void test_duplicate_links_again() throws Exception
    {
    
        String string = "duplinksagain"+System.currentTimeMillis();
        
        Dataset d = new Dataset();
        d.setName( string );
        
        Project p = new Project();
        p.setName( string );
        
        d.linkProject( p );
        d = (Dataset) iPojos.createDataObject( d, null );
        Set orig_ids = new HashSet( d.collectProjectLinks( new IdBlock() ));
        
        DatasetData dd = new DatasetData( d );
        Dataset toSend = dd.asDataset();

        Dataset updated = (Dataset) 
            iPojos.updateDataObject( toSend, null );
        
        Set updt_ids = new HashSet( updated.collectProjectLinks( new IdBlock() ));
        
        System.out.println( orig_ids );
        System.out.println( updt_ids );
        
        assertTrue( updt_ids.containsAll( orig_ids ));
        assertTrue( orig_ids.containsAll( updt_ids ));
        
        
    }
    
  @Test
    public void test_update_annotation() throws Exception
    {
        DataObject annotatedObject;
        AnnotationData data;
        
        Dataset d = new Dataset();
        d.setName( " update_annotation" );
        d = (Dataset) iPojos.createDataObject( d, null );
        annotatedObject = new DatasetData( d );
        
        data = new AnnotationData( AnnotationData.DATASET_ANNOTATION );
        data.setText( " update_annotation " );
        
        IObject updated = iPojos.updateDataObject(
                annotatedObject.asIObject(), null );

        IObject toUpdate = data.asIObject();
        ( (DatasetAnnotation) toUpdate ).setDataset( (Dataset) updated );
        IObject result = iPojos.updateDataObject( toUpdate, null ); /* boom */

        DataObject toReturn = new AnnotationData( (DatasetAnnotation) result );

    }
    
  @Test
    public void test_unloaded_ds_in_ui() throws Exception
    {
//        Project p = new Project(); p.setName("ui");
//        Dataset d = new Dataset(); d.setName("ui");
//        Image i = new Image(); i.setName("ui");
//        p.linkDataset( d );
//        d.linkImage( i );
//        
//        ProjectData pd = new ProjectData( (Project)
//                iPojos.createDataObject( p, null )
//                );
        
        PojoOptions po = new PojoOptions().exp( new Long( 0L ) );
        ProjectData pd_test = new ProjectData( (Project)
                iPojos.loadContainerHierarchy( Project.class, null, po.map())
                .iterator().next()
                );
        DatasetData dd_test = (DatasetData) pd_test.getDatasets().iterator().next();
        pd_test.setDescription("new value:ui");
        
        iPojos.updateDataObject( pd_test.asIObject(), null );
        
        try { 
            dd_test.getName();
            fail(" this should blow up ");
        } catch (Exception e ){ // TODO which exception?
            // good.
        }
        
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

    private void assertUniqueAnnotationCreation(String name, String text)
    {
        // Test
        List ds = iQuery.findAllByString( Dataset.class, "name", name, true, null);
        List as = iQuery.findAllByString( DatasetAnnotation.class, "content", text, true, null);
        
        assertTrue( ds.size() == 1 );
        assertTrue( as.size() == 1 );
    }

}

