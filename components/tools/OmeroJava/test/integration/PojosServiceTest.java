/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import ome.system.OmeroContext;
import ome.testing.OMEData;
import ome.testing.Paths;
import omero.ApiUsageException;
import omero.OptimisticLockException;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.ServerError;
import omero.api.IPixelsPrx;
import omero.api.IPojosPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.constants.CLASSIFICATIONME;
import omero.constants.CLASSIFICATIONNME;
import omero.constants.DECLASSIFICATION;
import omero.model.Annotation;
import omero.model.Category;
import omero.model.CategoryGroup;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Pixels;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.TextAnnotation;
import omero.model.TextAnnotationI;
import omero.sys.ParametersI;
import omero.sys.PojoOptions;
import static omero.rtypes.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import Ice.ObjectFactory;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.TextualAnnotationData;

/**
 * copied from client/test/ome/adapters/pojo/PojosServiceTest for the ticket
 * 1106 October, 2008
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.0
 */
@Test(groups = { "client", "integration", "blitz" })
public class PojosServiceTest extends TestCase {

    protected static Log log = LogFactory.getLog(PojosServiceTest.class);

    ServiceFactoryPrx factory;
    
    CreatePojosFixture2 fixture;

    OMEData data;

    List ids, results, mapped;

    IPojosPrx iPojos;

    IQueryPrx iQuery;

    IUpdatePrx iUpdate;

    Image img;

    ImageData imgData;

    Dataset ds;

    DatasetData dsData;
    
    PojoOptions GROUP_FILTER;

    PojoOptions OWNER_FILTER;

    @Override
    @BeforeTest
    protected void setUp() throws Exception {
        
        OmeroContext test = new OmeroContext(new String[]{
                "classpath:ome/config.xml",
                "classpath:ome/testing/data.xml"});
        data = (OMEData) test.getBean("data");
        
        File local = ResourceUtils.getFile("classpath:local.properties");
        omero.client client = new omero.client(local);
        factory = client.createSession();
        iPojos = factory.getPojosService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();

        omero.client root = new omero.client(local);
        root.createSession("root", client.getProperty("omero.rootpass"));
        fixture = CreatePojosFixture2.withNewUser(root);
        fixture.createAllPojos();

        GROUP_FILTER = new PojoOptions().grp(fixture.g.getId());
        OWNER_FILTER = new PojoOptions().exp(fixture.e.getId());

    }
    
    @Test
    public void testPojos() {
        new RatingAnnotationData(3);
    }

    @Test
    public void testGetSomethingThatsAlwaysThere() throws Exception {
        List l = iQuery.findAllByExample(new ExperimenterI(), null);
        assertTrue("Root has to exist.", l.size() > 0);
        Experimenter exp = (Experimenter) l.get(0);
        assertNotNull("Must have an id", exp.getId());
        assertNotNull("And a name", exp.getFirstName());

        // Now let's try to map it.
        ExperimenterData expData = new ExperimenterData((Experimenter) l.get(0));
        assertNotNull("And something should still be there", expData);
        assertTrue("And it should have an id", expData.getId() > -1);
        assertNotNull("And various other things", expData.getFirstName());
    }

    @Test
    public void testNowLetsTryToSaveSomething() throws Exception {
        imgData = simpleImageData();
        img = (Image) imgData.asIObject();

        img = (Image) iPojos.createDataObject(img, null);
        assertNotNull("We should get something back", img);
        assertNotNull("Should have an id", img.getId());

        Image img2 = (Image) iQuery.get(Image.class.getName(), img.getId().getValue());
        assertNotNull("And we should be able to find it again.", img2);

    }

    @Test
    public void testAndSaveSomtheingWithParents() throws Exception {
        saveImage();
        ds = img.linkedDatasetList().get(0);
        Long id = ds.getId().getValue();

        // another copy
        Image img2 = (Image) iQuery.findAllByQuery(
                "select i from Image i "
                        + "left outer join fetch i.datasetLinks "
                        + "where i.id = :id",
                new ParametersI().addId(img.getId())).get(0);
        assertTrue("It better have a dataset link too", img2
                .sizeOfDatasetLinks() > 0);
        Dataset ds2 = img2.linkedDatasetList().get(0);
        assertEquals("And the ids have to be the same", id.longValue(), ds2.getId().getValue());
    }

    @Test(groups = { "versions", "broken" })
    public void testButWeHaveToHandleTheVersions() throws Exception {
        Image img = new ImageI();
        img.setName( rstring("version handling") );
        Image sent = (Image) iUpdate.saveAndReturnObject(img);

        sent.setDescription( rstring(" veresion handling update") );
        RInt version = sent.getVersion();

        // Version incremented
        Image sent2 = (Image) iUpdate.saveAndReturnObject(sent);
        RInt version2 = sent2.getVersion();
        assertTrue(version.getValue() != version2.getValue());

        // Resetting; should get error
        sent2.setVersion(version);
        TextAnnotation iann = new TextAnnotationI();
        iann.setTextValue( rstring(" version handling "));
        // iann.setImage(sent2);

        try {
            iUpdate.saveAndReturnObject(sent2);
            fail("Need optmistic lock exception.");
        } catch (OptimisticLockException e) {
            // ok.
        }

        // Fixing the change;
        // now it should work.
        sent2.setVersion( version2 );
        iUpdate.saveAndReturnObject(iann);

    }

    @Test
    public void testNowOnToSavingAndDeleting() throws Exception {
        imgData = simpleImageData();
        img = (Image) imgData.asIObject();

        assertNull("Image doesn't have an id.", img.getId());
        img = (Image)iPojos.createDataObject(img, null);
        assertNotNull("Presto change-o, now it does.", img.getId());
        iPojos.deleteDataObject(img, null);

        img = (Image) iQuery.find(Image.class.getName(), img.getId().getValue());
        assertNull("we should have deleted it ", img);

    }

    @Test
    public void testLetsTryToLinkTwoThingsTogether() throws Exception {
        imgData = simpleImageData();
        dsData = simpleDatasetData();

        img = (Image) imgData.asIObject();
        ds = (Dataset) dsData.asIObject();

        DatasetImageLink link = new DatasetImageLinkI();
        link.link(ds, img);

        IObject test = iPojos.link( Arrays.<IObject>asList(link) , null).get(0);
        assertNotNull("ILink should be there", test);

    }

    @Test(groups = { "broken", "ticket:541" })
    public void testAndHeresHowWeUnlinkThings() throws Exception {

        // Method 1:
        saveImage();
        List updated = unlinkImage();
        iUpdate.saveCollection(updated);

        // Make sure it's not linked.
        List list = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 2:
        saveImage();
        updated = unlinkImage();
        iPojos.updateDataObjects(updated, null);

        List list2 = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 3:
        saveImage();
        Dataset target = img.linkedDatasetList().get(0);
        // For querying
        DatasetImageLink dslink = img.findDatasetImageLink(target).iterator()
                .next();

        img.unlinkDataset(target);
        img = (Image) iPojos.updateDataObject(img, null);

        IObject test = iQuery.find(DatasetImageLink.class.getName(), dslink.getId()
                .getValue());
        assertNull(test);

        // Method 4;
        Dataset d = new DatasetI();
        d.setName(rstring("unlinking"));
        Project p = new ProjectI();
        p.setName( rstring("unlinking") );
        p = (Project) iPojos.createDataObject(p, null);
        d = (Dataset) iPojos.createDataObject(d, null);

        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d);
    }

    @Test(groups = { "broken", "ticket:541" })
    public void testHeresHowWeUnlinkFromJustOneSide() throws Exception {
        saveImage();
        DatasetImageLink link = img.copyDatasetLinks().get(0);
        img.removeDatasetImageLink2(link, false);

        iPojos.updateDataObject(img, null);

        DatasetImageLink test = (DatasetImageLink) iQuery.find(DatasetImageLink.class.getName(), link
                .getId().getValue());

        assertNull(test);

    }

    private void saveImage() throws ServerError {
        imgData = simpleImageDataWithDatasets();
        img = (Image) imgData.asIObject();

        img = (Image) iUpdate.saveAndReturnObject(img);
        assertTrue("It better have a dataset link",
                img.sizeOfDatasetLinks() > 0);
    }

    private List unlinkImage() {
        List updated = img.linkedDatasetList();
        for (Object o : updated) {
            img.unlinkDataset((Dataset)o);
        }
        updated.add(img);
        return updated;
    }

    //
    // READ API
    // 

    @Test
    public void test_loadContainerHierarchy() throws Exception {

        ids = Arrays.asList(fixture.pu9990.getId().getValue(), fixture.pu9991
                .getId().getValue());
        results = iPojos.loadContainerHierarchy(Project.class.getName(), ids, null);

        PojoOptions po = new PojoOptions().exp(rlong(0L));
        results = iPojos.loadContainerHierarchy(Project.class.getName(), null, po.map());

    }

    @Test(groups = "EJBExceptions")
    public void test_findContainerHierarchies() throws ServerError {

        PojoOptions defaults = new PojoOptions(), empty = new PojoOptions(new HashMap());

        ids = data.getMax("Image.ids", 2);
        results = iPojos.findContainerHierarchies(Project.class.getName(), ids, defaults
                .map());

        try {
            results = iPojos.findContainerHierarchies(Dataset.class.getName(), ids, empty
                    .map());
            fail("Should fail");
        } catch (ApiUsageException e) {
            // ok.
        }

        ids = data.getMax("Image.ids", 2);
        results = iPojos.findContainerHierarchies(CategoryGroup.class.getName(), ids,
                defaults.map());

    }

    @Test
    public void test_findAnnotations() throws ServerError {

        Map<Long, List<IObject>> m;

        ids = data.getMax("Image.Annotated.ids", 2);
        m = iPojos.findAnnotations(Image.class.getName(), ids, null, null);
        assertAnnotations(m);

        ids = data.getMax("Dataset.Annotated.ids", 2);
        m = iPojos.findAnnotations(Dataset.class.getName(), ids, null, null);
        assertTrue(m.size() > 0);
        assertAnnotations(m);

        Map<Long, Set<AnnotationData>> m2 = DataObject.asPojos(m);
        AnnotationData data = m2.values().iterator().next().iterator().next();
        assertNotNull(data.getOwner());

    }

    void assertAnnotations(Map<Long, List<IObject>> m) {
        Annotation ann = (Annotation) m.values().iterator().next().iterator()
                .next();
        assertNotNull(ann.getDetails().getOwner());
        assertTrue(ann.getDetails().getOwner().isLoaded());
        assertNotNull(ann.getDetails().getCreationEvent());
        assertTrue(ann.getDetails().getCreationEvent().isLoaded());
        // Annotations are immutable
        // assertNotNull(ann.getDetails().getUpdateEvent());
        // assertTrue(ann.getDetails().getUpdateEvent().isLoaded());

    }

    @Test
    public void test_retrieveCollection() throws Exception {
        Image i = (Image) iQuery.get(Image.class.getName(), fixture.iu5551.getId().getValue());
        i.unload();
        List<IObject> annotations = iPojos.retrieveCollection(i,
                ImageI.ANNOTATIONLINKS, null);
        assertTrue(annotations.size() > 0);
    }

    @Test
    public void test_findCGCPaths() throws Exception {
        ids = data.getMax("Image.ids", 2);
        results = iPojos.findCGCPaths(ids, CLASSIFICATIONME.value, null);
        results = iPojos.findCGCPaths(ids, CLASSIFICATIONNME.value, null);
        results = iPojos.findCGCPaths(ids, DECLASSIFICATION.value, null);
    }

    @Test(groups = "broken")
    public void test_findCGCPaths_declass() throws Exception {
        Paths paths = new Paths(data.get("CGCPaths.all"));
        List de = iPojos.findCGCPaths(new ArrayList(paths.uniqueImages()),
                DECLASSIFICATION.value, null);
        assertTrue(de.size() == paths.unique(Paths.CG, Paths.EXISTS,
                Paths.EXISTS, Paths.EXISTS).size());

        for (Iterator it = de.iterator(); it.hasNext();) {
            CategoryGroup cg = (CategoryGroup) it.next();
            Iterator it2 = cg.linkedCategoryList().iterator();
            while (it2.hasNext()) {
                Category c = (Category) it2.next();
                Iterator it3 = c.linkedImageList().iterator();
                while (it3.hasNext()) {
                    Image i = (Image) it3.next();
                    Set found = paths.find(cg.getId().getValue(), c.getId().getValue(), i.getId().getValue());
                    assertTrue(found.size() == 1);

                }
            }
        }

        Long single_i = paths.singlePath()[Paths.I.intValue()];
        List one_de = iPojos.findCGCPaths(Collections.singletonList(single_i),
                DECLASSIFICATION.value, null);
        assertTrue(one_de.size() == paths.unique(Paths.CG, Paths.WILDCARD,
                Paths.WILDCARD, single_i).size());

    }

    @Test
    public void test_findCGCPaths_class() throws Exception {
        // Finding a good test
        Long[] targetPath = null;
        Paths paths = new Paths(data.get("CGCPaths.all"));
        Set withNoImages = paths.find(Paths.WILDCARD, Paths.WILDCARD,
                Paths.NULL_IMAGE);

        for (Iterator it = withNoImages.iterator(); it.hasNext();) {
            Long n = (Long) it.next();

            // Must be at least two Categories in one CG since we're only
            // examining the Categories in this CategoryGroup w/o an image.
            // Now need one with an image.
            Long[] values = paths.get(n);
            Set target = paths.find(values[Paths.CG.intValue()],
                    Paths.WILDCARD, Paths.EXISTS);
            if (target.size() > 0) {
                targetPath = paths.get((Long) target.iterator().next());
                break;
            }
        }

        assert targetPath != null : "No valid category group found for classification test.";

        List single = Collections.singletonList(targetPath[Paths.I.intValue()]);
        List me = iPojos.findCGCPaths(single, CLASSIFICATIONME.value, null);
        List nme = iPojos.findCGCPaths(single, CLASSIFICATIONNME.value, null);

        for (Iterator it = nme.iterator(); it.hasNext();) {
            CategoryGroup group = (CategoryGroup) it.next();
            if (group.getId().equals(targetPath[Paths.CG.intValue()])) {
                for (Iterator it2 = group.linkedCategoryList().iterator(); it
                        .hasNext();) {
                    Category c = (Category) it2.next();
                    if (c.getId().equals(targetPath[Paths.C.intValue()])) {
                        fail("Own category should not be included.");
                    }
                }
            }

        }

        for (Iterator it3 = nme.iterator(); it3.hasNext();) {
            CategoryGroup group = (CategoryGroup) it3.next();
            if (group.getId().equals(targetPath[Paths.CG.intValue()])) {
                fail("Should not be in mutually-exclusive set.");
            }
        }

    }

    @Test(groups = "EJBExceptions")
    public void testCountingApiExceptions() throws Exception{

        List ids = Collections.singletonList(new Long(1));

        // Does not exist
        try {
            iPojos.getCollectionCount("DoesNotExist", "meNeither", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Missing plural on dataset
        try {
            iPojos.getCollectionCount("ome.model.containers.Project",
                    "dataset", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Null ids
        try {
            iPojos.getCollectionCount("ome.model.containers.Project",
                    "datasets", null, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Poorly formed
        try {
            iPojos.getCollectionCount("hackers.rock!!!", "", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Empty Class string
        try {
            iPojos.getCollectionCount("", "datasets", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Empty Class string
        try {
            iPojos.getCollectionCount(null, "datasets", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Empty property string
        try {
            iPojos.getCollectionCount("ome.model.core.Image", "", ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

        // Null property string
        try {
            iPojos.getCollectionCount("ome.model.core.Image", null, ids, null);
            fail("An exception should have been thrown");
        } catch (ApiUsageException e) {
            // ok.
        }

    }

    @Test
    public void test_getCollectionCount() throws Exception {
        Long id = fixture.iu5551.getId().getValue();
        Map m = iPojos.getCollectionCount(Image.class.getName(),
                ImageI.ANNOTATIONLINKS, Collections.singletonList(id), null);
        Long count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

        id = fixture.du7771.getId().getValue();
        m = iPojos.getCollectionCount(Dataset.class.getName(),
                DatasetI.IMAGELINKS, Collections.singletonList(id), null);
        count = (Long) m.get(id);
        assertTrue(count.longValue() > 0);

    }

    @Test
    public void test_getImages() throws Exception {
        ids = data.getMax("Project.ids", 2);
        List images = iPojos.getImages(Project.class.getName(), ids, null);
    }

    @Test
    public void test_getUserDetails() throws Exception {
        Map m = iPojos.getUserDetails(Collections.singletonList(fixture.TESTER),
                null);
        Experimenter e = (Experimenter) m.get(fixture.TESTER);
        ExperimenterGroup g;
        assertNotNull(g = e.getPrimaryGroupExperimenterMap().getParent());
        assertNotNull(g.getName());
        for (ExperimenterGroup gg : e.linkedExperimenterGroupList()) {
            assertNotNull(gg.getName());
        }
    }

    @Test(groups = "EJBExceptions")
    public void test_getUserImages() throws Exception {
        try {
            results = iPojos.getUserImages(null);
            fail("APIUsage: experimenter/group option must be set.");
        } catch (ApiUsageException e) {
            // ok.
        }

        results = iPojos.getUserImages(new PojoOptions().exp(fixture.e.getId())
                .map());
        assertTrue(results.size() > 0);

    }

    //
    // Misc
    //

    @Test(groups = { "broken", "ticket:334" })
    public void testAndForTheFunOfItLetsGetTheREWorking() throws Exception {

        Pixels pix = (Pixels) iQuery.findAll(Pixels.class.getName(), null).get(0);
        IPixelsPrx pixDB = factory.getPixelsService();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(pix.getId().getValue());
        re.load();

        omero.romio.PlaneDef pd = new omero.romio.PlaneDef();
        pd.slice = omero.romio.XY.value;
        pd.z = 0;
        pd.t = 0;
        re.render(pd);

    }

    // /
    // ========================================================================
    // / ~ Versions
    // /
    // ========================================================================

    @Test(groups = { "versions", "broken", "ticket:118" })
    public void test_version_doesnt_increase_on_non_change() throws Exception {
        Image img = new ImageI();
        img.setName( rstring(" no vers. increment ")) ;
        img = (Image) iUpdate.saveAndReturnObject(img);

        Image test = (Image) iUpdate.saveAndReturnObject(img);

        fail("must move details correction to the merge event listener "
                + "or version will always be incremented. ");

        assertTrue(img.getVersion().equals(test.getVersion()));

    }

    @Test(groups = { "versions", "broken", "ticket:118" })
    public void test_version_doesnt_increase_on_linked_update()
            throws Exception {
        TextAnnotation ann = new TextAnnotationI();
        Image img = new ImageI();

        img.setName( rstring("version_test") );
        ann.setTextValue( rstring("version_test") );
        img.linkAnnotation(ann);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ann = (TextAnnotation) img.linkedAnnotationList().get(0);

        assertNotNull(img.getId());
        assertNotNull(ann.getId());

        int orig_img_version = img.getVersion().getValue();
        // No longer exists int orig_ann_version = ann.getVersion().intValue();

        ann.setTextValue( rstring("updated version_test") );

        ann = (TextAnnotation) iUpdate.saveAndReturnObject(ann);
        img = (Image) iQuery.get(Image.class.getName(), img.getId().getValue()); // ann.getImage();

        // No longer existsint new_ann_version = ann.getVersion().intValue();
        int new_img_version = img.getVersion().getValue();

        assertFalse(ann.getTextValue().getValue().contains("updated"));
        assertTrue(orig_img_version == new_img_version);

    }

    // /
    // ========================================================================
    // / ~ Counts
    // /
    // ========================================================================

    @Test
    public void test_counts() throws Exception {
        Dataset counts;
        long self = factory.getAdminService().getEventContext()
                .userId;

        // Counts are now always loaded
        counts = loadForCounts(fixture.du7770.getId().getValue(), null);
        // 7770 has not links
        assertNull(counts.getAnnotationLinksCountPerOwner().get(self));

        counts = loadForCounts(fixture.du7771.getId().getValue(), null);
        // Here we get the first map, since fixture creates entities
        // as other users too
        assertNotNull(counts.getImageLinksCountPerOwner());
        assertTrue(counts.getImageLinksCountPerOwner().values().iterator()
                .next() > 0);
        assertNotNull(counts.getAnnotationLinksCountPerOwner());
        assertTrue(counts.getAnnotationLinksCountPerOwner().values().iterator()
                .next().equals(1L));

    }

    private Dataset loadForCounts(Long id, Map options) throws ServerError {
        Dataset obj = (Dataset) iPojos.loadContainerHierarchy(Dataset.class.getName(),
                Collections.singletonList(id), options).iterator().next();
        return obj;
    }

    // /
    // ========================================================================
    // / ~ Various bug-like checks
    // /
    // ========================================================================

    @Test
    public void test_no_duplicate_rows() throws Exception {
        String name = "TEST:" + System.currentTimeMillis();

        // Save Project.
        Project p = new ProjectI();
        p.setName( rstring(name) );
        p = (Project) iUpdate.saveAndReturnObject(p);

        // Check only one
        List list = iQuery.findAllByString(Project.class.getName(), "name", name, true,
                null);
        assertTrue(list.size() == 1);
        assertEquals(((Project) list.get(0)).getId().getValue(),p.getId().getValue());

        // Update it.
        ProjectData pd = new ProjectData(p);
        pd.setDescription("....testnodups....");
        Project send = (Project) pd.asIObject();
        assertEquals(p.getId().getValue(), pd.getId());
        assertEquals(send.getId().getValue(), pd.getId());

        Project result = (Project) iPojos.updateDataObject(send, null);
        ProjectData test = new ProjectData(result);
        assertEquals(test.getId(), p.getId().getValue());

        // Check again.
        List list2 = iQuery.findAllByString(Project.class.getName(), "name", name, true,
                null);
        assertTrue(list2.size() == 1);
        assertEquals(((Project) list.get(0)).getId().getValue(),
                ((Project) list2.get(0)).getId().getValue());

    }

    @Test
    public void test_no_duplicate_links() throws Exception {
        Image img = new ImageI();
        img.setName( rstring("duplinks") );

        Dataset ds = new DatasetI();
        ds.setName( rstring("duplinks") );

        img.linkDataset(ds);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ds = img.linkedDatasetList().get(0);

        List imgLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));

        List dsLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("parent.id", ds.getId()));

        assertTrue(imgLinks.size() == 1);
        assertTrue(dsLinks.size() == 1);

        assertTrue(((DatasetImageLink) imgLinks.get(0)).getId().equals(
                ((DatasetImageLink) dsLinks.get(0)).getId()));

    }

    @Test
    public void test_no_duplicates_on_save_array() throws Exception {
        Image img = new ImageI();
        img.setName( rstring("duplinks") );

        Dataset ds = new DatasetI();
        ds.setName( rstring("duplinks") );

        img.linkDataset(ds);

        List<IObject> retVal = iUpdate
                .saveAndReturnArray(Arrays.asList(img, ds ));
        img = (Image) retVal.get(0);
        ds = (Dataset) retVal.get(1);

        List imgLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("child.id", img.getId()));

        List dsLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new ParametersI().addLong("parent.id", ds.getId()));

        assertTrue(imgLinks.size() == 1);
        assertTrue(dsLinks.size() == 1);

        assertTrue(((DatasetImageLink) imgLinks.get(0)).getId().equals(
                ((DatasetImageLink) dsLinks.get(0)).getId()));

    }

    @Test
    public void test_annotating_a_dataset_cglib_issue() throws Exception {

        // Setup: original is our in-memory, used every where object.
        Dataset original = new DatasetI();
        original.setName( rstring(" two rows ") );
        original = (Dataset) iPojos.createDataObject(original, null);
        DatasetData annotatedObject = new DatasetData(original);
        Dataset annotated = (Dataset) iPojos.updateDataObject(annotatedObject
                .asIObject(), null);
        // Dataset m = new Dataset( original.getId(), false);
        TextAnnotation annotation = new TextAnnotationI();
        annotation.setNs( rstring("") );
        annotation.setTextValue( rstring(" two rows content ") );

        // CGLIB
        TextAnnotation object = (TextAnnotation) iPojos.createDataObject(annotation, null);
        DataObject returnedToUser = new TextualAnnotationData(object);

        // Now working but iPojos is still returning a CGLIB class.
        assertTrue(String.format("Class %s should equal class %s", object
                .getClass(), annotation.getClass()), object.getClass().equals(
                annotation.getClass()));
    }

    @Test
    public void test_annotating_a_dataset() throws Exception {
        long self = factory.getAdminService().getEventContext()
                .userId;

        String name = " two rows " + System.currentTimeMillis();
        String text = " two rows content " + System.currentTimeMillis();
        String desc = " new description " + System.currentTimeMillis();

        // Setup: original is our in-memory, used every where object.
        Dataset original = new DatasetI();
        original.setName( rstring (name) );
        original = (Dataset) iPojos.createDataObject(original, null);

        // No longer return these from create methods.
        assertNull(original.getAnnotationLinksCountPerOwner());
        // assertNull(original.getAnnotationLinksCountPerOwner().get(self));

        original.setDescription( rstring(desc) );

        TextAnnotation annotation = new TextAnnotationI();
        annotation.setNs( rstring("") );
        annotation.setTextValue( rstring(text) );
        original.linkAnnotation(annotation);

        original = (Dataset) iPojos.createDataObject(original, null);
        annotation = (TextAnnotation) original.linkedAnnotationList().get(0);

        assertUniqueAnnotationCreation(name, text);

        Dataset test = (Dataset) iQuery.get(Dataset.class.getName(), original.getId().getValue());

        assertTrue(desc.equals(test.getDescription()));

        // createDataObjects no longer does counts
        // assertNotNull(original.getAnnotationLinksCountPerOwner());
        // assertNotNull(original.getAnnotationLinksCountPerOwner().get(self));
        // assertTrue(original.getAnnotationLinksCountPerOwner().get(self) >
        // 0L);

    }

    @Test
    public void test_two_datasets_and_a_project() throws Exception {
        String name = " 2&1 " + System.currentTimeMillis();
        Project p = new ProjectI();
        p.setName( rstring(name) );

        p = (Project) iPojos.createDataObject(p, null);

        Dataset d1 = new DatasetI();
        d1.setName( rstring(name) );
        d1 = (Dataset) iPojos.createDataObject(d1, null);

        Dataset d2 = new DatasetI();
        d2.setName( rstring(name) );
        d2 = (Dataset) iPojos.createDataObject(d2, null);

        ProjectDatasetLink l1 = new ProjectDatasetLinkI();
        ProjectDatasetLink l2 = new ProjectDatasetLinkI();

        l1.setParent(p);
        l1.setChild(d1);

        l2.setParent(p);
        l2.setChild(d2);

        p.addProjectDatasetLink2(l1, true);
        p.addProjectDatasetLink2(l2, true);

        p = (Project) iPojos.updateDataObject(p, null);

        Iterator it = p.copyDatasetLinks().iterator();
        while (it.hasNext()) {
            ProjectDatasetLink link = (ProjectDatasetLink) it.next();
            if (link.getChild().getId().getValue() == d1.getId().getValue()) {
                l1 = link;
                d1 = link.getChild();
            } else if (link.getChild().getId().getValue() == d2.getId().getValue()) {
                l2 = link;
                d2 = link.getChild();
            } else {
                fail(" Links aren't set up propertly");
            }

        }

        d1.setDescription( rstring(name) );

        Dataset test = (Dataset) iPojos.updateDataObject(d1, null);

        ProjectDatasetLink link1 = (ProjectDatasetLink) iQuery.get(ProjectDatasetLink.class.getName(), l1
                .getId().getValue());

        assertNotNull(link1);
        assertEquals(link1.getParent().getId().getValue(), p.getId().getValue()); 
        assertEquals(link1.getChild().getId().getValue(), d1.getId().getValue());

        ProjectDatasetLink link2 = (ProjectDatasetLink) iQuery.get(ProjectDatasetLink.class.getName(), l2
                .getId().getValue());

        assertNotNull(link2);
        assertEquals(link2.getParent().getId().getValue(), p.getId().getValue());
        assertEquals(link2.getChild().getId().getValue(), d2.getId().getValue());

    }

    @Test
    public void test_delete_annotation() throws Exception {
        String string = "delete_annotation" + System.currentTimeMillis();

        Dataset d = new DatasetI();
        d.setName( rstring(string) );

        TextAnnotation a = new TextAnnotationI();
        a.setNs( rstring("") );
        a.setTextValue(rstring(string) );
        d.linkAnnotation(a);

        d = (Dataset) iPojos.createDataObject(d, null);
        DatasetAnnotationLink al = d.copyAnnotationLinks().iterator()
                .next();
        a = (TextAnnotation) al.getChild();

        iPojos.deleteDataObject(al, null);
        iPojos.deleteDataObject(a, null);

        Object o = iQuery.find(TextAnnotation.class.getName(), a.getId().getValue());
        assertNull(o);

    }

    @Test
    public void test_duplicate_links_again() throws Exception {

        String string = "duplinksagain" + System.currentTimeMillis();

        Dataset d = new DatasetI();
        d.setName( rstring(string) );

        Project p = new ProjectI();
        p.setName( rstring(string) );

        d.linkProject(p);
        d = (Dataset) iPojos.createDataObject(d, null);
        List<Project> orig = d.linkedProjectList();
        Set orig_ids = new HashSet();
        for (Project _p : orig) {
            orig_ids.add(_p.getId().getValue());
        }

        DatasetData dd = new DatasetData(d);
        Dataset toSend = dd.asDataset();

        Dataset updated = (Dataset) iPojos.updateDataObject(toSend, null);

        List<Project> updt = updated.linkedProjectList();
        Set updt_ids = new HashSet();
        for (Project _p : updt) {
            updt_ids.add(_p.getId().getValue());
        }

        if (log.isDebugEnabled()) {
            log.debug(orig_ids);
            log.debug(updt_ids);
        }

        assertTrue(updt_ids.containsAll(orig_ids));
        assertTrue(orig_ids.containsAll(updt_ids));

    }

    @Test
    public void test_update_annotation() throws Exception {
        DataObject annotatedObject;
        AnnotationData data;

        Dataset d = new DatasetI();
        d.setName( rstring(" update_annotation") );
        d = (Dataset) iPojos.createDataObject(d, null);
        annotatedObject = new DatasetData(d);

        data = new TextualAnnotationData(" update_annotation ");
       
        IObject updated = iPojos.updateDataObject(annotatedObject.asIObject(),
                null);

        DatasetAnnotationLink link = ((Dataset) updated).linkAnnotation(data.asAnnotation());
        link = (DatasetAnnotationLink) iPojos.updateDataObject(link, null);
        link.getChild().unload();

        DataObject toReturn = 
        	new TextualAnnotationData((TextAnnotation) link.getChild());

    }

    @Test(groups = { "version", "broken" })
    public void test_unloaded_ds_in_ui() throws Exception {
        Project p = new ProjectI();
        p.setName( rstring("ui") );
        Dataset d = new DatasetI();
        d.setName( rstring("ui") );
        Image i = new ImageI();
        i.setName( rstring("ui") );
        p.linkDataset(d);
        d.linkImage(i);

        p = (Project) iPojos.createDataObject(p, null);

        ProjectData pd_test = new ProjectData((Project)iPojos.loadContainerHierarchy(
                Project.class.getName(), Collections.singletonList(p.getId().getValue()), null)
                .iterator().next());
        DatasetData dd_test = pd_test.getDatasets().iterator().next();
        pd_test.setDescription("new value:ui");

        iPojos.updateDataObject(pd_test.asIObject(), null);

        try {
            dd_test.getName();
            fail(" this should blow up ");
        } catch (Exception e) { // TODO which exception?
            // good.
        }

    }

    // TODO move to another class
    // now let's test all methods that use the filtering functionality
    // ===========================================================
  
    private void assertFilterWorked(List<?> _results,
            Integer min, Integer max, Experimenter e, ExperimenterGroup g) {
        if (min != null) {
            assertTrue(_results.size() > min);
        }
        if (max != null) {
            assertTrue(_results.size() < max);
        }
        List<IObject> __results = (List<IObject>) _results;
        if (e != null) {
            for (IObject iobj : __results) {
                assertEquals(e.getId().getValue(),
                        iobj.getDetails().getOwner().getId().getValue());
            }
        }
        if (g != null) {
            for (IObject iobj : __results) {
                assertEquals(g.getId().getValue(),
                        iobj.getDetails().getGroup().getId().getValue());
            }
        }
    }

    @Test(groups = "ticket:318")
    public void testFilters_getUserImages() throws Exception {

        // nothing should throw an exception
        try {
            iPojos.getUserImages(null);
            fail();
        } catch (ApiUsageException api) {
            // ok
        }

        // TODO MOVE TO FIXTURE
        // First we'll need to create an image from the user but not in the
        // group,
        // and from the group but not the user
        // Image i1 = new Image(); i.setName("user not group");
        // i.getDetails().setOwner(fixture.e);
        // i.getDetails().setGroup(new ExperimenterGroup(1L));
        // i =
        // Image i2 = new Image(); i.

        // just filtering for the user should get us everything
        List<Image> imgs = iPojos.getUserImages(OWNER_FILTER.map());
        assertFilterWorked(imgs, 0, null, fixture.e, null);

        // now for groups
        imgs = iPojos.getUserImages(GROUP_FILTER.map());
        assertFilterWorked(imgs, 0, null, null, fixture.g);

    }

    @Test(groups = "ticket:318")
    public void testFilters_getImages() throws Exception {

        // there are about 6 projects in our fixture
        ids = data.getMax("Project.ids", 100);

        List<Image> images = iPojos.getImages(Project.class.getName(), ids, OWNER_FILTER
                .map());
        assertFilterWorked(images, null, 100, fixture.e, null);

        images = iPojos.getImages(Project.class.getName(), ids, GROUP_FILTER.map());
        assertFilterWorked(images, null, 100, null, fixture.g);

    }

    @Test(groups = "ticket:318")
    public void testFilters_findCGCPaths() throws Exception {
        ids = data.getMax("Image.ids", 100);

        results = iPojos.findCGCPaths(ids, CLASSIFICATIONME.value,
                OWNER_FILTER.map());
        assertFilterWorked(results, null, 100, fixture.e, null);

        results = iPojos.findCGCPaths(ids, CLASSIFICATIONME.value,
                GROUP_FILTER.map());
        assertFilterWorked(results, null, 100, null, fixture.g);

        results = iPojos.findCGCPaths(ids, CLASSIFICATIONNME.value,
                OWNER_FILTER.map());
        assertFilterWorked(results, null, 100, fixture.e, null);

        results = iPojos.findCGCPaths(ids, CLASSIFICATIONNME.value,
                GROUP_FILTER.map());
        assertFilterWorked(results, null, 100, null, fixture.g);

        results = iPojos.findCGCPaths(ids, DECLASSIFICATION.value,
                OWNER_FILTER.map());
        assertFilterWorked(results, null, 100, fixture.e, null);

        results = iPojos.findCGCPaths(ids, DECLASSIFICATION.value,
                GROUP_FILTER.map());
        assertFilterWorked(results, null, 100, null, fixture.g);

    }

    @Test(groups = "ticket:318")
    public void testFilters_findContainerHierarchies() throws Exception {
        ids = data.getMax("Image.ids", 100);

        try {
            results = iPojos.findContainerHierarchies(Project.class.getName(), ids,
                    OWNER_FILTER.map());
            assertFilterWorked(results, null, 100, fixture.e, null);
        } catch (AssertionFailedError afe) {
            // this may fail since the images aren't filtered
        }

        // but this shouldn't.
        Iterator it = results.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof Image) {
                it.remove();
            }
        }
        assertFilterWorked(results, null, 100, fixture.e, null);

        try {
            results = iPojos.findContainerHierarchies(Project.class.getName(), ids,
                    GROUP_FILTER.map());
            assertFilterWorked(results, null, 100, null, fixture.g);
        } catch (AssertionFailedError afe) {
            // again, this may fail
        }

        // but this shouldn't.
        it = results.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof Image) {
                it.remove();
            }
        }
        assertFilterWorked(results, null, 100, null, fixture.g);

    }

    @Test(groups = "ticket:318")
    public void testFilters_loadContainerHierarchy() throws Exception {

        ids = data.getMax("Project.ids", 2);

        results = iPojos.loadContainerHierarchy(Project.class.getName(), ids,
                OWNER_FILTER.map());
        assertFilterWorked(results, null, 100, fixture.e, null);

        results = iPojos.loadContainerHierarchy(Project.class.getName(), ids,
                GROUP_FILTER.map());
        assertFilterWorked(results, null, 100, null, fixture.g);
    }

    @Test(groups = "ticket:1106")
    public void testCreateObjectSendingRootWithEmptyCollection() throws Exception {
        ProjectData data = new ProjectData();
        data.setName("name");
        Project p = (Project) iPojos.createDataObject(data.asIObject(), null);
        assertTrue(p.getDetails().getGroup().sizeOfGroupExperimenterMap() != 0);
        assertTrue(p.getDetails().getOwner().sizeOfGroupExperimenterMap() != 0);
    }
    
    // ~ Helpers
    // =========================================================================

    private ImageData simpleImageData() {
        // prepare data
        ImageData id = new ImageData();
        id.setName("My test image");
        id.setDescription("My test description");
        return id;
    }

    private DatasetData simpleDatasetData() {
        DatasetData dd = new DatasetData();
        dd.setName("t1");
        dd.setDescription("t1");
        return dd;
    }

    private ImageData simpleImageDataWithDatasets() {
        DatasetData dd = simpleDatasetData();
        Set dss = new HashSet();
        dss.add(dd);
        ImageData id = simpleImageData();
        id.setDatasets(dss);
        return id;
    }

    private void assertUniqueAnnotationCreation(String name, String text) throws ServerError {
        // Test
        List ds = iQuery.findAllByString(Dataset.class.getName(), "name", name, true,
                null);
        List as = iQuery.findAllByString(TextAnnotation.class.getName(), "textValue",
                text, true, null);

        assertTrue(ds.size() == 1);
        assertTrue(as.size() == 1);
    }

}
