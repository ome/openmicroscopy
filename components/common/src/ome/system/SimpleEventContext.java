/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.model.internal.Permissions;

/**
 * simple, non-thread-safe, serializable {@link ome.system.EventContext}
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec
 *          2006) $
 * @see EventContext
 * @since 3.0
 */
@RevisionDate("$Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1167 $")
public class SimpleEventContext implements EventContext, Serializable {

    private static final long serialVersionUID = -391820349350359539L;

    protected Long shareId;

    protected Long csId;

    protected Long cgId;

    protected Long cuId;

    protected Long ceId;

    protected String csName;

    protected String cgName;

    protected String cuName;

    protected String ceType;

    protected boolean isAdmin;

    protected boolean isReadOnly;

    protected List<Long> memberOfGroups;

    protected List<Long> leaderOfGroups;

    protected Permissions umask;

    private Permissions groupPermissions;

    /** Constructor for subclasses */
    protected SimpleEventContext() {
    }

    /** copy constructor. Makes defensive copies where necessary */
    public SimpleEventContext(EventContext ec) {
        if (ec == null) {
            throw new IllegalArgumentException("Argument cannot be null.");
        }
        copy(ec);
    }

    /**
     * Copies all values directly from the given instance into this instance. If
     * any of {@link #getCurrentEventId()}, {@link #isCurrentUserAdmin()},
     * {@link #isReadOnly()}, or {@link #getCurrentUmask()} throws an
     * exception, those fields will remain null assuming that the
     * {@code ome.security.SecuritySystem} will reload them later.
     */
    protected void copy(EventContext ec) {
        this.shareId = ec.getCurrentShareId();
        this.csId = ec.getCurrentSessionId();
        this.cgId = ec.getCurrentGroupId();
        this.cuId = ec.getCurrentUserId();
        this.csName = ec.getCurrentSessionUuid();
        this.cgName = ec.getCurrentGroupName();
        this.cuName = ec.getCurrentUserName();
        this.ceType = ec.getCurrentEventType();
        this.memberOfGroups = new ArrayList<Long>(ec.getMemberOfGroupsList());
        this.leaderOfGroups = new ArrayList<Long>(ec.getLeaderOfGroupsList());
        setGroupPermissions(ec.getCurrentGroupPermissions());
        try {
            this.isAdmin = ec.isCurrentUserAdmin();
            this.isReadOnly = ec.isReadOnly();
            this.ceId = ec.getCurrentEventId();
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }

    public Long getCurrentShareId() {
        return shareId;
    }

    public Long getCurrentSessionId() {
        return csId;
    }

    public String getCurrentSessionUuid() {
        return csName;
    }

    public Long getCurrentGroupId() {
        return cgId;
    }

    public String getCurrentGroupName() {
        return cgName;
    }

    public Long getCurrentUserId() {
        return cuId;
    }

    public String getCurrentUserName() {
        return cuName;
    }

    public boolean isCurrentUserAdmin() {
        return isAdmin;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public List<Long> getMemberOfGroupsList() {
        return memberOfGroups;
    }

    public List<Long> getLeaderOfGroupsList() {
        return leaderOfGroups;
    }

    public Long getCurrentEventId() {
        return ceId;
    }

    public String getCurrentEventType() {
        return ceType;
    }

    public Permissions getCurrentUmask() {
        return umask;
    }

    public Permissions getCurrentGroupPermissions() {
        return groupPermissions;
    }

    protected void setGroupPermissions(Permissions p) {
        this.groupPermissions = p;
        if (this.groupPermissions == null) {
            throw new ome.conditions.InternalException("null permissions");
        }
    }

}
