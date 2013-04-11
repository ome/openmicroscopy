/*
 * ome.adapters.pojos.itests.PojosServiceTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.adapters.pojos.itests;

// Java imports
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.OptimisticLockException;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.DatasetAnnotationLink;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.testing.CreatePojosFixture;
import ome.testing.OMEData;
import ome.testing.Paths;
import ome.util.CBlock;
import ome.util.IdBlock;
import ome.util.builders.PojoOptions;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.TextualAnnotationData;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.0
 */
@Test(groups = { "client", "integration" })
public class PojosServiceTest extends TestCase {

    protected static Logger log = LoggerFactory.getLogger(PojosServiceTest.class);

    ServiceFactory factory = new ServiceFactory("ome.client.test");

    CreatePojosFixture fixture;

    OMEData data;

    Set ids, results, mapped;

    IPojos iPojos;

    IQuery iQuery;

    IUpdate iUpdate;

    Image img;

    ImageData imgData;

    Dataset ds;

    DatasetData dsData;

    @Override
    @Configuration(beforeTestClass = true)
    protected void setUp() throws Exception {
        data = (OMEData) factory.getContext().getBean("data");
        iPojos = factory.getPojosService();
        iQuery = factory.getQueryService();
        iUpdate = factory.getUpdateService();

        try {
            iQuery.get(Experimenter.class, 0l);
        } catch (Throwable t) {
            // TODO no, no, really. This is ok. (And temporary)
        }

        iQuery.get(Experimenter.class, 0l);
        // if this one fails, skip rest.

        Login rootLogin = (Login) factory.getContext().getBean("rootLogin");
        ServiceFactory rootFactory = new ServiceFactory(rootLogin);
        fixture = CreatePojosFixture.withNewUser(rootFactory);
        fixture.createAllPojos();

        GROUP_FILTER = new PojoOptions().grp(fixture.g.getId());
        OWNER_FILTER = new PojoOptions().exp(fixture.e.getId());

    }

    @Test
    public void testGetSomethingThatsAlwaysThere() throws Exception {
        List l = iQuery.findAllByExample(new Experimenter(), null);
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

        img = iPojos.createDataObject(img, null);
        assertNotNull("We should get something back", img);
        assertNotNull("Should have an id", img.getId());

        Image img2 = iQuery.get(Image.class, img.getId().longValue());
        assertNotNull("And we should be able to find it again.", img2);

    }

    @Test
    public void testAndSaveSomtheingWithParents() throws Exception {
        saveImage();
        ds = img.linkedDatasetIterator().next();
        Long id = ds.getId();

        // another copy
        Image img2 = (Image) iQuery.findAllByQuery(
                "select i from Image i "
                        + "left outer join fetch i.datasetLinks "
                        + "where i.id = :id",
                new Parameters().addId(img.getId())).get(0);
        assertTrue("It better have a dataset link too", img2
                .sizeOfDatasetLinks() > 0);
        Dataset ds2 = img2.linkedDatasetIterator().next();
        assertTrue("And the ids have to be the same", id.equals(ds2.getId()));
    }

    @Test(groups = { "versions", "broken" })
    public void testButWeHaveToHandleTheVersions() throws Exception {
        Image img = new Image();
        img.setName("version handling");
        Image sent = iUpdate.saveAndReturnObject(img);

        sent.setDescription(" veresion handling update");
        Integer version = sent.getVersion();

        // Version incremented
        Image sent2 = iUpdate.saveAndReturnObject(sent);
        Integer version2 = sent2.getVersion();
        assertTrue(!version.equals(version2));

        // Resetting; should get error
        sent2.setVersion(version);
        TextAnnotation iann = new TextAnnotation();
        iann.setTextValue(" version handling ");
        // iann.setImage(sent2);

        try {
            iUpdate.saveAndReturnObject(sent2);
            fail("Need optmistic lock exception.");
        } catch (OptimisticLockException e) {
            // ok.
        }

        // Fixing the change;
        // now it should work.
        sent2.setVersion(version2);
        iUpdate.saveAndReturnObject(iann);

    }

    @Test
    public void testNowOnToSavingAndDeleting() throws Exception {
        imgData = simpleImageData();
        img = (Image) imgData.asIObject();

        assertNull("Image doesn't have an id.", img.getId());
        img = iPojos.createDataObject(img, null);
        assertNotNull("Presto change-o, now it does.", img.getId());
        iPojos.deleteDataObject(img, null);

        img = iQuery.find(Image.class, img.getId().longValue());
        assertNull("we should have deleted it ", img);

    }

    @Test
    public void testLetsTryToLinkTwoThingsTogether() throws Exception {
        imgData = simpleImageData();
        dsData = simpleDatasetData();

        img = (Image) imgData.asIObject();
        ds = (Dataset) dsData.asIObject();

        DatasetImageLink link = new DatasetImageLink();
        link.link(ds, img);

        ILink test = iPojos.link(new ILink[] { link }, null)[0];
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
                new Parameters().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 2:
        saveImage();
        updated = unlinkImage();
        iPojos.updateDataObjects((IObject[]) updated
                .toArray(new IObject[updated.size()]), null);

        List list2 = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new Parameters().addLong("child.id", img.getId()));
        assertTrue(list.size() == 0);

        // Method 3:
        saveImage();
        Dataset target = img.linkedDatasetIterator().next();
        // For querying
        DatasetImageLink dslink = img.findDatasetImageLink(target).iterator()
                .next();

        img.unlinkDataset(target);
        img = iPojos.updateDataObject(img, null);

        ILink test = iQuery.find(DatasetImageLink.class, dslink.getId()
                .longValue());
        assertNull(test);

        // Method 4;
        Dataset d = new Dataset();
        d.setName("unlinking");
        Project p = new Project();
        p.setName("unlinking");
        p = iPojos.createDataObject(p, null);
        d = iPojos.createDataObject(d, null);

        ProjectDatasetLink link = new ProjectDatasetLink();
        link.setParent(p);
        link.setChild(d);
    }

    @Test(groups = { "broken", "ticket:541" })
    public void testHeresHowWeUnlinkFromJustOneSide() throws Exception {
        saveImage();
        DatasetImageLink link = img.iterateDatasetLinks().next();
        img.removeDatasetImageLink(link, false);

        iPojos.updateDataObject(img, null);

        DatasetImageLink test = iQuery.find(DatasetImageLink.class, link
                .getId().longValue());

        assertNull(test);

    }

    private void saveImage() {
        imgData = simpleImageDataWithDatasets();
        img = (Image) imgData.asIObject();

        img = iUpdate.saveAndReturnObject(img);
        assertTrue("It better have a dataset link",
                img.sizeOfDatasetLinks() > 0);
    }

    private List unlinkImage() {
        List updated = img.eachLinkedDataset(new CBlock() {
            public Object call(IObject arg0) {
                img.unlinkDataset((Dataset) arg0);
                return arg0;
            }
        });
        updated.add(img);
        return updated;
    }

    //
    // READ API
    // 

    @Test
    public void test_loadContainerHierarchy() throws Exception {

        ids = new HashSet(Arrays.asList(fixture.pu9990.getId(), fixture.pu9991
                .getId()));
        results = iPojos.loadContainerHierarchy(Project.class, ids, null);

        PojoOptions po = new PojoOptions().exp(new Long(0L));
        results = iPojos.loadContainerHierarchy(Project.class, null, po.map());

    }

    @Test(groups = "EJBExceptions")
    public void test_findContainerHierarchies() {

        PojoOptions defaults = new PojoOptions(), empty = new PojoOptions(null);

        ids = new HashSet(data.getMax("Image.ids", 2));
        results = iPojos.findContainerHierarchies(Project.class, ids, defaults
                .map());

        try {
            results = iPojos.findContainerHierarchies(Dataset.class, ids, empty
                    .map());
            fail("Should fail");
        } catch (ApiUsageException e) {
            // ok.
        }

        ids = new HashSet(data.getMax("Image.ids", 2));
        results = iPojos.findContainerHierarchies(CategoryGroup.class, ids,
                defaults.map());

    }

    @Test
    public void test_findAnnotations() {

        Map<Long, Set<IObject>> m;

        ids = new HashSet(data.getMax("Image.Annotated.ids", 2));
        m = iPojos.findAnnotations(Image.class, ids, null, null);
        assertAnnotations(m);

        ids = new HashSet(data.getMax("Dataset.Annotated.ids", 2));
        m = iPojos.findAnnotations(Dataset.class, ids, null, null);
        assertTrue(m.size() > 0);
        assertAnnotations(m);

        Map<Long, Set<AnnotationData>> m2 = DataObject.asPojos(m);
        AnnotationData data = m2.values().iterator().next().iterator().next();
        assertNotNull(data.getOwner());

    }

    void assertAnnotations(Map<Long, Set<IObject>> m) {
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
        Image i = iQuery.get(Image.class, fixture.iu5551.getId());
        i.unload();
        Set annotations = (Set) iPojos.retrieveCollection(i,
                Image.ANNOTATIONLINKS, null);
        assertTrue(annotations.size() > 0);
    }

    @Test
    public void test_findCGCPaths() throws Exception {
        ids = new HashSet(data.getMax("Image.ids", 2));
        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_ME, null);
        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_NME, null);
        results = iPojos.findCGCPaths(ids, IPojos.DECLASSIFICATION, null);
    }

    @Test(groups = "broken")
    public void test_findCGCPaths_declass() throws Exception {
        Paths paths = new Paths(data.get("CGCPaths.all"));
        Set de = iPojos.findCGCPaths(paths.uniqueImages(),
                IPojos.DECLASSIFICATION, null);
        assertTrue(de.size() == paths.unique(Paths.CG, Paths.EXISTS,
                Paths.EXISTS, Paths.EXISTS).size());

        for (Iterator it = de.iterator(); it.hasNext();) {
            CategoryGroup cg = (CategoryGroup) it.next();
            Iterator it2 = cg.linkedCategoryIterator();
            while (it2.hasNext()) {
                Category c = (Category) it2.next();
                Iterator it3 = c.linkedImageIterator();
                while (it3.hasNext()) {
                    Image i = (Image) it3.next();
                    Set found = paths.find(cg.getId(), c.getId(), i.getId());
                    assertTrue(found.size() == 1);

                }
            }
        }

        Long single_i = paths.singlePath()[Paths.I.intValue()];
        Set one_de = iPojos.findCGCPaths(Collections.singleton(single_i),
                IPojos.DECLASSIFICATION, null);
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

        Set single = Collections.singleton(targetPath[Paths.I.intValue()]);
        Set me = iPojos.findCGCPaths(single, IPojos.CLASSIFICATION_ME, null);
        Set nme = iPojos.findCGCPaths(single, IPojos.CLASSIFICATION_NME, null);

        for (Iterator it = nme.iterator(); it.hasNext();) {
            CategoryGroup group = (CategoryGroup) it.next();
            if (group.getId().equals(targetPath[Paths.CG.intValue()])) {
                for (Iterator it2 = group.linkedCategoryIterator(); it
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
    public void testCountingApiExceptions() {

        Set ids = Collections.singleton(new Long(1));

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
        Long id = fixture.iu5551.getId();
        Map m = iPojos.getCollectionCount(Image.class.getName(),
                Image.ANNOTATIONLINKS, Collections.singleton(id), null);
        Integer count = (Integer) m.get(id);
        assertTrue(count.intValue() > 0);

        id = fixture.du7771.getId();
        m = iPojos.getCollectionCount(Dataset.class.getName(),
                Dataset.IMAGELINKS, Collections.singleton(id), null);
        count = (Integer) m.get(id);
        assertTrue(count.intValue() > 0);

    }

    @Test
    public void test_getImages() throws Exception {
        ids = new HashSet(data.getMax("Project.ids", 2));
        Set images = iPojos.getImages(Project.class, ids, null);
    }

    @Test
    public void test_getUserDetails() throws Exception {
        Map m = iPojos.getUserDetails(Collections.singleton(fixture.TESTER),
                null);
        Experimenter e = (Experimenter) m.get(fixture.TESTER);
        ExperimenterGroup g;
        assertNotNull(g = e.getPrimaryGroupExperimenterMap().parent());
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

        Pixels pix = iQuery.findAll(Pixels.class, null).get(0);
        IPixels pixDB = factory.getPixelsService();
        RenderingEngine re = factory.createRenderingEngine();
        re.lookupPixels(pix.getId());
        re.load();

        PlaneDef pd = new PlaneDef(0, 0);
        pd.setZ(0);
        re.render(pd);

    }

    // /
    // ========================================================================
    // / ~ Versions
    // /
    // ========================================================================

    @Test(groups = { "versions", "broken", "ticket:118" })
    public void test_version_doesnt_increase_on_non_change() throws Exception {
        Image img = new Image();
        img.setName(" no vers. increment ");
        img = iUpdate.saveAndReturnObject(img);

        Image test = iUpdate.saveAndReturnObject(img);

        fail("must move details correction to the merge event listener "
                + "or version will always be incremented. ");

        assertTrue(img.getVersion().equals(test.getVersion()));

    }

    @Test(groups = { "versions", "broken", "ticket:118" })
    public void test_version_doesnt_increase_on_linked_update()
            throws Exception {
        TextAnnotation ann = new TextAnnotation();
        Image img = new Image();

        img.setName("version_test");
        ann.setTextValue("version_test");
        img.linkAnnotation(ann);

        img = iUpdate.saveAndReturnObject(img);
        ann = (TextAnnotation) img.linkedAnnotationList().get(0);

        assertNotNull(img.getId());
        assertNotNull(ann.getId());

        int orig_img_version = img.getVersion().intValue();
        // No longer exists int orig_ann_version = ann.getVersion().intValue();

        ann.setTextValue("updated version_test");

        ann = iUpdate.saveAndReturnObject(ann);
        img = iQuery.get(Image.class, img.getId()); // ann.getImage();

        // No longer existsint new_ann_version = ann.getVersion().intValue();
        int new_img_version = img.getVersion().intValue();

        assertFalse(ann.getTextValue().contains("updated"));
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
                .getCurrentUserId();

        // Counts are now always loaded
        counts = loadForCounts(fixture.du7770.getId(), null);
        // 7770 has not links
        assertNull(counts.getAnnotationLinksCountPerOwner().get(self));

        counts = loadForCounts(fixture.du7771.getId(), null);
        // Here we get the first map, since fixture creates entities
        // as other users too
        assertNotNull(counts.getImageLinksCountPerOwner());
        assertTrue(counts.getImageLinksCountPerOwner().values().iterator()
                .next() > 0);
        assertNotNull(counts.getAnnotationLinksCountPerOwner());
        assertTrue(counts.getAnnotationLinksCountPerOwner().values().iterator()
                .next().equals(1L));

    }

    private Dataset loadForCounts(Long id, Map options) {
        Dataset obj = iPojos.loadContainerHierarchy(Dataset.class,
                Collections.singleton(id), options).iterator().next();
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
        Project p = new Project();
        p.setName(name);
        p = iUpdate.saveAndReturnObject(p);

        // Check only one
        List list = iQuery.findAllByString(Project.class, "name", name, true,
                null);
        assertTrue(list.size() == 1);
        assertTrue(((Project) list.get(0)).getId().equals(p.getId()));

        // Update it.
        ProjectData pd = new ProjectData(p);
        pd.setDescription("....testnodups....");
        Project send = (Project) pd.asIObject();
        assertEquals(p.getId().intValue(), pd.getId());
        assertEquals(send.getId().intValue(), pd.getId());

        Project result = iPojos.updateDataObject(send, null);
        ProjectData test = new ProjectData(result);
        assertEquals(test.getId(), p.getId().intValue());

        // Check again.
        List list2 = iQuery.findAllByString(Project.class, "name", name, true,
                null);
        assertTrue(list2.size() == 1);
        assertTrue(((Project) list.get(0)).getId().equals(
                ((Project) list2.get(0)).getId()));

    }

    @Test
    public void test_no_duplicate_links() throws Exception {
        Image img = new Image();
        img.setName("duplinks");

        Dataset ds = new Dataset();
        ds.setName("duplinks");

        img.linkDataset(ds);

        img = iUpdate.saveAndReturnObject(img);
        ds = img.linkedDatasetIterator().next();

        List imgLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new Parameters().addLong("child.id", img.getId()));

        List dsLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new Parameters().addLong("parent.id", ds.getId()));

        assertTrue(imgLinks.size() == 1);
        assertTrue(dsLinks.size() == 1);

        assertTrue(((DatasetImageLink) imgLinks.get(0)).getId().equals(
                ((DatasetImageLink) dsLinks.get(0)).getId()));

    }

    @Test
    public void test_no_duplicates_on_save_array() throws Exception {
        Image img = new Image();
        img.setName("duplinks");

        Dataset ds = new Dataset();
        ds.setName("duplinks");

        img.linkDataset(ds);

        IObject[] retVal = iUpdate
                .saveAndReturnArray(new IObject[] { img, ds });
        img = (Image) retVal[0];
        ds = (Dataset) retVal[1];

        List imgLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new Parameters().addLong("child.id", img.getId()));

        List dsLinks = iQuery.findAllByQuery(DatasetImageLink.class.getName(),
                new Parameters().addLong("parent.id", ds.getId()));

        assertTrue(imgLinks.size() == 1);
        assertTrue(dsLinks.size() == 1);

        assertTrue(((DatasetImageLink) imgLinks.get(0)).getId().equals(
                ((DatasetImageLink) dsLinks.get(0)).getId()));

    }

    @Test
    public void test_annotating_a_dataset_cglib_issue() throws Exception {

        // Setup: original is our in-memory, used every where object.
        Dataset original = new Dataset();
        original.setName(" two rows ");
        original = iPojos.createDataObject(original, null);
        DatasetData annotatedObject = new DatasetData(original);
        Dataset annotated = (Dataset) iPojos.updateDataObject(annotatedObject
                .asIObject(), null);
        // Dataset m = new Dataset( original.getId(), false);
        TextAnnotation annotation = new TextAnnotation();
        annotation.setNs("");
        annotation.setTextValue(" two rows content ");

        // CGLIB
        TextAnnotation object = iPojos.createDataObject(annotation, null);
        DataObject returnedToUser = new TextualAnnotationData(object);

        // Now working but iPojos is still returning a CGLIB class.
        assertTrue(String.format("Class %s should equal class %s", object
                .getClass(), annotation.getClass()), object.getClass().equals(
                annotation.getClass()));
    }

    @Test
    public void test_annotating_a_dataset() throws Exception {
        long self = factory.getAdminService().getEventContext()
                .getCurrentUserId();

        String name = " two rows " + System.currentTimeMillis();
        String text = " two rows content " + System.currentTimeMillis();
        String desc = " new description " + System.currentTimeMillis();

        // Setup: original is our in-memory, used every where object.
        Dataset original = new Dataset();
        original.setName(name);
        original = iPojos.createDataObject(original, null);

        // No longer return these from create methods.
        assertNull(original.getAnnotationLinksCountPerOwner());
        // assertNull(original.getAnnotationLinksCountPerOwner().get(self));

        original.setDescription(desc);

        TextAnnotation annotation = new TextAnnotation();
        annotation.setNs("");
        annotation.setTextValue(text);
        original.linkAnnotation(annotation);

        original = iPojos.createDataObject(original, null);
        annotation = (TextAnnotation) original.linkedAnnotationIterator()
                .next();

        assertUniqueAnnotationCreation(name, text);

        Dataset test = iQuery.get(Dataset.class, original.getId().longValue());

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
        Project p = new Project();
        p.setName(name);

        p = iPojos.createDataObject(p, null);

        Dataset d1 = new Dataset();
        d1.setName(name);
        d1 = iPojos.createDataObject(d1, null);

        Dataset d2 = new Dataset();
        d2.setName(name);
        d2 = iPojos.createDataObject(d2, null);

        ProjectDatasetLink l1 = new ProjectDatasetLink();
        ProjectDatasetLink l2 = new ProjectDatasetLink();

        l1.setParent(p);
        l1.setChild(d1);

        l2.setParent(p);
        l2.setChild(d2);

        p.addProjectDatasetLink(l1, true);
        p.addProjectDatasetLink(l2, true);

        p = iPojos.updateDataObject(p, null);

        Iterator it = p.iterateDatasetLinks();
        while (it.hasNext()) {
            ProjectDatasetLink link = (ProjectDatasetLink) it.next();
            if (link.child().getId().equals(d1.getId())) {
                l1 = link;
                d1 = link.child();
            } else if (link.child().getId().equals(d2.getId())) {
                l2 = link;
                d2 = link.child();
            } else {
                fail(" Links aren't set up propertly");
            }

        }

        d1.setDescription(name);

        Dataset test = iPojos.updateDataObject(d1, null);

        ProjectDatasetLink link1 = iQuery.get(ProjectDatasetLink.class, l1
                .getId().longValue());

        assertNotNull(link1);
        assertTrue(link1.parent().getId().equals(p.getId()));
        assertTrue(link1.child().getId().equals(d1.getId()));

        ProjectDatasetLink link2 = iQuery.get(ProjectDatasetLink.class, l2
                .getId().longValue());

        assertNotNull(link2);
        assertTrue(link2.parent().getId().equals(p.getId()));
        assertTrue(link2.child().getId().equals(d2.getId()));

    }

    @Test
    public void test_delete_annotation() throws Exception {
        String string = "delete_annotation" + System.currentTimeMillis();

        Dataset d = new Dataset();
        d.setName(string);

        TextAnnotation a = new TextAnnotation();
        a.setNs("");
        a.setTextValue(string);
        d.linkAnnotation(a);

        d = iPojos.createDataObject(d, null);
        DatasetAnnotationLink al = d.unmodifiableAnnotationLinks().iterator()
                .next();
        a = (TextAnnotation) al.child();

        iPojos.deleteDataObject(al, null);
        iPojos.deleteDataObject(a, null);

        Object o = iQuery.find(TextAnnotation.class, a.getId().longValue());
        assertNull(o);

    }

    @Test
    public void test_duplicate_links_again() throws Exception {

        String string = "duplinksagain" + System.currentTimeMillis();

        Dataset d = new Dataset();
        d.setName(string);

        Project p = new Project();
        p.setName(string);

        d.linkProject(p);
        d = iPojos.createDataObject(d, null);
        Set orig_ids = new HashSet(d.collectProjectLinks(new IdBlock()));

        DatasetData dd = new DatasetData(d);
        Dataset toSend = dd.asDataset();

        Dataset updated = iPojos.updateDataObject(toSend, null);

        Set updt_ids = new HashSet(updated.collectProjectLinks(new IdBlock()));

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

        Dataset d = new Dataset();
        d.setName(" update_annotation");
        d = iPojos.createDataObject(d, null);
        annotatedObject = new DatasetData(d);

        data = new TextualAnnotationData(" update_annotation ");
       
        IObject updated = iPojos.updateDataObject(annotatedObject.asIObject(),
                null);

        ILink link = ((Dataset) updated).linkAnnotation(data.asAnnotation());
        link = iPojos.updateDataObject(link, null);
        link.getChild().unload();

        DataObject toReturn = 
        	new TextualAnnotationData((TextAnnotation) link.getChild());

    }

    @Test(groups = { "version", "broken" })
    public void test_unloaded_ds_in_ui() throws Exception {
        Project p = new Project();
        p.setName("ui");
        Dataset d = new Dataset();
        d.setName("ui");
        Image i = new Image();
        i.setName("ui");
        p.linkDataset(d);
        d.linkImage(i);

        p = iPojos.createDataObject(p, null);

        ProjectData pd_test = new ProjectData(iPojos.loadContainerHierarchy(
                Project.class, Collections.singleton(p.getId()), null)
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

    PojoOptions GROUP_FILTER;

    PojoOptions OWNER_FILTER;

    private void assertFilterWorked(Set<? extends IObject> results,
            Integer min, Integer max, Experimenter e, ExperimenterGroup g) {
        if (min != null) {
            assertTrue(results.size() > min);
        }
        if (max != null) {
            assertTrue(results.size() < max);
        }
        if (e != null) {
            for (IObject iobj : results) {
                assertTrue(e.getId().equals(
                        iobj.getDetails().getOwner().getId()));
            }
        }
        if (g != null) {
            for (IObject iobj : results) {
                assertTrue(g.getId().equals(
                        iobj.getDetails().getGroup().getId()));
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
        Set<Image> imgs = iPojos.getUserImages(OWNER_FILTER.map());
        assertFilterWorked(imgs, 0, null, fixture.e, null);

        // now for groups
        imgs = iPojos.getUserImages(GROUP_FILTER.map());
        assertFilterWorked(imgs, 0, null, null, fixture.g);

    }

    @Test(groups = "ticket:318")
    public void testFilters_getImages() throws Exception {

        // there are about 6 projects in our fixture
        ids = new HashSet(data.getMax("Project.ids", 100));

        Set<Image> images = iPojos.getImages(Project.class, ids, OWNER_FILTER
                .map());
        assertFilterWorked(images, null, 100, fixture.e, null);

        images = iPojos.getImages(Project.class, ids, GROUP_FILTER.map());
        assertFilterWorked(images, null, 100, null, fixture.g);

    }

    @Test(groups = "ticket:318")
    public void testFilters_findCGCPaths() throws Exception {
        ids = new HashSet(data.getMax("Image.ids", 100));

        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_ME,
                OWNER_FILTER.map());
        assertFilterWorked(results, null, 100, fixture.e, null);

        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_ME,
                GROUP_FILTER.map());
        assertFilterWorked(results, null, 100, null, fixture.g);

        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_NME,
                OWNER_FILTER.map());
        assertFilterWorked(results, null, 100, fixture.e, null);

        results = iPojos.findCGCPaths(ids, IPojos.CLASSIFICATION_NME,
                GROUP_FILTER.map());
        assertFilterWorked(results, null, 100, null, fixture.g);

        results = iPojos.findCGCPaths(ids, IPojos.DECLASSIFICATION,
                OWNER_FILTER.map());
        assertFilterWorked(results, null, 100, fixture.e, null);

        results = iPojos.findCGCPaths(ids, IPojos.DECLASSIFICATION,
                GROUP_FILTER.map());
        assertFilterWorked(results, null, 100, null, fixture.g);

    }

    @Test(groups = "ticket:318")
    public void testFilters_findContainerHierarchies() throws Exception {
        ids = new HashSet(data.getMax("Image.ids", 100));

        try {
            results = iPojos.findContainerHierarchies(Project.class, ids,
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
            results = iPojos.findContainerHierarchies(Project.class, ids,
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

        ids = new HashSet(data.getMax("Project.ids", 2));

        results = iPojos.loadContainerHierarchy(Project.class, ids,
                OWNER_FILTER.map());
        assertFilterWorked(results, null, 100, fixture.e, null);

        results = iPojos.loadContainerHierarchy(Project.class, ids,
                GROUP_FILTER.map());
        assertFilterWorked(results, null, 100, null, fixture.g);
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

    private void assertUniqueAnnotationCreation(String name, String text) {
        // Test
        List ds = iQuery.findAllByString(Dataset.class, "name", name, true,
                null);
        List as = iQuery.findAllByString(TextAnnotation.class, "textValue",
                text, true, null);

        assertTrue(ds.size() == 1);
        assertTrue(as.size() == 1);
    }

}
