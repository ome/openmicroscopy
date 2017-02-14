/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import loci.common.Location;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.IniFileLoader;
import omero.util.TempFileManager;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.ini4j.IniFile;
import org.ini4j.IniFile.Mode;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Various configuration workflows
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see ImportFixture
 * @see ImportReader
 * @see ImportConfig
 * @see ImportLibrary
 * @see ImportCandidates
 * @see OMEROWrapper
 * @see OMEROMetadataStoreClient
 *
 * @since Beta4.1
 */
public class IniFileLoaderTest {

    private IniFileLoader ini;
    private Preferences maps;
    private File temporaryFile;

    private static final String[] data = new String[] {
            "\\\\hostname1\\path1;  \\\\hostname2\\path2",
            "\\\\hostname1\\path1;  \\\\hostname2\\path2 ",
            " \\\\hostname1\\path1;  \\\\hostname2\\path2",
            " \\\\hostname1\\path1;  \\\\hostname2\\path2 ",
            " \\\\hostname1\\path1;\\\\hostname2\\path2 ",
            " /hostname1/path1;\\\\hostname2\\path2 ",
            " /hostname1/path1;\\\\hostname2\\path2 ",
            " /hostname1/path1 ; \\\\hostname2\\path2 ",
            "/hostname1/path1 ; \\\\hostname2\\path2"
    };

    @BeforeMethod
    public void setUp() throws Exception {
        temporaryFile = TempFileManager.create_path("initest");
        ini = new IniFileLoader(null);
        maps = Preferences.userNodeForPackage(getClass());
    }

    @AfterMethod
    public void tearDown() throws Exception {
        temporaryFile.delete();
    }

    protected void assertMaps(Map<String, List<String>> rv) {
        Assert.assertEquals(1, rv.size());
        List<String> list = rv.get("A");
        Assert.assertEquals(list.size(), 2);
        String first = list.get(0);
        if (!"\\\\hostname1\\path1".equals(first)
            && !"/hostname1/path1".equals(first)) {
            Assert.fail(String.format("%s does not equal %s or %s",
                    first, "\\\\hostname1\\path1", "/hostname1/path1"));
        }
        Assert.assertEquals(list.get(1), "\\\\hostname2\\path2");
    }

    @Test
    public void testFlexReaderServerMapUNC() throws Exception {
        FileUtils.writeLines(temporaryFile, Arrays.asList(
                "[FlexReaderServerMaps]",
                "CIA-1=\\\\\\\\hostname1\\\\path1;\\\\\\\\hostname1\\\\path2"));
        Preferences test = new IniFile(temporaryFile, Mode.RW);
        Preferences maps = test.node("FlexReaderServerMaps");
        Map<String, List<String>> parsedMaps = ini.parseFlexMaps(maps);
        Assert.assertEquals(parsedMaps.size(), 1);
        List<String> serverPaths = parsedMaps.get("CIA-1");
        Assert.assertEquals(serverPaths.size(), 2);
        Assert.assertEquals(serverPaths.get(0), "\\\\hostname1\\path1");
        Assert.assertEquals(serverPaths.get(1), "\\\\hostname1\\path2");
    }

    @Test
    public void testFlexReaderServerMapUnix() throws Exception {
        FileUtils.writeLines(temporaryFile, Arrays.asList(
                "[FlexReaderServerMaps]",
                "CIA-1=/mnt/path1;/mnt/path2"));
        Preferences test = new IniFile(temporaryFile, Mode.RW);
        Preferences maps = test.node("FlexReaderServerMaps");
        Map<String, List<String>> parsedMaps = ini.parseFlexMaps(maps);
        Assert.assertEquals(parsedMaps.size(), 1);
        List<String> serverPaths = parsedMaps.get("CIA-1");
        Assert.assertEquals(serverPaths.size(), 2);
        Assert.assertEquals(serverPaths.get(0), "/mnt/path1");
        Assert.assertEquals(serverPaths.get(1), "/mnt/path2");
    }

    @Test
    public void testAllData() throws Exception {
        for (String item : data) {
            maps.put("A",item);
            assertMaps(ini.parseFlexMaps(maps));
        }
    }

    @Test
    public void testDuplicatedSections() throws Exception {
        FileUtils.writeLines(temporaryFile, Arrays.asList(
                "[Section]",
                "A=1",
                "[Section]",
                "A=2"));
        Preferences test = new IniFile(temporaryFile, Mode.RW);
        String a1 = test.node("Section").get("A", "missing");
        Assert.assertEquals(a1, "2");
    }

    @Test
    public void testDuplicatedSections2() throws Exception {
        FileUtils.writeLines(temporaryFile, Arrays.asList(
                "[Section]",
                "[Section]",
                "A=1"));
        Preferences test = new IniFile(temporaryFile, Mode.RW);
        String a1 = test.node("Section").get("A", "missing");
        Assert.assertEquals(a1, "1");
    }

    @Test
    public void testLocation() throws Exception {
        ch.qos.logback.classic.Logger lociLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("loci");
        lociLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        Location loc = new Location("/");
        Assert.assertTrue(loc.exists());
    }

}
