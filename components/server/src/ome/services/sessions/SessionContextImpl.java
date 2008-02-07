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

import ome.model.meta.Session;

public class SessionContextImpl implements SessionContext {
    private final Session session;
    private final List<Long> leaderOfGroups;
    private final List<Long> memberOfGroups;
    private final List<String> roles; /* group names for memberOfGroups */

    public SessionContextImpl(Session session, List<Long> lGroups,
            List<Long> mGroups, List<String> roles) {
        this.session = session;
        this.leaderOfGroups = Collections.unmodifiableList(new ArrayList(
                lGroups));
        this.memberOfGroups = Collections.unmodifiableList(new ArrayList(
                mGroups));
        this.roles = Collections.unmodifiableList(new ArrayList(roles));
    }

    public Session getSession() {
        return session;
    }

    public List<String> getUserRoles() {
        return roles;
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
        throw new UnsupportedOperationException();
    }

    public boolean isReadOnly() {
        throw new UnsupportedOperationException();
    }
}