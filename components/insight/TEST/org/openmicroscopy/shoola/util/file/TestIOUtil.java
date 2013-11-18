/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.file;

//Java imports
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.io.Files;

import junit.framework.TestCase;



/**
 * Tests for the IOUtil class.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class TestIOUtil
    extends TestCase
{

    //Helpers
    /**
     * Closes the specified steam.
     * 
     * @param stream The stream to close.
     */
    private static void closeStream(InputStream stream)
    {
        if (stream == null) return;
        try {
            stream.close();
        } catch (Exception e) {}
    }

    /**
     * Unzips the file using the command line. Return <code>true</code>
     * if successful, <code>false</code> otherwise.
     * 
     * @param zip The file to unzip.
     * @param destDir The destination folder.
     * @return See above.
     */
    private boolean unzip(File zip, File destDir)
    {
        Process p = null;
        try {
            List<String> cmds = new ArrayList<String>();
            cmds.add("unzip");
            cmds.add(zip.getAbsolutePath());
            cmds.add("-d");
            cmds.add(destDir.getAbsolutePath());
            ProcessBuilder pb = new ProcessBuilder(cmds);
            p = pb.start();
            if (p.waitFor() != 0) return false;
        } catch (Exception e) {
            return false;
        } finally {
            if (p != null) {
                //just in case.
                closeStream(p.getErrorStream());
                closeStream(p.getInputStream());
                p.destroy();
            }
        }
        return true;
    }

    /**
     * Deletes the file
     * 
     * @param f The file to handle.
     */
    private void clean(File f)
    {
        try {
            if (f.isFile()) FileUtils.deleteQuietly(f);
            if (f.isDirectory()) FileUtils.deleteDirectory(f);
        } catch (Exception e) {}
    }

    //Tests
    /** Creates a directory and a file to add it and zip it.*/
    public void testZipDirectory()
    {
        try {
            File dir = Files.createTempDir();
            File f = File.createTempFile("test", ".tmp", dir);
            File zip = IOUtil.zipDirectory(dir);
            assertEquals(FilenameUtils.getExtension(zip.getName()), "zip");
            File destDir = Files.createTempDir();
            boolean b = unzip(zip, destDir);
            assertEquals(true, b);
            File[] files = destDir.listFiles();
            assertEquals(1, files.length);
            assertEquals(f.getName(), files[0].getName());
            //clean
            clean(dir);
            clean(zip);
            clean(destDir);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /** Creates a directory with subfolder and zips it.*/
    public void testZipDirectoryWithSubfolder()
    {
        try {
            File dir = Files.createTempDir();
            File f = File.createTempFile("test", ".tmp", dir);
            File subfolder =Files.createTempDir();
            File f1 = File.createTempFile("test1", ".tmp", subfolder);

            FileUtils.moveDirectoryToDirectory(subfolder, dir, false);

            File zip = IOUtil.zipDirectory(dir);
            File destDir = Files.createTempDir();
            boolean b = unzip(zip, destDir);
            assertEquals(true, b);
            File[] files = destDir.listFiles();
            assertEquals(2, files.length);
            for (int i = 0; i < files.length; i++) {
                File ff = files[i];
                if (ff.isFile()) {
                    assertEquals(f.getName(), ff.getName());
                } else {
                    assertEquals(subfolder.getName(), ff.getName());
                    File[] list = ff.listFiles();
                    assertEquals(1, list.length);
                    assertEquals(f1.getName(), list[0].getName());
                }
            }
            //clean
            clean(dir);
            clean(zip);
            clean(destDir);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
