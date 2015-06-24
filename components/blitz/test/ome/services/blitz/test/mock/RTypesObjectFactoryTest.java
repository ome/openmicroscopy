/*
 *   $Id$
 *
 *   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import ome.model.meta.Experimenter;

import omero.api.ServiceFactoryPrx;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Guarantees that the RTypes can be properly serialized.
 */
public class RTypesObjectFactoryTest extends MockObjectTestCase {

    MockFixture fixture;

    @AfterMethod(groups = "integration")
    public void shutdownFixture() {
        fixture.tearDown();
    }

    @Test(groups = "integration")
    public void testLoadExperimenter() throws Exception {
        fixture = new MockFixture(this);
        ServiceFactoryPrx sf = fixture.createServiceFactory();
        sf.closeOnDestroy();


        Experimenter e = new Experimenter();
        e.setId(1L);
        e.setOmeName("name");
        e.setLdap(false);
        fixture.mock("mock-ome.api.IAdmin").expects(once()).method(
                "getExperimenter").will(returnValue(e));
        sf.getAdminService().getExperimenter(0L);
    }

}
