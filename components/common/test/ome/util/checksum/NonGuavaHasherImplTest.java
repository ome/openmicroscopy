/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.util.checksum;

import java.util.zip.Checksum;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.hash.HashCode;

/**
 * Unit test for the {@link NonGuavaHasherImpl} class.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 5.0
 */
public class NonGuavaHasherImplTest extends MockObjectTestCase {

    private Mock mockChecksum;

    private NonGuavaHasherImpl nonGuavaHasherImpl;

    @BeforeClass
    public void setUp() {
        this.mockChecksum = mock(Checksum.class);
        this.mockChecksum.expects(atLeastOnce()).method("update");
        this.nonGuavaHasherImpl =
                new NonGuavaHasherImpl((Checksum) this.mockChecksum.proxy());
    }

    @Test
    public void testHash() {
        long expected = 4021013999L; //0xefabcdef
        this.mockChecksum.expects(once()).method("getValue")
            .withNoArguments().will(returnValue(expected));
        HashCode actual = this.nonGuavaHasherImpl.hash();
        Assert.assertEquals(actual.toString(), Long.toHexString(expected));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutBooleanShouldThrowUOE() {
        this.nonGuavaHasherImpl.putBoolean(false);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutCharShouldThrowUOE() {
        this.nonGuavaHasherImpl.putChar((char) 5);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutDoubleShouldThrowUOE() {
        this.nonGuavaHasherImpl.putDouble((double) 5.5);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutFloatShouldThrowUOE() {
        this.nonGuavaHasherImpl.putFloat((float) 5.5);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutLongShouldThrowUOE() {
        this.nonGuavaHasherImpl.putLong(5L);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutObjectShouldThrowUOE() {
        this.nonGuavaHasherImpl.putObject(new Object(), null);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutShortShouldThrowUOE() {
        this.nonGuavaHasherImpl.putShort((short) 5);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutStringShouldThrowUOE() {
        this.nonGuavaHasherImpl.putString(null);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPutStringWithCharsetShouldThrowUOE() {
        this.nonGuavaHasherImpl.putString(null, null);
    }

    @Test
    public void testPutByte() {
        Object actual = this.nonGuavaHasherImpl.putByte((byte) 0xf);
        Assert.assertTrue(actual instanceof NonGuavaHasherImpl);
    }

    @Test
    public void testPutBytes() {
        byte[] arg = {0x0, 0x1};
        Object actual = this.nonGuavaHasherImpl.putBytes(arg);
        Assert.assertTrue(actual instanceof NonGuavaHasherImpl);
    }

    @Test
    public void testPutBytesWithOffset() {
        byte[] arg = {0x0, 0x1};
        Object actual = this.nonGuavaHasherImpl.putBytes(arg, 1, 1);
        Assert.assertTrue(actual instanceof NonGuavaHasherImpl);
    }

    @Test
    public void testPutInt() {
        Object actual = this.nonGuavaHasherImpl.putInt(1);
        Assert.assertTrue(actual instanceof NonGuavaHasherImpl);
    }
}
