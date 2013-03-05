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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ChecksumProviderFactoryImplTest {

    private ChecksumProviderFactoryImpl cpf;
    
    @BeforeClass
    protected void setUp() throws Exception {
        this.cpf = new ChecksumProviderFactoryImpl();
    }

    @Test
    public void testGetProviderWithSHA1ChecksumType() {
        ChecksumProvider cp = this.cpf.getProvider(ChecksumType.SHA1);
        Assert.assertEquals(cp.getClass(), SHA1ChecksumProviderImpl.class);
    }

    @Test
    public void testGetProviderWithMD5ChecksumType() {
        ChecksumProvider cp = this.cpf.getProvider(ChecksumType.MD5);
        Assert.assertEquals(cp.getClass(), MD5ChecksumProviderImpl.class);
    }

}
