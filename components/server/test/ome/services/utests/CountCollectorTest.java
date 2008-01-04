/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.utests;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import ome.conditions.ApiUsageException;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.services.util.CountCollector;

import org.testng.annotations.Test;

public class CountCollectorTest extends TestCase {

    protected CountCollector c;

    protected long current = 0;

    protected Long next() {
        return current++;
    }

    @Test
    public void testLookupTablesCreated() throws Exception {
        c = new CountCollector();

        Long id = next();
        Project p = new Project(id, true);
        c.addCounts(Project.class, ProjectDatasetLink.PARENT, Collections
                .<Object[]> singletonList(new Object[] { id, 1000L }));
        c.collect(p);
        assertEquals(p.getDetails().getCounts().get(ProjectDatasetLink.PARENT),
                1000L);
    }

    @Test
    public void testNoCountGiven() throws Exception {
        c = new CountCollector();

        Project p = new Project(next(), true);
        c.addCounts(Project.class, ProjectDatasetLink.PARENT, queryResults(-1L,
                499L));
        c.collect(p);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testNegativeCountGiven() throws Exception {
        c = new CountCollector();

        Project p = new Project(next(), true);
        c.addCounts(Project.class, ProjectDatasetLink.PARENT, queryResults(1L,
                -1L));
        c.collect(p);
    }

    @Test
    public void testWhatHappensOnNullIdThough() throws Exception {
        c = new CountCollector();
        Project p = new Project();
        c.collect(p);
    }

    // Helpers ~
    // =========================================================================

    private List<Object[]> queryResults(Long id, Long count) {
        return Collections.<Object[]> singletonList(new Object[] { id, count });
    }

}
