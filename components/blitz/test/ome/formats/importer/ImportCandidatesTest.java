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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Collections2;

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

    public static final List<String> files = new ArrayList<String>();
    static {
        File[] list = (new File(folder)).listFiles();
        for (int i = 0; i < list.length; i++) {
            files.add(list[i].getAbsolutePath());
        }
    }

    public ImportCandidatesTest() {
    }

    @Test
    public void testOrder() {
        for(List<String> imp : Collections2.orderedPermutations(files)) {
            String[] f = new String[imp.size()];
            f = imp.toArray(f);
            testImportCandidates(f);
        }
        
        int failed = 0;
        StringBuilder sb = new StringBuilder();
        for (String listing : results.keySet()) {
            if (!results.get(listing)) {
                failed++;
                sb.append("Failed:\n");
                sb.append(listing);
                sb.append("----\n");
            }
        }
        Assert.assertTrue(sb.toString(), failed == 0);
    }

    void testImportCandidates(String[] files) {
        StringBuilder sb = new StringBuilder();
        for (String f : files) {
            sb.append(f);
            sb.append("\n");
        }

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
            boolean ok = expectedNumber == cons.size();
            if (ok) {
                for (ImportContainer con : cons) {
                    if (!expectedFiles
                            .contains(con.getFile().getAbsolutePath())) {
                        ok = false;
                        sb.append("Unexpected file "+con.getFile().getAbsolutePath()+"\n");
                        break;
                    }
                }
            }
            else {
                sb.append(cons.size()+" containers ("+expectedNumber+" expected)\n");
            }
            results.put(sb.toString(), ok);
        }
    }
}
