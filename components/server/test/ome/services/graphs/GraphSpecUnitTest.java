/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import static ome.services.graphs.GraphOpts.Op.REAP;
import static ome.services.graphs.GraphOpts.Op.SOFT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.IObject;
import ome.model.annotations.DoubleAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.services.graphs.GraphOpts.Op;
import ome.system.EventContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.util.SqlAction;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.stub.DefaultResultStub;
import org.springframework.beans.FatalBeanException;
import org.testng.annotations.Test;

@Test
public class GraphSpecUnitTest extends MockGraphTest {

    private final static Op DEFAULT = Op.HARD;

    /**
     * Test that various entry strings will be properly parsed. These are the
     * values passed in as a list to the {@link GraphSpec} constructors in
     * spec.xml
     *
     * Here we create a spec of "/Image" which serves as the basis. Any entry
     * also named "/Image" should not count as a subspec. A different path which
     * is also in spec.xml <em>will</em> count as a subspec, and finally, if the
     * value is neither the name of the top spec ("self") or of a value in
     * spec.xml, then again, it does <em>not</em> get a subspec.
     */
    @Test
    public void testGraphEntry() throws Exception {

        GraphSpec spec = new BaseGraphSpec(null, "/Image", "/Image",
                "/Image/Roi");
        assertValidEntry(spec, "/Image", "/Image", DEFAULT, false);
        assertValidEntry(spec, "/Image;REAP", "/Image", REAP, false);
        assertValidEntry(spec, "/Image/Roi;SOFT", "/Image/Roi", SOFT, true);
        assertValidEntry(spec, "/Image/Roi", "/Image/Roi", DEFAULT, true);
        assertValidEntry(spec, "/Image/Foo", "/Image/Foo", DEFAULT, false);

        assertInvalidEntry(";"); // No name
        assertInvalidEntry(";;"); // No name
        assertInvalidEntry(";HARD"); // No name
        assertInvalidEntry(";HARD;"); // No name
        assertInvalidEntry(";;;"); // Too many semicolons
    }

    private void assertValidEntry(GraphSpec spec, String string, String name,
            Op op, boolean hasSubSpec) throws Exception {
        GraphEntry de = new GraphEntry(spec, string);
        de.postProcess(specXml);
        assertEquals(name, de.getName());
        assertEquals(op, getOp(de));
        // assertEquals(hasSubSpec, de.getSubSpec() != null);
    }

    private void assertInvalidEntry(String string) {
        try {
            new GraphEntry(new BaseGraphSpec(null), string);
            fail("Should have failed: " + string);
        } catch (FatalBeanException fbe) {
            // good
        }
    }

    /**
     * Load all the specs in the XML file to guarantee that they are valid.
     *
     * @throws Exception
     */
    @Test
    public void testSpecXml() throws Exception {
        Map<String, GraphSpec> specs = specXml
                .getBeansOfType(GraphSpec.class);
        assertTrue(specs.size() > 0);
    }

    /**
     * Demonstrates how the specs will be used during deletion.
     */
    @Test
    public void testUsage() throws Exception {

        // Setup
        Mock sessionMock = mock(Session.class);
        Session session = (Session) sessionMock.proxy();

        Mock queryMock = mock(Query.class);
        queryMock.setDefaultStub(new DefaultResultStub());
        Query query = (Query) queryMock.proxy();

        sessionMock.expects(atLeastOnce()).method("createQuery")
                .will(returnValue(query));
        prepareGetRelationship();

        GraphSpec roi = specXml.getBean("/Roi", BaseGraphSpec.class);
        GraphState ids = new GraphState(null, null, session, roi);
        roi.initialize(1, null, null);
        // roi.graph(session, 0, ids); // Requires mock setup
    }

    /**
     * Test that iterator returns all specs.
     */
    @Test
    public void testSubSpecIterator() throws Exception {
        Map<String, GraphSpec> specs = specXml
                .getBeansOfType(GraphSpec.class);
        GraphSpec image = specs.get("/Image");
        image.postProcess(specXml);
        image.initialize(1, null, null);

        Iterator<GraphSpec> it = image.walk();
        List<GraphSpec> expected = new ArrayList<GraphSpec>();
        expected.add(specs.get("/Annotation")); // Roi's annotation
        expected.add(specs.get("/Roi"));
        expected.add(specs.get("/Annotation")); // file's annotation
        expected.add(specs.get("/OriginalFile"));
        expected.add(specs.get("/Image/Pixels/RenderingDef"));
        expected.add(specs.get("/Image/Pixels/Channel"));
        expected.add(specs.get("/Annotation"));
        expected.add(specs.get("/Instrument"));
        expected.add(image);
        while (it.hasNext()) {
            GraphSpec found = it.next();
            assertTrue(found.toString() + " not expected", expected.size() > 0);
            GraphSpec want = expected.remove(0);
            // assertEquals(want, found);
            // No longer able to test by equality
            assertEquals(want.getName(), found.getName());
        }
        assertEquals(expected.toString(), 0, expected.size());
    }

    /**
     * Tests that option strings get appropriately applied to each element. For
     * sub elements, either the full path can be given. E.g. for
     *
     * <pre>
     * delSpec: /Image
     *      value: /Annotation;;/Image/AnnotationLink/Annotation
     *
     * delSpec: /Annotation
     *      value: /FileAnnotation
     *      value: /LongAnnotation
     *      ...
     * </pre>
     *
     * to set an option on LongAnnotation one would pass:
     *
     * <pre>
     * {"/Image/AnnotationLink/FileAnnotation":"ns.includes=%example%"}
     * </pre>
     *
     * or pass only the last element:
     *
     * <pre>
     * {"/FileAnnotation":"ns.includes=%example%"}
     * </pre>
     *
     * In the case of annotations, setting an option on a supertype will apply
     * to all subclasses:
     *
     * <pre>
     * {"/NumericAnnotation":"..."}
     * </pre>
     *
     * will count for {@link LongAnnotation} and {@link DoubleAnnotation}.
     *
     */
    @Test
    public void testOptions() throws Exception {

        BaseGraphSpec spec;
        AnnotationGraphSpec ads;
        Map<String, String> options;

        spec = new BaseGraphSpec(Arrays.asList("/Image;SOFT"));
        options = new HashMap<String, String>();
        options.put("/Image", "KEEP");
        spec.initialize(1, "", options);
        assertEquals(Op.KEEP, getOp(spec.entries.get(0)));

        spec = new BaseGraphSpec(Arrays.asList("/Project/Dataset/Image;SOFT"));
        options = new HashMap<String, String>();
        options.put("/Project/Dataset/Image", "KEEP");
        spec.initialize(1, "", options);
        assertEquals(Op.KEEP, getOp(spec.entries.get(0)));

        spec = new BaseGraphSpec(Arrays.asList("/Project/Dataset/Image;SOFT"));
        options = new HashMap<String, String>();
        options.put("/", "KEEP");
        spec.initialize(1, "", options);
        assertEquals(Op.KEEP, getOp(spec.entries.get(0)));

        // check that values get applied to subclasses
        ads = (AnnotationGraphSpec) specXml.getBean("/Annotation");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP");
        ads.initialize(1, "", options);
        GraphEntry de = ads.entries().get(0);
        assertEquals(de.getName(), "/FileAnnotation");
        assertEquals(Op.KEEP, getOp(de));

        // Now check that something between /Annotation and the concrete
        // class /FileAnnotation takes precedence
        ads = (AnnotationGraphSpec) specXml.getBean("/Annotation");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP");
        options.put("/TypeAnnotation", "SOFT");
        ads.initialize(1, "", options);
        de = ads.entries().get(0);
        assertEquals(de.getName(), "/FileAnnotation");
        assertEquals(Op.SOFT, getOp(de));

        // Now test whether or not we can correctly parse off the "excludes"
        // statement
        ads = (AnnotationGraphSpec) specXml.getBean("/Annotation");
        options = new HashMap<String, String>();
        options.put("/FileAnnotation", "KEEP;excludes=dontkeepme");
        ads.initialize(1, "", options);
        de = ads.entries().get(0);
        assertEquals(de.getName(), "/FileAnnotation");
        assertEquals(Op.KEEP, getOp(de));
        assertEquals("dontkeepme", ads.getExclude(0));

        // and check that skipping will not take place, if there are
        // excludes since then its necessary to perform the query
        // anyway.
        ads = (AnnotationGraphSpec) specXml.getBean("/Annotation");
        options = new HashMap<String, String>();
        options.put("/FileAnnotation", "KEEP;excludes=dontkeepme");
        ads.initialize(1, "", options);
        assertTrue(ads.overrideKeep());

        // And check the same thing for the case where we have an annotation
        // attached to another object
        spec = (BaseGraphSpec) specXml.getBean("/Image");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP;excludes=dontkeepme");
        spec.initialize(1, "", options);

        GraphEntry entry = findEntry(spec, "/Annotation");
        assertTrue(entry.isKeep());
        assertTrue(entry.getSubSpec().overrideKeep());

        // And THEN check that the recursive check of KEEP/skip() doesn't
        // go too far
        spec = (BaseGraphSpec) specXml.getBean("/Dataset");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP;excludes=keepme");
        spec.initialize(1, "", options);
        entry = findEntry(spec, "/Image");
        assertFalse(entry.isKeep());
        entry = findEntry(spec, "/Annotation");
        assertTrue(entry.isKeep());
        assertFalse(spec.overrideKeep());

    }

    @Test(groups = "ticket:2959", expectedExceptions = GraphException.class)
    public void testGraphOpsDirectly() throws Exception {
        GraphOpts opts = new GraphOpts();
        EventContext ec = createEventContext(false);
        opts.push(Op.FORCE, true, ec);
    }

    /**
     * Tests the logic added to GraphState to handle SOFT graphs which must
     * rollback previously graphd objects. A stack of maps is kept which
     * get either committed or graphd based on exception handling.
     */
    @Test(groups = "ticket:3032")
    public void testGraphStateTransactionalIdCounting() throws Exception {

        String savepoint = null;
        Mock sMock = mock(Session.class);
        Session session = (Session) sMock.proxy();

        Mock qMock = mock(Query.class);
        Query q = (Query) qMock.proxy();

        sMock.expects(once()).method("createQuery").will(returnValue(q));
        qMock.expects(once()).method("setParameter");
        qMock.expects(once()).method("list").will(returnValue(Arrays.asList()));

        BaseGraphSpec spec = new BaseGraphSpec(Arrays.asList("/Foo"));
        spec.setBeanName("/Foo");
        spec.setExtendedMetadata(specXml.getBean(ExtendedMetadata.class));
        spec.postProcess(specXml);

        final GraphState ids = new GraphState(null, null, session, spec);
        assertEquals(0, ids.getTotalFoundCount());

        // Now we try to ignore all the method calls on the session since
        // that is not what we are testing.
        sMock.setDefaultStub(new DefaultResultStub());

        ids.addGraphIds(step("t", IObject.class, 1));
        assertEquals(1, ids.getProcessedIds("t").size());
        assertEquals(1, ids.getTotalProcessedCount());

        ids.addGraphIds(step("a", IObject.class, 2));
        assertEquals(2, ids.getTotalProcessedCount());
        assertEquals(1, ids.getProcessedIds("a").size());
        assertEquals(1, ids.getProcessedIds("t").size());

        GraphStep step = step("x", IObject.class, 3);
        step.savepoint(ids);
        ids.addGraphIds(step);
        assertEquals(2, ids.getTotalProcessedCount());
        assertEquals(1, ids.getProcessedIds("x").size());
        assertEquals(0, ids.getProcessedIds("t").size());
        assertEquals(0, ids.getProcessedIds("a").size());

        step.rollback(ids);
        assertEquals(2, ids.getTotalProcessedCount());
        assertEquals(0, ids.getProcessedIds("x").size());
        assertEquals(1, ids.getProcessedIds("t").size());
        assertEquals(1, ids.getProcessedIds("a").size());

        step = step("t", IObject.class, 4);
        step.savepoint(ids);
        ids.addGraphIds(step);
        assertEquals(2, ids.getTotalProcessedCount());
        assertEquals(0, ids.getProcessedIds("a").size());
        assertEquals(1, ids.getProcessedIds("t").size());
        step.release(ids);
        assertEquals(3, ids.getTotalProcessedCount());
        assertEquals(1, ids.getProcessedIds("a").size());
        assertEquals(2, ids.getProcessedIds("t").size());

    }

    GraphStep step(String type, Class<IObject> k, long id) {

        BaseGraphSpec spec = new BaseGraphSpec("/"+type, "/"+type);
        GraphStep step = new GraphStep(0, new LinkedList<GraphStep>(),
                spec, spec.entries.get(0), new long[0]) {

                    @Override
                    public void action(Callback cb, Session session,
                            SqlAction sql, GraphOpts opts) throws GraphException {
                        // no-op
                    }

                    @Override
                    public void onRelease(Class<IObject> k, Set<Long> ids)
                            throws GraphException {

                        // no-op

                    }

        };
        return step;
    }
}
