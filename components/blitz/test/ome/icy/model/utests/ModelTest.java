/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import junit.framework.TestCase;
import ome.model.meta.Experimenter;
import omero.RFloat;
import omero.model.ArcI;
import omero.model.ExperimenterI;
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
        new ExperimenterI().copyObject(e, new IceMapper());
    }
}
