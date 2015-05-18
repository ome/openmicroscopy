/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

package ome.formats.utests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.targets.AbspathImportTarget;
import ome.formats.importer.targets.ImportTarget;
import ome.formats.importer.targets.ModelImportTarget;
import ome.formats.importer.targets.RelpathImportTarget;
import ome.formats.importer.targets.TargetBuilder;
import ome.model.units.BigResult;
import omero.model.IObject;
import omero.model.Screen;

import org.testng.annotations.Test;

public class TargetsTest {

    public static class TestTarget implements ImportTarget {

        @Override
        public void init(String target) {
        }

        @Override
        public IObject load(OMEROMetadataStoreClient client, ImportContainer ic) {
            return null;
        }

    }

    TargetBuilder tb() {
        return new TargetBuilder();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testResistReuse() {
        tb().parse("Screen:1").parse("Dataset:1");
    }

    @Test
    public void testBuilder() throws BigResult {
        ImportTarget t;
        t = tb().parse("Screen:1").build();
        assertTrue(t instanceof ModelImportTarget);
        t = tb().parse("Dataset:1").build();
        assertTrue(t instanceof ModelImportTarget);
        t = tb().parse("abspath:/a/b/c").build();
        assertTrue(t instanceof AbspathImportTarget);
        t = tb().parse("relpath:/a/b/c").build();
        assertTrue(t instanceof RelpathImportTarget);
        t = tb().parse("/a/b/c").build();
        assertTrue(t instanceof RelpathImportTarget);
        t = tb().parse("unknown").build();
        assertTrue(t instanceof RelpathImportTarget);
        t = tb().parse(TestTarget.class.getName()+":stuff").build();
        assertTrue(t instanceof TestTarget);
    }

    @Test
    public void testModelImportTarget() throws BigResult {
        TargetBuilder b = new TargetBuilder();
        ModelImportTarget t = (ModelImportTarget) b.parse("Screen:1").build();
        assertEquals(Screen.class, t.getObjectType());
        assertEquals(Long.valueOf(1L), t.getObjectId());
    }

}