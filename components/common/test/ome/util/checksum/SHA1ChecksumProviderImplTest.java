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

import java.util.EnumMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class SHA1ChecksumProviderImplTest
    extends AbstractChecksumProviderAlgorithmTest {

    private static EnumMap<ChecksumTestVector, String> map =
            new EnumMap<ChecksumTestVector, String>(ChecksumTestVector.class);

    @BeforeClass
    public void setUp() {
        map.put(ChecksumTestVector.ABC, "a9993e364706816aba3e25717850c26c9cd0d89d");
        map.put(ChecksumTestVector.EMPTYARRAY, "da39a3ee5e6b4b0d3255bfef95601890afd80709");
        map.put(ChecksumTestVector.SMALLFILE, "7bfd66e82aefb371d3a57da52346546e9417115a");
        map.put(ChecksumTestVector.MEDIUMFILE, "e0cd96d729092977366c136f115696d1df7946f6");
        map.put(ChecksumTestVector.BIGFILE, "c5362b32b0fbacb6ec4be7bc40b647405a8f73ce");
    }

    public SHA1ChecksumProviderImplTest() {
        super(new SHA1ChecksumProviderImpl(), map);
    }

    @AfterMethod
    public void resetChecksum() {
        super.checksumProvider = new SHA1ChecksumProviderImpl();
    }

}
