/*
 * ome.server.itests.query.QueryTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query.pojos;

// Java imports
import java.util.Arrays;
import java.util.List;

import ome.model.acquisition.Instrument;
import ome.model.acquisition.Objective;
import ome.model.annotations.DoubleAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.enums.Correction;
import ome.model.enums.Immersion;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosFindHierarchiesQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
import ome.services.query.StringQuerySource;
import ome.tools.lsid.LsidUtils;
import ome.util.RdfPrinter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

/**
 * tests for a generic data access
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
@Test(groups = "internal")
public class QueryTest extends AbstractManagedContextTest {

    private static Log log = LogFactory.getLog(QueryTest.class);

    @Test
    public void testFindHierarchies() throws Exception {

        PojosFindHierarchiesQueryDefinition queryDef = new PojosFindHierarchiesQueryDefinition(
                new Parameters().addClass(Project.class)
                        .addIds(
                                Arrays.asList(9090L, 9091L, 9092L, 9990L,
                                        9991L, 9992L)).addOptions(null));

        List result = (List) iQuery.execute(queryDef);
        walkResult(result);
    }

    @Test
    public void testFilteredCalls() {

        PojosLoadHierarchyQueryDefinition queryDef = new PojosLoadHierarchyQueryDefinition(
                new Parameters().addClass(Project.class)
                        .addIds(
                                Arrays.asList(9090L, 9091L, 9092L, 9990L,
                                        9991L, 9992L)).addLong("ownerId",
                                10000L).addOptions(null));
        List result = (List) iQuery.execute(queryDef);
        walkResult(result);
    }

    @Test
    public void testCriteriaCalls() {
        Parameters p = new Parameters().addClass(Project.class).addIds(
                Arrays.asList(9090L, 9091L, 9092L, 9990L, 9991L, 9992L))
                .addLong("ownerId", null).addOptions(null);

        PojosLoadHierarchyQueryDefinition queryDef = new PojosLoadHierarchyQueryDefinition(
                p);

        List result = (List) iQuery.execute(queryDef);
        walkResult(result);
    }

    @Test
    public void testGetById() {
    }

    @Test
    public void testGetByName() {
    }

    @Test
    public void testGetListByExample() {
    }

    @Test
    public void testGetUniqueByExample() {
    }

    @Test
    public void testGetUniqueByMap() {
    }

    @Test
    public void testPersist() {
    }

    @Test
    public void testQueryList() {
    }

    @Test
    public void testQueryUnique() {
    }

    @Test
    public void testCounts() throws Exception {
        String s_dataset = LsidUtils.parseType(Dataset.ANNOTATIONLINKS);
        String s_annotations = LsidUtils.parseField(Dataset.ANNOTATIONLINKS);
        String works = String.format(
                "select target.id, count(collection) from %s target "
                        + "join target.%s collection group by target.id",
                s_dataset, s_annotations);

        Query q = new StringQuerySource().lookup(works, null);
        // select sum(*) from Dataset ds " +
        // "group by ds.id having ds.id in (1L)");
        List result = (List) iQuery.execute(q);
        System.out.println(result);
    }

    @Test
    public void testGetExperimenter() throws Exception {
        Experimenter e = iQuery.get(Experimenter.class, 0);
        // Previously with the default flag, the group
        // was automatically loaded. That's no longer the
        // case
        assertTrue(e.sizeOfGroupExperimenterMap() < 0);
        // Now to get the groups, we have to ask for them.
        e = iQuery.findByQuery("select e from Experimenter e"
                + " join fetch e.groupExperimenterMap m where e.id = 0", null);
        assertNotNull(e.getPrimaryGroupExperimenterMap());
        assertNotNull(e.getPrimaryGroupExperimenterMap().parent());

    }

    /**
     * currently documentation in
     * {@link ome.api.IQuery#findByExample(ome.model.IObject)} and
     * {@link ome.api.IQuery#findAllByExample(ome.model.IObject, ome.parameters.Filter)}
     * states that findByExample doesn't work on ids. If this changes, update.
     */
    @Test(groups = "ticket:711")
    public void test_examplById() throws Exception {
        Experimenter ex = new Experimenter(new Long(0), false);
        try {
            Experimenter e = iQuery.findByExample(ex);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("unique result"));
        }

    }

    @Test(groups = "ticket:1150")
    public void testFloatsDontGetRounded() throws Exception {
        Correction correction = iQuery.findAll(Correction.class, null).get(0);
        Immersion immersion = iQuery.findAll(Immersion.class, null).get(0);

        Objective o = new Objective();
        Instrument instrument = new Instrument();
        o.setCorrection(correction);
        o.setImmersion(immersion);
        o.setInstrument(instrument);
        // o.setLensNA(new Float(1.4));
        o.setLensNA(1.4);

        Objective t1 = iUpdate.saveAndReturnObject(o);

        // Test value via jdbc
        Float lensNA = jdbcTemplate.queryForObject(
                "SELECT lensna FROM objective WHERE id = ?", Float.class, t1
                        .getId());
        assertEquals(1.4, lensNA.floatValue(), 0.01);
        assertEquals(1.4, lensNA.floatValue(), Float.MIN_VALUE);

        // Test value return by iUpdate
        assertEquals(1.4, t1.getLensNA().floatValue(), 0.001);
        assertEquals(1.4, t1.getLensNA().floatValue(), Float.MIN_VALUE);
        assertEquals(1.4, t1.getLensNA().floatValue());

        // Test via query
        Objective t2 = iQuery.find(Objective.class, o.getId());
        assertEquals(1.4, t2.getLensNA().floatValue());
    }

    @Test(groups = "ticket:1150")
    public void testDoublesDontGetRounded() throws Exception {
        DoubleAnnotation da = new DoubleAnnotation();
        da.setDoubleValue(1.4);

        DoubleAnnotation t1 = iUpdate.saveAndReturnObject(da);
        assertEquals(1.4, t1.getDoubleValue().doubleValue(), Double.MIN_VALUE);

    }

    protected void walkResult(List result) {
        RdfPrinter rdf = new RdfPrinter();
        rdf.filter("results are", result);
        System.out.println(rdf.getRdf());
    }

}
