/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.server.itests.AbstractManagedContextTest;
import ome.testing.ObjectFactory;
import ome.util.SqlAction;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the JDBC-based array methods of {@link PgArrayHelper}
 */
@Test(groups = "integration")
public class SqlActionTest extends AbstractManagedContextTest {

    String key;
    String value;
    OriginalFile f;
    Pixels p;
    SqlAction sql;

    @BeforeMethod
    public void setup() {
        sql = (SqlAction) applicationContext.getBean("simpleSqlAction");
        f = makefile();
        p = makepixels();
        key = uuid();
        value = uuid();
    }

    // OriginalFile
    @Test(enabled=true)
    public void testGetEmptyFileParams() {
        Map<String, String> t = sql.getFileParams(f.getId());
        assertEquals(t.size(), 0);
    }

    @Test(enabled=true)
    public void testSetFileParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(key, value);
        sql.setFileParams(f.getId(), params);
        Map<String, String> t = sql.getFileParams(f.getId());
        assertNotNull(t);
        assertEquals(params.size(), t.size());
        assertTrue(t.containsKey(key));
        assertEquals(value, t.get(key));
    }

    @Test(enabled=true)
    public void testFileGetKeys() {
        testSetFileParams();
        List<String> keys = sql.getFileParamKeys(f.getId());
        assertTrue(keys.contains(key));
    }

    @Test(enabled=true)
    public void testSetSingleFileParam() {
        testSetFileParams();
        String uuid = uuid();
        sql.setFileParam(f.getId(), key, uuid);
        sql.setFileParam(f.getId(), uuid, uuid);
        Map<String, String> params = sql.getFileParams(f.getId());
        assertEquals(uuid, params.get(key));
        assertEquals(uuid, params.get(uuid));
    }

    @Test(enabled=true)
    public void testBadGetFileParamsReturnsNull() {
        assertNull(sql.getFileParamKeys(-1));
    }


    // Pixels
    @Test(enabled=true)
    public void testGetEmptyPixelsParams() {
        Map<String, String> t = sql.getPixelsParams(p.getId());
        assertEquals(t.size(), 0);
    }

    @Test(enabled=true)
    public void testSetPixelsParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(key, value);
        sql.setPixelsParams(p.getId(), params);
        Map<String, String> t = sql.getPixelsParams(p.getId());
        assertEquals(params.size(), t.size());
        assertTrue(t.containsKey(key));
        assertEquals(value, t.get(key));
    }

    @Test(enabled=true)
    public void testPixelsGetKeys() {
        testSetPixelsParams();
        List<String> keys = sql.getPixelsParamKeys(p.getId());
        assertTrue(keys.contains(key));
    }

    @Test(enabled=true)
    public void testSetSinglePixelsParam() {
        testSetPixelsParams();
        String uuid = uuid();
        sql.setPixelsParam(p.getId(), key, uuid);
        sql.setPixelsParam(p.getId(), uuid, uuid);
        Map<String, String> params = sql.getPixelsParams(p.getId());
        assertEquals(uuid, params.get(key));
        assertEquals(uuid, params.get(uuid));
    }

    @Test(enabled=true)
    public void testBadGetPixelsParamsReturnsNull() {
        assertNull(sql.getPixelsParamKeys(-1));
    }



    private OriginalFile makefile() {
        OriginalFile f = new OriginalFile();
        f.setName("name");
        f.setPath("path");
        f.setSha1("");
        f.setSize(0L);
        f = iUpdate.saveAndReturnObject(f);
        return f;
    }

    private Pixels makepixels() {
        Pixels p = ObjectFactory.createPixelGraph(null);
        Image i = iUpdate.saveAndReturnObject(p.getImage());
        p = i.getPrimaryPixels();
        return p;
    }

}
