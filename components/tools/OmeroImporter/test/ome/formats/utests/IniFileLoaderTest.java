/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import junit.framework.TestCase;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.IniFile;
import org.ini4j.IniFile.Mode;
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
public class IniFileLoaderTest extends TestCase {

    Log log = LogFactory.getLog(IniFileLoaderTest.class);
    IniFileLoader ini = new IniFileLoader(null);
    Preferences maps = Preferences.userNodeForPackage(getClass());
    

    protected void assertMaps(Map<String, List<String>> rv) {
        assertEquals(1, rv.size());
        List<String> list = rv.get("A");
        assertEquals(2, list.size());
        assertEquals("\\\\hostname1\\path1", list.get(0));
        assertEquals("\\\\hostname2\\path2", list.get(1));
    }
    
    String[] data = new String[] {
            "\\\\hostname1\\path1;  \\\\hostname2\\path2",
            "\\\\hostname1\\path1;  \\\\hostname2\\path2 ",
            " \\\\hostname1\\path1;  \\\\hostname2\\path2",
            " \\\\hostname1\\path1;  \\\\hostname2\\path2 ",
            " \\\\hostname1\\path1;\\\\hostname2\\path2 ",
            " //hostname1/path1;//hostname2/path2 ",
            " //hostname1/path1;\\\\hostname2\\path2 ",
            " //hostname1/path1 ; \\\\hostname2\\path2 ",
            "//hostname1\\path1 ; \\\\hostname2\\path2"
    };
    
    @Test
    public void testAllData() throws Exception {
        for (String item : data) {
            maps.put("A",item);
            assertMaps(ini.parseFlexMaps(maps));            
        }
    }
    
    @Test
    public void testDuplicatedSections() throws Exception {
        File f = TempFileManager.create_path("initest");
        FileUtils.writeLines(f, Arrays.asList(
                "[Section]",
                "A=1",
                "[Section]",
                "A=2"));
        Preferences test = new IniFile(f, Mode.RW);
        String a1 = test.node("Section").get("A", "missing");
        assertEquals("2",a1);
    }

    @Test
    public void testDuplicatedSections2() throws Exception {
        File f = TempFileManager.create_path("initest");
        FileUtils.writeLines(f, Arrays.asList(
                "[Section]",
                "[Section]",
                "A=1"));
        Preferences test = new IniFile(f, Mode.RW);
        String a1 = test.node("Section").get("A", "missing");
        assertEquals("1",a1);
    }


    public void testLocation() throws Exception {
        loci.common.LogTools.setDebug(true);
        Location loc = new Location("/");
        assertTrue(loc.exists());
        loc = new Location("\\squig.openmicroscopy.org.uk");
        assertTrue(loc.exists());
    }

}
