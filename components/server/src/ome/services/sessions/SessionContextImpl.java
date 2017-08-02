/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ome.model.enums.AdminPrivilege;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.services.sessions.stats.SessionStats;
import ome.system.Roles;

public class SessionContextImpl implements SessionContext {
    
    private final Count count;
    private final Roles _roles;
    private final Session session;
    private final SessionStats stats;
    private final Set<AdminPrivilege> adminPrivileges;
    private final List<Long> leaderOfGroups;
    private final List<Long> memberOfGroups;
    private final List<String> roles; /* group names for memberOfGroups */
    private Long shareId = null;

    public SessionContextImpl(Session session, List<Long> lGroups,
            List<Long> mGroups, List<String> roles, SessionStats stats,
            SessionContext previous) {
        this(session, Collections.<AdminPrivilege>emptySet(), lGroups, mGroups, roles, stats, new Roles(), previous);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public SessionContextImpl(Session session, Set<AdminPrivilege> adminPrivileges, List<Long> lGroups,
            List<Long> mGroups, List<String> roles, SessionStats stats,
            Roles _roles, SessionContext previous) {
        this._roles = _roles;
        this.stats = stats;
        this.session = session;
        if (previous != null) {
            this.count = previous.count();
            this.shareId = previous.getCurrentShareId();
        } else {
            this.count = new SessionContext.Count(session.getUuid());
        }
        this.adminPrivileges = ImmutableSet.copyOf(adminPrivileges);
        this.leaderOfGroups = Collections.unmodifiableList(new ArrayList(
                lGroups));
        this.memberOfGroups = Collections.unmodifiableList(new ArrayList(
                mGroups));
        this.roles = Collections.unmodifiableList(new ArrayList(roles));

        // Force NPE
        getCurrentGroupPermissions();
    }

    public Count count() {
        return count;
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

    public Permissions getCurrentGroupPermissions() {
        return session.getDetails().getGroup().getDetails().getPermissions();
    }

    public Long getCurrentUserId() {
        return session.getDetails().getOwner().getId();
    }

    public String getCurrentUserName() {
        return session.getDetails().getOwner().getOmeName();
    }

    @Override
    public Long getCurrentSudoerId() {
        final Experimenter sudoer = session.getSudoer();
        return sudoer == null ? null : sudoer.getId();
    }

    @Override
    public String getCurrentSudoerName() {
        final Experimenter sudoer = session.getSudoer();
        return sudoer == null ? null : sudoer.getOmeName();
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

        if (memberOfGroups.contains(_roles.getSystemGroupId())) {
            return true;
        }
        return false;
    }

    @Override
    public Set<AdminPrivilege> getCurrentAdminPrivileges() {
        return adminPrivileges;
    }

    public boolean isReadOnly() {
        throw new UnsupportedOperationException();
    }

    public Permissions getCurrentUmask() {
        throw new UnsupportedOperationException();
    }
}