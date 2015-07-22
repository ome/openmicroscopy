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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.targets.ImportTarget;
import ome.formats.importer.targets.ModelImportTarget;
import ome.formats.importer.targets.TargetBuilder;
import ome.formats.importer.targets.TemplateImportTarget;
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
        t = tb().parse("/a/b/c").build();
        assertTrue(t instanceof TemplateImportTarget);
        t = tb().parse("unknown").build();
        assertTrue(t instanceof TemplateImportTarget);
        t = tb().parse(TestTarget.class.getName()+":stuff").build();
        assertTrue(t instanceof TestTarget);
    }

    @Test
    public void testModelImportTarget() throws Exception {
        TargetBuilder b = new TargetBuilder();
        ModelImportTarget t = (ModelImportTarget) b.parse("Screen:1").build();
        assertEquals(Screen.class, t.getObjectType());
        IObject obj = t.load(null, null);
        assertEquals(1L, obj.getId().getValue());
    }

    @Test
    public void testTemplateRegexes() {
        Pattern p;
        Matcher m;

        p = Pattern.compile("(?<Container1>.*)");
        m = p.matcher("everything");
        assertTrue(m.matches());
        assertEquals("everything", m.group("Container1"));

        p = Pattern.compile("(?<Ignore>/home)/(?<Container1>.*)");
        m = p.matcher("/home/user/MyLab/2015-01-01/");
        assertTrue(m.matches());
        assertEquals("/home", m.group("Ignore"));
        assertEquals("user/MyLab/2015-01-01/", m.group("Container1"));

        // Explicit "ignore"
        p = Pattern.compile("(?<Ignore>(/[^/]+){2})/(?<Container1>.*?)");
        m = p.matcher("/home/user/MyLab/2015-01-01/");
        assertTrue(m.matches());
        assertEquals("/home/user", m.group("Ignore"));
        assertEquals("MyLab/2015-01-01/", m.group("Container1"));

        // Implicit "ignore"
        p = Pattern.compile("^.*user/(?<Container1>.*?)");
        m = p.matcher("/home/user/MyLab/2015-01-01/");
        assertTrue(m.matches());
        try {
            m.group("Ignore"); // Not included
        } catch (IllegalArgumentException iae) {
            // good
        }
        assertEquals("MyLab/2015-01-01/", m.group("Container1"));

        // Group
        p = Pattern.compile("^.*user/(?<Group>[^/]+)/(?<Container1>.*?)");
        m = p.matcher("/home/user/MyLab/2015-01-01/");
        assertTrue(m.matches());
        assertEquals("MyLab", m.group("Group"));
        assertEquals("2015-01-01/", m.group("Container1"));

        // Container2 takes all extra paths
        p = Pattern.compile("^.*user/(?<Container2>([^/]+/)*)(?<Container1>([^/]+/))");
        m = p.matcher("/home/user/MyLab/2015-01-01/foo/");
        assertTrue(m.matches());
        assertEquals("MyLab/2015-01-01/", m.group("Container2"));
        assertEquals("foo/", m.group("Container1"));
        m = p.matcher("/home/user/MyLab/");
        assertTrue(m.matches());
        assertEquals("MyLab/", m.group("Container1"));

        // TODO:
        // Guarantee whether all paths will end in / or not
        // Add a helper for: ([^/]+/)
        // Add a name for IDs CID1, CID2
    }

    // Absolute:
    // --------
    // /tmp/my-data/JRSLab/2015-05-foo/some-dir/a.fake
    // /{-2}/{Group}/{Container}/
    // /{-2}/{Group}/{Container*}/
    // /{-2}/{Group}/{Container+}/
    // /{-2}/{Group}/{Container}/{Container*}
    // /{-2}/{Group}/{Container*}/{Container}

    // Relative:

    String[][] data = new String[][] {
        new String[] {"/{-3}/{Group}/" },
        new String[] {"{/" },
    };

    @Test
    public void testTemplateBuilding() {
        TargetBuilder b = new TargetBuilder();
        b.parse("");
    }
}