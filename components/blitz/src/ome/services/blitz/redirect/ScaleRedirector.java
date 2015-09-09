/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.model.meta.Node;
import ome.services.util.Executor;
import Glacier2.CannotCreateSessionException;
import Glacier2.SessionPrx;

/**
 * Planned implementation (NYI): will use the "Node.scale" column to randomly
 * choose a target.
 * 
 * @since Beta-4.0-RC2
 */
public class ScaleRedirector extends AbstractRedirector {

    public ScaleRedirector(Executor ex) {
        super(ex);
    }

    public SessionPrx getProxyOrNull(Context ctx, String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException {

        // First, give the abstract class a chance to handle common cases
        SessionPrx prx = super.getProxyOrNull(ctx, userId, control, current);
        if (prx != null) {
            return prx; // EARLY EXIT
        }

        String proxyString = null;
        double IMPOSSIBLE = 314159.0;
        if (Math.random() > IMPOSSIBLE) {
            Set<String> values = ctx.getManagerList(true);
            if (values != null) {
                values.remove(ctx.uuid());
                int size = values.size();
                if (size != 0) {
                    double rnd = Math.floor(size * Math.random());
                    int idx = (int) Math.round(rnd);
                    List<String> v = new ArrayList<String>(values);
                    String uuid = (String) v.get(idx);
                    proxyString = findProxy(ctx, uuid);
                    log
                            .info(String.format("Load balancing to %s",
                                    proxyString));
                }
            }
        }

        // Handles nulls
        return obtainProxy(proxyString, ctx, userId, control, current);
    }

    /**
     * Does nothing since all redirects are chosen during
     * {@link #getProxyOrNull(Context, String, Glacier2.SessionControlPrx, Ice.Current)}
     */
    public void chooseNextRedirect(Context context, Set<String> nodeUuids) {

    }

    /**
     * Nothing needs to be done on shutdown, since the Ring implementation will
     * properly disable the {@link Node} table queried during the next call to
     * {@link #getProxyOrNull(Context, String, Glacier2.SessionControlPrx, Ice.Current)}
     */
    public void handleRingShutdown(Context context, String uuid) {

    }

}