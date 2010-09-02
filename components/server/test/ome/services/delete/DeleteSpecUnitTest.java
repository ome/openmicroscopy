/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import static ome.services.delete.DeleteEntry.DEFAULT;
import static ome.services.delete.DeleteEntry.Op.REAP;
import static ome.services.delete.DeleteEntry.Op.SOFT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.services.delete.DeleteEntry.Op;
import ome.tools.hibernate.ExtendedMetadata;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class DeleteSpecUnitTest extends MockObjectTestCase {

    private ClassPathXmlApplicationContext specXml;

    @BeforeMethod
    public void setup() {
        StaticApplicationContext sac = new StaticApplicationContext();

        ConstructorArgumentValues cav = new ConstructorArgumentValues();
        cav.addGenericArgumentValue(ExtendedMetadata.class);
        RootBeanDefinition mock = new RootBeanDefinition(
                Mock.class, cav, null);

        RootBeanDefinition em = new RootBeanDefinition();
        em.setFactoryBeanName("mock");
        em.setFactoryMethodName("proxy");

        sac.registerBeanDefinition("mock", mock);
        sac.registerBeanDefinition("extendedMetadata", em);
        sac.refresh();

        specXml = new ClassPathXmlApplicationContext(
                new String[]{"classpath:ome/services/delete/spec.xml"}, sac);
    }

    /**
     * Test that various entry strings will be properly parsed. These are the
     * values passed in as a list to the {@link DeleteSpec} constructors in
     * spec.xml
     *
     * Here we create a spec of "/Image" which serves as the basis. Any entry
     * also named "/Image" should not count as a subspec. A different path
     * which is also in spec.xml <em>will</em> count as a subspec, and finally,
     * if the value is neither the name of the top spec ("self") or of a value
     * in spec.xml, then again, it does <em>not</em> get a subspec.
     */
    @Test
    public void testDeleteEntry() throws Exception {

        DeleteSpec spec = new BaseDeleteSpec(null, "/Image", "/Image", "/Image/Roi");
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
            Op op, boolean hasSubSpec) {
        DeleteEntry de = new DeleteEntry(spec, string);
        de.postProcess(specXml.getBeansOfType(DeleteSpec.class));
        assertEquals(name, de.name);
        assertEquals(op, de.op);
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
        DeleteSpec roi = specXml
                .getBean("/Roi", BaseDeleteSpec.class);
        Mock mock = mock(Session.class);
        Session session = (Session) mock.proxy();
        DeleteIds ids = new DeleteIds(session, roi);
        roi.initialize(1, null, null);
        // roi.delete(session, 0, ids); // Requires mock setup
    }

    /**
     * Test that iterator returns all specs.
     */
    @Test
    public void testSubSpecIterator() throws Exception {
        Map<String, DeleteSpec> specs = specXml.getBeansOfType(DeleteSpec.class);
        DeleteSpec image = specs.get("/Image");
        image.postProcess(specs);
        image.initialize(1, null, null);

        Iterator<DeleteSpec> it = image.walk();
        List<DeleteSpec> expected = new ArrayList<DeleteSpec>();
        expected.add(specs.get("/Roi"));
        expected.add(specs.get("/Image/Pixels/RenderingDef"));
        expected.add(specs.get("/Image/Pixels/Channel"));
        expected.add(image);
        while (it.hasNext()) {
            DeleteSpec want = expected.remove(0);
            DeleteSpec found = it.next();
            assertEquals(want, found);
        }
        assertEquals(expected.toString(), 0, expected.size());
    }
}
