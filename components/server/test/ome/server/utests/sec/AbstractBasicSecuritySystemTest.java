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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ome.api.ITypes;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecurityFilter;
import ome.security.SystemTypes;
import ome.security.basic.BasicACLVoter;
import ome.security.basic.BasicSecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.security.basic.OmeroInterceptor;
import ome.security.basic.OneGroupSecurityFilter;
import ome.security.basic.TokenHolder;
import ome.services.sessions.SessionManager;
import ome.services.sessions.stats.NullSessionStats;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.testing.MockServiceFactory;
import ome.tools.hibernate.ExtendedMetadata;

public abstract class AbstractBasicSecuritySystemTest extends
        MockObjectTestCase {

    MockServiceFactory sf;

    Mock mockMgr, mockEc;

    EventContext ec;

    SessionManager mgr;

    BasicSecuritySystem sec;

    BasicACLVoter aclVoter;

    // login information
    Principal p;

    // "current" details
    Experimenter user;

    ExperimenterGroup group;

    EventType type;

    Event event;

    List<Long> leaderOfGroups, memberOfGroups;

    CurrentDetails cd;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();

        sf = new MockServiceFactory();
        sf.mockAdmin = mock(LocalAdmin.class);
        sf.mockTypes = mock(ITypes.class);
        sf.mockQuery = mock(LocalQuery.class);
        sf.mockUpdate = mock(LocalUpdate.class);

        mockEc = mock(EventContext.class);
        mockMgr = mock(SessionManager.class);
        mgr = (SessionManager) mockMgr.proxy();

        cd = new CurrentDetails();
        SystemTypes st = new SystemTypes();
        TokenHolder th = new TokenHolder();
        OmeroInterceptor oi = new OmeroInterceptor(new Roles(),
                st, new ExtendedMetadata.Impl(),
                cd, th, new NullSessionStats());
        SecurityFilter filter = new OneGroupSecurityFilter();
        sec = new BasicSecuritySystem(oi, st, cd, mgr, new Roles(), sf,
                new TokenHolder(), filter);
        aclVoter = new BasicACLVoter(cd, st, th, filter);
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

        mockEc.expects(atLeastOnce()).method("getCurrentShareId").will(
                returnValue(null));
        mockEc.expects(atLeastOnce()).method("getCurrentSessionUuid").will(
                returnValue("session-uuid"));
        mockEc.expects(atLeastOnce()).method("getCurrentSessionId").will(
                returnValue(1L));
        mockEc.expects(atLeastOnce()).method("getCurrentUserId").will(
                returnValue(1L));
        mockEc.expects(atLeastOnce()).method("getCurrentUserName").will(
                returnValue("some-user"));
        mockEc.expects(atLeastOnce()).method("getCurrentGroupName").will(
                returnValue("test"));
        mockEc.expects(atLeastOnce()).method("getCurrentGroupId").will(
                returnValue(1L));
        mockEc.expects(atLeastOnce()).method("getMemberOfGroupsList").will(
                returnValue(memberOfGroups));
        mockEc.expects(atLeastOnce()).method("getLeaderOfGroupsList").will(
                returnValue(leaderOfGroups));
        ec = (EventContext) mockEc.proxy();
        mockMgr.expects(atLeastOnce()).method("getEventContext").will(
                returnValue(ec));

        doReadOnly(readOnly);

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

        mockEc.expects(atLeastOnce()).method("getCurrentSessionId").will(
                returnValue(1L));
        mockEc.expects(atLeastOnce()).method("getCurrentUserId").will(
                returnValue(0L));
        mockEc.expects(atLeastOnce()).method("getCurrentGroupId").will(
                returnValue(0L));
        mockEc.expects(atLeastOnce()).method("getMemberOfGroupsList").will(
                returnValue(memberOfGroups));
        mockEc.expects(atLeastOnce()).method("getLeaderOfGroupsList").will(
                returnValue(leaderOfGroups));
        ec = (EventContext) mockEc.proxy();
        mockMgr.expects(atLeastOnce()).method("getEventContext").will(
                returnValue(ec));

        doReadOnly(readOnly);
    }

    protected void doReadOnly(boolean readOnly) {
        if (!readOnly) {
            sf.mockAdmin.expects(once()).method("userProxy").will(
                    returnValue(user));
            sf.mockAdmin.expects(once()).method("groupProxy").will(
                    returnValue(group));
            sf.mockUpdate.expects(once()).method("saveAndReturnObject").will(
                    returnValue(event));
        }
    }

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        super.verify();
        sec.invalidateEventContext();
        super.tearDown();
    }

}
