/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import ome.icy.fixtures.BlitzServerFixture;
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;

import org.testng.annotations.Test;

public class ThumbStoreCoverageTest extends MockedBlitzTest {

    @Test
    public void testSizeMethodsWork() throws Exception {

        fixture = new BlitzServerFixture(200, 200);
        ServiceFactoryPrx session = fixture.createSession();

        fixture.methodCall();
        RawPixelsStorePrx prx = session.createRawPixelsStore();

        fixture.methodCall();
        fixture.getPixelsStore().expects(once()).method("setPixelsId");
        prx.setPixelsId(1L);

        fixture.methodCall();
        fixture.getPixelsStore().expects(once()).method("getTimepointSize")
                .will(returnValue(Integer.valueOf(1)));
        int size = prx.getTimepointSize();

        assertTrue(size == 1);
    }

}
