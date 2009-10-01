/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.IniFileLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    
    
}
