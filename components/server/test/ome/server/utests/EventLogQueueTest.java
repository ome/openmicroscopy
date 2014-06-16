/*
 * Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.server.utests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.meta.EventLog;
import ome.services.eventlogs.EventLogQueue;
import ome.util.SqlAction;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.DefaultResultStub;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = { "query", "fulltext" })
public class EventLogQueueTest extends MockObjectTestCase {

    static long eventLogId = 0;

    EventLogQueue q;

    List<Object[]> results;

    SqlAction sql;

    Mock sqlMock;

    @BeforeMethod
    public void setup() {
        results = new ArrayList<Object[]>();
        q = new EventLogQueue() {
            @Override
            protected List<Object[]> lookup() {
               List<Object[]> copy = new ArrayList<Object[]>(results);
               results.clear();
               return copy;
            }
        };
        sqlMock = mock(SqlAction.class);
        sqlMock.setDefaultStub(new DefaultResultStub());
        sql = (SqlAction) sqlMock.proxy();
        q.setSqlAction(sql);
    }

    Object[] project(long id, String action, int skipped) {
        return eventlog(Project.class.getName(), id, action, skipped);
    }

    Object[] dataset(long id, String action, int skipped) {
        return eventlog(Dataset.class.getName(), id, action, skipped);
    }

    Map<String, Object> fields(Object...items) {
        List<Object> list = new ArrayList<Object>();
        list.addAll(Arrays.asList(items));
        Map<String, Object> rv = new HashMap<String, Object>();
        while (!list.isEmpty()) {
            String key = list.remove(0).toString();
            Object obj = list.remove(0);
            rv.put(key,  obj);
        }
        return rv;
    }

    void match(Map<String, Object> fields) {
        EventLog log = q.next();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if ("id".equals(k)) {
                assertEquals(v, log.getId());
            } else if ("objType".equals(k)) {
                assertEquals(v, log.getEntityType());
            } else if ("objId".equals(k)) {
                assertEquals(v, log.getEntityId());
            } else if ("action".equals(k)) {
                assertEquals(v, log.getAction());
            } else {
                throw new IllegalArgumentException(k);
            }
        }
    }

    Object[] eventlog(String type, long id, String action, int skipped) {
        Object[] rv = new Object[5];
        rv[0] = eventLogId++;
        rv[1] = type;
        rv[2] = id;
        rv[3] = action;
        rv[4] = skipped;
        return rv;
    }

    @Test
    public void testLoadsOnHasNext() {
        results.add(project(1, "INSERT", 0));
        assertTrue(q.hasNext());
        assertEquals(1L, q.next().getEntityId().longValue());
        assertFalse(q.hasNext());
    }

    @Test
    public void testNextReturnsTwo() {
        results.add(project(1, "INSERT", 0));
        results.add(project(2, "INSERT", 0));
        assertTrue(q.hasNext());
        assertEquals(1L, q.next().getEntityId().longValue());
        assertTrue(q.hasNext());
        assertEquals(2L, q.next().getEntityId().longValue());
        assertFalse(q.hasNext());
    }

    @Test
    public void testDupesSkipped() {
        results.add(project(1, "INSERT", 0));
        results.add(project(1, "INSERT", 0));
        assertTrue(q.hasNext());
        assertEquals(1L, q.next().getEntityId().longValue());
        assertFalse(q.hasNext());
    }

    @Test
    public void testTwoTypes() {
        results.add(project(1, "INSERT", 0));
        results.add(dataset(1, "INSERT", 0));
        assertTrue(q.hasNext());
        match(fields("objType", Project.class.getName(), "objId", 1L));
        assertTrue(q.hasNext());
        match(fields("objType", Dataset.class.getName(), "objId", 1L));
        assertFalse(q.hasNext());
    }

    @Test
    public void testBacklog() {
        q.addEventLog(Project.class, 1L);
        assertTrue(q.hasNext());
        match(fields("id", -1L, "objId", 1L));
        assertFalse(q.hasNext());
    }

    @Test
    public void testBacklogIgnoresRegistered() {
        results.add(project(1, "INSERT", 0));
        assertTrue(q.hasNext());
        q.addEventLog(Project.class, 1L);
        assertTrue(q.hasNext());
        match(fields("objId", 1L));
        assertFalse(q.hasNext());
    }

    @Test
    public void testStopSet() {
        q.addEventLog(Project.class, 1L);
        assertTrue(q.hasNext());
        q.setStop(true);
        assertFalse(q.hasNext());
        q.setStop(false);
        assertTrue(q.hasNext());
    }
}
