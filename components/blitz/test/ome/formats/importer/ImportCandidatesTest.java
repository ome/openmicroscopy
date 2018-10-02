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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Collections2;

import junit.framework.Assert;
import omero.util.TempFileManager;

/**
 * Tests that filesets aren't skipped if they reference a common file. 
 * Also makes sure that the order of these files doesn't matter either.
 *
 * * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ImportCandidatesTest {

    private File testFolder;
    private final List<String> testFiles = new ArrayList<String>();
    private final Set<String> expectedFilesets = new HashSet<String>();
    
    public ImportCandidatesTest() {
    }

    @BeforeClass
    public void createFiles() throws Exception {
        testFolder = TempFileManager.create_path("ImportCandidatesTest", "", true);
        
        // Create a test.nhdr / test.raw pair, but also an additional 
        // test_extra.nhdr referencing the same test.raw file
        // Expected result: Two filesets test.nhdr and test_extra.nhdr 
        // should be imported.
        final String content = "NRRD0001\n" + 
                         "type: float\n" + 
                         "sizes: 7 38 39 40\n" + 
                         "data file: ./test.raw\n" + 
                         "encoding: raw";
        
        File f = new File(testFolder, "test.raw");
        echo("", f);
        testFiles.add(f.getAbsolutePath());
        
        f = new File(testFolder, "test.nhdr");
        echo(content, f);
        testFiles.add(f.getAbsolutePath());
        expectedFilesets.add(f.getAbsolutePath());
        
        f = new File(testFolder, "test_extra.nhdr");
        echo(content, f);
        testFiles.add(f.getAbsolutePath());
        expectedFilesets.add(f.getAbsolutePath());
    }

    @AfterClass
    public void deleteFiles() throws Exception {
        FileUtils.deleteQuietly(testFolder);
    }
    
    @Test
    public void testOrder() {
        // Test all possible permutations; the result should always be the same
        for(List<String> imp : Collections2.orderedPermutations(testFiles)) {
            String[] f = new String[imp.size()];
            f = imp.toArray(f);
            testImportCandidates(f);
        }
    }
    
    @Test
    public void testFolder() {
        // test the folder itself
        testImportCandidates(new String[] {testFolder.getAbsolutePath()});
    }

    /**
     * Test a specific combination of files
     * @param files The files to check
     */
    void testImportCandidates(String[] files) {
        ImportConfig config = new ImportConfig();
        OMEROWrapper w = new OMEROWrapper(config);
        IObserver o = new IObserver() {
            public void update(IObservable importLibrary, ImportEvent event) {
                // nothing to do
            }
        };

        ImportCandidates ic = new ImportCandidates(w, files, o);
        
        List<ImportContainer> cons = ic.getContainers();
        Assert.assertEquals(info(files, cons), expectedFilesets.size(), cons.size());
        
        for(ImportContainer con : cons) {
            Assert.assertTrue(info(files, cons), expectedFilesets.contains(con.getFile().getAbsolutePath()));
        }
    }
    
    /**
     * Just generates a String with detailed information about the 
     * current test situation
     * @param files The files array being tested
     * @param cons The ImportContainer returned from ImportCandidates
     * @return An informative String
     */
    private String info(String[] files, List<ImportContainer> cons) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nInput:\n");
        for(String f : files)
            sb.append(f+"\n");
        sb.append("\nExpected filesets:\n");
        for(String s : expectedFilesets)
            sb.append(s+"\n");
        sb.append("\nActual filesets:\n");
        for(ImportContainer c : cons)
            sb.append(c.getFile().getAbsolutePath()+"\n");
        return sb.toString();
    }
    
    /**
     * Simply writes the content into the file
     * @param content The content
     * @param file The file
     * @throws IOException If the file couldn't be wrote
     */
    private static void echo(String content, File file) throws IOException {
        try(BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(content);
        }
    }
}
