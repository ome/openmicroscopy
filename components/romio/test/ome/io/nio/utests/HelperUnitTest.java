/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import java.io.File;

import org.testng.annotations.*;
import junit.framework.TestCase;
import ome.io.nio.PixelsService;
import ome.util.PathUtil;

public class HelperUnitTest extends TestCase {


    /** Default root for testing "/OMERO/" */
	private static String ROOT = PathUtil.getInstance().getDataFilePath();

	private String p(String path) {
        if (File.separator.equals("\\")) {
            return path.replace("/", "\\");
        }
        return path;
    }

    //
    // SINGLE DIRECTORY TESTS
    //
    @Test
    public void testPixelsSingleDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(1));
        assertEquals(p(ROOT) + p("Pixels/1"), path);
    }

    @Test
    public void testFilesSingleDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(1));
        assertEquals(p(ROOT) + p("Files/1"), path);
    }

    @Test
    public void testPixelsSingleDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(999));
        assertEquals(p(ROOT) + p("Pixels/999"), path);
    }

    @Test
    public void testFilesSingleDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(999));
        assertEquals(p(ROOT) + p("Files/999"), path);
    }

    //
    // TWO DIRECTORY TESTS
    //
    @Test
    public void testPixelsTwoDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(1001));
        assertEquals(p(ROOT) + p("Pixels/Dir-001/1001"), path);
    }

    @Test
    public void testFilesTwoDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(1001));
        assertEquals(p(ROOT) + p("Files/Dir-001/1001"), path);
    }

    @Test
    public void testPixelsTwoDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(999999));
        assertEquals(p(ROOT) + p("Pixels/Dir-999/999999"), path);
    }

    @Test
    public void testFilesTwoDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(999999));
        assertEquals(p(ROOT) + p("Files/Dir-999/999999"), path);
    }

    //
    // THREE DIRECTORY TESTS
    //
    @Test
    public void testPixelsThreeDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getPixelsPath(new Long(1000001));
        assertEquals(p(ROOT) + p("Pixels/Dir-001/Dir-000/1000001"), path);
    }

    @Test
    public void testFilesThreeDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(1000001));
        assertEquals(p(ROOT) + p("Files/Dir-001/Dir-000/1000001"), path);
    }

    @Test
    public void testPixelsThreeDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT)
                .getPixelsPath(new Long(999999999));
        assertEquals(p(ROOT) + p("Pixels/Dir-999/Dir-999/999999999"), path);
    }

    @Test
    public void testFilesThreeDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT).getFilesPath(new Long(999999999));
        assertEquals(p(ROOT) + p("Files/Dir-999/Dir-999/999999999"), path);
    }

    //
    // FOUR DIRECTORY TESTS
    //
    @Test
    public void testPixelsFourDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT)
                .getPixelsPath(new Long(1000000001));
        assertEquals(p(ROOT) + p("Pixels/Dir-001/Dir-000/Dir-000/1000000001"),
                path);
    }

    @Test
    public void testFilesFourDirectoryLowerBoundsPath() {
        String path = new PixelsService(ROOT)
                .getFilesPath(new Long(1000000001));
        assertEquals(p(ROOT) + p("Files/Dir-001/Dir-000/Dir-000/1000000001"), path);
    }

    @Test
    public void testPixelsFourDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT)
                .getPixelsPath((long) Integer.MAX_VALUE);
        assertEquals(p(ROOT) + p("Pixels/Dir-002/Dir-147/Dir-483/2147483647"),
                path);
    }

    @Test
    public void testFilesFourDirectoryUpperBoundsPath() {
        String path = new PixelsService(ROOT)
                .getFilesPath((long) Integer.MAX_VALUE);
        assertEquals(p(ROOT) + p("Files/Dir-002/Dir-147/Dir-483/2147483647"), path);
    }
}
