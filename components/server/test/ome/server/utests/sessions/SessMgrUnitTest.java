/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import java.util.Collections;
import java.util.List;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionException;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.services.sessions.SessionManagerImpl;
import ome.system.Principal;
import ome.system.Roles;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessMgrUnitTest extends MockObjectTestCase {

    private static class TestSessionManager extends SessionManagerImpl {

    }

    private Mock adminMock, updateMock, queryMock;
    private TestSessionManager mgr;
    private Session session;

    @BeforeTest
    public void config() {
        mgr = new TestSessionManager();
        mgr.setRoles(new Roles());

        session = new Session();
        session.setUuid("uuid");
        session.setId(1L);
    }

    Principal principal = new Principal("u", "g", "Test");
    String credentials = "password";
    Experimenter user = new Experimenter(1L, true);
    ExperimenterGroup group = new ExperimenterGroup(1L, true);
    List<Long> m_ids = Collections.singletonList(1L);
    List<Long> l_ids = Collections.singletonList(1L);
    List<String> userRoles = Collections.singletonList("single");

    void prepareSessionCreation() {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(true));
        adminMock.expects(once()).method("userProxy").will(returnValue(user));
        adminMock.expects(once()).method("groupProxy").will(returnValue(group));
        adminMock.expects(once()).method("getMemberOfGroupIds").will(
                returnValue(m_ids));
        adminMock.expects(once()).method("getLeaderOfGroupIds").will(
                returnValue(l_ids));
        adminMock.expects(once()).method("getUserRoles").will(
                returnValue(userRoles));
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(true));
        queryMock.expects(once()).method("findAllByQuery")
                .will(
                        returnValue(Collections
                                .singletonList(new ExperimenterGroup())));
        updateMock.expects(once()).method("saveObject");
    }

    @Test
    public void testCreateNewSession() throws Exception {
        /*
         * user logs in with: - non-sessioned principal / !E session =>
         * createSession - non-sessioned principal / E session => joinSession -
         * sessionedPrincipal => hasSession() != null, then ok
         */

        prepareSessionCreation();
        session = mgr.create(principal, credentials);
        assertNotNull(session);
        assertNotNull(session.getUuid());

    }

    @Test
    public void testThatPermissionsAreAlreadyNonReadable() throws Exception {
        testCreateNewSession();
        Permissions p = session.getDetails().getPermissions();
        assertFalse(p.isGranted(Role.GROUP, Right.READ));
        assertFalse(p.isGranted(Role.GROUP, Right.WRITE));
        assertFalse(p.isGranted(Role.WORLD, Right.READ));
        assertFalse(p.isGranted(Role.WORLD, Right.WRITE));
    }

    @Test
    public void testThatDefaultPermissionsAreAssigned() throws Exception {
        testCreateNewSession();
        assertNotNull(session.getDefaultPermissions());
    }

    @Test
    public void testThatDefaultTypeAreAssigned() throws Exception {
        testCreateNewSession();
        assertNotNull(session.getDefaultEventType());
    }

    @Test
    public void testThatStartedTimeIsSet() throws Exception {
        testCreateNewSession();
        assertNotNull(session.getStarted());
        assertTrue(session.getStarted().getTime() <= System.currentTimeMillis());
    }

    @Test
    public void testThatCreatedSessionIsFindable() throws Exception {
        testCreateNewSession();
        // now user has an active session, so basic security wiring will
        // allow method executions
        assertNotNull(mgr.find(session.getUuid()));
    }

    @Test
    public void testThatCreatedSessionIsUpdateable() throws Exception {

        Session rv = new Session();

        testCreateNewSession();
        session.setDefaultEventType("somethingnew");
        updateMock.expects(once()).method("saveAndReturnObject").will(
                returnValue(rv));
        Session test = mgr.update(session);
        assertTrue(test == rv); // insists that a copy is performed
    }

    @Test(expectedExceptions = SessionException.class)
    public void testThatNonexteantSessionIsNOTUpdateable() throws Exception {
        session.setUuid("DoesNotExist");
        Session test = mgr.update(session);
    }

    @Test
    public void testThatCopiesHaveAllTheRightFields() throws Exception {
        testCreateNewSession();
        Session copy = mgr.copy(session);
        assertFalse(copy == session);
        assertNotNull(copy.getId());
        assertNotNull(copy.getStarted());
        assertNotNull(copy.getUuid());
        assertNotNull(copy.getDefaultEventType());
        assertNotNull(copy.getDefaultPermissions());
        assertNotNull(copy.getDetails().getPermissions());
    }

    @Test
    public void testThatUpdateProperlyHandlesDetails() throws Exception {
        testCreateNewSession();

        Session updated = new Session();
        updated.getDetails().copy(Details.create()); // As close to nulll as
        // poss

    }

    @Test
    public void testThatTimedOutSessionsAreMarkedAsSuch() throws Exception {
        // With a restricted cache something should get push out.
        // mgr.setCache(new TestCache("quick", 1, 0, 0, null));

        prepareSessionCreation();
        Session s1 = mgr.create(principal, credentials);

        prepareSessionCreation();
        Session s2 = mgr.create(principal, credentials);
        assertTrue(mgr.find(s1.getUuid()) == null
                || mgr.find(s2.getUuid()) == null);
    }

    @Test
    public void testThatManagerCanCloseASession() throws Exception {
        testCreateNewSession();
        Constraint closedSession = new Constraint() {
            public boolean eval(Object arg0) {
                Session s = (Session) arg0;
                return (System.currentTimeMillis() - s.getClosed().getTime()) < 1000L;
            }

            public StringBuffer describeTo(StringBuffer arg0) {
                return arg0.append("closed time was less than 1 sec ago");
            }

        };
        updateMock.expects(once()).method("saveObject").with(closedSession);
        mgr.close(session.getUuid());
        assertNull(mgr.find(session.getUuid()));

    }

    @Test
    public void testThatManagerHandleAnExceptionOnClose() throws Exception {
        testCreateNewSession();
        Constraint closedSession = new Constraint() {
            public boolean eval(Object arg0) {
                Session s = (Session) arg0;
                return (System.currentTimeMillis() - s.getClosed().getTime()) < 1000L;
            }

            public StringBuffer describeTo(StringBuffer arg0) {
                return arg0.append("closed time was less than 1 sec ago");
            }

        };
        updateMock.expects(once()).method("saveObject").with(closedSession);
        mgr.close(session.getUuid());
        assertNull(mgr.find(session.getUuid()));
        fail("NYI");
    }

    @Test
    public void testThatManagerKeepsUpWithRolesPerSession() throws Exception {
        testCreateNewSession();

        List<String> roles = mgr.getUserRoles(session.getUuid());
        assertNotNull(roles);
        assertTrue(roles.size() > 0);
    }

    @Test
    public void testThatManagerCanHandleEvent() throws Exception {
        testCreateNewSession();
        List<String> preUserRoles = mgr.getUserRoles(session.getUuid());
        mgr.onApplicationEvent(null);
        List<String> postUserRoles = mgr.getUserRoles(session.getUuid());
        fail("Should here remove user from group and have roles updated."
                + "--depends on whether or not we need to catch the hiberate events"
                + "--or just events from AdminImpl");
    }

    @Test
    public void testReplacesNullGroupAndType() throws Exception {
        prepareForCreateSession();
        Session session = mgr.create(new Principal("fake", null, null),
                credentials);
        assertNotNull(session.getDefaultEventType());
        assertNotNull(session.getDetails().getGroup());
    }

    void prepareForCreateSession() {
        adminMock.expects(once()).method("userProxy").will(returnValue(user));
        adminMock.expects(once()).method("groupProxy").will(returnValue(group));
        adminMock.expects(once()).method("getMemberOfGroupIds").will(
                returnValue(m_ids));
        adminMock.expects(once()).method("getLeaderOfGroupIds").will(
                returnValue(l_ids));
        adminMock.expects(once()).method("getUserRoles").will(
                returnValue(userRoles));
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(true));
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testCreateSessionFailsAUEOnNullPrincipal() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(false));
        mgr.create(null, "password");
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testCreateSessionFailsAUEOnNullOmeName() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(false));
        mgr.create(new Principal(null, null, null), "password");
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testCreateSessionFailsSV() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(false));
        mgr.create(principal, "password");
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testChecksForDefaultGroupsOnCreation() throws Exception {
        prepareForCreateSession();
        queryMock.expects(once()).method("findAllByQuery").will(
                returnValue(Collections.EMPTY_LIST));
        mgr.create(new Principal("user", "user", "User"), "user");
    }

    @Test
    public void testShouldManagerDisallowMultipleSessions() throws Exception {
        fail("DECIDE");
    }

    @Test
    public void testWhatHappensIfAnEventOccursDuringUpdateEtc()
            throws Exception {
        fail("NYI");
    }

    @Test
    public void testcreationChecksIfUserIsMemberOfGroup() throws Exception {
        fail("NYI");
    }

    @Test
    public void testUpdateChecksIfUserIsMemberOfGroup() throws Exception {
        fail("NYI");
    }
}
