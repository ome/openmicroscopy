/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query;

import ome.conditions.ApiUsageException;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

/**
 * These tests are meant to find strange HQL issues which throw exceptions like
 * {@link NullPointerException} or {@link IllegalStateException} when they
 * really should not.
 */
@Test(groups = "integration")
public class HqlQueriesTest extends AbstractManagedContextTest {

    @Test(groups = "hibernate_exception", expectedExceptions = ApiUsageException.class)
    public void testPoorlyFormedHqlThrowsAUENotIllegalState() throws Exception {

        String badQuery = "select o from Job j "
                + "join fetch j.originalFileLinks links "
                + "join fetch o.format " + "where " + "j.id = 0 "
                + "and o.details.owner.id = 0 "
                + "and o.format.value = 'text/x-python' ";

        iQuery.findAllByQuery(badQuery, null);
    }
}
