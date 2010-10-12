/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import static ome.services.delete.DeleteEntry.DEFAULT;
import static ome.services.delete.DeleteOpts.Op.REAP;
import static ome.services.delete.DeleteOpts.Op.SOFT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.model.annotations.DoubleAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.services.delete.DeleteOpts.Op;
import ome.system.EventContext;
import ome.tools.hibernate.ExtendedMetadata;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.stub.DefaultResultStub;
import org.springframework.beans.FatalBeanException;
import org.testng.annotations.Test;

@Test
public class DeleteSpecUnitTest extends MockDeleteTest {

    /**
     * Test that various entry strings will be properly parsed. These are the
     * values passed in as a list to the {@link DeleteSpec} constructors in
     * spec.xml
     *
     * Here we create a spec of "/Image" which serves as the basis. Any entry
     * also named "/Image" should not count as a subspec. A different path which
     * is also in spec.xml <em>will</em> count as a subspec, and finally, if the
     * value is neither the name of the top spec ("self") or of a value in
     * spec.xml, then again, it does <em>not</em> get a subspec.
     */
    @Test
    public void testDeleteEntry() throws Exception {

        DeleteSpec spec = new BaseDeleteSpec(null, "/Image", "/Image",
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

    private void assertValidEntry(DeleteSpec spec, String string, String name,
            Op op, boolean hasSubSpec) throws Exception {
        DeleteEntry de = new DeleteEntry(spec, string);
        de.postProcess(specXml);
        assertEquals(name, de.getName());
        assertEquals(op, getOp(de));
        // assertEquals(hasSubSpec, de.getSubSpec() != null);
    }

    private void assertInvalidEntry(String string) {
        try {
            new DeleteEntry(new BaseDeleteSpec(null), string);
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
        Map<String, DeleteSpec> specs = specXml
                .getBeansOfType(DeleteSpec.class);
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

        DeleteSpec roi = specXml.getBean("/Roi", BaseDeleteSpec.class);
        DeleteState ids = new DeleteState(specXml, session, roi);
        roi.initialize(1, null, null);
        // roi.delete(session, 0, ids); // Requires mock setup
    }

    /**
     * Test that iterator returns all specs.
     */
    @Test
    public void testSubSpecIterator() throws Exception {
        Map<String, DeleteSpec> specs = specXml
                .getBeansOfType(DeleteSpec.class);
        DeleteSpec image = specs.get("/Image");
        image.postProcess(specXml);
        image.initialize(1, null, null);

        Iterator<DeleteSpec> it = image.walk();
        List<DeleteSpec> expected = new ArrayList<DeleteSpec>();
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
            DeleteSpec found = it.next();
            assertTrue(found.toString() + " not expected", expected.size() > 0);
            DeleteSpec want = expected.remove(0);
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

        DeleteSpecFactory dsf;
        BaseDeleteSpec spec;
        AnnotationDeleteSpec ads;
        Map<String, String> options;

        dsf = specXml.getBean(DeleteSpecFactory.class);
        spec = new BaseDeleteSpec(Arrays.asList("/Image;SOFT"));
        options = new HashMap<String, String>();
        options.put("/Image", "KEEP");
        spec.initialize(1, "", options);
        assertEquals(Op.KEEP, getOp(spec.entries.get(0)));

        dsf = specXml.getBean(DeleteSpecFactory.class);
        spec = new BaseDeleteSpec(Arrays.asList("/Project/Dataset/Image;SOFT"));
        options = new HashMap<String, String>();
        options.put("/Project/Dataset/Image", "KEEP");
        spec.initialize(1, "", options);
        assertEquals(Op.KEEP, getOp(spec.entries.get(0)));

        dsf = specXml.getBean(DeleteSpecFactory.class);
        spec = new BaseDeleteSpec(Arrays.asList("/Project/Dataset/Image;SOFT"));
        options = new HashMap<String, String>();
        options.put("/", "KEEP");
        spec.initialize(1, "", options);
        assertEquals(Op.KEEP, getOp(spec.entries.get(0)));

        // check that values get applied to subclasses
        dsf = specXml.getBean(DeleteSpecFactory.class);
        ads = (AnnotationDeleteSpec) dsf.get("/Annotation");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP");
        ads.initialize(1, "", options);
        DeleteEntry de = ads.entries().get(0);
        assertEquals(de.getName(), "/FileAnnotation");
        assertEquals(Op.KEEP, getOp(de));

        // Now check that something between /Annotation and the concrete
        // class /FileAnnotation takes precedence
        dsf = specXml.getBean(DeleteSpecFactory.class);
        ads = (AnnotationDeleteSpec) dsf.get("/Annotation");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP");
        options.put("/TypeAnnotation", "SOFT");
        ads.initialize(1, "", options);
        de = ads.entries().get(0);
        assertEquals(de.getName(), "/FileAnnotation");
        assertEquals(Op.SOFT, getOp(de));

        // Now test whether or not we can correctly parse off the "excludes"
        // statement
        dsf = specXml.getBean(DeleteSpecFactory.class);
        ads = (AnnotationDeleteSpec) dsf.get("/Annotation");
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
        dsf = specXml.getBean(DeleteSpecFactory.class);
        ads = (AnnotationDeleteSpec) dsf.get("/Annotation");
        options = new HashMap<String, String>();
        options.put("/FileAnnotation", "KEEP;excludes=dontkeepme");
        ads.initialize(1, "", options);
        assertTrue(ads.overrideKeep());
        
        // And check the same thing for the case where we have an annotation
        // attached to another object
        dsf = specXml.getBean(DeleteSpecFactory.class);
        spec = (BaseDeleteSpec) dsf.get("/Image");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP;excludes=dontkeepme");
        spec.initialize(1, "", options);

        DeleteEntry entry = findEntry(spec, "/Annotation");
        assertTrue(entry.isKeep());
        assertTrue(entry.getSubSpec().overrideKeep());
        
        // And THEN check that the recursive check of KEEP/skip() doesn't
        // go too far
        dsf = specXml.getBean(DeleteSpecFactory.class);
        spec = (BaseDeleteSpec) dsf.get("/Dataset");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP;excludes=keepme");
        spec.initialize(1, "", options);
        entry = findEntry(spec, "/Image");
        assertFalse(entry.isKeep());
        entry = findEntry(spec, "/Annotation");
        assertTrue(entry.isKeep());
        assertFalse(spec.overrideKeep());
        
    }

    @Test(groups = "ticket:2959", expectedExceptions = DeleteException.class)
    public void testDeleteOpsDirectly() throws Exception {
        DeleteOpts opts = new DeleteOpts();
        EventContext ec = createEventContext(false);
        opts.push(Op.FORCE, true, ec);
    }

    /**
     * Tests the logic added to DeleteState to handle SOFT deletes which must
     * rollback previously deleted objects. A stack of maps is kept which
     * get either committed or deleted based on exception handling.
     */
    @Test(groups = "ticket:3032")
    public void testDeleteStateTransactionalIdCounting() throws Exception {

        String savepoint = null;
        Mock sMock = mock(Session.class);
        Session session = (Session) sMock.proxy();

        Mock qMock = mock(Query.class);
        Query q = (Query) qMock.proxy();

        sMock.expects(once()).method("createQuery").will(returnValue(q));
        qMock.expects(once()).method("setParameter");
        qMock.expects(once()).method("list").will(returnValue(Arrays.asList()));

        BaseDeleteSpec spec = new BaseDeleteSpec(Arrays.asList("/Foo"));
        spec.setBeanName("/Foo");
        spec.setExtendedMetadata(specXml.getBean(ExtendedMetadata.class));
        spec.postProcess(specXml);

        final DeleteState ids = new DeleteState(specXml, session, spec);
        assertEquals(0, ids.getTotalFoundCount());

        // Now we try to ignore all the method calls on the session since
        // that is not what we are testing.
        sMock.setDefaultStub(new DefaultResultStub());

        ids.addDeletedIds(step("t", IObject.class, 1));
        assertEquals(1, ids.getDeletedsIds("t").size());
        assertEquals(1, ids.getTotalDeletedCount());

        ids.addDeletedIds(step("a", IObject.class, 2));
        assertEquals(2, ids.getTotalDeletedCount());
        assertEquals(1, ids.getDeletedsIds("a").size());
        assertEquals(1, ids.getDeletedsIds("t").size());

        DeleteStep step = step("x", IObject.class, 3);
        ids.savepoint(step);
        ids.addDeletedIds(step);
        assertEquals(2, ids.getTotalDeletedCount());
        assertEquals(1, ids.getDeletedsIds("x").size());
        assertEquals(0, ids.getDeletedsIds("t").size());
        assertEquals(0, ids.getDeletedsIds("a").size());

        ids.rollback(step);
        assertEquals(2, ids.getTotalDeletedCount());
        assertEquals(0, ids.getDeletedsIds("x").size());
        assertEquals(1, ids.getDeletedsIds("t").size());
        assertEquals(1, ids.getDeletedsIds("a").size());

        step = step("t", IObject.class, 4);
        ids.savepoint(step);
        ids.addDeletedIds(step);
        assertEquals(2, ids.getTotalDeletedCount());
        assertEquals(0, ids.getDeletedsIds("a").size());
        assertEquals(1, ids.getDeletedsIds("t").size());
        ids.release(savepoint);
        assertEquals(3, ids.getTotalDeletedCount());
        assertEquals(1, ids.getDeletedsIds("a").size());
        assertEquals(2, ids.getDeletedsIds("t").size());

    }

    DeleteStep step(String type, Class<IObject> k, long id) {

        BaseDeleteSpec spec = new BaseDeleteSpec("/"+type, "/"+type);
        DeleteStep step = new DeleteStep(0, new LinkedList<DeleteStep>(),
                spec, spec.entries.get(0), Arrays.asList(0L));
        return step;
    }
}
