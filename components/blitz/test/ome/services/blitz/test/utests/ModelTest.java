/*
 *   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.utests;

import static omero.rtypes.rdouble;
import static omero.rtypes.rstring;

import static ome.formats.model.UnitsFactory.makePower;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.units.UNITS;
import omero.model.ArcI;
import omero.model.ChannelI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.EventI;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.ImageI;
import omero.model.PixelsI;
import omero.model.ProjectI;
import omero.util.IceMapper;

import org.testng.annotations.Test;

public class ModelTest extends TestCase {

    @Test(groups = "ticket:636")
    public void testInheritanceInConcreteClasses() throws Exception {
        ArcI arcI = new ArcI();
        // arcI.unload();
        arcI.setPower(makePower(1.0f, UNITS.WATT));
    }

    @Test
    public void testMapper() throws Exception {

        Experimenter e = new Experimenter();
        e.setOmeName("hi");
        e.setLdap(false);
        e.linkExperimenterGroup(new ExperimenterGroup("foo", false));

        IceMapper mapper = new IceMapper();
        ExperimenterI ei = (ExperimenterI) mapper.map(e);
        assertEquals(new Integer(1), new Integer(ei
                .sizeOfGroupExperimenterMap()));

    }

    @Test
    public void testCopyObject() throws Exception {
        Experimenter e = new Experimenter();
        e.setOmeName("hi");
        e.setLdap(false);
        e.linkExperimenterGroup(new ExperimenterGroup("foo", false));
        ExperimenterI ei = new ExperimenterI();
        ei.copyObject(e, new IceMapper());
        // This may not hold without being called from the top level mapper
        // method
        // assertEquals(new Integer(1), new
        // Integer(ei.sizeOfGroupExperimenterMap()));

        Pixels p = new Pixels();
        Image i = new Image();
        p.setImage(i);
        p.getDetails().setOwner(e);
        new PixelsI().copyObject(p, new IceMapper());

    }

    @Test
    public void testFillObject() throws Exception {
        ExperimenterI ei = new ExperimenterI();
        ei.setOmeName(rstring("name"));
        ei.linkExperimenterGroup(new ExperimenterGroupI());
        Experimenter e = (Experimenter) ei.fillObject(new IceMapper());
        assertEquals(new Integer(1),
                new Integer(e.sizeOfGroupExperimenterMap()));

        PixelsI p = new PixelsI();
        ImageI i = new ImageI();
        p.setImage(i);
        p.getDetails().setOwner(ei);
        p.fillObject(new IceMapper());
    }

    @Test
    public void testCounts() throws Exception {
        Map<Long, Long> counts = new HashMap<Long, Long>();
        counts.put(1L, 1L);
        class CExperimenter extends Experimenter {
            CExperimenter(Map<Long, Long> counts) {
                setAnnotationLinksCountPerOwner(counts);
            }
        }

        Experimenter e = new CExperimenter(counts);
        ExperimenterI ei = new ExperimenterI();
        ei.copyObject(e, new IceMapper());
        Map<Long, Long> countsi = ei.getAnnotationLinksCountPerOwner();
        assertEquals(new Long(1L), countsi.get(1L));
    }

    @Test
    public void testLoadedness1() throws Exception {
        ExperimenterGroup g = new ExperimenterGroup();
        Experimenter e = new Experimenter();
        Project p = new Project();
        p.getDetails().setOwner(e);
        e.linkExperimenterGroup(g);
        assertEquals(1, e.sizeOfGroupExperimenterMap());

        IceMapper mapper = new IceMapper();
        ProjectI pi = (ProjectI) mapper.handleOutput(Project.class, p);
        ExperimenterI ei = (ExperimenterI) pi.getDetails().getOwner();
        assertEquals(1, e.sizeOfGroupExperimenterMap());

    }

    @Test
    public void testLoadedness2() throws Exception {
        ExperimenterGroup g = new ExperimenterGroup();
        Experimenter e = new Experimenter();
        e.linkExperimenterGroup(g);

        Project p = new Project();
        p.getDetails().setOwner(e);
        p.getDetails().setGroup(g);

        Dataset d = new Dataset();
        d.getDetails().setOwner(e);
        d.getDetails().setGroup(g);

        p.linkDataset(d); // Adding an extra object

        assertEquals(1, e.sizeOfGroupExperimenterMap());

        IceMapper mapper = new IceMapper();
        ProjectI pi = (ProjectI) mapper.handleOutput(Project.class, p);
        ExperimenterI ei = (ExperimenterI) pi.getDetails().getOwner();
        ExperimenterGroupI gi = (ExperimenterGroupI) pi.getDetails().getGroup();
        assertEquals(1, ei.sizeOfGroupExperimenterMap());
        assertEquals(1, gi.sizeOfGroupExperimenterMap());

    }

    @Test
    public void testLoadedness3() throws Exception {
        Experimenter e = new Experimenter();
        e.putAt(Experimenter.GROUPEXPERIMENTERMAP, null);

        Project p = new Project();
        p.getDetails().setOwner(e);

        assertEquals(-1, e.sizeOfGroupExperimenterMap());

        IceMapper mapper = new IceMapper();
        ProjectI pi = (ProjectI) mapper.handleOutput(Project.class, p);
        ExperimenterI ei = (ExperimenterI) pi.getDetails().getOwner();
        assertEquals(-1, ei.sizeOfGroupExperimenterMap());

    }

    @Test
    public void testRemoval() throws Exception {
        omero.model.Image i = new ImageI();
        assertEquals(0, i.sizeOfDatasetLinks());
        DatasetImageLink link = i.linkDataset(new DatasetI());
        assertEquals(1, i.sizeOfDatasetLinks());
        i.removeDatasetImageLink(link);
        assertEquals(0, i.sizeOfDatasetLinks());
        
        link = i.linkDataset(new DatasetI());
        assertEquals(1, i.sizeOfDatasetLinks());
        i.removeDatasetImageLinkFromBoth(link, true);
        assertEquals(0, i.sizeOfDatasetLinks());
        
        omero.model.Dataset d = new DatasetI();
        i.linkDataset(d);
        assertEquals(1, i.sizeOfDatasetLinks());
        i.unlinkDataset(d);
        assertEquals(0, i.sizeOfDatasetLinks());
    }
    
    @Test
    public void testReloading() throws Exception {
        
        // This is our "real" graph
        omero.model.Image i = new ImageI(1L, true);
        i.getDetails().setUpdateEvent(new EventI(1L, false));
        omero.model.Dataset d = new DatasetI(1L, true);
        d.getDetails().setUpdateEvent(new EventI(1L, false));
        i.linkDataset(d);
        assertEquals(1, i.sizeOfDatasetLinks());
        i.unloadDatasetLinks();
        assertEquals(-1, i.sizeOfDatasetLinks());
        
        // We shouldn't be able to reload from just any image
        omero.model.Image badId = new ImageI(666L, true);
        badId.getDetails().setUpdateEvent(new EventI(1L, false));
        omero.model.Image badUp = new ImageI(1L, true);
        badUp.getDetails().setUpdateEvent(new EventI(0L, false));
        
        try {
            i.reloadDatasetLinks(badId);
            fail();
        } catch (omero.ClientError ce) {
            // good
        }
        
        try {
            i.reloadDatasetLinks(badUp);
            fail();
        } catch (omero.ClientError ce) {
            // good
        }
        
        // From this we want to reload
        omero.model.Image i2 = new ImageI(1L, true);
        i2.getDetails().setUpdateEvent(new EventI(1L, false));
        omero.model.Dataset d2 = new DatasetI(1L, true);
        i2.linkDataset(d2);
        assertEquals(1, i2.sizeOfDatasetLinks());
        i.reloadDatasetLinks(i2);
        assertEquals(1, i.sizeOfDatasetLinks());
        assertEquals(-1, i2.sizeOfDatasetLinks());
        
    }

    @Test(groups = "ticket:2547")
    public void testOrderedCollectionsTicket2547() {
        PixelsI pixels = new PixelsI();
        ChannelI channel0 = new ChannelI();
        ChannelI channel1 = new ChannelI();
        pixels.addChannel(channel0);
        assertEquals(1, pixels.sizeOfChannels());
        ChannelI old = (ChannelI) pixels.setChannel(0, channel1);
        assertEquals(old, channel0);
        assertEquals(1, pixels.sizeOfChannels());
    }
}
