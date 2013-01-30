/*
 * Copyright (C) 2012 - 2013 University of Dundee & Open Microscopy Environment.
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

package ome.services.blitz.test.utests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ome.services.blitz.repo.path.FsFile;

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
    
    @Test
    public void testChildPathLegal() {
        final FsFile parent = new FsFile("a/b/c");
        final FsFile child = new FsFile("a/b/c/d/e");
        Assert.assertEquals(child.getPathFrom(parent).toString(), "d/e",
                "unexpected result for relative path");
    }
    
    @Test
    public void testChildPathSame() {
        final FsFile path = new FsFile("a/b/c");
        Assert.assertEquals(path.getPathFrom(path).toString(), "",
                "relative path to same directory should be empty");
    }
    
    @Test
    public void testChildPathIllegal() {
        final FsFile parent = new FsFile("a/c/c");
        final FsFile child = new FsFile("a/b/c/d/e");
        Assert.assertNull(child.getPathFrom(parent),
                "relative path may only be within parent directory");
    }
    
    @Test
    public void testEmptyPathEmptiness() {
        Assert.assertTrue(FsFile.emptyPath.getComponents().isEmpty(),
                "the empty path should be empty");
        Assert.assertTrue(FsFile.emptyPath.toString().isEmpty(),
                "the empty path should be empty");
    }
}
