/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import ome.io.nio.PixelsService;

public class HelperUnitTest {

    /** Temporary root for testing */
    private static String ROOT = 
        PathUtil.getInstance().getTemporaryDataFilePath();

    private String p(String path) {
        if (File.separator.equals("\\")) {
            return path.replace("/", "\\");
        }
        return path;
    }

    @AfterClass
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(ROOT));
    }

    //
    // TAILING SLASH TESTS
    //
    @Test
    public void testMissingTrailingSlash() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(1));
        Assert.assertEquals(p(ROOT) + p("Pixels/1"), path);
    }

    //
    // SINGLE DIRECTORY TESTS
    //
    @Test
    public void testPixelsSingleDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(1));
        Assert.assertEquals(p(ROOT) + p("Pixels/1"), path);
    }

    @Test
    public void testFilesSingleDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(1));
        Assert.assertEquals(p(ROOT) + p("Files/1"), path);
    }

    @Test
    public void testPixelsSingleDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(999));
        Assert.assertEquals(p(ROOT) + p("Pixels/999"), path);
    }

    @Test
    public void testFilesSingleDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(999));
        Assert.assertEquals(p(ROOT) + p("Files/999"), path);
    }

    //
    // TWO DIRECTORY TESTS
    //
    @Test
    public void testPixelsTwoDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(1001));
        Assert.assertEquals(p(ROOT) + p("Pixels/Dir-001/1001"), path);
    }

    @Test
    public void testFilesTwoDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(1001));
        Assert.assertEquals(p(ROOT) + p("Files/Dir-001/1001"), path);
    }

    @Test
    public void testPixelsTwoDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(999999));
        Assert.assertEquals(p(ROOT) + p("Pixels/Dir-999/999999"), path);
    }

    @Test
    public void testFilesTwoDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(999999));
        Assert.assertEquals(p(ROOT) + p("Files/Dir-999/999999"), path);
    }

    //
    // THREE DIRECTORY TESTS
    //
    @Test
    public void testPixelsThreeDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(1000001));
        Assert.assertEquals(p(ROOT) + p("Pixels/Dir-001/Dir-000/1000001"), path);
    }

    @Test
    public void testFilesThreeDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(1000001));
        Assert.assertEquals(p(ROOT) + p("Files/Dir-001/Dir-000/1000001"), path);
    }

    @Test
    public void testPixelsThreeDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT)
                .getPixelsPath(new Long(999999999));
        Assert.assertEquals(p(ROOT) + p("Pixels/Dir-999/Dir-999/999999999"), path);
    }

    @Test
    public void testFilesThreeDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(999999999));
        Assert.assertEquals(p(ROOT) + p("Files/Dir-999/Dir-999/999999999"), path);
    }

    //
    // FOUR DIRECTORY TESTS
    //
    @Test
    public void testPixelsFourDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT)
                .getPixelsPath(new Long(1000000001));
        Assert.assertEquals(p(ROOT) + p("Pixels/Dir-001/Dir-000/Dir-000/1000000001"),
                path);
    }

    @Test
    public void testFilesFourDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT)
                .getFilesPath(new Long(1000000001));
        Assert.assertEquals(p(ROOT) + p("Files/Dir-001/Dir-000/Dir-000/1000000001"), path);
    }

    @Test
    public void testPixelsFourDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT)
                .getPixelsPath((long) Integer.MAX_VALUE);
        Assert.assertEquals(p(ROOT) + p("Pixels/Dir-002/Dir-147/Dir-483/2147483647"),
                path);
    }

    @Test
    public void testFilesFourDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT)
                .getFilesPath((long) Integer.MAX_VALUE);
        Assert.assertEquals(p(ROOT) + p("Files/Dir-002/Dir-147/Dir-483/2147483647"), path);
    }
}
