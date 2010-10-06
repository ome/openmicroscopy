/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import static ome.services.delete.DeleteEntry.DEFAULT;
import static ome.services.delete.DeleteOpts.Op.REAP;
import static ome.services.delete.DeleteOpts.Op.SOFT;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.model.annotations.Annotation;
import ome.model.annotations.BasicAnnotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.DoubleAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.ListAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.NumericAnnotation;
import ome.model.annotations.TermAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.annotations.TimestampAnnotation;
import ome.model.annotations.TypeAnnotation;
import ome.model.annotations.XmlAnnotation;
import ome.security.basic.CurrentDetails;
import ome.services.delete.DeleteOpts.Op;
import ome.system.OmeroContext;
import ome.tools.hibernate.ExtendedMetadata;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.DefaultResultStub;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class DeleteSpecUnitTest extends MockObjectTestCase {

    private OmeroContext specXml;

    private Mock emMock;

    @BeforeMethod
    public void setup() {
        StaticApplicationContext sac = new StaticApplicationContext();

        ConstructorArgumentValues cav = new ConstructorArgumentValues();
        cav.addGenericArgumentValue(ExtendedMetadata.class);
        RootBeanDefinition mock = new RootBeanDefinition(Mock.class, cav, null);


        RootBeanDefinition em = new RootBeanDefinition();
        em.setFactoryBeanName("mock");
        em.setFactoryMethodName("proxy");

        RootBeanDefinition cd = new RootBeanDefinition(CurrentDetails.class);

        sac.registerBeanDefinition("currentDetails", cd);
        sac.registerBeanDefinition("mock", mock);
        sac.registerBeanDefinition("extendedMetadata", em);
        sac.refresh();

        emMock = sac.getBean("mock", Mock.class);
        emMock.expects(atLeastOnce())
                .method("getAnnotationTypes")
                .will(returnValue(new HashSet<Class<?>>(Arrays
                        .<Class<?>> asList(Annotation.class,
                                BasicAnnotation.class,
                                BooleanAnnotation.class,
                                NumericAnnotation.class,
                                DoubleAnnotation.class,
                                LongAnnotation.class,
                                TermAnnotation.class,
                                TimestampAnnotation.class,
                                ListAnnotation.class,
                                TextAnnotation.class,
                                CommentAnnotation.class,
                                ome.model.annotations.TagAnnotation.class,
                                XmlAnnotation.class,
                                TypeAnnotation.class,
                                FileAnnotation.class))));
        specXml = new OmeroContext(
                new String[] { "classpath:ome/services/delete/spec.xml" }, sac);
    }

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
        emMock.expects(once()).method("getRelationship")
                .will(returnValue("shapes"));
        emMock.expects(once()).method("getRelationship")
                .will(returnValue("annotationLinks"));
        emMock.expects(once()).method("getRelationship")
                .will(returnValue("parent"));
        emMock.expects(once()).method("getRelationship")
                .will(returnValue("child"));

        DeleteSpec roi = specXml.getBean("/Roi", BaseDeleteSpec.class);
        DeleteIds ids = new DeleteIds(specXml, session, roi);
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
        // Find the right entry for /Annotation
        Integer idx = null;
        DeleteEntry entry = null;
        for (int i = 0; i < spec.entries.size(); i++) {
            entry = spec.entries.get(i);
            if (entry.getName().equals("/Annotation")) {
                idx = i;
                break;
            }
        }
        assertNotNull(idx);
        assertTrue(entry.isKeep());
        assertTrue(entry.getSubSpec().overrideKeep());
        
        // And THEN check that the recursive check of KEEP/skip() doesn't
        // go too far
        dsf = specXml.getBean(DeleteSpecFactory.class);
        spec = (BaseDeleteSpec) dsf.get("/Dataset");
        options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP;excludes=keepme");
        spec.initialize(1, "", options);
        // Find the right entry for /Image
        idx = null;
        for (int i = 0; i < spec.entries.size(); i++) {
            entry = spec.entries.get(i);
            if (entry.getName().equals("/Image")) {
                idx = i;
                break;
            }
        }
        assertNotNull(idx);
        assertFalse(spec.overrideKeep());
        
    }

    private Object getOp(DeleteEntry de) throws Exception {
        Field field = DeleteEntry.class.getDeclaredField("operation");
        field.setAccessible(true);
        return field.get(de);
    }

}
