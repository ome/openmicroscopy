/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sharing;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.meta.Share;
import ome.services.sharing.BlobShareStore;
import ome.services.sharing.data.ShareData;
import ome.system.OmeroContext;

import org.springframework.orm.hibernate3.HibernateTemplate;
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
    HibernateTemplate ht;

    @BeforeMethod
    public void setup() throws Exception {
        ctx = new OmeroContext(new String[] {
                "classpath:ome/services/datalayer.xml",
                "classpath:ome/services/config-local.xml" });
        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");
        store = new BlobShareStore(ht);
        store.init();
    }

    @AfterMethod
    public void cleanup() throws Exception {
        store.close();
    }

    @Test
    public <T extends IObject> void testSimple() {
        store.set(new Share(1L, true), 3L, Collections.<T> emptyList(),
                Collections.<Long> emptyList(), Collections
                        .<String> emptyList(), false);
        store.set(new Share(2L, true), 4L, Arrays.asList(new Image(1L, false)),
                Arrays.asList(1L, 2L), Arrays.asList("example@example.com"),
                true);
        ShareData data = store.get(2L);
        assertEquals(2L, data.id);
        assertEquals("example@example.com", data.guests.get(0));
    }
    // Helpers
    // ====================

}
