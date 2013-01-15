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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo.path;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.5
 */
@Test(groups = {"fs"})
public class FsFileTest {
    @Test
    public void testFsFileConstructorEquivalence() {
        final Set<FsFile> files = new HashSet<FsFile>();
        files.add(new FsFile("a", "b", "c"));
        files.add(new FsFile(Arrays.asList("a", "b", "c")));
        files.add(new FsFile(new FsFile("a", "b", "c"), 6));
        files.add(new FsFile(new FsFile("p", "q", "a", "b", "c"), 3));
        files.add(new FsFile("a/b/c"));
        files.add(new FsFile("//a//b//c//"));
        files.add(FsFile.concatenate(new FsFile(),
                                     new FsFile("a"),
                                     new FsFile(),
                                     new FsFile("b"),
                                     new FsFile("c")));
        Assert.assertEquals(files.size(), 1,
                "different means of constructing the same FsFile should be equivalent");
        Assert.assertTrue(files.add(new FsFile("c/b/a")),
                "different FsFiles should not be equivalent");
        
    }
}
