/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.query;

import java.util.HashSet;
import java.util.Set;

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.CollectionCountQueryDefinition;
import ome.services.query.IObjectClassQuery;
import ome.services.query.PojosFindHierarchiesQueryDefinition;
import ome.services.query.PojosGetImagesQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
import ome.services.query.QueryFactory;
import ome.services.query.StringQuery;

import org.testng.annotations.Test;

@Test(groups = { "query" })
public class UniqueResultTest extends AbstractManagedContextTest {

    // ~ 72
    // =========================================================================

    private static final String TICKET_72 = "ticket:72";

    private static final String TICKET_83 = "ticket:83";

    @Test(groups = { TICKET_72 })
    public void test_queryUnique() throws Exception {

        Project p = getProject();

        Query<IObject> q1 = query(false);
        try {

            IObject o1 = iQuery.execute(q1);
            fail("Should have thrown an exception.");
        } catch (ClassCastException cce) {
            // ok.
        }

        // This should work
        Query<IObject> q2 = query(true);
        IObject o2 = iQuery.execute(q2);

        // And this should work.
        IObject o3 = iQuery.findByQuery(
                "select p from Project p where p.id = :id", new Parameters()
                        .addId(p.getId()).setFilter(new Filter().unique()));
    }

    @Test(groups = { TICKET_72 })
    public void test_queryList() throws Exception {
        Project p = getProject();

        try {

            iQuery.findAllByQuery("select p from Project p where p.id = :id",
                    new Parameters().addId(p.getId()).setFilter(
                            new Filter().unique()));
            fail("Should have thrown class cast exception.");
        } catch (InternalException ie) {
            assertTrue(ie.getMessage().contains("ClassCastException"));
            // ok.
        }

    }

    Parameters p;

    // ~ 83
    // =========================================================================
    @Test(groups = { TICKET_83 })
    public void test_ContainerFindHierarchies_unique() throws Exception {
        p = new Parameters().addIds(getNewProjects(1, TICKET_83)).addClass(
                Project.class);
        p.unique();
        Query q = new PojosFindHierarchiesQueryDefinition(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_ContainerFindHierarchies_list() throws Exception {
        p = new Parameters().addIds(getNewProjects(2, TICKET_83)).addClass(
                Project.class);
        assertFalse(p.isUnique());
        Query q = new PojosFindHierarchiesQueryDefinition(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_ContainerGetImages_unique() throws Exception {
        p = new Parameters().addIds(getNewProjects(1, TICKET_83)).addClass(
                Project.class);
        p.unique();
        Query q = new PojosGetImagesQueryDefinition(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_ContainerGetImages_list() throws Exception {
        p = new Parameters().addIds(getNewProjects(2, TICKET_83)).addClass(
                Project.class);
        assertFalse(p.isUnique());
        Query q = new PojosGetImagesQueryDefinition(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_ContainerLoadHierarchies_unique() throws Exception {
        p = new Parameters().addIds(getNewProjects(1, TICKET_83)).addClass(
                Project.class);
        p.unique();
        Query q = new PojosLoadHierarchyQueryDefinition(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_ContainerLoadHierarchies_list() throws Exception {
        p = new Parameters().addIds(getNewProjects(2, TICKET_83)).addClass(
                Project.class);
        assertFalse(p.isUnique());
        Query q = new PojosLoadHierarchyQueryDefinition(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_CollectionCount_unique() throws Exception {
        p = new Parameters().addIds(getNewProjects(1, TICKET_83)).addString(
                "field", Project.DATASETLINKS);
        p.unique();
        Query q = new CollectionCountQueryDefinition(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_CollectionCount_list() throws Exception {
        p = new Parameters().addIds(getNewProjects(2, TICKET_83)).addString(
                "field", Project.DATASETLINKS);
        assertFalse(p.isUnique());
        Query q = new CollectionCountQueryDefinition(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_IObjectClassQuery_unique() throws Exception {
        p = new Parameters().addClass(Project.class).addLong("id",
                getNewProjects(1, TICKET_83).iterator().next());

        p.unique();
        Query q = new IObjectClassQuery(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_IObjectClassQuery_list() throws Exception {
        p = new Parameters().addClass(Project.class);
        assertFalse(p.isUnique());
        Query q = new IObjectClassQuery(p);
        iQuery.execute(q);
    }

    // Removed PojosFindAnnotationsQueryDefinition methods

    @Test(groups = { TICKET_83 })
    public void test_StringQuery_unique() throws Exception {
        p = new Parameters().addString(StringQuery.STRING,
                "select p from Project p where p.id = :" + Parameters.ID)
                .addId(getNewImages(1, TICKET_83).iterator().next());
        p.unique();
        Query q = new StringQuery(p);
        iQuery.execute(q);
    }

    @Test(groups = { TICKET_83 })
    public void test_StringQuery_list() throws Exception {
        p = new Parameters().addString(
                StringQuery.STRING,
                "select p from Project p where p.id in (:" + Parameters.IDS
                        + ")").addIds(getNewImages(2, TICKET_83));
        assertFalse(p.isUnique());
        Query q = new StringQuery(p);
        iQuery.execute(q);
    }

    // ~ Private helpers
    // =========================================================================
    private Project getProject() {
        Project p = iQuery.findAll(Project.class, new Filter().page(0, 1)).get(
                0);
        return p;
    }

    private Set<Long> getNewProjects(int howMany, String name) {
        Set<Long> results = new HashSet<Long>();
        for (int i = 0; i < howMany; i++) {
            Project prj = new Project();
            prj.setName(name);
            results.add(iUpdate.saveAndReturnObject(prj).getId());
        }
        return results;
    }

    private Set<Long> getNewImages(int howMany, String name) {
        Set<Long> results = new HashSet<Long>();
        for (int i = 0; i < howMany; i++) {
            Image img = new Image();
            img.setName(name);
            results.add(iUpdate.saveAndReturnObject(img).getId());
        }
        return results;
    }

    private QueryFactory factory() {
        return (QueryFactory) applicationContext.getBean("queryFactory");
    }

    private Query query(boolean unique) {
        Filter f = new Filter();
        if (unique) {
            f.unique();
        }

        Query q = factory().lookup("select p from Project p where p.id = :id",
                new Parameters().addId(getProject().getId()).setFilter(f));
        return q;
    }

}
