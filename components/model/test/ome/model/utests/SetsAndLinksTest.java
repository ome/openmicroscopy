/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model.utests;

import java.util.List;

import junit.framework.TestCase;
import ome.conditions.ApiUsageException;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.Plane;
import ome.model.display.Thumbnail;
import ome.model.jobs.ImportJob;
import ome.model.jobs.JobOriginalFileLink;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SetsAndLinksTest extends TestCase {

    Project p;

    Dataset d;

    Image i;

    Pixels pix;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        p = new Project();
        d = new Dataset();
        i = new Image();
        pix = new Pixels();
    }

    @Test
    public void test_linking() throws Exception {
        p.linkDataset(d);

        assertTrue(p.linkedDatasetList().size() == 1);
        assertTrue(p.linkedDatasetIterator().next().equals(d));

    }

    @Test
    public void test_unlinking() throws Exception {
        p.linkDataset(d);
        p.unlinkDataset(d);
        assertTrue(p.linkedDatasetList().size() == 0);

        p.linkDataset(d);
        p.clearDatasetLinks();
        assertTrue(p.linkedDatasetList().size() == 0);

    }

    @Test
    public void test_retrieving() throws Exception {
        p.linkDataset(d);
        List l = p.eachLinkedDataset(null);
        assertTrue(l.size() == 1);
        assertTrue(l.get(0).equals(d));
    }

    @Test
    public void test_adding_a_placeholder() throws Exception {
        Project p = new Project();
        Dataset d = new Dataset(new Long(1L), false);

        p.linkDataset(d);
    }

    @Test(groups = "ticket:60")
    public void test_cantLinkNullSet() throws Exception {
        p.putAt(Project.DATASETLINKS, null); // This is a workaround.
        try {
            p.linkDataset(d);
            fail("Should not be allowed.");
        } catch (ApiUsageException api) {
            // ok.
        }

    }

    @Test(groups = "ticket:60")
    public void test_butWeStillWantToUseUnloadeds() throws Exception {
        d.unload();
        p.linkDataset(d);
    }

    @Test(groups = "ticket:60")
    public void test_andTheReverseToo() throws Exception {
        d.putAt(Dataset.PROJECTLINKS, null); // This is a workaround.
        try {
            p.linkDataset(d);
            fail("Should not be allowed.");
        } catch (ApiUsageException api) {
            // ok.
        }
    }

    // Default Experimenter Group
    @Test
    public void test_one_way_to_default_link() throws Exception {
        Experimenter experimenter = new Experimenter();
        ExperimenterGroup defaultGroup = new ExperimenterGroup();
        ExperimenterGroup defaultGroup2 = new ExperimenterGroup();

        experimenter.linkExperimenterGroup(defaultGroup);
        testIsDefault(experimenter, defaultGroup);

        GroupExperimenterMap map = experimenter
                .linkExperimenterGroup(defaultGroup2);
        experimenter.setPrimaryGroupExperimenterMap(map);
        testIsDefault(experimenter, defaultGroup2);
    }

    @Test(groups = { "broken", "ticket:346" })
    public void testAddingFillsContainer() throws Exception {
        Pixels p = new Pixels();
        Thumbnail tb = new Thumbnail();
        tb.setPixels(p);
        assertTrue(p.iterateThumbnails().hasNext());
    }

    @Test(groups = { "broken", "ticket:346" })
    public void testLinkingFillsContainer() throws Exception {
        Project p = new Project();
        Dataset d = new Dataset();
        ProjectDatasetLink link = new ProjectDatasetLink();
        link.link(p, d);
        assertNotNull(link.parent());
        assertNotNull(link.child());
        assertTrue(link.parent().sizeOfDatasetLinks() == 1);
        assertTrue(link.child().sizeOfProjectLinks() == 1);
    }

    @Test(groups = "jobs")
    public void testUnidirectionalLinks() throws Exception {
        ImportJob job = new ImportJob();
        OriginalFile file = new OriginalFile();
        job.linkOriginalFile(file);
        assertTrue(job.sizeOfOriginalFileLinks() == 1);
        job.unlinkOriginalFile(file);
        assertTrue(job.sizeOfOriginalFileLinks() == 0);
        JobOriginalFileLink link = new JobOriginalFileLink();
        link.link(job, file);
        job.addJobOriginalFileLink(link, true);
        assertTrue(job.sizeOfOriginalFileLinks() == 1);
        job.clearOriginalFileLinks();
        assertTrue(job.sizeOfOriginalFileLinks() == 0);
    }

    @Test
    public void testOrderedRelationshipsCanHaveUnloadedAdd() {
        Pixels p = new Pixels();
        Channel c = new Channel(1L, false);
        p.addChannel(c);

        Plane pi = new Plane(1L, false);
        try {
            p.addPlane(pi);
            fail("This should not be accepted.");
        } catch (IllegalStateException ise) {
            // good.
        }
    }
    
    // This is now allowed? (ticket:2067)
    // Wed Aug  4 09:04:28 BST 2010 <callan@blackcat.ca>
    @Test(groups={"broken"}, expectedExceptions = NullPointerException.class)
    public void testNullArentAddableToOrderedCollections() {
        Pixels p = new Pixels();
        p.addChannel(null);
        assertEquals(1, p.sizeOfChannels());
    }

    // ~ Private helpers
    // ===========================================================================
    private void testIsDefault(Experimenter user, ExperimenterGroup group) {
        ExperimenterGroup t = user.getPrimaryGroupExperimenterMap().parent();
        assertEquals(group, t);
    }

}
