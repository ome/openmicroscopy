/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import ome.icy.fixtures.BlitzServerFixture;
import omero.api.ServiceFactoryPrx;
import omero.grid.InteractiveProcessPrx;
import omero.model.ScriptJobI;

import org.testng.annotations.Test;

public class ScriptProcessorTest extends MockedBlitzTest {

    @Test
    public void testTryToAcquireProcess() throws Exception {

        fixture = new BlitzServerFixture(3, 3);
        ServiceFactoryPrx session = fixture.createSession();

        ScriptJobI job = new ScriptJobI();
        InteractiveProcessPrx prx = session.acquireInteractiveProcess(job, 1);

    }

}
