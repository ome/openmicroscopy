/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import static ome.services.delete.DeleteEntry.DEFAULT;
import static ome.services.delete.DeleteEntry.Op.REAP;
import static ome.services.delete.DeleteEntry.Op.SOFT;

import java.util.Map;

import ome.services.delete.DeleteEntry.Op;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class DeleteSpecUnitTest extends MockObjectTestCase {

    private ClassPathXmlApplicationContext specXml;

    @BeforeClass
    public void setup() {
        specXml = new ClassPathXmlApplicationContext(
                "classpath:ome/services/delete/spec.xml");
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

        DeleteSpec spec = new BaseDeleteSpec("/Image", "/Image", "/Image/Roi");
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
        assertEquals(hasSubSpec, de.getSubSpec() != null);
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
        BaseDeleteSpec roi = specXml
                .getBean("/Image/Roi", BaseDeleteSpec.class);
        Mock mock = mock(Session.class);
        Session session = (Session) mock.proxy();
        roi.initialize(1, null);
        roi.delete(session, 0);
    }
}
