/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ome.api.IQuery;
import ome.io.nio.OriginalFilesService;
import ome.model.IObject;
import ome.model.core.OriginalFile;
import ome.model.meta.EventLog;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.fulltext.EventLogLoader;
import ome.services.fulltext.FullTextIndexer;
import ome.services.fulltext.FullTextIndexer.Parser;
import ome.services.util.Executor;
import ome.testing.FileUploader;

import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext" })
public class FullTextTest extends AbstractManagedContextTest {

    FullTextIndexer fti;

    @Test(enabled = false, groups = "manual")
    public void testIndexWholeDb() throws Exception {
        final boolean[] more = new boolean[] { true };
        final long max = iQuery.findByQuery(
                "select el from EventLog el order by id desc",
                new Parameters(new Filter().page(0, 1))).getId();
        fti = new FullTextIndexer(getExecutor(), new EventLogLoader() {

            long last_id = 0;

            @Override
            protected EventLog query() {
                EventLog el = rawQuery().findByQuery(
                        "select el from EventLog el "
                                + "where el.id > :id order by id",
                        new Parameters(new Filter().page(0, 1)).addId(last_id));

                if (el == null) {
                    last_id = Long.MAX_VALUE;
                } else {
                    last_id = el.getId();
                }

                if (last_id >= max) {
                    more[0] = false;
                }
                last_id = el.getId();
                return el;
            }

        });

        set(fti);

        while (more[0]) {
            fti.run();
        }
    }

    public void testSimpleCreation() throws Exception {
        fti = new FullTextIndexer(getExecutor(), getLogs());
        fti.run();
    }

    public void testCreateFile() throws Exception {

        // Test data
        final String str = UUID.randomUUID().toString();

        // Parser setup
        Parser parser = new Parser() {
            public String parse(File file) {
                return str;
            }
        };
        Map<String, Parser> parsers = new HashMap<String, Parser>();
        parsers.put("text/plain", parser);

        // Upload
        FileUploader upload = new FileUploader(this.factory, str, "uuid",
                "/dev/null");
        try {
            upload.run();
        } catch (Exception e) {
            // This seems to be throwing an exception
            // when run in the server
        }

        // Index
        CreationLogLoader logs = new CreationLogLoader(new OriginalFile(upload
                .getId(), false));
        fti = new FullTextIndexer(getExecutor(), logs, getFileService(),
                parsers);
        set(fti);
        fti.run();
    }

    // Helpers
    // =========================================================================

    class CreationLogLoader extends EventLogLoader {
        IObject obj;

        public CreationLogLoader(IObject obj) {
            this.obj = obj;
        }

        @Override
        public EventLog next() {
            EventLog el = rawQuery()
                    .findByQuery(
                            "select el from EventLog el "
                                    + "where el.entityType = :type and el.entityId = :id",
                            new Parameters().addString("type",
                                    obj.getClass().getName())
                                    .addId(obj.getId()));
            obj = null;
            return el;
        }

        @Override
        public boolean hasNext() {
            return obj != null;
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

    EventLogLoader getLogs() {
        return (EventLogLoader) this.applicationContext
                .getBean("eventLogLoader");
    }

    void set(FullTextIndexer fti) {
        Map bridges = (Map) this.applicationContext.getBean("bridges");
        bridges.put("fieldBridges", fti);
    }
}
