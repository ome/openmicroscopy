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

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import ome.util.Utils;

import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SHA1ChecksumProviderImplTest {

    private SHA1ChecksumProviderImpl sha1;
    private File smallFile, mediumFile, bigFile;

    // SHA1('abc')
    private static String ABC_SHA1 = "a9993e364706816aba3e25717850c26c9cd0d89d";

    // SHA1('')
    private static String EMPTYARRAY_SHA1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709";

    // file < 8192 Bytes
    private static String SMALLFILE_SHA1 = "89336a1baf365cf51b67105019beca71b858c227";

    // file == 8192 Bytes
    private static String MEDIUMFILE_SHA1 = "733982976662e28c682650f1066d62071cd7538a";

    // file > 8192 Bytes
    private static String BIGFILE_SHA1 = "c5362b32b0fbacb6ec4be7bc40b647405a8f73ce";

    @BeforeClass
    protected void setUp() throws Exception {
        try {
            this.smallFile = ResourceUtils.getFile("classpath:cruisecontrol-test.txt");
            this.mediumFile = ResourceUtils.getFile("classpath:test.txt");
            this.bigFile = ResourceUtils.getFile("classpath:test.bmp");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("IOException during test set up.");
        }
        this.sha1 = new SHA1ChecksumProviderImpl();
    }

    @Test
    public void testGetChecksumWithByteArray() {
        String actual = Utils.bytesToHex(this.sha1.getChecksum("abc".getBytes()));
        Assert.assertEquals(actual, ABC_SHA1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetChecksumWithNullByteArrayShouldThrowNPE() {
        byte nullArray[] = null;
        this.sha1.getChecksum(nullArray);
    }

    @Test
    public void testGetChecksumWithEmptyByteArray() {
        String actual = Utils.bytesToHex(this.sha1.getChecksum("".getBytes()));
        Assert.assertEquals(actual, EMPTYARRAY_SHA1);
    }

    @Test
    public void testGetChecksumWithSmallFilePathString() {
        String actual = Utils.bytesToHex(this.sha1
                .getChecksum(this.smallFile.getAbsolutePath()));

        Assert.assertEquals(actual, SMALLFILE_SHA1);
    }

    @Test
    public void testGetChecksumWithMediumFilePathString() {
        String actual = Utils.bytesToHex(this.sha1
                .getChecksum(this.mediumFile.getAbsolutePath()));

        Assert.assertEquals(actual, MEDIUMFILE_SHA1);
    }

    @Test
    public void testGetChecksumWithBigFilePathString() {
        String actual = Utils.bytesToHex(this.sha1
                .getChecksum(this.bigFile.getAbsolutePath()));

        Assert.assertEquals(actual, BIGFILE_SHA1);
    }
    
}
