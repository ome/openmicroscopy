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

import java.nio.ByteBuffer;

import ome.util.Utils;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MD5ChecksumProviderImplTest {

    private MD5ChecksumProviderImpl md5;

    // Using MD5('abc') as test vector value
    private static String TESTVECTOR = "900150983cd24fb0d6963f7d28e17f72";

    @BeforeClass
    protected void setUp() throws Exception {
        this.md5 = new MD5ChecksumProviderImpl();
    }

    @Test
    public void testGetChecksumWithByteArray() {
        String actual = Utils.bytesToHex(this.md5.getChecksum("abc".getBytes()));
        Assert.assertEquals(actual, TESTVECTOR);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetChecksumWithEmptyByteArrayShouldThrowNPE() {
        byte nullArray[] = null;
        this.md5.getChecksum(nullArray);
    }

    @Test
    public void testGetChecksumWithByteBuffer() {
        String actual = Utils.bytesToHex(this.md5.getChecksum(
                ByteBuffer.wrap("abc".getBytes())));
        Assert.assertEquals(actual, TESTVECTOR);
    }

    @Test
    public void testGetChecksumWithEmptyByteBufferReturnsNull() {
        byte[] actual = this.md5.getChecksum(ByteBuffer.allocateDirect(0));
        Assert.assertNull(actual);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetChecksumWithBogusFilePathStringShouldThrowUOE() {
        this.md5.getChecksum("foobar/biz/buz");
    }

}
