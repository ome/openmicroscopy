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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

/**
 * A basic set of pure unit tests for {@link AbstractChecksumProvider}. Makes
 * heavy use of old jMock (1.x).
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public class AbstractChecksumProviderTest extends MockObjectTestCase {

    private Mock mockHashFunction, mockHasher;

    private AbstractChecksumProvider abstractChecksumProvider;

    @BeforeClass
    public void setUp() throws Exception {
        this.mockHashFunction = mock(HashFunction.class);
        this.mockHasher = mock(Hasher.class);
    }

    @Test
    public void testAbstractChecksumProviderCtor() {
        this.mockHashFunction.expects(once()).method("newHasher")
            .withNoArguments()
            .will(returnValue(this.mockHasher.proxy()));
        this.abstractChecksumProvider = new AbstractChecksumProvider(
                (HashFunction) mockHashFunction.proxy());
    }

    @Test
    public void testPutBytesWithByteArray() {
        this.mockHasher.expects(once()).method("putBytes");
        Object actual = this.abstractChecksumProvider.putBytes("abc".getBytes());
        Assert.assertTrue(actual instanceof AbstractChecksumProvider);
    }

    @Test
    public void testPutBytesWithByteBuffer() {
        this.mockHasher.expects(once()).method("putBytes");
        Object actual = this.abstractChecksumProvider.putBytes(
                ByteBuffer.wrap("abc".getBytes()));
        Assert.assertTrue(actual instanceof AbstractChecksumProvider);
    }

}
