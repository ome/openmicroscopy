/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.redirect;

import java.util.Set;

import ome.api.local.LocalConfig;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import omero.constants.cluster.REDIRECT;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Glacier2.CannotCreateSessionException;
import Glacier2.SessionPrx;

/**
 * {@link Redirector} implementation which uses the {@link REDIRECT} config key
 * via {@link LocalConfig} to know which {@link ome.model.meta.Node} is currently active.
 * 
 *@since Beta-4.0-RC2
 */
public class ConfigRedirector extends AbstractRedirector {

    public ConfigRedirector(Executor ex) {
        super(ex);
    }

    /**
     * Create or retrieve and returns a {@link SessionPrx} which the current
     * method takes control of. If it is not returned, then it should be
     * properly destroyed.
     * 
     * @param userId
     *            Not null.
     * @param control
     * @param current
     * @return Possibly null.
     * @throws CannotCreateSessionException
     */
    public SessionPrx getProxyOrNull(Context ctx, String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {

        // First, give the abstract class a chance to handle common cases
        SessionPrx prx = super.getProxyOrNull(ctx, userId, control, current);
        if (prx != null) {
            return prx; // EARLY EXIT
        }

        // If by the end of this method this string is non-null, then
        // SM.create() will be called on it.
        String proxyString = null;

        // If there is a redirect, then we honor it as long as it doesn't
        // point back to us, in which case we bail.
        String redirect = getRedirect(ctx);
        if (redirect != null) {
            log.info("Found redirect: " + redirect);
            if (redirect.equals(ctx.uuid())) {
                log.info("Redirect points to this instance; setting null");
                proxyString = null;
            } else {
                proxyString = findProxy(ctx, redirect);
                if (proxyString == null || proxyString.length() == 0) {
                    log.warn("No proxy found for manager: " + redirect);
                } else {
                    log.info("Resolved redirect to: " + proxyString);
                }
            }
        }

        // Handles nulls
        return obtainProxy(proxyString, ctx, userId, control, current);
    }

    public void chooseNextRedirect(Context ctx, Set<String> nodeUuids) {
        // First remove any redirect so that new sessions
        // won't be created on the to-be-closed node.
        String redirect = getRedirect(ctx);
        if (!nodeUuids.contains(redirect)) {
            initializeRedirect(ctx, null);
        }
        initializeRedirect(ctx, ctx.uuid());
        log.info("Current redirect: " + getRedirect(ctx));

    }

    public void handleRingShutdown(final Context ctx, final String downUuid) {
        executor.execute(ctx.principal(), new Executor.SimpleWork(this,
                "removeRedirectIfEquals") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                LocalConfig config = (LocalConfig) sf.getConfigService();
                return config.setConfigValueIfEquals(REDIRECT.value, null,
                        downUuid);
            }
        });
        if (!ctx.uuid().equals(downUuid)) {
            if (initializeRedirect(ctx, ctx.uuid())) {
                log.info("Installed self as new redirect: " + ctx.uuid());
            }
        }

    }

}