/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2018 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;

import junit.framework.Assert;

/**
 * Tests that the order of the files to import doesn't matter.
 *
 * * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ImportCandidatesTest {

    private static final String folder = "/tmp/21768/";

    private int expectedNumber = -1;
    private HashSet<String> expectedFiles = null;
    private HashMap<String, Boolean> results = new HashMap<String, Boolean>();

    public static final String[] files;
    static {
        File[] list = (new File(folder)).listFiles();
        files = new String[list.length];
        for (int i = 0; i < list.length; i++) {
            files[i] = list[i].getAbsolutePath();
        }
    }

    public ImportCandidatesTest() {
    }

    @Test
    public void testOrder() {
        generatePermutations(files.length, files);
        int failed = 0;
        StringBuilder sb = new StringBuilder();
        for (String listing : results.keySet()) {
            if (!results.get(listing)) {
                failed++;
                sb.append("Failed:");
                sb.append(listing);
                sb.append("----");
            }
        }
        Assert.assertTrue(sb.toString(), failed == 0);
    }

    void testImportCandidates(String[] files) {
        System.out.println("\nTesting:");
        StringBuilder sb = new StringBuilder();
        for (String f : files) {
            sb.append(f);
            sb.append("\n");
        }
        System.out.println(sb.toString());

        ImportConfig config = new ImportConfig();
        OMEROWrapper w = new OMEROWrapper(config);
        IObserver o = new IObserver() {
            public void update(IObservable importLibrary, ImportEvent event) {
                // nothing to do
            }
        };

        ImportCandidates ic = new ImportCandidates(w, files, o);
        List<ImportContainer> cons = ic.getContainers();

        if (expectedFiles == null) {
            // first import test, set this as the expected result for the
            // subsequent tests
            expectedNumber = cons.size();
            expectedFiles = new HashSet<String>();
            for (ImportContainer con : cons) {
                expectedFiles.add(con.getFile().getAbsolutePath());
            }
        } else {
            results.put(sb.toString(), expectedNumber == cons.size());
            for (ImportContainer con : cons) {
                Assert.assertTrue(
                        con.getFile().getAbsolutePath() + " was not expected!",
                        expectedFiles
                                .contains(con.getFile().getAbsolutePath()));
            }
        }
    }

    /**
     * Generate all possible permutations. See
     * https://en.wikipedia.org/wiki/Heap's_algorithm
     * 
     * @param n
     *            The number of files (start with files.length)
     * @param files
     *            The files
     */
    void generatePermutations(int n, String[] files) {
        if (n > 1) {
            generatePermutations(n - 1, files);
            for (int i = 0; i < n - 1; i++) {
                if (n % 2 == 0) {
                    String tmp = files[i];
                    files[i] = files[n - 1];
                    files[n - 1] = tmp;
                } else {
                    String tmp = files[0];
                    files[0] = files[n - 1];
                    files[n - 1] = tmp;
                }
                generatePermutations(n - 1, files);
            }
        } else {
            testImportCandidates(files);
        }
    }
}
