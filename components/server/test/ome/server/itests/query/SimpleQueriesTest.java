/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query;

import java.util.HashSet;
import java.util.List;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.server.itests.AbstractManagedContextTest;

@Test(groups = { "query" })
public class SimpleQueriesTest extends AbstractManagedContextTest {

    private static final String TICKET_72 = "ticket:72";

    private static final String TICKET_347 = "ticket:347";

    @Test(groups = { TICKET_72 })
    public void test_findAll() throws Exception {

        Project p = new Project();
        p.setName(TICKET_72);
        p = iUpdate.saveAndReturnObject(p);

        List<Project> list = iQuery.findAll(Project.class, null);
    }

    @Test(groups = { TICKET_347, "broken", "Unscheduled" })
    public void testRefresh() throws Exception {
        Project p = new Project();
        p.setName(TICKET_347);
        p = iUpdate.saveAndReturnObject(p);

        // Test 1

        p = iQuery.refresh(p);
        assertNotNull(p);
        assertNotNull(p.getId());
        assertEquals(p.getName(), TICKET_347);

        // Test 2

        Dataset d = new Dataset();
        d.setName(TICKET_347);
        p.putAt(Project.DATASETLINKS, new HashSet());
        p.linkDataset(d);

        // this should fail since dataset is a new instance.
        try {
            p = iQuery.refresh(p);
            fail("refresh should throw ApiUsage on transient entities.");
        } catch (ApiUsageException api) {
            // ok
        }

        // Test 3

        // now let's try it without transient entities
        p = iQuery.get(p.getClass(), p.getId());
        d.clearProjectLinks();
        p.putAt(Project.DATASETLINKS, new HashSet());

        d = iUpdate.saveAndReturnObject(d);
        p.linkDataset(d);
        p = iUpdate.saveAndReturnObject(p);

        p = iQuery.refresh(p);
        assertNotNull(p);
        assertTrue(p.sizeOfDatasetLinks() == 1);

        // Test 4

        // now let's add something else to a collection
        ProjectDatasetLink link = new ProjectDatasetLink();
        link.link(new Project(p.getId(), false), new Dataset(d.getId(), false));
        link = iUpdate.saveAndReturnObject(link);

        p = iQuery.refresh(p);
        assertNotNull(p);
        assertTrue(p.sizeOfDatasetLinks() == 2);

        // Test 5

        // now let's remove something
        iUpdate.deleteObject(link);
        p = iQuery.refresh(p);
        assertNotNull(p);
        assertTrue(p.sizeOfDatasetLinks() == 1);

    }
}
