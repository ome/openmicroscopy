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

import org.testng.Assert;
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
        Assert.assertTrue(t instanceof ModelImportTarget);
        t = tb().parse("Dataset:1").build();
        Assert.assertTrue(t instanceof ModelImportTarget);
        t = tb().parse("/a/b/c").build();
        Assert.assertTrue(t instanceof TemplateImportTarget);
        t = tb().parse("unknown").build();
        Assert.assertTrue(t instanceof TemplateImportTarget);
        t = tb().parse(TestTarget.class.getName()+":stuff").build();
        Assert.assertTrue(t instanceof TestTarget);
    }

    @Test
    public void testModelImportTarget() throws Exception {
        TargetBuilder b = new TargetBuilder();
        ModelImportTarget t = (ModelImportTarget) b.parse("Screen:1").build();
        Assert.assertEquals(t.getObjectType(), Screen.class);
        IObject obj = t.load(null, null);
        Assert.assertEquals(obj.getId().getValue(), 1L);
    }

    @Test
    public void testTemplateRegexes() {
        Pattern p;
        Matcher m;

        p = Pattern.compile("(?<Container1>.*)");
        m = p.matcher("everything");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group("Container1"), "everything");

        p = Pattern.compile("(?<Ignore>/home)/(?<Container1>.*)");
        m = p.matcher("/home/user/MyLab/2015-01-01/");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group("Ignore"), "/home");
        Assert.assertEquals(m.group("Container1"), "user/MyLab/2015-01-01/");

        // Explicit "ignore"
        p = Pattern.compile("(?<Ignore>(/[^/]+){2})/(?<Container1>.*?)");
        m = p.matcher("/home/user/MyLab/2015-01-01/");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group("Ignore"), "/home/user");
        Assert.assertEquals(m.group("Container1"), "MyLab/2015-01-01/");

        // Implicit "ignore"
        p = Pattern.compile("^.*user/(?<Container1>.*?)");
        m = p.matcher("/home/user/MyLab/2015-01-01/");
        Assert.assertTrue(m.matches());
        try {
            m.group("Ignore"); // Not included
        } catch (IllegalArgumentException iae) {
            // good
        }
        Assert.assertEquals(m.group("Container1"), "MyLab/2015-01-01/");

        // Group
        p = Pattern.compile("^.*user/(?<Group>[^/]+)/(?<Container1>.*?)");
        m = p.matcher("/home/user/MyLab/2015-01-01/");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group("Group"), "MyLab");
        Assert.assertEquals(m.group("Container1"), "2015-01-01/");

        // Container2 takes all extra paths
        p = Pattern.compile("^.*user/(?<Container2>([^/]+/)*)(?<Container1>([^/]+/))");
        m = p.matcher("/home/user/MyLab/2015-01-01/foo/");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group("Container2"), "MyLab/2015-01-01/");
        Assert.assertEquals(m.group("Container1"), "foo/");
        m = p.matcher("/home/user/MyLab/");
        Assert.assertTrue(m.matches());
        Assert.assertEquals(m.group("Container1"), "MyLab/");

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