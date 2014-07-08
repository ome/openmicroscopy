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

/**
 * Unit test for the {@link NonGuavaHashFunctionImpl} class.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 5.0
 */
public class NonGuavaHashFunctionImplTest extends MockObjectTestCase {

    private Mock mockChecksum;

    private NonGuavaHashFunctionImpl nonGuavaHashFunctionImpl;

    @BeforeClass
    protected void setUp() throws Exception {
        this.mockChecksum = mock(Checksum.class);
        this.mockChecksum.expects(atLeastOnce()).method("reset");
        this.nonGuavaHashFunctionImpl =
                new NonGuavaHashFunctionImpl((Checksum) this.mockChecksum.proxy());
    }

    @Test
    public void testBits() {
        int expected = 1;
        int actual = this.nonGuavaHashFunctionImpl.bits();
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testNewHasher() {
        Object actual = this.nonGuavaHashFunctionImpl.newHasher();
        Assert.assertTrue(actual instanceof NonGuavaHasherImpl);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testNewHasherWithInputSizeShouldThrowUOE() {
        this.nonGuavaHashFunctionImpl.newHasher(5);
    }

}
