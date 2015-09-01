/*
 * ome.util.mem.TestCopiableArray
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

import org.testng.annotations.*;
import junit.framework.TestCase;

/**
 * Unit test for {@link CopiableArray}.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class TestCopiableArray extends TestCase {

    private int SIZE = 10000;

    private SimpleCopiableArray copiableArray;

    private MockBody element; // Mock to play the element role.

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() {
        copiableArray = new SimpleCopiableArray(SIZE);
        element = new MockBody();
    }

    @Test
    public void testMakeNew() {
        try {
            new SimpleCopiableArray(0);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
    }

    @Test
    public void testSize() {
        assertEquals("Should setthe size to argument passed to constructor.",
                SIZE, copiableArray.getSize(), 0);

    }

    @Test
    public void testSet() {
        try {
            copiableArray.set(null, SIZE);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.set(null, -1);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
    }

    @Test
    public void testGet() {
        try {
            copiableArray.get(SIZE);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.get(-1);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
    }

    @Test
    public void testSetAndGet() {
        for (int i = 0; i < copiableArray.getSize(); i++) {
            copiableArray.set(element, i);
        }
        for (int i = 0; i < copiableArray.getSize(); i++) {
            assertEquals("Wrong Copiable", element, copiableArray.get(i));
            assertNotNull(copiableArray.get(i));
        }
    }

    @Test
    public void testSetAndGet1() {
        SimpleCopiableArray array = new SimpleCopiableArray(copiableArray
                .getSize());
        MockBody mb;
        for (int i = 0; i < copiableArray.getSize(); i++) {
            mb = new MockBody();
            copiableArray.set(mb, i);
            array.set(mb, i);
        }
        for (int i = 0; i < copiableArray.getSize(); i++) {
            assertSame("Wrong Copiable", array.get(i), copiableArray.get(i));
        }
    }

    @Test
    public void testCopy() {
        try {
            copiableArray.copy(-1, 0);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.copy(SIZE, SIZE);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.copy(0, SIZE);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.copy(1, 0);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
    }

    @Test
    public void testCopy1() {
        SimpleCopiableArray array = new SimpleCopiableArray(2);
        array.set(element, 0);
        assertEquals("Wrong Copiable", element, array.get(0));
        assertNull("Element should be null", array.get(1));
        element.copy(element);
        element.activate();
        array.copy(0, 1);
        element.verify();
        assertNotNull("Element shouldn't be null", array.get(1));
        assertEquals("Wrong Copiable", element, array.get(1));
    }

    @Test
    public void testCopyNull() {
        SimpleCopiableArray array = new SimpleCopiableArray(2);
        assertNull("Element should be null", array.get(0));
        assertNull("Element should be null", array.get(1));
        array.copy(0, 1);
        assertNull("Element should be null", array.get(1));
    }

}
