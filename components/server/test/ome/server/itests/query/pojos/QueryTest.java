/*
 * ome.server.itests.query.QueryTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query.pojos;

// Java imports
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;
import ome.conditions.ApiUsageException;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Objective;
import ome.model.annotations.DoubleAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.enums.Correction;
import ome.model.enums.Immersion;
import ome.model.meta.Experimenter;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosFindHierarchiesQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
import ome.services.query.StringQuerySource;
import ome.services.util.Executor;
import ome.services.util.Executor.SimpleWork;
import ome.system.ServiceFactory;
import ome.tools.lsid.LsidUtils;
import ome.util.RdfPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
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

    private static Logger log = LoggerFactory.getLogger(QueryTest.class);

    @Test
    public void testFindHierarchies() throws Exception {

        PojosFindHierarchiesQueryDefinition queryDef = new PojosFindHierarchiesQueryDefinition(
                new Parameters().addClass(Project.class)
                        .addIds(
                                Arrays.asList(9090L, 9091L, 9092L, 9990L,
                                        9991L, 9992L)));

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
                                10000L));
        List result = (List) iQuery.execute(queryDef);
        walkResult(result);
    }

    @Test
    public void testCriteriaCalls() {
        Parameters p = new Parameters().addClass(Project.class).addIds(
                Arrays.asList(9090L, 9091L, 9092L, 9990L, 9991L, 9992L))
                .addLong("ownerId", null);

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
        assertFloatsNotRounded(1.4);
        assertFloatsNotRounded(1.35);
        assertFloatsNotRounded(1.225);
        assertFloatsNotRounded(1.1125);
        assertFloatsNotRounded(1.03333);
    }

    private void assertFloatsNotRounded(double dbl) {
        Correction correction = iQuery.findAll(Correction.class, null).get(0);
        Immersion immersion = iQuery.findAll(Immersion.class, null).get(0);

        Objective o = new Objective();
        Instrument instrument = new Instrument();
        o.setCorrection(correction);
        o.setImmersion(immersion);
        o.setInstrument(instrument);
        // o.setLensNA(new Float(dbl));
        o.setLensNA(dbl);

        Objective t1 = iUpdate.saveAndReturnObject(o);

        // Test value via jdbc
        String jdbcQuery = "SELECT lensNa FROM Objective WHERE id = :id";
        Float lensNA = (Float) iQuery.projection(jdbcQuery,
            new Parameters().addId(t1.getId())).get(0)[0];
        assertEquals(dbl, lensNA.floatValue(), 0.01);
        try {
            assertEquals(dbl, lensNA.floatValue(), Float.MIN_VALUE);
        } catch (AssertionFailedError e) {
            // This is what fails!!
        }

        // now test is with double which is our chosen solution
        Double lensNADoubled = (Double) iQuery.projection(jdbcQuery,
            new Parameters().addId(t1.getId())).get(0)[0];
        assertEquals(dbl, lensNADoubled.doubleValue(), 0.01);
        assertEquals(dbl, lensNADoubled.doubleValue(), Float.MIN_VALUE);
        assertEquals(dbl, lensNADoubled.doubleValue(), Double.MIN_VALUE);

        // Test value return by iUpdate
        // Now changing these to doubleValue() post #1150 fix.
        assertEquals(dbl, t1.getLensNA().doubleValue(), 0.001);
        assertEquals(dbl, t1.getLensNA().doubleValue(), Float.MIN_VALUE);
        assertEquals(dbl, t1.getLensNA().doubleValue());

        // Test via query
        Objective t2 = iQuery.find(Objective.class, o.getId());
        assertEquals(dbl, t2.getLensNA().doubleValue());
    }

    @Test(groups = "ticket:1150")
    public void testDoublesDontGetRounded() throws Exception {
        DoubleAnnotation da = new DoubleAnnotation();
        da.setDoubleValue(1.4);

        DoubleAnnotation t1 = iUpdate.saveAndReturnObject(da);
        assertEquals(1.4, t1.getDoubleValue().doubleValue(), Double.MIN_VALUE);

    }

    public void testNullFromGetPrimaryPixelsWithNoPixels() throws Exception {
        Image i = new Image("null from get primary pixels");
        i = iUpdate.saveAndReturnObject(i);
        i = iQuery.get(Image.class, i.getId());
        assertEquals(-1, i.sizeOfPixels());
        try {
            i.getPrimaryPixels();
            fail("must throw");
        } catch (ApiUsageException aue) {
            // good
        }

        // Try "internally" with active session
        final long id = i.getId();
        ((Executor) this.applicationContext.getBean("executor")).execute(
                loginAop.p, new SimpleWork(this, "internal query test") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        Image t = (Image) session.get(Image.class, id);
                        assertEquals(0, t.sizeOfPixels());
                        try {
                            t.getPrimaryPixels();
                            fail("should throw");
                        } catch (IndexOutOfBoundsException aiobe) {
                            // good;
                        }
                        return null;
                    }
                });

        try {
            i.unmodifiablePixels();
            fail("must throw");
        } catch (ApiUsageException aue) {
            // good
        }
    }

    public void testNullFromGetPrimaryPixelsWithPixels() throws Exception {
        // This needs the in-memory testing stuff
        // Pixels p = ObjectFactory.createPixelGraph(null);
        // p = iUpdate.saveAndReturnObject(p);
        Pixels p = iQuery.findAll(Pixels.class, new Filter().page(0, 1)).get(0);
        Image i = iQuery.get(Image.class, p.getImage().getId());
        assertEquals(-1, i.sizeOfPixels());
        try {
            i.getPrimaryPixels();
            fail("must throw");
        } catch (ApiUsageException aue) {
            // good
        }

        // Try "internally" with active session
        final long id = i.getId();
        ((Executor) this.applicationContext.getBean("executor")).execute(
                loginAop.p, new SimpleWork(this, "internal query test") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        Image t = (Image) session.get(Image.class, id);
                        assertTrue(t.unmodifiablePixels().size() > 0 );
                        assertNotNull(t.getPrimaryPixels());
                        return null;
                    }
                });

        try {
            i.unmodifiablePixels();
            fail("must throw");
        } catch (ApiUsageException aue) {
            // good
        }
    }

    protected void walkResult(List result) {
        RdfPrinter rdf = new RdfPrinter();
        rdf.filter("results are", result);
        System.out.println(rdf.getRdf());
    }

}
