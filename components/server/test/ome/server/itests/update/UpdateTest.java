/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.update;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ome.api.ITypes;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.CodomainMapContext;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.model.enums.Format;
import ome.model.jobs.ImportJob;
import ome.model.jobs.JobStatus;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.parameters.Parameters;
import ome.testing.ObjectFactory;

import org.testng.annotations.Test;

public class UpdateTest extends AbstractUpdateTest {

    @Test
    public void testSaveSimpleObject() throws Exception {
        Pixels p = ObjectFactory.createPixelGraph(null);
        p = iUpdate.saveAndReturnObject(p);

        // FIXME This can no longer be done this way.
        // List logs = securitySystem.getCurrentEvent().collectLogs(null);
        // assertTrue(logs.size() > 0);

        Pixels check = (Pixels) iQuery.findByQuery("select p from Pixels p "
                + " left outer join fetch p.channels " + "  where p.id = :id",
                new Parameters().addId(p.getId()));

        assertTrue("channel ids differ", equalCollections(p.unmodifiableChannels(),
                check.unmodifiableChannels()));
        assertTrue("pixels dims differ", p.getPixelsDimensions().getId().equals(
                check.getPixelsDimensions().getId()));
    }

    @Test
    public void test_uh_oh_duplicate_rows_0() throws Exception {
        String name = "SIMPLE:" + System.currentTimeMillis();
        Project p = new Project();
        p.setName(name);
        p = iUpdate.saveAndReturnObject(p);

        Project compare = iQuery.findByString(Project.class, "name",
                name);

        assertTrue(p.getId().equals(compare.getId()));

        Project send = new Project();
        send.setName(p.getName());
        send.setId(p.getId());
        send.setVersion(p.getVersion()); // This is important.
        send.setDescription("...test...");
        Project test = iUpdate.saveAndReturnObject(send);

        assertTrue(p.getId().equals(test.getId()));

        iQuery.findByString(Project.class, "name", name);

    }

    @Test
    public void test_images_pixels() throws Exception {
        Image image = new Image();
        image.setName("test");

        Pixels active = ObjectFactory.createPixelGraph(null);
        image.addPixels(active);

        image = iUpdate.saveAndReturnObject(image);
        active = image.getPrimaryPixels();
        Pixels other = ObjectFactory.createPixelGraph(null);
        other.setImage(active.getImage());

        iUpdate.saveAndReturnObject(other);

    }

    @Test
    public void test_index_save() throws Exception {

        RenderingDef def = ObjectFactory.createRenderingDef();
        CodomainMapContext enhancement = ObjectFactory
                .createPlaneSlicingContext();
        ChannelBinding binding = ObjectFactory.createChannelBinding();

        // What we're interested in
        def.addChannelBinding(binding);
        def.addCodomainMapContext(enhancement);

        def = iUpdate.saveAndReturnObject(def);

    }

    @Test
    public void test_index_save_order() throws Exception {

        RenderingDef def = ObjectFactory.createRenderingDef();

        ChannelBinding binding1 = ObjectFactory.createChannelBinding();
        binding1.setInputStart(new Float(1.0));

        ChannelBinding binding2 = ObjectFactory.createChannelBinding();
        binding2.setInputStart(new Float(2.0));

        ChannelBinding binding3 = ObjectFactory.createChannelBinding();
        binding3.setInputStart(new Float(3.0));

        def.addChannelBinding(binding1);
        def.addChannelBinding(binding2);
        def.addChannelBinding(binding3);

        def = iUpdate.saveAndReturnObject(def);

    }

    @Test
    public void test_experimenters_groups() throws Exception {
        Experimenter e = new Experimenter();
        ExperimenterGroup g_1 = new ExperimenterGroup();
        ExperimenterGroup g_2 = new ExperimenterGroup();

        e.setOmeName("j.b." + System.currentTimeMillis());
        e.setFirstName(" Joe ");
        e.setLastName(" Brown ");

        g_1.setName("DEFAULT: " + System.currentTimeMillis());
        g_2.setName("NOTDEFAULT: " + System.currentTimeMillis());

        // The instances must be unloaded to prevent spurious deletes!
        // Need versions. See:
        // https://trac.openmicroscopy.org.uk/omero/ticket/118
        // https://trac.openmicroscopy.org.uk/omero/ticket/346
        e = iUpdate.saveAndReturnObject(e);
        g_1 = iUpdate.saveAndReturnObject(g_1);
        g_2 = iUpdate.saveAndReturnObject(g_2);
        e.unload();
        g_1.unload();
        g_2.unload();

        // Intended to be default
        GroupExperimenterMap defaultLink = new GroupExperimenterMap();
        defaultLink.link(g_1, e);
        defaultLink = iUpdate.saveAndReturnObject(defaultLink);

        // Intended to be second, not default
        GroupExperimenterMap notDefaultLink = new GroupExperimenterMap();
        notDefaultLink.link(g_2, e);
        notDefaultLink = iUpdate.saveAndReturnObject(notDefaultLink);

        Experimenter test = (Experimenter) iQuery.findByQuery(
                " select e from Experimenter e "
                        + " join fetch e.defaultGroupLink l "
                        + " join fetch l.parent p " + " where e.id = :id ",
                new Parameters().addId(defaultLink.child().getId()));
        assertNotNull(test.getPrimaryGroupExperimenterMap());
        assertTrue(test.getPrimaryGroupExperimenterMap().parent().getName().startsWith(
                "DEFAULT"));

    }

    @Test
    /** attempt to reproduce an error seen on the client side */
    public void test_save_array() throws Exception {

        loginRoot();

        Long e = -1L;
        List<Experimenter> es = iQuery.findAll(Experimenter.class, null);
        for (Experimenter experimenter : es) {
            Long l = experimenter.getId();
            if (!l.equals(new Long(0L))) {
                e = l;
            }
        }

        Project[] ps = new Project[] { new Project(), new Project(),
                new Project() };
        for (Project project : ps) {
            project.setName("save-array");
        }
        ps[0].getDetails().setOwner(new Experimenter(e, false));
        ps[1].getDetails().setOwner(new Experimenter(e, false));

        Dataset[] ds = new Dataset[] { new Dataset(), new Dataset() };
        for (Dataset dataset : ds) {
            dataset.setName("save-array");
        }

        for (Dataset dataset : ds) {
            for (Project project : ps) {
                project.linkDataset(dataset);
            }
        }

        iUpdate.saveAndReturnArray(ps);
    }

    // ~ Problems with values returned by update
    // =========================================================================

    String err = "obj is loaded, set is not null AND not filled!";

    @Test(groups = { "broken", "ticket:346" })
    public void testAddingReturnsNonEmptySets() throws Exception {
        // using the add method works
        Pixels p = ObjectFactory.createPixelGraph(null);
        Thumbnail tb = ObjectFactory.createThumbnails(p);
        assertPixels(tb);

        // passing it in as a proxy is ok.
        p = ObjectFactory.createPixelGraph(null);
        p = iUpdate.saveAndReturnObject(p);
        p = new Pixels(p.getId(), false);
        tb = ObjectFactory.createThumbnails(p);
        tb.setPixels(p);
        assertPixels(tb);

        // issues with using the setter with a non-proxy
        p = ObjectFactory.createPixelGraph(null);
        tb = new Thumbnail();
        tb.setMimeType("");
        tb.setSizeX(1);
        tb.setSizeY(1);
        tb.setPixels(p);
        assertPixels(tb);

    }

    protected void assertPixels(Thumbnail tb) {
        Thumbnail test = iUpdate.saveAndReturnObject(tb);
        Thumbnail copy = iQuery.get(test.getClass(), test.getId());
        assertFalse(err, copy.getPixels().isLoaded()
                && copy.getPixels().sizeOfThumbnails() == 0);
        assertFalse(err, test.getPixels().isLoaded()
                && test.getPixels().sizeOfThumbnails() == 0);
    }

    @Test(groups = { "broken", "ticket:346" })
    public void testLinkingReturnsNonEmptySets() throws Exception {

        // using the link methods does what it's supposed to
        Project p = new Project();
        p.setName("test");
        Dataset d = new Dataset();
        d.setName("test");
        p.linkDataset(d);
        ProjectDatasetLink link = (ProjectDatasetLink) p.collectDatasetLinks(
                null).get(0);
        assertLink(link);

        // and using proxies works
        p = iUpdate.saveAndReturnObject(p);
        p = new Project(p.getId(), false);
        d = iUpdate.saveAndReturnObject(d);
        d = new Dataset(d.getId(), false);
        link = new ProjectDatasetLink();
        link.link(p, d);
        assertLink(link);

        // but there are issues with passing in non-proxies when not using the
        // reverse methods.
        p = new Project();
        p.setName("test");
        d = new Dataset();
        d.setName("test");
        link = new ProjectDatasetLink();
        link.link(p, d);
        assertLink(link);
    }

    @Test(groups = {"jobs","ticket:667"} )
    public void testLinkingUnidirectionally() throws Exception {
        ITypes t = this.factory.getTypesService();
        ImportJob job = new ImportJob();
        OriginalFile file = new OriginalFile();
        file.setPath("");
        file.setSha1("");
        file.setName("");
        file.setSize(0L);
        file.setFormat(t.allEnumerations(Format.class).get(0));
        job.linkOriginalFile(file);
        job.setImageDescription("test");
        job.setImageName("image name");
	job.setUsername("root");
	job.setGroupname("system");
	job.setType("Test");
	job.setMessage("foo");
        job.setSubmitted(new Timestamp(System.currentTimeMillis()));
        job.setScheduledFor(new Timestamp(System.currentTimeMillis()));
        job.setStatus(t.getEnumeration(JobStatus.class, "Submitted"));
        iUpdate.saveObject(job);

    }
    
    protected void assertLink(ProjectDatasetLink link) {
        ProjectDatasetLink test = iUpdate.saveAndReturnObject(link);
        ProjectDatasetLink copy = iQuery.get(test.getClass(), test.getId());
        assertFalse(err, copy.parent().isLoaded()
                && copy.parent().sizeOfDatasetLinks() == 0);
        assertFalse(err, copy.child().isLoaded()
                && copy.child().sizeOfProjectLinks() == 0);
        assertFalse(err, test.parent().isLoaded()
                && test.parent().sizeOfDatasetLinks() == 0);
        assertFalse(err, test.child().isLoaded()
                && test.child().sizeOfProjectLinks() == 0);
    }

}
