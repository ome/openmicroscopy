/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.redirect;

import java.util.Set;

import ome.model.meta.Node;
import ome.services.blitz.fire.Ring;
import ome.services.blitz.fire.SessionManagerI;
import ome.system.Principal;
import Glacier2.CannotCreateSessionException;
import Glacier2.SessionPrx;

/**
 * Strategy interface for helping a {@link Ring} instance decide whether to
 * redirect {@link SessionPrx} creation to another {@link SessionManagerI}
 * instance. The {@link Ring} instance is passed in
 * 
 *@since Beta-4.0-RC2
 */
public interface Redirector {

    /**
     * Interface implemented by Ring instances to allow passing in the context
     * necessary for making strategy decisions.
     */
    public interface Context {

        /**
         * The UUID for the local node which will be used as the redirect lookup
         * string for this {@link Context}.
         */
        String uuid();

        /**
         * String representation of the proxy to the local node which may be
         * contacted to create new sessions.
         * 
         * @return See above.
         */
        String getDirectProxy();

        /**
         * {@link Principal} instance which can be used for internal calls the
         * Executor.
         */
        Principal principal();

        /**
         * Active communicator for use by the {@link Redirector} instance.
         */
        Ice.Communicator getCommunicator();
        
        /**
         * Return all known managers in the current cluster context, possibly
         * filtering out the inactive ones.
         */
        Set<String> getManagerList(boolean activeOnly);
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
    public SessionPrx getProxyOrNull(Context context, String userId,
            Glacier2.SessionControlPrx control, Ice.Current current)
            throws CannotCreateSessionException;

    /**
     * Gives the {@link Redirector} a chance to configure the next appropriate
     * redirect based on the {@link Set} of current {@link Node} uuids.
     */
    public void chooseNextRedirect(Context context, Set<String> nodeUuids);

    /**
     * Gives the {@link Redirector} a chance to remove the current {@link Ring}
     * when it is being shutdown.
     */
    public void handleRingShutdown(Context context, String uuid);

}