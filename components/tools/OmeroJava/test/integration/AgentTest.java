/*
 *   Copyright 2019 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import org.testng.annotations.BeforeClass;

import omero.cmd.UpdateSessionTimeoutRequest;
import omero.cmd.Response;
import omero.model.Session;
import omero.sys.EventContext;
import omero.sys.Principal;
import omero.api.IConfigPrx;
import omero.api.ISessionPrx;
import omero.cmd.OK;

import static omero.rtypes.rlong;

/**
 * Tests for changing the UserAgent when creating sessions
 *
 */
public class AgentTest extends AbstractServerTest {

    protected static final String TEST_AGENT = "sessionTestAgent";

    protected static final Map<String, String> TEST_AGENT_CONTEXT =
            ImmutableMap.of(omero.constants.AGENT.value, TEST_AGENT);

    /** Helper reference to the <code>IConfig</code> service. */
    protected IConfigPrx iConfig;

    /** Helper reference to the <code>ISession</code> service. */
    protected ISessionPrx iSession;

    /** Helper reference to <code>EventContext</code> of the active user. */
    protected EventContext ctx;

    /** Server default session time to live. */
    protected long timeToLive;

    /** Server default session time to idle. */
    protected long timeToIdle;

    /** Server default user maximum time to live. */
    protected long maxUserTimeToLive;

    /** Server default user maximum time to live. */
    protected long maxUserTimeToIdle;

    @BeforeClass
    public void setup() throws Exception {
        ctx = newUserAndGroup("rw----");
        iConfig = factory.getConfigService();
        iSession = factory.getSessionService();
        timeToLive = Long.parseLong(iConfig.getConfigValue(
                "omero.sessions.maximum"));
        timeToIdle = Long.parseLong(iConfig.getConfigValue(
                "omero.sessions.timeout"));
        maxUserTimeToLive = Long.parseLong(iConfig.getConfigValue(
                "omero.sessions.max_user_time_to_live"));
        maxUserTimeToIdle = Long.parseLong(iConfig.getConfigValue(
                "omero.sessions.max_user_time_to_idle"));
    }

    @Test
    public void testSetAgent() throws Exception {
        Session session = iSession.createUserSession(
                0, 2000, ctx.groupName, TEST_AGENT_CONTEXT);
        Assert.assertEquals(TEST_AGENT, session.getUserAgent().getValue());
    }

    @Test
    public void testSetAgentCreateSessionWithTimeout() throws Exception {
        Principal principal = new Principal(
                ctx.userName, ctx.groupName, "Test", ctx.groupPermissions);
        ISessionPrx iSession = root.getSession().getSessionService();
        Session session = iSession.createSessionWithTimeout(
                principal, 1000, TEST_AGENT_CONTEXT);
        Assert.assertEquals(TEST_AGENT, session.getUserAgent().getValue());
    }

    @Test
    public void testSetAgentCreateSessionWithTimeouts() throws Exception {
        Principal principal = new Principal(
                ctx.userName, ctx.groupName, "Test", ctx.groupPermissions);
        ISessionPrx iSession = root.getSession().getSessionService();
        Session session = iSession.createSessionWithTimeouts(
                principal, 1000, 2000, TEST_AGENT_CONTEXT);
        Assert.assertEquals(TEST_AGENT, session.getUserAgent().getValue());
    }

    @Test
    public void testUpdateSessionTimeToIdleAndTimeToLive() throws Exception {
        Session session = iSession.createUserSession(
                timeToLive, timeToIdle, ctx.groupName);
        System.err.println(session.getUuid().getValue());
        UpdateSessionTimeoutRequest change = new UpdateSessionTimeoutRequest(
                session.getUuid().getValue(), rlong(timeToIdle + 1),
                rlong(timeToIdle + 2));
        Response response = doChange(change);
        Assert.assertTrue(response instanceof OK);
        session = iSession.getSession(session.getUuid().getValue());
        Assert.assertEquals(session.getTimeToLive().getValue(), timeToIdle + 1);
        Assert.assertEquals(session.getTimeToIdle().getValue(), timeToIdle + 2);
    }

    @Test
    public void testUpdateSessionTimeToIdleBeyondMaximum() throws Exception {
        Session session = iSession.createUserSession(
                timeToLive, timeToIdle, ctx.groupName);
        UpdateSessionTimeoutRequest change = new UpdateSessionTimeoutRequest(
                session.getUuid().getValue(), rlong(timeToLive),
                rlong(maxUserTimeToIdle + 1));
        Response response = doChange(change);
        Assert.assertTrue(response instanceof OK);
        session = iSession.getSession(session.getUuid().getValue());
        Assert.assertEquals(session.getTimeToLive().getValue(), timeToLive);
        Assert.assertEquals(session.getTimeToIdle().getValue(),
                maxUserTimeToIdle);
    }

    @Test(expectedExceptions=AssertionError.class)
    public void testNonAdminDisabling() throws Exception {
        Session session = iSession.createUserSession(
                timeToLive, timeToIdle, ctx.groupName);
        long timeToLive = Long.parseLong(iConfig.getConfigValue(
                "omero.sessions.timeout"));
        UpdateSessionTimeoutRequest change = new UpdateSessionTimeoutRequest(
                session.getUuid().getValue(), rlong(timeToLive), rlong(0));
        doChange(change);
    }

}
