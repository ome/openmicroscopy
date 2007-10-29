/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import ome.icy.fixtures.BlitzServerFixture;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

@Test(groups = { "integration", "manual" })
public class MockedBlitzTest extends MockObjectTestCase {

    public BlitzServerFixture fixture;

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        if (fixture != null) {
            fixture.tearDown();
            fixture = null;
        }
    }

}
