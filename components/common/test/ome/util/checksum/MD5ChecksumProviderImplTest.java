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
public class MD5ChecksumProviderImplTest
    extends AbstractChecksumProviderIntegrationTest {

    private static EnumMap<ChecksumTestVector, String> map =
            new EnumMap<ChecksumTestVector, String>(ChecksumTestVector.class);

    @BeforeClass
    public void setUp() {
        map.put(ChecksumTestVector.ABC, "900150983cd24fb0d6963f7d28e17f72");
        map.put(ChecksumTestVector.EMPTYARRAY, "d41d8cd98f00b204e9800998ecf8427e");
        map.put(ChecksumTestVector.SMALLFILE, "b384ecf5b00f018808f7de12b9a25261");
        map.put(ChecksumTestVector.MEDIUMFILE, "29de50b1cecadf8946ac3e5aae2385fe");
        map.put(ChecksumTestVector.BIGFILE, "24c462d241a6576c774b0667cc699691");
    }

    public MD5ChecksumProviderImplTest() {
        super(new MD5ChecksumProviderImpl(), map);
    }

    @AfterMethod
    public void resetChecksum() {
        super.checksumProvider = new MD5ChecksumProviderImpl();
    }
}
