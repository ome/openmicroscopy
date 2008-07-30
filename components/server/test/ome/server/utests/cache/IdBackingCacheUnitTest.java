/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ome.api.local.LocalQuery;
import ome.model.IObject;
import ome.model.core.Image;
import ome.services.util.cache.Cache;
import ome.services.util.cache.IdBackedCache;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
@Test(groups = "sessions")
public class IdBackingCacheUnitTest extends MockObjectTestCase {

    Mock qMock, cMock;
    Cache c;
    LocalQuery q;
    IdBackedCache<IObject, Serializable> store;
    List<Long> cacheKeys;
    List<Long> dbKeys;

    @BeforeMethod
    public void setup() throws Exception {

        cMock = mock(Cache.class);
        c = (Cache) cMock.proxy();

        qMock = mock(LocalQuery.class);
        q = (LocalQuery) qMock.proxy();

        store = new IdBackedCache(q, c);

        cacheKeys = new ArrayList<Long>(1025);
        for (long i = 0; i < 1025; i++) {
            cacheKeys.add(i);
        }
        cacheKeys.remove(new Long(666));

        dbKeys = new ArrayList<Long>(cacheKeys);
        dbKeys.remove(1L);
    }

    @Test
    public void testGet() {
        cMock.expects(once()).method("getType").will(returnValue(Image.class));
        qMock.expects(once()).method("find").will(returnValue(null));
        cMock.expects(once()).method("remove").with(eq(1L));
        cMock.expects(once()).method("get").with(eq(1L))
                .will(returnValue(null));

        assertNull(store.get(1L));
    }

    @Test
    public void testReap() {
        cMock.expects(once()).method("reap");
        cMock.expects(once()).method("getKeys").will(returnValue(cacheKeys));
        qMock.expects(once()).method("execute").will(returnValue(dbKeys));
        cMock.expects(once()).method("remove").with(eq(1L));

        store.reap();
    }

}
