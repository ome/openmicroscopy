/*
 * ome.system.SimpleEventContext
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

// Java imports

// Third-party libraries

// Application-internal dependencies

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

    private static final long serialVersionUID = -3918201598642847439L;

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

    /** Constructor for subclasses */
    protected SimpleEventContext() {
    }

    /** copy constructor. Makes defensive copies where necessary */
    public SimpleEventContext(EventContext ec) {
        if (ec == null) {
            throw new IllegalArgumentException("Argument cannot be null.");
        }

        csId = ec.getCurrentSessionId();
        cgId = ec.getCurrentGroupId();
        cuId = ec.getCurrentUserId();
        ceId = ec.getCurrentEventId();
        csName = ec.getCurrentSessionUuid();
        cgName = ec.getCurrentGroupName();
        cuName = ec.getCurrentUserName();
        ceType = ec.getCurrentEventType();
        isAdmin = ec.isCurrentUserAdmin();
        isReadOnly = ec.isReadOnly();
        memberOfGroups = new ArrayList<Long>(ec.getMemberOfGroupsList());
        leaderOfGroups = new ArrayList<Long>(ec.getLeaderOfGroupsList());
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
}
