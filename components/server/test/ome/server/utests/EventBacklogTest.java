/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import ome.model.meta.EventLog;
import ome.services.eventlogs.EventBacklog;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext" })
public class EventBacklogTest extends MockObjectTestCase {

    EventBacklog b;

    @BeforeMethod
    public void setup() {
        b = new EventBacklog();
    }

    @Test
    public void testAddedOnlyOnce() {
        EventLog log = new EventLog(1L, "Image", "INSERT", null);
        assertTrue(b.add(log));
        assertFalse(b.add(log));
    }

    @Test
    public void testOrdered() {
        EventLog log1 = new EventLog(1L, "Image", "INSERT", null);
        EventLog log2 = new EventLog(2L, "Image", "INSERT", null);
        assertTrue(b.add(log1));
        assertTrue(b.add(log2));
        assertEquals(new Long(1L), b.remove().getEntityId());
        assertEquals(new Long(2L), b.remove().getEntityId());
        assertNull(b.remove());
    }

    @Test
    public void testPop() {
        EventLog log1 = new EventLog(1L, "Image", "INSERT", null);
        EventLog log2 = new EventLog(1L, "Image", "UPDATE", null);
        EventLog log3 = new EventLog(1L, "Dataset", "UPDATE", null);
        assertTrue(b.add(log1));
        assertTrue(b.add(log2));
        assertTrue(b.add(log3));

        EventLog test = b.remove();
        assertEquals("Image", test.getEntityType());
        assertEquals("INSERT", test.getAction());

        test = b.remove();
        assertEquals("Image", test.getEntityType());
        assertEquals("UPDATE", test.getAction());

        test = b.remove();
        assertEquals("Dataset", test.getEntityType());
        assertEquals("UPDATE", test.getAction());

    }

    @Test(groups = "ticket:1102")
    public void testBacklogLockedWhileIndexingFromBacklog() {

        EventLog currentIndex = new EventLog(1L, "Image", "UPDATE", null);
        EventLog reindexed1 = new EventLog(1L, "Dataset", "UPDATE", null);
        EventLog reindexed2 = new EventLog(2L, "Dataset", "UPDATE", null);

        // Index is clear. We are currently processing currentIndex
        assertTrue(b.add(reindexed1));
        assertTrue(b.add(reindexed2));

        // Now we start taking from the backlog. At this point, the backlog
        // ignores all additions until the backlog is empty.
        assertNotNull(b.remove());
        assertFalse(b.add(currentIndex)); // Recursion otherwise!!

        assertNotNull(b.remove());
        assertFalse(b.add(currentIndex));

        // Once it is emptied, we should be able to fill it up again.
        assertNull(b.remove());
        b.flipState();
        assertTrue(b.add(currentIndex));
        assertTrue(b.add(reindexed1));
        assertTrue(b.add(reindexed2));

        // Now we empty it again, and test if it can be filled.
        assertNotNull(b.remove());
        assertFalse(b.add(currentIndex));
        assertNotNull(b.remove());
        assertFalse(b.add(currentIndex));
        assertNotNull(b.remove());
        assertFalse(b.add(currentIndex));
        assertNull(b.remove());
        b.flipState();
        assertTrue(b.add(currentIndex));
    }
}
