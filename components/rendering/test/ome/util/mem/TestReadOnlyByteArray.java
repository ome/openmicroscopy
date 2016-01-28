/*
 * ome.util.mem.TestReadOnlyByteArray
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

import org.testng.annotations.*;
import junit.framework.TestCase;

/**
 * Tests the normal operation of <code>ReadOnlyByteArray</code> and possible
 * exceptions.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class TestReadOnlyByteArray extends TestCase {

    @Test
    public void testReadOnlyByteArray() {
        try {
            new ReadOnlyByteArray(null, 0, 0);
            fail("Shouldn't accept null base.");
        } catch (NullPointerException npe) {
        }

        byte[] base = new byte[0];
        try {
            new ReadOnlyByteArray(base, -1, 0);
            fail("Shouldn't accept negative offset.");
        } catch (IllegalArgumentException iae) {
        }
        try {
            new ReadOnlyByteArray(base, 0, -1);
            fail("Shouldn't accept negative length.");
        } catch (IllegalArgumentException iae) {
        }
        try {
            new ReadOnlyByteArray(base, 1, 0);
            fail("Shouldn't accept inconsistent [offset, offset+length].");
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testGetEmptyArray() {
        byte[] base = new byte[] { 0, 1, 2 };
        ReadOnlyByteArray roba = new ReadOnlyByteArray(base, 1, 0);
        try {
            roba.get(0);
            fail("Shouldn't accept index 0 if length is 0.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        try {
            roba.get(-1);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        try {
            roba.get(1);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
    }

    @Test
    public void testGet1LengthArray() {
        byte[] base = new byte[] { 0, 1 };
        ReadOnlyByteArray roba = new ReadOnlyByteArray(base, 1, 1);
        assertEquals("Calculated wrong base offset.", 1, roba.get(0));
        try {
            roba.get(-1);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        try {
            roba.get(1);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
    }

    @Test
    public void testGet2LengthArray() {
        byte[] base = new byte[] { 0, 1, 2, 3, 4 };
        ReadOnlyByteArray roba = new ReadOnlyByteArray(base, 2, 2);
        assertEquals("Calculated wrong base offset.", 2, roba.get(0));
        assertEquals("Calculated wrong base offset.", 3, roba.get(1));
        try {
            roba.get(-1);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        try {
            roba.get(2);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
    }

    @Test
    public void testGet3LengthArray() {
        byte[] base = new byte[] { 0, 1, 2 };
        ReadOnlyByteArray roba = new ReadOnlyByteArray(base, 0, 3);
        assertEquals("Calculated wrong base offset.", 0, roba.get(0));
        assertEquals("Calculated wrong base offset.", 1, roba.get(1));
        assertEquals("Calculated wrong base offset.", 2, roba.get(2));
        try {
            roba.get(-1);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        try {
            roba.get(3);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
    }

}
