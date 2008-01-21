/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.adapters.pojos.utests;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.ProjectData;

public class PojosTest extends TestCase {

    private static Log log = LogFactory.getLog(PojosTest.class);

    IObject[] all;

    Project p;

    Dataset d1, d2, d3;

    Image i1, i2, i3;

    CategoryGroup cg;

    Category c;

    Annotation iann;

    Annotation dann;

    Experimenter e;

    ExperimenterGroup g;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        p = new Project(new Long(1), true);
        d1 = new Dataset(new Long(2), true);
        d2 = new Dataset(new Long(3), true);
        d3 = new Dataset(new Long(4), true);
        i1 = new Image(new Long(5), true);
        i2 = new Image(new Long(6), true);
        i3 = new Image(new Long(7), true);
        cg = new CategoryGroup(new Long(8), true);
        c = new Category(new Long(9), true);
        iann = new TextAnnotation(new Long(10), true);
        dann = new TextAnnotation(new Long(11), true);
        e = new Experimenter(new Long(12), true);
        g = new ExperimenterGroup(new Long(13), true);

        all = new IObject[] { p, d1, d2, d3, i1, i2, i3, cg, c, iann, dann, e,
                g };

        p.linkDataset(d1);
        p.linkDataset(d2);
        p.linkDataset(d3);
        d1.linkImage(i1);
        d1.linkImage(i2);
        d1.linkImage(i3);
        d2.linkImage(i2);
        d3.linkImage(i3);
        i2.linkCategory(c);
        c.linkCategoryGroup(cg);
        i3.linkAnnotation(iann);
        d3.linkAnnotation(dann);

        e.linkExperimenterGroup(g);

        setOwnerAndGroup(e, g, all);

    }

    void setOwnerAndGroup(Experimenter e, ExperimenterGroup g, IObject[] objs) {
        for (int i = 0; i < objs.length; i++) {
            objs[i].getDetails().setOwner(e);
            objs[i].getDetails().setGroup(g);
        }
    }

    @Test
    public void test() {
        ProjectData pd = new ProjectData(p);
        assertNotNull(pd.getDatasets());
        assertFalse(pd.getDatasets().size() == 0);
        // Can no longer check assertFalse(==) between classes.
        // Compiler complains.
        assertTrue(pd.getDatasets().iterator().next().getClass() == DatasetData.class);
        if (log.isDebugEnabled()) {
            log.debug(pd);
        }
    }

    @Test
    public void test_modying_got_set_does_nothing() throws Exception {
        ProjectData pd = new ProjectData(p);
        Set got = pd.getDatasets();
        got.add("X");

        assertTrue(got.size() > pd.getDatasets().size());
        assertTrue(got != pd.getDatasets());

    }

    @Test
    public void testReverseMapping() throws Exception {
        ProjectData pd = new ProjectData();
        DatasetData dd = new DatasetData();

        Set dds = new HashSet();
        dds.add(dd);
        pd.setDatasets(dds);

        Set pds = new HashSet();
        pds.add(pd);
        dd.setProjects(pds);

        Project prj = (Project) pd.asIObject();
        assertTrue(prj.sizeOfDatasetLinks() > 0);
        assertTrue((prj.linkedDatasetList().get(0)).sizeOfProjectLinks() > 0);
    }

    @Test
    public void testNoDuplicateLinks() throws Exception {
        Project p_2 = new Project();
        Dataset d_2 = new Dataset();
        Image i_2 = new Image();
        p_2.linkDataset(d_2);
        d_2.linkImage(i_2);

        ProjectData pd = new ProjectData(p_2);
        DatasetData dd = pd.getDatasets().iterator().next();
        Dataset test = (Dataset) dd.asIObject();

        Set p_links = new HashSet(p_2.collectDatasetLinks(null));
        Set d_links = new HashSet(test.collectProjectLinks(null));

        if (log.isDebugEnabled()) {
            log.debug(p_links);
            log.debug(d_links);
        }

        assertTrue(p_links.containsAll(d_links));

        DatasetData dd2 = new DatasetData(test);
        ImageData id = (ImageData) dd2.getImages().iterator().next();
        Image test2 = (Image) id.asIObject();

        Set d2_links = new HashSet(d_2.collectImageLinks(null));
        Set i2_links = new HashSet(test2.collectDatasetLinks(null));

        if (log.isDebugEnabled()) {
            log.debug(d2_links);
            log.debug(i2_links);
        }

        assertTrue(d2_links.containsAll(i2_links));
    }

    @Test
    public void test_p_and_d() throws Exception {
        Project p = new Project();
        Dataset d = new Dataset();

        p.linkDataset(d);

        ProjectData pd = new ProjectData(p);

        Set datasets = pd.getDatasets();

        DatasetData dd = new DatasetData();
        datasets.add(dd);

        pd.setDatasets(datasets);

        Project test = (Project) pd.asIObject();

        assertTrue(test.sizeOfDatasetLinks() > 1);
    }

    @Test
    public void test_walk_a_graph() throws Exception {
        ProjectData pd = new ProjectData(p);
        Iterator it = pd.getDatasets().iterator();
        DatasetData dd = null;
        while (it.hasNext()) {
            dd = (DatasetData) it.next();
            if (dd.asIObject() == d3) {
                break;
            }
        }
        assert dd != null;
        assertTrue(dd.getAnnotations().size() == 1);
        assertTrue(dd.getImages().size() == 1);

        it = dd.getImages().iterator();
        ImageData id = null;
        while (it.hasNext()) {
            id = (ImageData) it.next();
            assertTrue(id.asIObject() == i3);
        }

        dd = null;
        it = pd.getDatasets().iterator();
        while (it.hasNext()) {
            dd = (DatasetData) it.next();
            if (dd.asIObject() == d2) {
                break;
            }
        }

        assert dd != null;
        id = (ImageData) dd.getImages().iterator().next();
        assertTrue(id.getCategories().size() == 1);

        CategoryData cd = (CategoryData) id.getCategories().iterator().next();
        CategoryGroupData cgd = cd.getGroup();
        CategoryGroup haha = (CategoryGroup) cgd.asIObject();

    }

    @Test(groups = { "broken", "clientsession" })
    public void test_bidirectional() throws Exception {
        CategoryData cd = new CategoryData(c);
        ImageData id = (ImageData) cd.getImages().iterator().next();

        Set ref_imgs = cd.getImages();
        Set ref_cats = id.getCategories();

        ImageData add = new ImageData();
        Set imgs = cd.getImages();
        imgs.add(add);
        cd.setImages(imgs);

        assertTrue(cd.getImages().size() > ref_imgs.size());
        assertTrue(id.getCategories().size() > ref_cats.size());

    }

    @Test(groups = { "ticket:295" })
    public void testPermissionsData() throws Exception {
        PermissionData pd = new PermissionData();
        assertTrue(pd.isGroupRead());
        assertTrue(pd.isGroupWrite());
        assertTrue(pd.isUserRead());
        assertTrue(pd.isUserWrite());
        assertTrue(pd.isWorldRead());
        assertTrue(pd.isWorldWrite());
        assertFalse(pd.isLocked());

        Permissions p = new Permissions(Permissions.GROUP_PRIVATE);
        p.set(Flag.LOCKED);
        pd = new PermissionData(p);
        assertTrue(pd.isGroupRead());
        assertTrue(pd.isGroupWrite());
        assertTrue(pd.isUserRead());
        assertTrue(pd.isUserWrite());
        assertFalse(pd.isWorldRead());
        assertFalse(pd.isWorldWrite());
        assertTrue(pd.isLocked());

        Image i = new Image();
        i.getDetails().setPermissions(new Permissions());
        ImageData id = new ImageData(i);
        pd = id.getPermissions();
        assertTrue(pd.isGroupRead());
        assertTrue(pd.isGroupWrite());
        assertTrue(pd.isUserRead());
        assertTrue(pd.isUserWrite());
        assertTrue(pd.isWorldRead());
        assertTrue(pd.isWorldWrite());

        p = id.asImage().getDetails().getPermissions();
        p.revoke(Role.GROUP, Right.WRITE);
        p.set(Flag.LOCKED);
        assertFalse(pd.isGroupWrite());
        assertTrue(pd.isLocked());

        pd.setLocked(false);
        pd.setUserRead(false);
        assertFalse(pd.isUserRead());
        assertFalse(p.isGranted(Role.USER, Right.READ));
        assertFalse(p.isSet(Flag.LOCKED));
    }

    @Test(groups = "ticket:308")
    public void testLongValuedCounts() throws Exception {
        // Counts are returned by the server and no longer
        // settable in the client. See integration tests.
    }

    @Test(groups = "ticket:316")
    public void testDefaultGroupIsAvailable() throws Exception {

        ExperimenterGroup g1 = new ExperimenterGroup();
        ExperimenterGroup g2 = new ExperimenterGroup();

        Experimenter exp = new Experimenter();

        exp.linkExperimenterGroup(g1);
        exp.linkExperimenterGroup(g2);

        ExperimenterData ed = new ExperimenterData(exp);
        assertNotNull(ed.getDefaultGroup());
        assertTrue(ed.getDefaultGroup().asGroup() == g1);
        assertTrue(ed.getGroups().size() == 2);
        Iterator<GroupData> it = ed.getGroups().iterator();
        assertTrue(g1 == it.next().asGroup());
        assertTrue(g2 == it.next().asGroup());
    }

    @Test
    public void testAnnotationHasOwnerDetails() {
        TagAnnotation tag = new TagAnnotation();
        Details d = tag.getDetails();
        d.setOwner(new Experimenter(1L, false));
        d.setGroup(new ExperimenterGroup(2L, false));
        AnnotationData aData = new AnnotationData(tag);
        assertNotNull(aData.getOwner());
        aData = (AnnotationData) DataObject.asPojo(tag);
        assertNotNull(aData.getOwner());
    }

}
