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

//
// Upping timeout since travis can fail with the following:
// [nomemorytestng] FAILED: testBasicUsage on null(omero.util.test.TempFileManagerTest)
// [nomemorytestng] org.testng.internal.thread.ThreadTimeoutException: Method org.testng.internal.TestNGMethod.testBasicUsage() didn't finish within the time-out 1000
// [nomemorytestng]    at java.net.Inet6AddressImpl.lookupAllHostAddr(Native Method)
// [nomemorytestng]    at java.net.InetAddress$1.lookupAllHostAddr(InetAddress.java:901)
// [nomemorytestng]    at java.net.InetAddress.getAddressesFromNameService(InetAddress.java:1293)
// [nomemorytestng]    at java.net.InetAddress.getLocalHost(InetAddress.java:1469)
// [nomemorytestng]    at sun.management.VMManagementImpl.getVmId(VMManagementImpl.java:135)
// [nomemorytestng]    at sun.management.RuntimeImpl.getName(RuntimeImpl.java:59)
// [nomemorytestng]    at omero.util.TempFileManager.pid(TempFileManager.java:279)
// [nomemorytestng]    at omero.util.TempFileManager.<init>(TempFileManager.java:118)
//
@Test(groups = "unit", timeOut = 30000)
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
