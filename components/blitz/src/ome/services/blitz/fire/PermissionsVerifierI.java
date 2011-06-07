/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.SessionException;
import ome.model.meta.Session;
import ome.services.sessions.SessionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Glacier2._PermissionsVerifierDisp;
import Ice.Current;
import Ice.StringHolder;

/**
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2006-12-15 12:28:54 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1175 $")
public class PermissionsVerifierI extends _PermissionsVerifierDisp {

    private final static Log log = LogFactory
            .getLog(PermissionsVerifierI.class);

    private final Ring ring;

    private final SessionManager manager;

    public PermissionsVerifierI(Ring ring, SessionManager manager) {
        this.ring = ring;
        this.manager = manager;
    }

    public boolean checkPermissions(String userId, String password,
            StringHolder reason, Current __current) {

        try {
            // ticket:2212, ticket:3652
            //
            // At the very first, check if the userId or password is
            // actually a session id, if so we enforce that the userId
            // and the password are identical, becauser we have no other
            // method of signalling to the SessionManagerI instance that
            // the user has not provided a real password
            //
            // The addition of the userId check was added from an odd
            // case in which root added a username equal to the current
            // session. This prevents any LDAP lookup etc. from happening,
            // but by "giving" the user a session id (passed via the username)
            // s/he **will** be able to login **AS ROOT**!
            //

            Object session = null;

            // Local checks are faster.
            try {
                session = manager.find(password);
            } catch (SessionException e) {
                // pass
            }

            if (session == null) {
                try {
                    session = manager.find(userId); // ticket:3652
                } catch (SessionException e) {
                    // pass
                }
            }

            // If that doesn't work, make sure that the cluster doesn't know
            // something this instance doesn't. (Default of nullRedirector
            // returns null immediately)
            if (session == null) {
                if (ring.checkPassword(userId)) {
                    session = "ring.checkPassword(userId)";
                }
            }

            if (session == null) {
                if (ring.checkPassword(password)) {
                    session = "ring.checkPassword(password)";
                }
            }


            // If any of the above blocks returned a valid value
            // then the password and/or userId matches an active
            // session. As long as userId == password, we return
            // true. Otherwise we return false, otherwise it
            // would be possible to circumvent the @HasPassword
            // restrictions.
            if (session != null) {
                if (userId.equals(password)) {
                    return true;
                } else {
                    log.warn("username and password don't match: " + userId);
                    reason.value = "username and password must be equal; use joinSession";
                    return false;
                }
            }

            // First check locally. Since we typically use redirects in the
            // cluster, it's most likely that our password will be in memory
            // in this instance.
            if (manager.executePasswordCheck(userId, password)) {
                return true;
            } else {
                reason.value = "Password check failed";
                return false;
            }

        } catch (Throwable t) {
            reason.value = "Internal error. Please contact your administrator:\n"
                    + t.getMessage();
            log.error("Exception thrown while checking password for:" + userId,
                    t);
            return false;
        }
    }

}
