/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import ome.api.IQuery;
import ome.io.nio.OriginalFilesService;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.meta.EventLog;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.fulltext.EventLogLoader;
import ome.services.fulltext.FullTextBridge;
import ome.services.fulltext.FullTextIndexer;
import ome.services.fulltext.FullTextThread;
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

    class CreationLogLoader extends EventLogLoader {
        IObject obj;

        public CreationLogLoader(IObject obj) {
            this.obj = obj;
        }

        @Override
        public EventLog query() {
            if (obj == null) {
                return null;
            } else {
                EventLog el = rawQuery().findByQuery(
                        "select el from EventLog el "
                                + "where el.action = 'INSERT' and "
                                + "el.entityType = :type and "
                                + "el.entityId = :id",
                        new Parameters().addString("type",
                                obj.getClass().getName()).addId(obj.getId()));
                obj = null;
                return el;
            }
        }

        @Override
        public boolean more() {
            return false;
        }

    }

    IQuery rawQuery() {
        return (IQuery) this.applicationContext
                .getBean("internal:ome.api.IQuery");
    }

    OriginalFilesService getFileService() {
        return (OriginalFilesService) this.applicationContext
                .getBean("/OMERO/Files");
    }

    Executor getExecutor() {
        return (Executor) this.applicationContext.getBean("executor");
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
        };
        ell.setQueryService(this.rawQuery());
        return ell;
    }

    void indexObject(IObject o) {
        CreationLogLoader logs = new CreationLogLoader(o);
        ftb = new FullTextBridge();
        fti = new FullTextIndexer(logs);
        ftt = new FullTextThread(getExecutor(), fti, ftb);
        ftt.run();
    }

}
