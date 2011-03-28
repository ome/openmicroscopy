/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import ome.api.IQuery;
import ome.io.nio.OriginalFilesService;
import ome.model.core.Image;
import ome.model.meta.EventLog;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.eventlogs.EventLogLoader;
import ome.services.fulltext.FullTextBridge;
import ome.services.fulltext.FullTextIndexer;
import ome.services.fulltext.FullTextThread;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;

import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext" })
public abstract class AbstractTest extends AbstractManagedContextTest {

    FullTextThread ftt;
    FullTextIndexer fti;
    FullTextBridge ftb;
    Image i;

    // Helpers
    // =========================================================================

    IQuery rawQuery() {
        return (IQuery) this.applicationContext
                .getBean("internal-ome.api.IQuery");
    }

    OriginalFilesService getFileService() {
        return (OriginalFilesService) this.applicationContext
                .getBean("/OMERO/Files");
    }

    Executor getExecutor() {
        return (Executor) this.applicationContext.getBean("executor");
    }

    SessionManager getManager() {
        return (SessionManager) this.applicationContext
                .getBean("sessionManager");
    }

    /**
     * Returns a simple {@link EventLogLoader} which only loads the last
     * {@link EventLog}
     * 
     * @return
     */
    EventLogLoader getLogs() {
        EventLogLoader ell = new EventLogLoader() {
            int todo = 1;

            @Override
            protected EventLog query() {
                if (todo < 0) {
                    return null;
                } else {
                    todo--;
                    return this.lastEventLog();
                }
            }

            @Override
            public long more() {
                return 0;
            }
        };
        ell.setQueryService(this.rawQuery());
        return ell;
    }

}
