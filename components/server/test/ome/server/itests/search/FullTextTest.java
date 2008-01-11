/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import ome.server.itests.AbstractManagedContextTest;
import ome.services.fulltext.EventLogLoader;
import ome.services.fulltext.FullTextIndexer;
import ome.services.util.Executor;

import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext" })
public class FullTextTest extends AbstractManagedContextTest {

    FullTextIndexer fti;

    public void testSimpleCreation() throws Exception {
        fti = new FullTextIndexer(getExecutor(), getLogs());
        Thread t = new Thread(fti);
        t.start();
        t.join();
    }

    Executor getExecutor() {
        return (Executor) this.applicationContext.getBean("executor");
    }

    EventLogLoader getLogs() {
        return (EventLogLoader) this.applicationContext
                .getBean("eventLogLoader");
    }

}
