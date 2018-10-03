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
 * Tests that filesets aren't skipped if they reference a common file. Also
 * makes sure that the order of these files doesn't matter either.
 *
 * * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ImportCandidatesTest {

    private File testFolder;
    private final List<String> testFiles = new ArrayList<String>();
    private final Set<String> expectedFilesets = new HashSet<String>();

    @BeforeClass
    /**
     * Creates the nhdr test file sets
     */
    public void createFiles() throws Exception {
        testFolder = TempFileManager.create_path("ImportCandidatesTest", "",
                true);

        // Create a test.nhdr / test.raw pair, but also an additional
        // test_extra.nhdr referencing the same test.raw file
        // Expected result: Two filesets test.nhdr and test_extra.nhdr
        // should be imported.
        final String content = "NRRD0001\n" + "type: float\n"
                + "sizes: 7 38 39 40\n" + "data file: ./test.raw\n"
                + "encoding: raw";

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
    /**
     * Deletes the nhdr test file sets
     */
    public void deleteFiles() throws Exception {
        FileUtils.deleteQuietly(testFolder);
    }

    @Test
    /**
     * Uses the nhdr filesets (two nhdr files referencing the same raw file).
     * Tests all possible permutations of the file paths passed to
     * ImportCandidates.
     */
    public void testOrder() {
        for (List<String> imp : Collections2.orderedPermutations(testFiles)) {
            String[] f = new String[imp.size()];
            f = imp.toArray(f);

            List<ImportContainer> cons = createImportCandidates(f);

            Assert.assertEquals(info(f, cons), expectedFilesets.size(),
                    cons.size());

            for (ImportContainer con : cons) {
                Assert.assertTrue(info(f, cons), expectedFilesets
                        .contains(con.getFile().getAbsolutePath()));
            }
        }
    }

    @Test
    /**
     * Uses the nhdr filesets (two nhdr files referencing the same raw file).
     * Passes the whole folder to ImportCandidates.
     */
    public void testFolder() {
        // test the folder itself
        String[] f = new String[] { testFolder.getAbsolutePath() };

        List<ImportContainer> cons = createImportCandidates(f);

        Assert.assertEquals(info(f, cons), expectedFilesets.size(),
                cons.size());

        for (ImportContainer con : cons) {
            Assert.assertTrue(info(f, cons),
                    expectedFilesets.contains(con.getFile().getAbsolutePath()));
        }
    }

    @Test
    /**
     * Tests a single fake/pattern fileset
     */
    public void testSingleFileset() throws Exception {
        File fakeFolder = TempFileManager.create_path("ImportCandidatesTest_1",
                "", true);

        File f = new File(fakeFolder, "test1.pattern");
        echo("test1_T<1-3>.fake", f);
        echo("", new File(fakeFolder, "test1_T1.fake"));
        echo("", new File(fakeFolder, "test1_T2.fake"));
        echo("", new File(fakeFolder, "test1_T3.fake"));

        String[] files = new String[] { fakeFolder.getAbsolutePath() };

        List<ImportContainer> cons = createImportCandidates(files);

        Assert.assertEquals(info(files, cons), 1, cons.size());

        Assert.assertTrue(info(files, cons), f.getAbsolutePath()
                .equals(cons.iterator().next().getFile().getAbsolutePath()));

        FileUtils.deleteQuietly(fakeFolder);
    }

    @Test
    /**
     * Tests two distinct fake/pattern filesets
     */
    public void testMultifileFilesets() throws Exception {
        File fakeFolder = TempFileManager.create_path("ImportCandidatesTest_2",
                "", true);

        File f1 = new File(fakeFolder, "test1.pattern");
        echo("test1_T<1-3>.fake", f1);
        echo("", new File(fakeFolder, "test1_T1.fake"));
        echo("", new File(fakeFolder, "test1_T2.fake"));
        echo("", new File(fakeFolder, "test1_T3.fake"));

        File f2 = new File(fakeFolder, "test2.pattern");
        echo("test1_T<4-6>.fake", f2);
        echo("", new File(fakeFolder, "test1_T4.fake"));
        echo("", new File(fakeFolder, "test1_T5.fake"));
        echo("", new File(fakeFolder, "test1_T6.fake"));

        String[] files = new String[] { fakeFolder.getAbsolutePath() };

        List<ImportContainer> cons = createImportCandidates(files);

        Assert.assertEquals(info(files, cons), 2, cons.size());

        for (ImportContainer con : cons) {
            Assert.assertTrue(info(files, cons),
                    f1.getAbsolutePath().equals(con.getFile().getAbsolutePath())
                            || f2.getAbsolutePath()
                                    .equals(con.getFile().getAbsolutePath()));
        }

        FileUtils.deleteQuietly(fakeFolder);
    }

    @Test
    /**
     * Tests two fake/pattern filesets where the files of one fileset are fully
     * included in the other fileset.
     */
    public void testContainingFilesets() throws Exception {
        File fakeFolder = TempFileManager.create_path("ImportCandidatesTest_3",
                "", true);

        File f1 = new File(fakeFolder, "test1.pattern");
        echo("test1_T<2-4>.fake", f1);
        echo("", new File(fakeFolder, "test1_T1.fake"));
        echo("", new File(fakeFolder, "test1_T2.fake"));
        echo("", new File(fakeFolder, "test1_T3.fake"));
        echo("", new File(fakeFolder, "test1_T4.fake"));
        echo("", new File(fakeFolder, "test1_T5.fake"));
        echo("", new File(fakeFolder, "test1_T6.fake"));

        File f2 = new File(fakeFolder, "test2.pattern");
        echo("test1_T<1-6>.fake", f2);

        String[] files = new String[] { fakeFolder.getAbsolutePath() };

        List<ImportContainer> cons = createImportCandidates(files);

        Assert.assertEquals(info(files, cons), 2, cons.size());

        for (ImportContainer con : cons) {
            Assert.assertTrue(info(files, cons),
                    f1.getAbsolutePath().equals(con.getFile().getAbsolutePath())
                            || f2.getAbsolutePath()
                                    .equals(con.getFile().getAbsolutePath()));
        }

        FileUtils.deleteQuietly(fakeFolder);
    }

    @Test
    /**
     * Tests fake/pattern filesets where both filesets share some files
     */
    public void testOverlappingFilesets() throws Exception {
        File fakeFolder = TempFileManager.create_path("ImportCandidatesTest_4",
                "", true);

        File f1 = new File(fakeFolder, "test1.pattern");
        echo("test1_T<1-4>.fake", f1);
        echo("", new File(fakeFolder, "test1_T1.fake"));
        echo("", new File(fakeFolder, "test1_T2.fake"));
        echo("", new File(fakeFolder, "test1_T3.fake"));
        echo("", new File(fakeFolder, "test1_T4.fake"));
        echo("", new File(fakeFolder, "test1_T5.fake"));
        echo("", new File(fakeFolder, "test1_T6.fake"));

        File f2 = new File(fakeFolder, "test2.pattern");
        echo("test1_T<3-6>.fake", f2);

        String[] files = new String[] { fakeFolder.getAbsolutePath() };

        List<ImportContainer> cons = createImportCandidates(files);

        Assert.assertEquals(info(files, cons), 2, cons.size());

        for (ImportContainer con : cons) {
            Assert.assertTrue(info(files, cons),
                    f1.getAbsolutePath().equals(con.getFile().getAbsolutePath())
                            || f2.getAbsolutePath()
                                    .equals(con.getFile().getAbsolutePath()));
        }

        FileUtils.deleteQuietly(fakeFolder);
    }

    /**
     * Creates a ImportCandidates from the provided files and returns its
     * ImportContainers.
     * 
     * @param files
     *            The file to import
     * @return The ImportContainers
     */
    private List<ImportContainer> createImportCandidates(String[] files) {
        ImportConfig config = new ImportConfig();
        OMEROWrapper w = new OMEROWrapper(config);
        IObserver o = new IObserver() {
            public void update(IObservable importLibrary, ImportEvent event) {
                // nothing to do
            }
        };
        return (new ImportCandidates(w, files, o)).getContainers();
    }

    /**
     * Just generates a String with detailed information about the current test
     * situation
     * 
     * @param files
     *            The files array being tested
     * @param cons
     *            The ImportContainer returned from ImportCandidates
     * @return An informative String
     */
    private String info(String[] files, List<ImportContainer> cons) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nInput:\n");
        for (String f : files)
            sb.append(f + "\n");
        sb.append("\nExpected filesets:\n");
        for (String s : expectedFilesets)
            sb.append(s + "\n");
        sb.append("\nActual filesets:\n");
        for (ImportContainer c : cons)
            sb.append(c.getFile().getAbsolutePath() + "\n");
        return sb.toString();
    }

    /**
     * Simply writes the content into the file
     * 
     * @param content
     *            The content
     * @param file
     *            The file
     * @throws IOException
     *             If the file couldn't be wrote
     */
    private static void echo(String content, File file) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(content);
        }
    }
}
