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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.MakePathComponentSafe;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
@Test(groups = {"fs"})
public class ClientFilePathTransformerTest extends FilePathTransformerTestBase {
    private ClientFilePathTransformer fptc;

    /**
     * Initialize a client file path transformer.
     * @throws IOException unexpected
     */
    @BeforeClass
    public void setup() throws IOException {
        this.fptc = new ClientFilePathTransformer(new MakePathComponentSafe(this.conservativeRules));
    }

    /**
     * Test maximum path depth trimming of client paths.
     * @throws IOException unexpected
     */
    @Test
    public void testClientPathToRepository() throws IOException {
        final File clientPath = componentsToFile("Batteries", "not", "included");
        Assert.assertEquals(fptc.getFsFileFromClientFile(clientPath, 4), new FsFile("Batteries/not/included"),
                "unexpected result in converting from client path to repository path");
        Assert.assertEquals(fptc.getFsFileFromClientFile(clientPath, 3), new FsFile("Batteries/not/included"),
                "unexpected result in converting from client path to repository path");
        Assert.assertEquals(fptc.getFsFileFromClientFile(clientPath, 2), new FsFile("not/included"),
                "unexpected result in converting from client path to repository path");
        Assert.assertEquals(fptc.getFsFileFromClientFile(clientPath, 1), new FsFile("included"),
                "unexpected result in converting from client path to repository path");
    }

    /**
     * Test minimum path depth calculation failure given client paths including identical paths.
     * @throws IOException unexpected
     */
    @Test
    public void testGetMinimumDepthImpossibleSame() throws IOException {
        final File clientPath1 = componentsToFile("Batteries", "not", "included");
        final File clientPath2 = componentsToFile("Batteries", "not", "included");
        final File clientPath3 = componentsToFile("Batteries", "are", "included");
        fptc.getMinimumDepth(Arrays.asList(clientPath1, clientPath3));
        try {
            fptc.getMinimumDepth(Arrays.asList(clientPath1, clientPath2, clientPath3));
            Assert.fail("getMinimumDepth must not give a result for file sets whose elements cannot be made distinguishable");
        } catch (IllegalArgumentException e) { }
    }

    /**
     * Test minimum path depth calculation failure given client paths including paths only identical after sanitization.
     * @throws IOException unexpected
     */
    @Test
    public void testGetMinimumDepthImpossibleSanitizedSame() throws IOException {
        final File clientPath1 = componentsToFile("*");
        final File clientPath2 = componentsToFile("x");
        try {
            fptc.getMinimumDepth(Arrays.asList(clientPath1, clientPath2));
            Assert.fail("getMinimumDepth must not give a result for file sets whose elements cannot be made distinguishable");
        } catch (IllegalArgumentException e) { }
    }

    /**
     * Test that the minimum path depth for a set of paths is as expected.
     * @param expectedDepth the expected minimum depth
     * @param paths a set of paths, each String being a path, each character being a path component
     * @throws IOException unexpected, test fails
     */
    private void testMinimumDepth(int expectedDepth, String... paths) throws IOException {
        final Collection<File> files = new ArrayList<File>(paths.length);
        for (final String path : paths) {
            final char[] componentChars = new char[path.length()];
            path.getChars(0, path.length(), componentChars, 0);
            final String[] componentStrings = new String[path.length()];
            for (int i = 0; i < componentChars.length; i++)
                componentStrings[i] = Character.toString(componentChars[i]);
            files.add(componentsToFile(componentStrings));
        }
        Assert.assertEquals(fptc.getMinimumDepth(files), expectedDepth,
                "unexpected result for minimum path depth for files");
    }

    /**
     * Test minimum path depth calculation.
     * @throws IOException unexpected
     */
    @Test
    public void testGetMinimumDepthPossible() throws IOException {
        testMinimumDepth(1, "abcd", "abce", "abcf");
        testMinimumDepth(2, "abcd", "abdd", "abcf");
        testMinimumDepth(1, "abcd", "abc", "abcf");
        testMinimumDepth(2, "abcd", "abd", "abcf");
        testMinimumDepth(3, "abcd", "accd", "abcf");
        testMinimumDepth(4, "abcd", "bbcd", "abcf");
        testMinimumDepth(4, "aabcd", "bbcd", "d");
    }

    /**
     * Test that different file paths are not judged to be too similar.
     * @throws IOException unexpected
     */
    @Test
    public void testFilesDistinct() throws IOException {
        final Set<File> files = new HashSet<File>();
        files.add(componentsToFile("a"));
        files.add(componentsToFile("abc"));
        files.add(componentsToFile("a", "b", "c"));
        files.add(componentsToFile("b", "c", "a"));
        Assert.assertNull(fptc.getTooSimilarFiles(files),
                "sufficiently distinct file-paths should be permitted");
    }

    /**
     * Test that too-similar file paths are properly grouped and reported.
     * @throws IOException unexpected
     */
    @Test
    public void testFilesSameSanitized() throws IOException {
        final Set<File> similar1 = new HashSet<File>();
        similar1.add(componentsToFile("abc"));
        similar1.add(componentsToFile("ABC"));
        final Set<File> similar2 = new HashSet<File>();
        similar2.add(componentsToFile("a:"));
        similar2.add(componentsToFile("A;"));
        final Set<Set<File>> similarFileGroups = new HashSet<Set<File>>();
        similarFileGroups.add(similar1);
        similarFileGroups.add(similar2);
        final Set<File> files = new HashSet<File>();
        files.addAll(similar1);
        files.addAll(similar2);
        files.add(componentsToFile("a", "b", "c"));
        files.add(componentsToFile("b", "c", "a"));
        Assert.assertEquals(fptc.getTooSimilarFiles(files), similarFileGroups,
                "unexpected similar file groups");
    }

    /**
     * Reverse the actions of {@link #setup()}.
     */
    @AfterClass
    public void tearDown() {
        this.fptc = null;
    }
}
