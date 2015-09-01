/*
 * ome.util.mem.TestByteArray
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

import java.io.IOException;

import ome.util.tests.common.MockInputStream;

import org.testng.annotations.*;

import junit.framework.TestCase;

/**
 * Tests the normal operation of <code>ByteArray</code> and possible
 * exceptions.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
public class TestByteArray extends TestCase {

    private void doTestSetBoundaryCases(ByteArray target) {
        byte value = (byte) 25;
        if (target.length == 0) {
            try {
                target.set(0, value);
                fail("Shouldn't accept index 0 if length is 0.");
            } catch (ArrayIndexOutOfBoundsException aiobe) {
            }
        }
        try {
            target.set(-1, value);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        try {
            target.set(target.length, value);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
    }

    private void doTestSetBufferBoundaryCases(ByteArray target) {
        try {
            target.set(0, null); // Null always checked b/f index.
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
        }
        target.set(0, new byte[] {}); // buf.len == 0 always checked b/f
                                        // index.

        byte[] values = new byte[] { 25, -1 };
        if (target.length == 0) {
            try {
                target.set(0, values);
                fail("Shouldn't accept index 0 if length is 0.");
            } catch (ArrayIndexOutOfBoundsException aiobe) {
            }
        }
        try {
            target.set(-1, values);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        try {
            target.set(target.length, values);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        if (0 < target.length) {
            byte original = target.get(target.length - 1);
            values[0] = (byte) (original + 1);
            try {
                target.set(target.length - 1, values);
                fail("Shouldn't accept a buffer that doesn't fit "
                        + "into the slice.");
            } catch (ArrayIndexOutOfBoundsException aiobe) {
                // Ok, but check that no value has been copied:
                assertEquals("No value should be copied if the buffer "
                        + "doesn't fit into the slice.", original, target
                        .get(target.length - 1));
            }
        }
    }

    private void doTestSetStreamBoundaryCases(ByteArray target)
            throws IOException {
        MockInputStream stream = new MockInputStream();
        try {
            target.set(0, 0, null); // Null always checked b/f int args.
            fail("Shouldn't accept null.");
        } catch (NullPointerException npe) {
        }
        assertEquals("Should do nothing and return 0 if maxLength <= 0.", 0,
                target.set(0, 0, stream));
        assertEquals("Should do nothing and return 0 if maxLength <= 0.", 0,
                target.set(0, -1, stream));
        if (target.length == 0) {
            try {
                target.set(0, 1, stream);
                fail("Shouldn't accept index 0 if length is 0.");
            } catch (ArrayIndexOutOfBoundsException aiobe) {
            }
        }
        try {
            target.set(-1, 1, stream);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
        try {
            target.set(target.length, 1, stream);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {
        }
    }

    private void doTestSetStreamEndOfStream(ByteArray target, byte[] base,
            int offset) throws IOException {
        // Create mock, set up expected calls, and transition to
        // verification mode.
        MockInputStream stream = new MockInputStream();
        stream.read(base, offset, 1, -1, null);
        stream.activate();

        // Test.
        assertEquals("Failed to signal end of stream.", -1, target.set(0, 1,
                stream));

        // Make sure all expected calls were performed.
        stream.verify();
    }

    // NB: This method assumes
    // base == target.base
    // offset == target.offset
    // values.length == target.length
    private void doTestSet(byte[] base, int offset, ByteArray target,
            byte[] values) {
        for (int i = 0; i < values.length; ++i) {
            target.set(i, values[i]);
            assertEquals("Set wrong value [index = " + i + "].", values[i],
                    target.get(i));
            assertEquals("Didn't set value in original base array "
                    + "[index = " + i + "].", values[i], base[offset + i]);
        }
    }

    // NB: This method assumes
    // base == target.base
    // offset == target.offset
    // offset+index+values.length <= target.length
    private void doTestSetBuffer(byte[] base, int offset, int index,
            ByteArray target, byte[] values) {
        target.set(index, values);
        for (int i = 0; i < values.length; ++i) {
            assertEquals("Set wrong value [index = " + i + "].", values[i],
                    target.get(index + i));
            assertEquals("Didn't set value in original base array "
                    + "[index = " + i + "].", values[i], base[offset + index
                    + i]);
        }
    }

    // NB: This method assumes
    // base == target.base
    // offset == target.offset
    // offset+index+values.length <= target.length
    // valuesToCheck <= values.length
    private void verifySetStream(byte[] base, int offset, int index,
            ByteArray target, int valuesToCheck) {
        for (int i = 0; i < valuesToCheck; ++i) {
            assertEquals("Set wrong value [index = " + i + "].", 1, target
                    .get(index + i));
            assertEquals("Didn't set value in original base array "
                    + "[index = " + i + "].", 1, base[offset + index + i]);
        }
        // NOTE: expected value is 1 b/c mock read writes 1 into the buffer.
    }

    @Test
    public void testByteArray() {
        try {
            new ByteArray(null, 0, 0);
            fail("Shouldn't accept null base.");
        } catch (NullPointerException npe) {
        }

        byte[] base = new byte[0];
        try {
            new ByteArray(base, -1, 0);
            fail("Shouldn't accept negative offset.");
        } catch (IllegalArgumentException iae) {
        }
        try {
            new ByteArray(base, 0, -1);
            fail("Shouldn't accept negative length.");
        } catch (IllegalArgumentException iae) {
        }
        try {
            new ByteArray(base, 1, 0);
            fail("Shouldn't accept inconsistent [offset, offset+length].");
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testSetWhenEmptySlice() {
        byte[] base = new byte[] { 0 };
        ByteArray target = new ByteArray(base, 1, 0);
        doTestSetBoundaryCases(target);
    }

    @Test
    public void testSetBufferWhenEmptySlice() {
        byte[] base = new byte[] { 0 };
        ByteArray target = new ByteArray(base, 1, 0);
        doTestSetBufferBoundaryCases(target);
    }

    @Test
    public void testSetStreamWhenEmptySlice() throws IOException {
        byte[] base = new byte[] { 0 };
        ByteArray target = new ByteArray(base, 1, 0);
        doTestSetStreamBoundaryCases(target);
    }

    @Test
    public void testSetWhen1LengthSlice() {
        byte[] values = new byte[] { 25 };
        byte[] base = new byte[] { 0, 1 };
        ByteArray target = new ByteArray(base, 1, 1);
        doTestSetBoundaryCases(target);
        doTestSet(base, 1, target, values);
    }

    @Test
    public void testSetBufferWhen1LengthSlice() {
        byte[] values = new byte[] { 25 };
        byte[] base = new byte[] { 0, 1 };
        ByteArray target = new ByteArray(base, 1, 1);
        doTestSetBufferBoundaryCases(target);
        doTestSetBuffer(base, 1, 0, target, values);
    }

    @Test
    public void testSetStreamWhen1LengthSlice() throws IOException {
        // Create mock, set up expected calls, and transition to
        // verification mode.
        byte[] base = new byte[] { 0, 0 };
        MockInputStream stream = new MockInputStream();
        stream.read(base, 1, 1, 1, null); // Will set base[1]=1.
        stream.activate();

        // Test.
        ByteArray target = new ByteArray(base, 1, 1);
        doTestSetStreamBoundaryCases(target);
        doTestSetStreamEndOfStream(target, base, 1);
        assertEquals("Should return the bytes written.", 1, target.set(0, 1,
                stream));
        verifySetStream(base, 1, 0, target, 1);

        // Make sure all expected calls were performed.
        stream.verify();
    }

    @Test
    public void testSetWhen2LengthSlice() {
        byte[] values = new byte[] { 25, -1 };
        byte[] base = new byte[] { 0, 1 };
        ByteArray target = new ByteArray(base, 0, 2);
        doTestSetBoundaryCases(target);
        doTestSet(base, 0, target, values);
    }

    @Test
    public void testSetBufferWhen2LengthSlice() {
        byte[] values = new byte[] { 25, -1 };
        byte[] base = new byte[] { 0, 1 };
        ByteArray target = new ByteArray(base, 0, 2);
        doTestSetBufferBoundaryCases(target);
        doTestSetBuffer(base, 0, 0, target, values);
    }

    @Test
    public void testSetStreamWhen2LengthSlice() throws IOException {
        // Create mock, set up expected calls, and transition to
        // verification mode.
        byte[] base = new byte[] { 0, 0 };
        MockInputStream stream = new MockInputStream();
        stream.read(base, 0, 2, 2, null); // Will set base[0,1]=1.
        stream.activate();

        // Test.
        ByteArray target = new ByteArray(base, 0, 2);
        doTestSetStreamBoundaryCases(target);
        doTestSetStreamEndOfStream(target, base, 0);
        assertEquals("Should return the bytes written.", 2, target.set(0, 3,
                stream)); // maxLen should be set to 2.
        verifySetStream(base, 0, 0, target, 2);

        // Make sure all expected calls were performed.
        stream.verify();
    }

    @Test
    public void testSetWhen3LengthSlice() {
        byte[] values = new byte[] { 25, -1, 7 };
        byte[] base = new byte[] { 0, 1, 2, 3 };
        ByteArray target = new ByteArray(base, 1, 3);
        doTestSetBoundaryCases(target);
        doTestSet(base, 1, target, values);
    }

    @Test
    public void testSetBufferWhen3LengthSlice() {
        byte[] values = new byte[] { 25, -1, 7 };
        byte[] base = new byte[] { 0, 1, 2, 3 };
        ByteArray target = new ByteArray(base, 1, 3);
        doTestSetBufferBoundaryCases(target);
        doTestSetBuffer(base, 1, 0, target, values);
    }

    @Test
    public void testSetStreamWhen3LengthSlice() throws IOException {
        // Create mock, set up expected calls, and transition to
        // verification mode.
        byte[] base = new byte[] { 0, 0, 0, 0 };
        MockInputStream stream = new MockInputStream();
        stream.read(base, 2, 2, 1, null); // Will set base[2]=1.
        stream.activate();

        // Test.
        ByteArray target = new ByteArray(base, 1, 3);
        doTestSetStreamBoundaryCases(target);
        doTestSetStreamEndOfStream(target, base, 1);
        assertEquals("Should return the bytes written.", 1, target.set(1, 2,
                stream)); // EOS reached b/f maxLen.
        verifySetStream(base, 1, 1, target, 1);

        // Make sure all expected calls were performed.
        stream.verify();
    }

}
