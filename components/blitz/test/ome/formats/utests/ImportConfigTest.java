/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.util.Map;
import java.util.Properties;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
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
public class ImportConfigTest {

    Logger log = LoggerFactory.getLogger(ImportConfigTest.class);
    ImportConfig config;
    Properties p;

    void basic() {
        p = new Properties();
        config = new ImportConfig(null, null, p);
    }

    @Test
    public void testSimple() throws Exception {
        ImportConfig config = new ImportConfig();
        config.hostname.set("foo");
        Map<String, String> dump = config.map();
        Assert.assertEquals(dump.get("hostname"), "foo");
    }

    @Test
    public void testDefaultsDontGetStored() {
        basic();
        ImportConfig.StrValue str = new ImportConfig.StrValue("foo", config, "default");
        str.store();
        Assert.assertNull(p.getProperty("foo"));
    }

    @Test
    public void testDoesntLoadOverExistingValues() {
        basic();
        p.put("src", "prop");
        ImportConfig.StrValue str = new ImportConfig.StrValue("src", config);
        str.set("set");
        str.load();
        Assert.assertEquals(str.get(), "set");
    }

    @Test
    public void testDisableStorage() {
        basic();
        ImportConfig.PassValue pass = new ImportConfig.PassValue("pass", config);
        pass.store();
        Assert.assertEquals(p.getProperty("pass",""), "");
    }

    @Test
    public void testDefaultGetsLoaded() {
        basic();
        ImportConfig.IntValue port = new ImportConfig.IntValue("port", config, 1111);
        Assert.assertEquals(port.get().intValue(), 1111);
    }

}
