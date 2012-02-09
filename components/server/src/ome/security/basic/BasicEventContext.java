/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.services.messages.RegisterServiceCleanupMessage;
import ome.services.sessions.stats.SessionStats;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.SimpleEventContext;

/**
 * {@link EventContext} implementation for use within the security system. Holds
 * various other information needed for proper functioning of a {@link Thread}.
 * 
 * Not-thread-safe. Intended to be held by a {@link ThreadLocal}
 */
class BasicEventContext extends SimpleEventContext {

    private final static Log log = LogFactory.getLog(BasicEventContext.class);

    // Additions beyond simple event context
    // =========================================================================

    /**
     * Prinicpal should only be set once (on
     * {@link PrincipalHolder#login(Principal)}.
     */
    private final Principal p;
    
    private final SessionStats stats;

    private Set<String> disabledSubsystems;

    private Set<RegisterServiceCleanupMessage> serviceCleanups;

    private Set<IObject> lockCandidates;

    private List<EventLog> logs;

    private Event event;

    private Experimenter owner;

    private ExperimenterGroup group;

    private Map<String, String> callContext;

    public BasicEventContext(Principal p, SessionStats stats) {
        if (p == null || stats == null) {
            throw new RuntimeException("Principal and stats canot be null.");
        }
        this.p = p;
        this.stats = stats;
    }

    void invalidate() {
        owner = null;
        group = null;
        event = null;
    }

    /**
     * Making {@link SimpleEventContext#copy(EventContext)} available to
     * package-private classes.
     */
    void copyContext(EventContext ec) {
        super.copy(ec);
        setCallContext(callContext); // Re-apply values.
    }

    // Call Context (ticket:3529)
    // =========================================================================

    static Long parseId(Map<String, String> ctx, String key) {
        Long rv = null;
        if (ctx != null && ctx.containsKey(key)) {
            String s = ctx.get(key);
            try {
                rv = Long.valueOf(ctx.get(key));
                log.debug("Using call requested group: " + key + "=" + s);
            } catch (Exception e) {
                log.warn("Ignoring invalid requested group: " + key + "=" + s);
            }
        }
        return rv;
    }

    public Map<String, String> getCallContext() {
        return callContext;
    }

    public Map<String, String> setCallContext(Map<String, String> ctx) {
        Map<String, String> rv = callContext;
        callContext = ctx;
        Long gid = parseId(ctx, "omero.group");
        if (gid != null) {
            setGroup(new ExperimenterGroup(gid, false));
        }

        Long sid = parseId(ctx, "omero.share");
        if (sid != null) {
            setShareId(sid);
        }

        return rv;
    }

    // ~ Setters for superclass state
    // =========================================================================

    @Override
    public Permissions getCurrentUmask() {
        Permissions umask = super.getCurrentUmask();
        if (umask == null) {
            umask = new Permissions();
            setUmask(umask);
        }
        return umask;
    }

    public void setUmask(Permissions umask) {
        this.umask = umask;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }

    public void setShareId(Long id) {
        this.shareId = id;
    }

    // ~ Accessors for other state
    // =========================================================================

    public Principal getPrincipal() {
        return p;
    }
    
    public SessionStats getStats() {
        return stats;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
        this.ceId = event.getId();
        if (event.isLoaded()) {
            if (event.getType().isLoaded()) {
                this.ceType = event.getType().getValue();
            }
        }
    }

    public Experimenter getOwner() {
        return owner;
    }

    public void setOwner(Experimenter owner) {
        this.owner = owner;
        this.cuId = owner.getId();
        if (owner.isLoaded()) {
            this.cuName = owner.getOmeName();
        }
    }

    public ExperimenterGroup getGroup() {
        return group;
    }

    public void setGroup(ExperimenterGroup group) {
        this.group = group;
        this.cgId = group.getId();
        if (group.isLoaded()) {
            this.cgName = group.getName();
            this.groupPermissions = group.getDetails().getPermissions();
        }
    }

    public Set<String> getDisabledSubsystems() {
        return disabledSubsystems;
    }

    public void setDisabledSubsystems(Set<String> disabledSubsystems) {
        this.disabledSubsystems = disabledSubsystems;
    }

    public Set<RegisterServiceCleanupMessage> getServiceCleanups() {
        return serviceCleanups;
    }

    public void setServiceCleanups(
            Set<RegisterServiceCleanupMessage> serviceCleanups) {
        this.serviceCleanups = serviceCleanups;
    }

    public Set<IObject> getLockCandidates() {
        return lockCandidates;
    }

    public void setLockCandidates(Set<IObject> lockCandidates) {
        this.lockCandidates = lockCandidates;
    }

    public List<EventLog> getLogs() {
        return logs;
    }

    public void setLogs(List<EventLog> logs) {
        this.logs = logs;
    }

    // ~ Special logic for groups
    // =========================================================================

    @Override
    public List<Long> getMemberOfGroupsList() {
        return memberOfGroups;
    }

    @Override
    public List<Long> getLeaderOfGroupsList() {
        return leaderOfGroups;
    }

    public void setMemberOfGroups(List<Long> groupIds) {
        this.memberOfGroups = groupIds;
    }

    public void setLeaderOfGroups(List<Long> groupIds) {
        this.leaderOfGroups = groupIds;
    }

    // Other
    // =========================================================================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("(");
        sb.append("Principal:" + p);
        sb.append(";");
        sb.append(this.owner);
        sb.append(";");
        sb.append(this.group);
        sb.append(";");
        sb.append(this.event);
        sb.append(";");
        sb.append("ReadOnly:" + this.isReadOnly);
        sb.append(")");
        return sb.toString();
    }

}
