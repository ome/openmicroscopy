/*
 *   Copyright 2006-2018 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests.sec;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import ome.api.ITypes;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.enums.AdminPrivilege;
import ome.model.enums.EventType;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.SecurityFilter;
import ome.security.SystemTypes;
import ome.security.basic.BasicACLVoter;
import ome.security.basic.BasicSecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.security.basic.EventProviderInDb;
import ome.security.basic.LightAdminPrivileges;
import ome.security.basic.NodeProviderInDb;
import ome.security.basic.OmeroInterceptor;
import ome.security.basic.OneGroupSecurityFilter;
import ome.security.basic.TokenHolder;
import ome.security.policy.DefaultPolicyService;
import ome.server.utests.DummyExecutor;
import ome.server.utests.TestSessionCache;
import ome.services.sessions.SessionManager;
import ome.services.sessions.SessionProvider;
import ome.services.sessions.SessionProviderInDb;
import ome.services.sessions.stats.NullSessionStats;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.testing.MockServiceFactory;
import ome.tools.hibernate.ExtendedMetadata;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractBasicSecuritySystemTest extends
        MockObjectTestCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

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

        cd = new CurrentDetails(new TestSessionCache(this));
        SystemTypes st = new SystemTypes();
        TokenHolder th = new TokenHolder();
        final Roles roles = new Roles();
        final LightAdminPrivileges mockAdminPrivileges = new LightAdminPrivileges(roles) {
            @Override
            public ImmutableSet<AdminPrivilege> getSessionPrivileges(Session session) {
                return getAllPrivileges();
            }
        };
        final Executor executor = new DummyExecutor(null, sf);
        final SessionProvider sessionProvider = new SessionProviderInDb(roles, new NodeProviderInDb("", executor), executor);
        OmeroInterceptor oi = new OmeroInterceptor(roles,
                st, new ExtendedMetadata.Impl(),
                cd, th, new NullSessionStats(),
                mockAdminPrivileges, null, new HashSet<String>(), new HashSet<String>());
        SecurityFilter filter = new OneGroupSecurityFilter();
        sec = new BasicSecuritySystem(oi, st, cd, mgr, sessionProvider, new EventProviderInDb(sf), roles, sf,
                th, Collections.singletonList(filter), new DefaultPolicyService(), aclVoter);
        aclVoter = new BasicACLVoter(cd, st, th, filter);
    }

    protected void prepareMocksWithUserDetails(boolean readOnly) {
        prepareMocksWithUserDetails(readOnly, Permissions.WORLD_WRITEABLE);
    }

    protected void prepareMocksWithUserDetails(boolean readOnly, Permissions perms) {
        // login
        p = new Principal("test", "test", "test");
        sec.login(p);

        // context
        user = new Experimenter(2L, true);
        group = new ExperimenterGroup(2L, true); // first non-"user" group
        group.getDetails().setPermissions(perms);
        type = new EventType(1L, true);
        type.setValue("test");
        event = new Event(1L, true);
        event.setType(type);
        event.setSession(new Session(1L, true));

        user.linkExperimenterGroup(group);
        leaderOfGroups = Collections.singletonList(1L);
        memberOfGroups = Collections.singletonList(1L);

        mockEc.expects(atLeastOnce()).method("getCurrentEventId").will(
                returnValue(1L));
        mockEc.expects(atLeastOnce()).method("isReadOnly").will(
                returnValue(readOnly));
        mockEc.expects(atLeastOnce()).method("isCurrentUserAdmin").will(
                returnValue(false));
        mockEc.expects(atLeastOnce()).method("getCurrentAdminPrivileges").will(
                returnValue(Collections.emptySet()));
        mockEc.expects(atLeastOnce()).method("getCurrentGroupPermissions").will(
                returnValue(Permissions.WORLD_WRITEABLE));
        mockEc.expects(atLeastOnce()).method("getCurrentEventType").will(
                returnValue("Test"));
        mockEc.expects(atLeastOnce()).method("getCurrentShareId").will(
                returnValue(null));
        mockEc.expects(atLeastOnce()).method("getCurrentSessionUuid").will(
                returnValue("session-uuid"));
        mockEc.expects(atLeastOnce()).method("getCurrentSessionId").will(
                returnValue(1L));
        mockEc.expects(atLeastOnce()).method("getCurrentUserId").will(
                returnValue(2L));
        mockEc.expects(atLeastOnce()).method("getCurrentUserName").will(
                returnValue("some-user"));
        mockEc.expects(atLeastOnce()).method("getCurrentSudoerId").will(
                returnValue(null));
        mockEc.expects(atLeastOnce()).method("getCurrentSudoerName").will(
                returnValue(null));
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
        group.getDetails().setPermissions(Permissions.WORLD_WRITEABLE);
        type = new EventType(0L, true);
        type.setValue("test");
        event = new Event(0L, true);
        event.setType(type);
        event.setSession(new Session(1L, true));

        user.linkExperimenterGroup(group);
        leaderOfGroups = Collections.singletonList(0L);
        memberOfGroups = Arrays.asList(0L, 1L);

        mockEc.expects(atLeastOnce()).method("getCurrentEventId").will(
                returnValue(1L));
        mockEc.expects(atLeastOnce()).method("isReadOnly").will(
                returnValue(readOnly));
        mockEc.expects(atLeastOnce()).method("isCurrentUserAdmin").will(
                returnValue(true));
        mockEc.expects(atLeastOnce()).method("getCurrentAdminPrivileges").will(
                returnValue(LightAdminPrivileges.getAllPrivileges()));
        mockEc.expects(atLeastOnce()).method("getCurrentGroupPermissions").will(
                returnValue(Permissions.WORLD_WRITEABLE));
        mockEc.expects(atLeastOnce()).method("getCurrentEventType").will(
                returnValue("Test"));
        mockEc.expects(atLeastOnce()).method("getCurrentUserName").will(
                returnValue("some-user"));
        mockEc.expects(atLeastOnce()).method("getCurrentSudoerName").will(
                returnValue(null));
        mockEc.expects(atLeastOnce()).method("getCurrentGroupName").will(
                returnValue("test"));
        mockEc.expects(atLeastOnce()).method("getCurrentShareId").will(
                returnValue(null));
        mockEc.expects(atLeastOnce()).method("getCurrentSessionUuid").will(
                returnValue("session-uuid"));
        mockEc.expects(atLeastOnce()).method("getCurrentSessionId").will(
                returnValue(1L));
        mockEc.expects(atLeastOnce()).method("getCurrentUserId").will(
                returnValue(0L));
        mockEc.expects(atLeastOnce()).method("getCurrentSudoerId").will(
                returnValue(null));
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
        sf.mockAdmin.expects(once()).method("groupProxy").will(
                returnValue(group));
        if (!readOnly) {
            sf.mockQuery.expects(once()).method("findByQuery").will(
                    returnValue(event.getSession()));
            sf.mockQuery.expects(once()).method("find").will(
                    returnValue(event.getSession()));
            sf.mockAdmin.expects(once()).method("userProxy").will(
                    returnValue(user));
            sf.mockUpdate.expects(once()).method("saveAndReturnObject").will(
                    returnValue(event));
        }
    }

    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
        super.verify();
        try {
            sec.invalidateEventContext();
        } catch (NoSuchElementException nsee) {
            log.warn("Never managed to login?!?");
        }
        super.tearDown();
    }

}
