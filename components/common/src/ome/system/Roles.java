/*
 * ome.system.Roles
 *
 *   Copyright 2006-2013 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.io.Serializable;

import com.google.common.base.Predicate;

import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/**
 * encapsulates the naming scheme for critical system groups and accounts.
 * 
 * These values are also used during install to initialize the database.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see ome.model.meta.Experimenter
 * @see ome.model.meta.ExperimenterGroup
 * @since 3.0-M3
 */
public final class Roles implements Serializable {

    private static final long serialVersionUID = -2488864989534638213L;

    public final Predicate<Experimenter> IS_ROOT_USER = new Predicate<Experimenter>() {
        @Override
        public boolean apply(Experimenter experimenter) {
            return isRootUser(experimenter);
        }
    };

    public final Predicate<ExperimenterGroup> IS_USER_GROUP = new Predicate<ExperimenterGroup>() {
        @Override
        public boolean apply(ExperimenterGroup group) {
            return isUserGroup(group);
        }
    };

    public final Predicate<ExperimenterGroup> IS_SYSTEM_GROUP = new Predicate<ExperimenterGroup>() {
        @Override
        public boolean apply(ExperimenterGroup group) {
            return isSystemGroup(group);
        }
    };

    private final long rId;

    private final String rName;

    private final long sgId;

    private final String sgName;

    private final long ugId;

    private final String ugName;

    private final long guestId;

    private final String guestName;

    private final long ggId;

    private final String ggName;

    /** default constructor which assigns hard-coded values to all roles */
    public Roles() {
        long nextUserId = 0;
        long nextGroupId = 0;
        /* these must be defined in the same order as in psql-footer.vm */
        this.rId = nextUserId++;
        this.rName = "root";
        this.sgId = nextGroupId++;
        this.sgName = "system";
        this.ugId = nextGroupId++;
        this.ugName = "user";
        this.guestId = nextUserId++;
        this.guestName = "guest";
        this.ggId = nextGroupId++;
        this.ggName = "guest";
    }

    /** constructor which allows full specification of all roles */
    public Roles(long rootUserId, String rootUserName,
            long systemGroupId, String systemGroupName, long userGroupId, String userGroupName,
            long guestUserId, String guestUserName, long guestGroupId, String guestGroupName) {
        this.rId = rootUserId;
        this.rName = rootUserName;
        this.sgId = systemGroupId;
        this.sgName = systemGroupName;
        this.ugId = userGroupId;
        this.ugName = userGroupName;
        this.guestId = guestUserId;
        this.guestName = guestUserName;
        this.ggId = guestGroupId;
        this.ggName = guestGroupName;
    }

    // ~ Checks
    // =========================================================================

    public boolean isRootUser(Experimenter user) {
        return user == null || user.getId() == null ? false : user.getId()
                .equals(getRootId());
    }

    public boolean isUserGroup(ExperimenterGroup group) {
        return group == null || group.getId() == null ? false : group.getId()
                .equals(getUserGroupId());
    }

    public boolean isSystemGroup(ExperimenterGroup group) {
        return group == null || group.getId() == null ? false : group.getId()
                .equals(getSystemGroupId());
    }

    // ~ Accessors
    // =========================================================================

    /**
     * @return the id of the root user
     */
    public long getRootId() {
        return rId;
    }

    /**
     * @return the {@link Experimenter#getOmeName()} of the root user
     */
    public String getRootName() {
        return rName;
    }

    /**
     * @return the id of the guest user
     */
    public long getGuestId() {
        return guestId;
    }

    /**
     * @return the {@link Experimenter#getOmeName()} of the guest user
     */
    public String getGuestName() {
        return guestName;
    }

    /**
     * @return the id of the system group
     */
    public long getSystemGroupId() {
        return sgId;
    }

    /**
     * @return the {@link ExperimenterGroup#getName()} of the system group
     */
    public String getSystemGroupName() {
        return sgName;
    }

    /**
     * @return the id of the user group
     */
    public long getUserGroupId() {
        return ugId;
    }

    /**
     * @return the {@link ExperimenterGroup#getName()} of the user group
     */
    public String getUserGroupName() {
        return ugName;
    }

    /**
     * @return the id of the guest group
     */
    public long getGuestGroupId() {
        return ggId;
    }

    /**
     * @return the {@link ExperimenterGroup#getName()} of the guest group
     */
    public String getGuestGroupName() {
        return ggName;
    }
}
