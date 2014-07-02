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
public class Murmur128ChecksumProviderImplTest
    extends AbstractChecksumProviderAlgorithmTest {

    private static EnumMap<ChecksumTestVector, String> map =
            new EnumMap<ChecksumTestVector, String>(ChecksumTestVector.class);

    @BeforeClass
    public void setUp() {
        map.put(ChecksumTestVector.ABC, "6778ad3f3f3f96b4522dca264174a23b");
        map.put(ChecksumTestVector.EMPTYARRAY, "00000000000000000000000000000000");
        map.put(ChecksumTestVector.SMALLFILE, "d575938aa70a6f42c8a87ed3f108abaa");
        map.put(ChecksumTestVector.MEDIUMFILE, "e0255441f8eb76a783a9efadd9589fdc");
        map.put(ChecksumTestVector.BIGFILE, "6b9a89bf3ad4af7405acffff6501dbf7");
    }

    public Murmur128ChecksumProviderImplTest() {
        super(new Murmur128ChecksumProviderImpl(), map);
    }

    @AfterMethod
    public void resetChecksum() {
        super.checksumProvider = new Murmur128ChecksumProviderImpl();
    }
}
