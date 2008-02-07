/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import ome.model.meta.EventLog;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.fulltext.PersistentEventLogLoader;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext" })
public class PersistentEventLogLoaderTest extends AbstractManagedContextTest {

    Executor ex;
    SessionManager sm;
    PersistentEventLogLoader ll;

    @BeforeMethod
    public void setup() {
        ex = (Executor) this.applicationContext.getBean("executor");
        sm = (SessionManager) this.applicationContext.getBean("sessionManager");
        ll = (PersistentEventLogLoader) this.applicationContext
                .getBean("eventLogLoader");
    }

    public void testInitialUseWithNoDbEntry() throws Exception {
        ome.model.meta.Session s = sm.create(new Principal("root", "system",
                "FullText"));
        ex.execute(new Principal(s.getUuid(), "system", "FullText"),
                new Executor.Work() {
                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
                        ll.deleteCurrentId();
                        EventLog log = ll.next();
                        assertTrue(log.getId() == null);
                        assertTrue(-1 == ll.getCurrentId());
                        for (EventLog log2 : ll) {
                            if (log2.getId() != null) {
                                break;
                            }
                        }
                        assertTrue(ll.getCurrentId() > 0);
                        return null;
                    }
                });
    }
}
