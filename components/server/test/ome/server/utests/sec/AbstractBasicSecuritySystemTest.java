/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ome.api.ITypes;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.basic.BasicSecuritySystem;
import ome.services.sessions.SessionManager;
import ome.system.Principal;
import ome.system.Roles;
import ome.testing.MockServiceFactory;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.Configuration;

public abstract class AbstractBasicSecuritySystemTest extends
        MockObjectTestCase {

    MockServiceFactory sf;
    
    Mock mockMgr;
    
    SessionManager mgr;

    BasicSecuritySystem sec;

    // login information
    Principal p;

    // "current" details
    Experimenter user;

    ExperimenterGroup group;

    EventType type;

    Event event;

    List<Long> leaderOfGroups, memberOfGroups;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        super.setUp();

        sf = new MockServiceFactory();
        sf.mockAdmin = mock(LocalAdmin.class);
        sf.mockTypes = mock(ITypes.class);
        sf.mockQuery = mock(LocalQuery.class);
        sf.mockUpdate = mock(LocalUpdate.class);
        
        mockMgr = mock(SessionManager.class);
        mgr = (SessionManager) mockMgr.proxy();
        mockMgr.expects(atLeastOnce()).method("assertSession");
        mockMgr.expects(atLeastOnce()).method("getEventContext")
        	.will(returnValue(null));
        sec = new BasicSecuritySystem(
        		(LocalQuery)sf.getQueryService(),
        		(LocalUpdate)sf.getUpdateService(),
        		mgr, new Roles());
    }

    protected void prepareMocksWithUserDetails(boolean readOnly) {
        // login
        p = new Principal("test", "test", "test");
        sec.login(p);

        // context
        user = new Experimenter(1L, true);
        group = new ExperimenterGroup(1L, true);
        type = new EventType(1L, true);
        event = new Event(1L, true);

        user.linkExperimenterGroup(group);
        leaderOfGroups = Collections.singletonList(1L);
        memberOfGroups = Collections.singletonList(1L);

    }

    protected void prepareMocksWithRootDetails(boolean readOnly) {
        // login
        p = new Principal("root", "system", "internal");
        sec.login(p);

        // context
        user = new Experimenter(0L, true);
        group = new ExperimenterGroup(0L, true);
        type = new EventType(0L, true);
        event = new Event(0L, true);

        user.linkExperimenterGroup(group);
        leaderOfGroups = Collections.singletonList(0L);
        memberOfGroups = Arrays.asList(0L, 1L);
    }

    @Override
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception {
        super.verify();
        sec.clearEventContext();
        super.tearDown();
    }

}
