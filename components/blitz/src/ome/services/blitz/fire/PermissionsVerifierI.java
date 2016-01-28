/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.ArrayList;
import java.util.List;

import ome.api.IQuery;
import ome.conditions.SecurityViolation;
import ome.conditions.SessionException;
import ome.model.meta.Experimenter;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Glacier2._PermissionsVerifierDisp;
import Ice.Current;
import Ice.StringHolder;

/**
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
public class PermissionsVerifierI extends _PermissionsVerifierDisp {

    private final static Logger log = LoggerFactory
            .getLogger(PermissionsVerifierI.class);

    private final Ring ring;

    private final SessionManager manager;

    private final Executor ex;

    private final Principal p;

    public PermissionsVerifierI(Ring ring, SessionManager manager, Executor ex, String uuid) {
        this.ring = ring;
        this.manager = manager;
        this.ex = ex;
        this.p = new Principal(uuid);
    }

    public boolean checkPermissions(final String userId, final String password,
            final StringHolder reason, final Current __current) {

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


            /*
             * ring.checkPassword calls SqlAction.activeSession
             * which logs the user id.
             *
            if (session == null) {
                if (ring.checkPassword(password)) {
                    session = "ring.checkPassword(password)";
                }
            }
            */


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
                final List<String> data = new ArrayList<String>();
                ex.execute(p, new Executor.SimpleWork("failedPassword", userId) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        IQuery q = sf.getQueryService();
                        ome.model.meta.Session s = q.findByString(
                                ome.model.meta.Session.class, "uuid", userId);

                        Experimenter e = null;
                        if (s != null) {
                            e = s.getOwner();
                            data.add(String.format("user=%s", e.getOmeName()));
                        } else {
                            e = q.findByString(Experimenter.class,
                                    "omeName", userId);
                            if (e != null) {
                                data.add(String.format("id=%s", e.getId()));
                            }
                        }

                        if (s != null) {
                            data.add(String.format("created=%s", s.getStarted()));
                            data.add(String.format("closed=%s", s.getClosed()));
                        }

                        return null;
                    }
                });

                reason.value = String.format("Password check failed for '%s': %s",
                        userId, data);

                return false;
            }

        } catch (SecurityViolation sv) {
            reason.value = sv.getMessage();
            return false;
        } catch (Throwable t) {
            reason.value = "Internal error. Please contact your administrator:\n"
                    + t.getMessage();
            log.error("Exception thrown while checking password for:" + userId,
                    t);
            return false;
        }
    }

}
