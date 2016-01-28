/*
 * ome.services.utests.HibernateUtilsTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.utests;

import ome.model.core.Image;
import ome.model.meta.Event;
import ome.tools.hibernate.HibernateUtils;

import org.hibernate.persister.entity.EntityPersister;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since Omero 3.0
 */
public class HibernateUtilsTest extends MockObjectTestCase {

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        super.verify();
        super.tearDown();
    }

    @Test
    public void testIdEquals() throws Exception {
        assertTrue(HibernateUtils.idEqual(null, null));
        assertTrue(HibernateUtils.idEqual(new Image(1L, false), new Image(1L,
                false)));
        assertFalse(HibernateUtils.idEqual(new Image(), new Image()));
        assertFalse(HibernateUtils.idEqual(new Image(1L, true), new Image(null,
                true)));
        assertFalse(HibernateUtils.idEqual(new Image(null, true), new Image(1L,
                false)));
        assertFalse(HibernateUtils.idEqual(new Image(null, true), new Image(
                null, true)));
        assertFalse(HibernateUtils.idEqual(new Image(null, true), null));
        assertFalse(HibernateUtils.idEqual(null, new Image(null, true)));
    }

    Mock mockPersister;

    EntityPersister persister;

    String[] names = { "details", "field1", "field2" };

    Event entity = new Event();

    int[] dirty = { 0 };

    // ~ Helpers
    // =========================================================================

    protected void setupMocks(Object[] current) {
        mockPersister = mock(EntityPersister.class);
        persister = (EntityPersister) mockPersister.proxy();
        mockPersister.expects(once()).method("getPropertyValues").will(
                returnValue(current));
        mockPersister.expects(once()).method("findDirty").will(
                returnValue(dirty));

    }

}
