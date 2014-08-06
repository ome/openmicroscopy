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
import java.util.EnumMap;

import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Abstract base class for unit test classes extending
 * {@link AbstractChecksumProvider}. This class and {@link ChecksumTestVector}
 * should be updated with relevant test vectors and tests whenever a new
 * checksum algorithm implementation is added. This class should be ignored by
 * TestNG. The design has been inspired by m.t.b.carroll at dundee.ac.uk.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public abstract class AbstractChecksumProviderAlgorithmTest {

    ChecksumProvider checksumProvider;

    private File smallFile, mediumFile, bigFile;

    private EnumMap<ChecksumTestVector, String> checksumValues;

    public AbstractChecksumProviderAlgorithmTest(ChecksumProvider cp,
            EnumMap<ChecksumTestVector, String> checksumValues) {
        try {
            this.smallFile = ResourceUtils.getFile("classpath:test.jpg");
            this.mediumFile = ResourceUtils.getFile("classpath:tinyTest.d3d.dv");
            this.bigFile = ResourceUtils.getFile("classpath:test.bmp");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("IOException during test set up.");
        }
        this.checksumProvider = cp;
        this.checksumValues = checksumValues;
    }

    @Test
    public void testChecksumAsStringWithByteArray() {
        String actual = this.checksumProvider
                .putBytes("abc".getBytes())
                .checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.ABC));
    }

    @Test
    public void testChecksumAsStringWithPartOfByteArrayZeroOffset() {
        String actual = this.checksumProvider
                .putBytes("abcdef".getBytes(), 0, 3)
                .checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.ABC));
    }

    @Test
    public void testChecksumAsStringWithPartOfByteArrayNonZeroOffset() {
        String actual = this.checksumProvider
                .putBytes("defabc".getBytes(), 3, 3)
                .checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.ABC));
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testChecksumAsStringWithPartOfByteArrayShouldThrowIOOB() {
        this.checksumProvider.putBytes("abcdef".getBytes(), -1, -1);
    }


    @Test(expectedExceptions = NullPointerException.class)
    public void testPutBytesWithNullByteArrayShouldThrowNPE() {
        byte nullArray[] = null;
        this.checksumProvider.putBytes(nullArray);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutBytesWithChunkOfNullByteArrayShouldThrowNPE() {
        byte nullArray[] = null;
        this.checksumProvider.putBytes(nullArray, 123, 456);
    }

    @Test
    public void testChecksumAsStringWithEmptyByteArray() {
        String actual = this.checksumProvider
                .putBytes("".getBytes())
                .checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.EMPTYARRAY));
    }

    @Test
    public void testChecksumAsStringWithByteBuffer() {
        String actual = this.checksumProvider
                .putBytes(ByteBuffer.wrap("abc".getBytes()))
                .checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.ABC));
    }

    @Test
    public void testChecksumAsStringWithTruncatedByteBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap("abcdef".getBytes());
        buffer.position(0).limit(3);
        String actual = this.checksumProvider
                .putBytes(buffer)
                .checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.ABC));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testChecksumAsBytesWithEmptyByteBufferShouldThrowIAE() {
        byte[] actual = this.checksumProvider
                .putBytes(ByteBuffer.allocateDirect(0))
                .checksumAsBytes();
        Assert.assertNull(actual);
    }

    @Test
    public void testChecksumAsStringWithSmallFilePathString() {
        String actual = this.checksumProvider
                .putFile(this.smallFile.getAbsolutePath())
                .checksumAsString();

        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.SMALLFILE));
    }

    @Test
    public void testChecksumAsStringWithMediumFilePathString() {
        String actual = this.checksumProvider
                .putFile(this.mediumFile.getAbsolutePath())
                .checksumAsString();

        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.MEDIUMFILE));
    }

    @Test
    public void testChecksumAsStringWithBigFilePathString() {
        String actual = this.checksumProvider
                .putFile(this.bigFile.getAbsolutePath())
                .checksumAsString();

        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.BIGFILE));
    }

    @Test
    public void testChecksumAsStringWithEmptyObject() {
        String actual = this.checksumProvider.checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.EMPTYARRAY));
    }

    @Test
    public void testChecksumAsStringWithSequentialPutBytes() {
        String actual = this.checksumProvider
                .putBytes("a".getBytes())
                .putBytes("bc".getBytes())
                .checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.ABC));
    }

    @Test
    public void testChecksumAsStringWithMixedPutBytes() {
        String actual = this.checksumProvider
                .putBytes("abc".getBytes())
                .putFile(this.smallFile.getAbsolutePath())
                .checksumAsString();
        Assert.assertEquals(actual, this.checksumValues
                .get(ChecksumTestVector.SMALLFILE));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testPutBytesAfterChecksumAsString() {
        this.checksumProvider
            .putBytes("abc".getBytes())
            .checksumAsString();
        this.checksumProvider.putBytes("abc".getBytes());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testPutBytesAfterChecksumAsBytes() {
        this.checksumProvider
            .putBytes("abc".getBytes())
            .checksumAsBytes();
        this.checksumProvider.putBytes("abc".getBytes());
    }
}
