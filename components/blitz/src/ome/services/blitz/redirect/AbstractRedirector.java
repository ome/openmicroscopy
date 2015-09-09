/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.redirect;

import ome.api.IConfig;
import ome.model.meta.Node;
import ome.parameters.Parameters;
import ome.services.blitz.fire.Ring;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import omero.constants.cluster.REDIRECT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Glacier2.CannotCreateSessionException;
import Glacier2.SessionControlPrx;
import Glacier2.SessionManagerPrx;
import Glacier2.SessionManagerPrxHelper;
import Glacier2.SessionPrx;
import Ice.Current;

/**
 * Base {@link Redirector}
 * 
 *@since Beta-4.0-RC2
 */
public abstract class AbstractRedirector implements Redirector {

    protected final static String ROUTED_FROM = "omero.routed_from";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final Executor executor;

    public AbstractRedirector(Executor ex) {
        this.executor = ex;
    }

    /**
     * Returns null early if the {@link Ice.Current} has already once been
     * routed by a {@link Redirector} implementation, returns an existing
     * session if it can be found, or returns a null.
     * 
     * @see #ROUTED_FROM
     */
    public SessionPrx getProxyOrNull(Context ctx, String userId,
            SessionControlPrx control, Current current)
            throws CannotCreateSessionException {

        if (alreadyRouted(current)) {
            log.info("Session creation already routed once for " + userId);
            return null;
        } else {
            // Check if the session is in ring
            String proxyString = proxyForSession(ctx, userId);
            if (proxyString != null
                    && !proxyString.equals(ctx.getDirectProxy())) {
                log.info(String.format("Returning remote session on %s",
                        proxyString));
                return obtainProxy(proxyString, ctx, userId, control, current);
            } else {
                return null;
            }
        }

    }

    protected boolean alreadyRouted(Ice.Current current) {
        if (current != null) {
            if (current.ctx != null) {
                return current.ctx.containsKey(ROUTED_FROM);
            }
        }
        return false;
    }

    protected SessionPrx obtainProxy(String proxyString, Context ctx,
            String userId, SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {
        if (proxyString != null) {
            current.ctx.put("omero.routed_from", ctx.getDirectProxy());
            Ice.ObjectPrx remote = ctx.getCommunicator().stringToProxy(
                    proxyString);
            SessionManagerPrx sessionManagerPrx = SessionManagerPrxHelper
                    .checkedCast(remote);
            try {
                return sessionManagerPrx.create(userId, control, current.ctx);
            } catch (Exception e) {
                if (e instanceof CannotCreateSessionException) {
                    throw (CannotCreateSessionException) e;
                } else {
                    log.error("Error while routing to " + remote, e);
                    throw new CannotCreateSessionException(
                        "Error while routing to remote blitz");
                }
            }
        }
        return null;
    }

    /**
     * Returns the current redirect, to which all calls to
     * {@link #getProxyOrNull(Context, String, Glacier2.SessionControlPrx, Ice.Current)}
     * will be pointed. May be null, but is typically set to a non-null value
     * when the first {@link Ring} joins the cluster.
     */
    protected String getRedirect(Context ctx) {
        return (String) executor.execute(ctx.principal(),
                new Executor.SimpleWork(this, "getRedirect") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getConfigService().getConfigValue(
                                REDIRECT.value);
                    }
                });
    }

    /**
     * Set the new redirect value if null, or if the uuid is null or empty, then
     * the existing redirect will be removed. Otherwise the value is set if it
     * is currently missing.
     */
    protected boolean initializeRedirect(Context ctx, final String managerUuid) {
        return (Boolean) executor.execute(ctx.principal(),
                new Executor.SimpleWork(this, "setRedirect") {
                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        IConfig config = sf.getConfigService();
                        if (managerUuid == null || managerUuid.length() == 0) {
                            config.setConfigValue(REDIRECT.value, null);
                            return true;
                        } else {
                            return config.setConfigValueIfEquals(
                                    REDIRECT.value, managerUuid, null);
                        }
                    }
                });
    }

    protected String findProxy(Context ctx, final String redirect) {
        final String query = "select node from Node node where node.uuid = :uuid";
        return nodeProxyQuery(ctx, redirect, query);
    }

    protected String proxyForSession(Context ctx, final String sessionUuid) {
        final String query = "select node from Node node "
                + "join node.sessions as s where s.uuid = :uuid";
        return nodeProxyQuery(ctx, sessionUuid, query);
    }

    protected String nodeProxyQuery(Context ctx, final String uuid,
            final String query) {
        return (String) executor.execute(ctx.principal(),
                new Executor.SimpleWork(this, "nodeProxyQuery") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        Parameters p = new Parameters().addString("uuid", uuid);
                        Node node = sf.getQueryService().findByQuery(query, p);
                        if (node == null) {
                            return null;
                        } else {
                            return node.getConn();
                        }
                    }
                });
    }
}
