/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the JDBC-based array methods of {@link PgArrayHelper}
 */
@Test(groups = "integration")
public class PgArrayHelperTest extends AbstractManagedContextTest {

    String key;
    String value;
    OriginalFile f;
    PgArrayHelper helper;

    @BeforeMethod
    public void setup() {
        helper = new PgArrayHelper(this.jdbcTemplate);
        f = makefile();
        key = uuid();
        value = uuid();
    }

    public void testSetParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(key, value);
        helper.setFileParams(f.getId(), params);
        Map<String, String> t = helper.getFileParams(f.getId());
        assertEquals(params.size(), t.size());
        assertTrue(t.containsKey(key));
        assertEquals(value, t.get(key));
    }

    public void testGetKeys() {
        testSetParams();
        List<String> keys = helper.getFileParamKeys(f.getId());
        assertTrue(keys.contains(key));
    }

    public void testSetSingleParam() {
        testSetParams();
        String uuid = uuid();
        helper.setFileParam(f.getId(), key, uuid);
        helper.setFileParam(f.getId(), uuid, uuid);
        Map<String, String> params = helper.getFileParams(f.getId());
        assertEquals(uuid, params.get(key));
        assertEquals(uuid, params.get(uuid));
    }

    public void testBadGetParamsReturnsNull() {
        assertNull(helper.getFileParamKeys(-1));
    }

    private OriginalFile makefile() {
        OriginalFile f = new OriginalFile();
        f.setName("name");
        f.setPath("path");
        f.setSha1("");
        f.setFormat(new Format("text/plain"));
        f.setSize(0L);
        f = iUpdate.saveAndReturnObject(f);
        return f;
    }

}
