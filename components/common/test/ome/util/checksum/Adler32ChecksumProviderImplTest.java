/*
 * Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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

import java.util.EnumMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class Adler32ChecksumProviderImplTest 
    extends AbstractChecksumProviderAlgorithmTest {

    private static EnumMap<ChecksumTestVector, String> map =
            new EnumMap<ChecksumTestVector, String>(ChecksumTestVector.class);

    @BeforeClass
    public void setUp() {
        map.put(ChecksumTestVector.ABC, "27014d02");
        map.put(ChecksumTestVector.EMPTYARRAY, "01000000");
        map.put(ChecksumTestVector.SMALLFILE, "a074942c");
        map.put(ChecksumTestVector.MEDIUMFILE, "5798ecc1");
        map.put(ChecksumTestVector.BIGFILE, "73b31b3b");
    }

    public Adler32ChecksumProviderImplTest() {
        super(new Adler32ChecksumProviderImpl(), map);
    }

    @AfterMethod
    public void resetChecksum() {
        super.checksumProvider = new Adler32ChecksumProviderImpl();
    }
}
