/*
 * ome.services.utests.HibernateUtilsTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.utests;

// Java imports

// Third-party libraries
import junit.framework.TestCase;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.*;

// Application-internal dependencies
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.tools.hibernate.HibernateUtils;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 3.0
 */
public class HibernateUtilsTest extends MockObjectTestCase {

    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception {
        super.verify();
        super.tearDown();
    }

    @Test
    public void testIdEquals() throws Exception {
        assertTrue(HibernateUtils.idEqual(null, null));
        assertTrue(HibernateUtils.idEqual(new Image(1L), new Image(1L)));
        assertFalse(HibernateUtils.idEqual(new Image(), new Image()));
        assertFalse(HibernateUtils.idEqual(new Image(1L), new Image(null)));
        assertFalse(HibernateUtils.idEqual(new Image(null), new Image(1L)));
        assertFalse(HibernateUtils.idEqual(new Image(null), new Image(null)));
        assertFalse(HibernateUtils.idEqual(new Image(null), null));
        assertFalse(HibernateUtils.idEqual(null, new Image(null)));
    }

    Mock mockPersister;

    EntityPersister persister;

    String[] names = { "details", "field1", "field2" };

    Event entity = new Event();

    int[] dirty = { 0 };

    @Test
    public void testOnlyLockedChanged() throws Exception {

        Object[] state = { null, null, null };
        Object[] current = { new Details(), null, null };
        setupMocks(current);

        assertOnlyLockedChanged(state, true);

        Details d = new Details();
        d.setPermissions(Permissions.READ_ONLY);
        current = new Object[] { d, null, null };
        setupMocks(current);

        assertOnlyLockedChanged(state, false);

    }

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

    protected void assertOnlyLockedChanged(Object[] state,
            boolean onlyLockChanged) {
        assertTrue(onlyLockChanged == HibernateUtils.onlyLockChanged(null,
                persister, entity, state, names));
    }
}
