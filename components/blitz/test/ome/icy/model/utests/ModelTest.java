/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import omero.RFloat;
import omero.RString;
import omero.model.ArcI;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.ImageI;
import omero.model.PixelsI;
import omero.util.IceMapper;

import org.testng.annotations.Test;

public class ModelTest extends TestCase {

    @Test(groups = "ticket:636")
    public void testInheritanceInConcreteClasses() throws Exception {
        ArcI arcI = new ArcI();
        // arcI.unload();
        arcI.setPower(new RFloat(1.0f));
    }

    @Test
    public void testCopyObject() throws Exception {
        Experimenter e = new Experimenter();
        e.setOmeName("hi");
        e.linkExperimenterGroup(new ExperimenterGroup("foo"));
        new ExperimenterI().copyObject(e, new IceMapper());

        Pixels p = new Pixels();
        Image i = new Image();
        p.setImage(i);
        p.getDetails().setOwner(e);
        new PixelsI().copyObject(p, new IceMapper());

    }

    @Test
    public void testFillObject() throws Exception {
        ExperimenterI e = new ExperimenterI();
        e.setOmeName(new RString("name"));
        e.linkExperimenterGroup(new ExperimenterGroupI());
        e.fillObject(new IceMapper());

        PixelsI p = new PixelsI();
        ImageI i = new ImageI();
        p.setImage(i);
        p.getDetails().owner = e;
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
}
