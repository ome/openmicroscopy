/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.gateway.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import omero.model.AdminPrivilege;
import omero.model.AdminPrivilegeI;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.enums.AdminPrivilegeChgrp;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.ChecksumAlgorithmCRC32;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Unit tests for converting between OMERO model enumerations and their string values.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.0
 */
@Test(groups = "unit")
public class UtilsTest {
    /**
     * Test converting enumeration instances to their corresponding string values.
     * @throws ReflectiveOperationException unexpected
     */
    @Test
    public void testConvertFrom() throws ReflectiveOperationException {
        final AdminPrivilege chmodInstance = new AdminPrivilegeI();
        chmodInstance.setValue(omero.rtypes.rstring(AdminPrivilegeChgrp.value));
        final ChecksumAlgorithm crcInstance = new ChecksumAlgorithmI();
        crcInstance.setValue(omero.rtypes.rstring(ChecksumAlgorithmCRC32.value));
        final List<String> values = Utils.fromEnum(ImmutableSet.of(chmodInstance, crcInstance));
        Assert.assertEquals(values.size(), 2);
        Assert.assertEquals(values.get(0), AdminPrivilegeChgrp.value);
        Assert.assertEquals(values.get(1), ChecksumAlgorithmCRC32.value);
    }

    /**
     * Test converting string values to their corresponding enumeration instances.
     * @throws ReflectiveOperationException unexpected
     */
    @Test
    public void testConvertTo() throws ReflectiveOperationException {
        final Collection<String> privilegeNames = ImmutableSet.of(AdminPrivilegeChgrp.value, AdminPrivilegeChown.value);
        final List<AdminPrivilege> privileges = Utils.toEnum(AdminPrivilege.class, AdminPrivilegeI.class, privilegeNames);
        Assert.assertEquals(privileges.size(), 2);
        Assert.assertEquals(privileges.get(0).getValue().getValue(), AdminPrivilegeChgrp.value);
        Assert.assertEquals(privileges.get(1).getValue().getValue(), AdminPrivilegeChown.value);
    }

    /**
     * Test exception in converting from non-enumeration instances.
     * @throws ReflectiveOperationException unexpected
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConvertFromBadClass() throws ReflectiveOperationException {
        Utils.fromEnum(Collections.singleton(new ImageI()));
    }

    /**
     * Test exception in converting to non-enumeration instances.
     * @throws ReflectiveOperationException unexpected
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConvertToBadClass() throws ReflectiveOperationException {
        Utils.toEnum(Image.class, ImageI.class, Collections.singleton("image"));
    }
}
