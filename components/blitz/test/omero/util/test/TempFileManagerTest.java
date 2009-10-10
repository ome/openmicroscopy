/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.util.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import omero.util.TempFileManager;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

@Test(groups = "unit")
public class TempFileManagerTest extends TestCase {

    @Test
    public void testBasicUsage() throws IOException {
        File p = TempFileManager.create_path("foo", ".bar");
        assertTrue(p.exists());
        TempFileManager.remove_path(p);
        assertFalse(p.exists());
    };

    @Test
    public void testNoCleanUp() throws IOException {
        File p = TempFileManager.create_path("foo", ".bar");
        assertTrue(p.exists());
    }

    @Test
    public void testDeleteOnExit() throws IOException {
        File p = TempFileManager.create_path("foo", ".bar");
        p.deleteOnExit();
    }

    @Test
    public void testUsingThePath() throws IOException {
        File p = TempFileManager.create_path("write", ".txt");
        FileUtils.writeLines(p, Arrays.asList("hi"));
        String hi = FileUtils.readFileToString(p).trim();
        assertEquals("hi", hi);
    }

    @Test
    public void testUsingThePathAndAFile() throws IOException {
        File p = TempFileManager.create_path("write", ".txt");
        FileUtils.writeLines(p, Arrays.asList("hi"));
        File f = new File(p.getAbsolutePath());
        String hi = FileUtils.readFileToString(f).trim();
        assertEquals("hi", hi);
    }

    @Test
    public void testFolderSimple() throws IOException {
        File p = TempFileManager.create_path("close", ".dir", true);
        assertTrue(p.exists());
        assertTrue(p.isDirectory());
    }

    @Test
    public void testFolderWrite() throws IOException {
        File p = TempFileManager.create_path("close", ".dir", true);
        assertTrue(p.exists());
        assertTrue(p.isDirectory());
        File f = new File(p, "file");
        FileUtils.writeStringToFile(f, "hi");
    }

    @Test
    public void testFolderDelete() throws IOException {
        File p = TempFileManager.create_path("close", ".dir", true);
        assertTrue(p.exists());
        assertTrue(p.isDirectory());
        File f = new File(p, "file");
        FileUtils.writeStringToFile(f, "hi");
        FileUtils.deleteDirectory(p);
    }

}
