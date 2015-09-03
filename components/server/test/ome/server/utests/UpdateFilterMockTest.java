/*
 * ome.server.utests.UpdateFilterMockTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests;

import java.util.Collection;
import java.util.Map;

import org.testng.annotations.*;

import ome.model.IObject;
import ome.model.internal.Details;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since Omero 2.0
 */
public class UpdateFilterMockTest extends AbstractLoginMockTest {

    // ~ NON GRAPHS (single elements)
    // =========================================================================

    @Test
    public void test_filter_null() throws Exception {
        filter.filter(null, (Object) null);
        filter.filter(null, (IObject) null);
        filter.filter(null, (Details) null);
        filter.filter(null, (Map) null);
        filter.filter(null, (Collection) null);
    }

    // ~ GRAPHS (multiple levels)
    // =========================================================================

}
