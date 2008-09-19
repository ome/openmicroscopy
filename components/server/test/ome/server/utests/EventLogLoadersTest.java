/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ome.api.IQuery;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.meta.EventLog;
import ome.services.fulltext.AllEntitiesPseudoLogLoader;
import ome.services.fulltext.AllEventsLogLoader;
import ome.services.fulltext.EventLogLoader;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext" })
public class EventLogLoadersTest extends MockObjectTestCase {

    EventLogLoader ell;
    EventLog el;
    List<EventLog> list;
    Mock q;
    IQuery svc;

    @BeforeMethod
    public void setup() {
        q = mock(IQuery.class);
        svc = (IQuery) q.proxy();
    }

    public void testBasic() throws Exception {

        el = new EventLog(1L, false);

        ell = new ListLogLoader();
        ((ListLogLoader) ell).logs.add(0, el);
        ((ListLogLoader) ell).logs.add(1, null);

        assertTrue(ell.hasNext());
        assertEquals(el, ell.next());
        assertFalse(ell.hasNext());
        try {
            ell.next();
            fail("Should throw");
        } catch (Exception nsee) {
            // ok
        }

        assertTrue(ell.more() == 0); // always true
    }

    public void testAllEntitiesLoader() throws Exception {
        ell = new AllEntitiesPseudoLogLoader();
        ell.setQueryService(svc);
        ((AllEntitiesPseudoLogLoader) ell).setClasses(Collections
                .singleton("cls1"));
        assertTrue(ell.more() > 0);

        returnNotNull();
        assertTrue(ell.hasNext());
        // Returns a pseudo EventLog so we don't need to test of identity
        returnNull();
        assertNotNull(ell.next().getEntityId());

        assertFalse(ell.hasNext());
        assertFalse(ell.more() > 0);
    }

    @Test(groups = "broken")
    // Error getting ordering of mocks setup
    public void testAllEventsLoader() throws Exception {
        ell = new AllEventsLogLoader();
        ell.setQueryService(svc);

        // First query invocation is to get the maximum value
        EventLog first = new EventLog(1L, false);
        EventLog last = new EventLog(2L, false);
        q.expects(once()).method("findByQuery").with(
                eq("select el from EventLog el order by id desc"), ANYTHING)
                .will(returnValue(last)).id("last");
        returnEl(last);

        q.expects(once()).method("findByQuery").after("last").will(
                returnValue(first)).id("first");
        assertTrue(ell.hasNext());
        assertTrue(ell.more() > 0); // More as long as last was positive

        q.expects(once()).method("findByQuery").after("first").will(
                returnValue(last)).id("lastagain");

        assertEquals(last, ell.next());

        q.expects(once()).method("findByQuery").after("lastagain").will(
                returnValue(null)).id("null");

        assertEquals(el, ell.next());

        assertFalse(ell.hasNext());
        assertFalse(ell.more() > 0);
    }

    @Test
    public void testHasNextReloadsAfterADiscontinuation() {
        ListLogLoader lll = new ListLogLoader();
        lll.logs.add(new EventLog(1L, false));

        boolean reached = false;
        for (EventLog test : lll) {
            reached = true;
            assertNotNull(test);
        }
        assertTrue(reached);
        assertTrue(lll.logs.size() == 0);

        reached = false;
        lll.logs.add(new EventLog(2L, false));
        for (EventLog test : lll) {
            reached = true;
            assertNotNull(test);
        }
        assertTrue(reached);
        assertTrue(lll.logs.size() == 0);
    }

    @Test
    public void testBatchSize() {
        el = new EventLog(1L, false);
        ell = new EventLogLoader() {
            @Override
            protected EventLog query() {
                return el;
            }

            @Override
            public long more() {
                return 0;
            }
        };

        int count = 0;
        for (EventLog test : ell) {
            count++;
            assertEquals(test, el);
        }
        assertEquals(EventLogLoader.DEFAULT_BATCH_SIZE, count);

        count = 0;
        for (EventLog test : ell) {
            count++;
            assertEquals(test, el);
        }
        assertEquals(EventLogLoader.DEFAULT_BATCH_SIZE, count);

    }

    @Test(groups = "ticket:1102")
    public void testBacklog() {
        el = null;
        ell = new EventLogLoader() {
            @Override
            protected EventLog query() {
                return el;
            }

            @Override
            public long more() {
                return 0;
            }
        };
        assertFalse(ell.hasNext());
        assertTrue(ell.addEventLog(Image.class, 1L));
        assertTrue(ell.addEventLog(Image.class, 2L));
        // Adding twice should not work
        assertFalse(ell.addEventLog(Image.class, 2L));
        assertTrue(ell.hasNext());
        // Once we start calling hasNext, next we can't add anymore
        assertFalse(ell.addEventLog(Image.class, 2L));

        // Now we consume both added logs
        assertNotNull(ell.next());
        assertNotNull(ell.next());
        assertFalse(ell.hasNext());

        // And now we should be able to add more logs
        assertTrue(ell.addEventLog(Image.class, 2L));

    }

    /**
     * Testing the structure:
     * 
     * p1 --> d2 --> i4 \___> d3 --> i5
     */
    @Test(groups = "ticket:1102")
    public void testBacklogAdvanced() {
        list = new ArrayList<EventLog>();
        list.add(new EventLog(1L, Project.class.getName(), "INSERT", null));
        list.add(new EventLog(2L, Dataset.class.getName(), "INSERT", null));
        list.add(new EventLog(3L, Dataset.class.getName(), "INSERT", null));
        list.add(new EventLog(4L, Image.class.getName(), "INSERT", null));
        list.add(new EventLog(5L, Image.class.getName(), "INSERT", null));
        ell = new EventLogLoader() {
            @Override
            protected EventLog query() {
                return list.size() == 0 ? null : list.remove(0);
            }

            @Override
            public long more() {
                return 0;
            }
        };

        EventLog current;

        // Get the first event log
        assertTrue(ell.hasNext());
        current = ell.next();
        assertEquals(new Long(1L), current.getEntityId());

        // While processing, it adds 4 to the backlog
        assertTrue(ell.addEventLog(Dataset.class, 2L));
        assertTrue(ell.addEventLog(Dataset.class, 3L));
        assertTrue(ell.addEventLog(Image.class, 4L));
        assertTrue(ell.addEventLog(Image.class, 5L));

        // Get each from the backlog,
        // 2
        current = ell.next();
        assertEquals(new Long(2L), current.getEntityId());
        assertFalse(ell.addEventLog(Project.class, 1L));
        assertFalse(ell.addEventLog(Image.class, 4L));
        // 3
        current = ell.next();
        assertEquals(new Long(3L), current.getEntityId());
        assertFalse(ell.addEventLog(Project.class, 1L));
        assertFalse(ell.addEventLog(Image.class, 4L));
        // 4
        current = ell.next();
        assertEquals(new Long(4L), current.getEntityId());
        assertFalse(ell.addEventLog(Project.class, 1L));
        assertFalse(ell.addEventLog(Dataset.class, 2L));
        // 5
        current = ell.next();
        assertEquals(new Long(5L), current.getEntityId());
        assertFalse(ell.addEventLog(Project.class, 1L));
        assertFalse(ell.addEventLog(Dataset.class, 3L));

        // The next item should be from the regular list
        // And it will add it's own objects
        current = ell.next();
        assertEquals(new Long(2L), current.getEntityId());
        assertTrue(ell.addEventLog(Project.class, 1L));
        assertTrue(ell.addEventLog(Image.class, 4L));

        // Which then get called from the backlog
        // 1
        current = ell.next();
        assertEquals(new Long(1L), current.getEntityId());
        assertFalse(ell.addEventLog(Dataset.class, 2L));
        assertFalse(ell.addEventLog(Dataset.class, 3L));
        assertFalse(ell.addEventLog(Image.class, 4L));
        assertFalse(ell.addEventLog(Image.class, 5L));
        // 4
        current = ell.next();
        assertEquals(new Long(4L), current.getEntityId());
        assertFalse(ell.addEventLog(Dataset.class, 2L));
        assertFalse(ell.addEventLog(Project.class, 4L));

        // Backlog is empty again, time for 3
        current = ell.next();
        assertEquals(new Long(3L), current.getEntityId());
        assertTrue(ell.addEventLog(Project.class, 1L));
        assertTrue(ell.addEventLog(Image.class, 5L));

        // Which then also get called from the backlog
        // 1
        current = ell.next();
        assertEquals(new Long(1L), current.getEntityId());
        assertFalse(ell.addEventLog(Dataset.class, 2L));
        assertFalse(ell.addEventLog(Dataset.class, 3L));
        assertFalse(ell.addEventLog(Image.class, 4L));
        assertFalse(ell.addEventLog(Image.class, 5L));
        // 4
        current = ell.next();
        assertEquals(new Long(5L), current.getEntityId());
        assertFalse(ell.addEventLog(Dataset.class, 3L));
        assertFalse(ell.addEventLog(Project.class, 1L));

        // Continue with the main list (empty backlog)
        current = ell.next();
        assertEquals(new Long(4L), current.getEntityId());
        assertTrue(ell.addEventLog(Project.class, 1L));
        assertTrue(ell.addEventLog(Dataset.class, 2L));

        // Processing backlog
        // 1
        current = ell.next();
        assertEquals(new Long(1L), current.getEntityId());
        assertFalse(ell.addEventLog(Dataset.class, 2L));
        assertFalse(ell.addEventLog(Dataset.class, 3L));
        assertFalse(ell.addEventLog(Image.class, 4L));
        assertFalse(ell.addEventLog(Image.class, 5L));
        // 2
        current = ell.next();
        assertEquals(new Long(2L), current.getEntityId());
        assertFalse(ell.addEventLog(Image.class, 4L));
        assertFalse(ell.addEventLog(Project.class, 1L));

        // Back to main list (last item)
        current = ell.next();
        assertEquals(new Long(5L), current.getEntityId());
        assertTrue(ell.addEventLog(Project.class, 1L));
        assertTrue(ell.addEventLog(Dataset.class, 3L));

        // Processing backlog
        // 1
        current = ell.next();
        assertEquals(new Long(1L), current.getEntityId());
        assertFalse(ell.addEventLog(Dataset.class, 2L));
        assertFalse(ell.addEventLog(Dataset.class, 3L));
        assertFalse(ell.addEventLog(Image.class, 4L));
        assertFalse(ell.addEventLog(Image.class, 5L));
        // 3
        current = ell.next();
        assertEquals(new Long(3L), current.getEntityId());
        assertFalse(ell.addEventLog(Image.class, 5L));
        assertFalse(ell.addEventLog(Project.class, 1L));

        // Now we should be finished
        assertFalse(ell.hasNext());
    }

    // ======================================================

    private void returnEl(EventLog log) {
        q.expects(once()).method("findByQuery").will(returnValue(log));
    }

    private void returnNull() {
        returnEl(null);
    }

    private EventLog returnNotNull() {
        EventLog log = new EventLog(1L, true);
        returnEl(log);
        return log;
    }

    private static class ListLogLoader extends EventLogLoader {
        public final List<EventLog> logs = new ArrayList<EventLog>();

        @Override
        protected EventLog query() {
            return logs.size() < 1 ? null : logs.remove(0);
        }

        @Override
        public long more() {
            return 0;
        }
    }
}
