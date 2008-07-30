/*
 * ome.security.basic.BasicEventContext
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;

/**
 * 
 */
class BasicEventContext implements EventContext {
    // this should never be null. Making private
    private Details details = Details.create();

    Long shareId;

    Permissions umask;

    boolean isAdmin = false;

    boolean isReadOnly = false;

    Collection<Long> memberOfGroups;

    Collection<Long> leaderOfGroups;

    Set<String> disabledSubsystems;

    Set<IObject> lockCandidates;

    List<EventLog> logs;

    // ~ EventContext interface
    // =========================================================================

    public Long getCurrentShareId() {
        return shareId;
    }

    public Long getCurrentSessionId() {
        Event e = getDetails().getCreationEvent();
        if (e != null && e.getSession() != null) {
            return e.getSession().getId();
        }
        return null;
    }

    public String getCurrentSessionUuid() {
        Event e = getDetails().getCreationEvent();
        if (e != null && e.getSession() != null) {
            return e.getSession().getUuid();
        }
        return null;
    }

    public Long getCurrentEventId() {
        Event e = this.details.getCreationEvent();
        return e == null ? null : e.getId();
    }

    public String getCurrentEventType() {
        Event e = this.details.getCreationEvent();
        return e == null ? null : e.getType() == null ? null : e.getType()
                .getValue();
    }

    public Long getCurrentGroupId() {
        ExperimenterGroup g = this.details.getGroup();
        return g == null ? null : g.getId();
    }

    public String getCurrentGroupName() {
        ExperimenterGroup g = this.details.getGroup();
        return g == null ? null : g.getName();
    }

    public Long getCurrentUserId() {
        Experimenter e = this.details.getOwner();
        return e == null ? null : e.getId();
    }

    public String getCurrentUserName() {
        Experimenter e = this.details.getOwner();
        return e == null ? null : e.getOmeName();
    }

    public List<Long> getLeaderOfGroupsList() {
        Collection<Long> l = this.leaderOfGroups;
        if (l == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Long>(l);
    }

    public List<Long> getMemberOfGroupsList() {
        Collection<Long> l = this.memberOfGroups;
        if (l == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Long>(l);
    }

    public boolean isCurrentUserAdmin() {
        return this.isAdmin;
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    // ~ Accessors
    // =========================================================================
    public Details getDetails() {
        if (this.details == null) {
            throw new InternalException(
                    "BasicEventContext.details should never be null.");
        }
        return details;
    }

    public void setDetails(Details details) {
        if (details == null) {
            throw new ApiUsageException("Details argument cannot be null.");
        }
        this.details = details;
    }
}
