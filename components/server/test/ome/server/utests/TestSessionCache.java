/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.server.utests;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionContext;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.stats.NullSessionStats;
import ome.services.sessions.stats.SessionStats;

/**
 * {@link SessionCache} implementation which will always return
 * and object for any given UUID. This is mostly useful as the
 * constructor argument to {@link CurrentDetails}.
 * 
 * @author Josh Moore
 * @since 5.0
 */
public class TestSessionCache extends SessionCache {

    final private MockObjectTestCase test;

    final private Map<String, Session> sessions = new HashMap<String, Session>();

    private SessionStats stats;

    public TestSessionCache(MockObjectTestCase test) {
        this(test, new NullSessionStats());
    }

    public TestSessionCache(MockObjectTestCase test, SessionStats stats) {
        this.test = test;
        this.stats = stats;
    }

    public void setSessionStats(SessionStats stats) {
        this.stats = stats;
    }

    public SessionContext getSessionContext(String uuid) {

        Session session = null;
        // TODO: are null sessions ever stored?
        if (sessions.containsKey(uuid)) {
            session = sessions.get(uuid);
        } else {
            session = fakeSession();
        }
        Mock mockContext = test.mock(SessionContext.class);
        mockContext.expects(test.atLeastOnce()).method("getSession").will(
                test.returnValue(session));
        mockContext.expects(test.atLeastOnce()).method("stats").will(
                test.returnValue(stats));

        SessionContext proxy = (SessionContext) mockContext.proxy();
        putSession(uuid, proxy);
        return super.getSessionContext(uuid);
    }

    public void setFakeSession(String uuid, Session session) {
        this.sessions.put(uuid, session);
    }

    protected Session fakeSession() {
        Session session = new Session();
        session.setStarted(new Timestamp(System.currentTimeMillis()));
        session.setTimeToLive(0L);
        session.setTimeToIdle(60000L);
        ExperimenterGroup group = new ExperimenterGroup();
        group.getDetails().setPermissions(Permissions.READ_ONLY);
        session.getDetails().setGroup(group);
        return session;
    }

}
