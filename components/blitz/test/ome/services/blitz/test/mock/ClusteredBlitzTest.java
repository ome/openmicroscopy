/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import net.sf.ehcache.Cache;
import ome.model.meta.Session;
import ome.services.blitz.fire.Ring;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 */
public class ClusteredBlitzTest extends MockObjectTestCase {

    MockFixture fixture1, fixture2;

    @BeforeClass(groups = "integration")
    public void setup() throws Exception {
        fixture1 = new MockFixture(this, "a");
        fixture2 = new MockFixture(this, "b");
    }

    @Test(groups = "integration")
    public void testSimple() throws Exception {
        Session s = fixture1.session();
        Cache c = fixture1.cache();
        fixture1.prepareServiceFactory(s, c);
        fixture1.createServiceFactory("username", "client1");
        Thread.sleep(1000L);
        fixture1.prepareServiceFactory(s, c);
        fixture2.createServiceFactory("my-session-uuid", "client2");

        Ring ring = fixture1.ring();

        // Tests


    }

}
