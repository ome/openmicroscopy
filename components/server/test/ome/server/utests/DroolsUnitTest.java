/*
 * ome.server.utests.DroolsUnitTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

// Java imports
import java.util.Date;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.rules.RulesEngine;

import org.drools.spi.ConsequenceException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * basic tests for Drools system.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class DroolsUnitTest extends
        AbstractDependencyInjectionSpringContextTests {

    // =========================================================================
    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception {
        super.setUp();
    }

    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception {
        super.tearDown();
    }

    // =========================================================================

    private RulesEngine re;

    @Override
    protected String[] getConfigLocations() {
        return new String[] { "ome/services/drools.xml" };
    }

    public void setEngine(RulesEngine eng) {
        this.re = eng;
    }

    @Test
    public void testAssertBaseObject() throws Exception {
        re.evaluate(new Object());
    }

    @Test
    public void testTimeInTheFuture() throws Exception {
        Date d = new Date(System.currentTimeMillis() + 1000000);
        Date d2 = new Date(d.getTime());
        re.evaluate(d);
        assertTrue(d.before(d2));

    }

    @Test
    public void testWithGraph() throws Exception {
        Project p = new Project();
        p.getDetails().copy(new Details());
        Experimenter e = new Experimenter();
        e.getDetails().copy(new Details());
        Event ev = new Event();
        Date d = new Date(System.currentTimeMillis());
        String description = "blah blah";
        p.getDetails().setOwner(e);
        e.getDetails().setCreationEvent(ev);
        // TODO ev.setTime

        re.evaluate(p);

    }

    @Test
    public void testSameObjectTwice() throws Exception {
        Object o = new Object();
        re.evaluate(o, o);
    }

    @Test
    public void testClassificationExclusivity() throws Exception {
        CategoryGroup cg = new CategoryGroup();
        Category c1 = new Category();
        Category c2 = new Category();
        Image i = new Image();

        // FIXME link them up

        try {
            re.evaluate(c1);
            // FIXME fail("Rule did not catch the error.");
        } catch (ConsequenceException e) {
            // good.
        }

    }

}
