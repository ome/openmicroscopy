/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

/**
 * The test vector for the file size checksum algorithm has the input data sizes.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.3
 */
@Test
public class FileSizeChecksumProviderImplTest 
    extends AbstractChecksumProviderAlgorithmTest {

    private static EnumMap<ChecksumTestVector, String> map =
            new EnumMap<ChecksumTestVector, String>(ChecksumTestVector.class);

    @BeforeClass
    public void setUp() {
        map.put(ChecksumTestVector.ABC, "0300000000000000");
        map.put(ChecksumTestVector.EMPTYARRAY, "0000000000000000");
        map.put(ChecksumTestVector.SMALLFILE, "5e20000000000000");
        map.put(ChecksumTestVector.MEDIUMFILE, "c075000000000000");
        map.put(ChecksumTestVector.BIGFILE, "3600040000000000");
    }

    public FileSizeChecksumProviderImplTest() {
        super(new FileSizeChecksumProviderImpl(), map);
    }

    @AfterMethod
    public void resetChecksum() {
        super.checksumProvider = new FileSizeChecksumProviderImpl();
    }
}
