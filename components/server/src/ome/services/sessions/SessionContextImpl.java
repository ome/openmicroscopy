/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ome.model.internal.Permissions;
import ome.model.meta.Session;
import ome.services.sessions.stats.SessionStats;
import ome.system.Roles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SessionContextImpl implements SessionContext {

    private final static Log log = LogFactory.getLog(SessionContextImpl.class);
    
    private int ref = 0;
    private final Object refLock = new Object();
    private final Roles _roles;
    private final Session session;
    private final SessionStats stats;
    private final List<Long> leaderOfGroups;
    private final List<Long> memberOfGroups;
    private final List<String> roles; /* group names for memberOfGroups */
    private Long shareId = null;

    @SuppressWarnings("unchecked")
    public SessionContextImpl(Session session, List<Long> lGroups,
            List<Long> mGroups, List<String> roles, SessionStats stats) {
        this(session, lGroups, mGroups, roles, stats, new Roles());
    }

    @SuppressWarnings("unchecked")
    public SessionContextImpl(Session session, List<Long> lGroups,
            List<Long> mGroups, List<String> roles, SessionStats stats,
            Roles _roles) {
        this._roles = _roles;
        this.stats = stats;
        this.session = session;
        this.leaderOfGroups = Collections.unmodifiableList(new ArrayList(
                lGroups));
        this.memberOfGroups = Collections.unmodifiableList(new ArrayList(
                mGroups));
        this.roles = Collections.unmodifiableList(new ArrayList(roles));
    }

    public int refCount() {
        synchronized (refLock) {
            return ref;
        }
    }

    public int increment() {
        synchronized (refLock) {
            if (ref < 0) {
                ref = 1;
            } else {
                // This should never happen, but just in case
                // some loop is incrementing indefinitely.
                if (ref < Integer.MAX_VALUE) {
                    ref = ref + 1;
                } else {
                    log.error("Reference count == MAX_VALUE");
                }
            }
            return ref;
        }
    }

    public int decrement() {
        synchronized (refLock) {
            if (ref < 1) {
                ref = 0;
            } else {
                ref = ref - 1;
            }
            return ref;
        }
    }

    public SessionStats stats() {
        return stats;
    }
    
    public Session getSession() {
        return session;
    }

    public List<String> getUserRoles() {
        return roles;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getCurrentShareId() {
        return shareId;
    }

    public Long getCurrentSessionId() {
        return getSession().getId();
    }

    public String getCurrentSessionUuid() {
        return getSession().getUuid();
    }

    public Long getCurrentEventId() {
        throw new UnsupportedOperationException();
    }

    public String getCurrentEventType() {
        return session.getDefaultEventType();
    }

    public Long getCurrentGroupId() {
        return session.getDetails().getGroup().getId();
    }

    public String getCurrentGroupName() {
        return session.getDetails().getGroup().getName();
    }

    public Long getCurrentUserId() {
        return session.getDetails().getOwner().getId();
    }

    public String getCurrentUserName() {
        return session.getDetails().getOwner().getOmeName();
    }

    public List<Long> getLeaderOfGroupsList() {
        return leaderOfGroups;
    }

    public List<Long> getMemberOfGroupsList() {
        return memberOfGroups;
    }

    public boolean isCurrentUserAdmin() {
        if (_roles == null) {
            throw new UnsupportedOperationException();
        }

        if (leaderOfGroups.contains(_roles.getSystemGroupId())) {
            return true;
        }
        return false;
    }

    public boolean isReadOnly() {
        throw new UnsupportedOperationException();
    }

    public Permissions getCurrentUmask() {
        throw new UnsupportedOperationException();
    }
}