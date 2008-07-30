/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sharing;

import java.util.Arrays;
import java.util.Collections;

import javax.sql.DataSource;

import junit.framework.TestCase;
import ome.model.IObject;
import ome.model.core.Image;
import ome.services.sharing.BlobShareStore;
import ome.services.sharing.data.ShareData;
import ome.system.OmeroContext;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(groups = "sharing")
public class BlobShareStoreTest extends TestCase {

    OmeroContext ctx;
    BlobShareStore store;
    JdbcTemplate template;
    DataSource ds;
    LobHandler lob;

    @BeforeMethod
    public void setup() throws Exception {
        ctx = new OmeroContext(new String[] {
                "classpath:ome/services/datalayer.xml",
                "classpath:ome/services/config-local.xml" });
        ds = (DataSource) ctx.getBean("dataSource");
        lob = (LobHandler) ctx.getBean("lobHandler");
        template = new JdbcTemplate(ds);
        template.execute("drop sequence seq_private_shares");
        template.execute("drop table private_shares");
        store = new BlobShareStore(template, lob);
    }

    @AfterMethod
    public void cleanup() throws Exception {
    }

    @Test
    public <T extends IObject> void testSimple() {
        store.set(1L, "user", Collections.<T> emptyList(), Collections
                .<Long> emptyList(), Collections.<String> emptyList(), false);
        store.set(2L, "other", Arrays.asList(new Image(1L, false)), Arrays
                .asList(1L, 2L), Arrays.asList("example@example.com"), true);
        ShareData data = store.get(2L);
        assertEquals(2L, data.id);
        assertEquals("example@example.com", data.guests.get(0));
    }
    // Helpers
    // ====================

}
