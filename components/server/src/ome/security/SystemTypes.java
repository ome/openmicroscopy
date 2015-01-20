/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import ome.model.IEnum;
import ome.model.IGlobal;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.jobs.Job;
import ome.model.meta.DBPatch;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.model.meta.Namespace;
import ome.model.meta.Node;
import ome.model.meta.ShareMember;
import ome.system.Roles;

/**
 * Defines what {@link IObject} classes are considered "system" types. System
 * types have special meaning with regard to ACL. They cannot be created except
 * by an administrator, primarily.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class SystemTypes {

    private final Roles roles;

    public SystemTypes() {
        this(new Roles());
    }

    public SystemTypes(Roles roles) {
        this.roles = roles;
    }

    /**
     * classes which cannot be created by regular users.
     * 
     * @see <a
     *      href="http://trac.openmicroscopy.org.uk/ome/ticket/156">ticket156</a>
     */
    public boolean isSystemType(Class<?> klass) {

        if (klass == null) {
            return false;
        }

        if (ome.model.meta.Session.class.isAssignableFrom(klass)) {
            return true;
        } else if (ShareMember.class.isAssignableFrom(klass)) {
            return true;
        } else if (Node.class.isAssignableFrom(klass)) {
            return true;
        } else if (Experimenter.class.isAssignableFrom(klass)) {
            return true;
        } else if (ExperimenterGroup.class.isAssignableFrom(klass)) {
            return true;
        } else if (GroupExperimenterMap.class.isAssignableFrom(klass)) {
            return true;
        } else if (Event.class.isAssignableFrom(klass)) {
            return true;
        } else if (EventLog.class.isAssignableFrom(klass)) {
            return true;
        } else if (IEnum.class.isAssignableFrom(klass)) {
            return true;
        } else if (Job.class.isAssignableFrom(klass)) {
            return true;
        } else if (DBPatch.class.isAssignableFrom(klass)) {
            return true;
        } else if (IGlobal.class.isAssignableFrom(klass)) {
            return true;
        } else if (Namespace.class.isAssignableFrom(klass)) {
            return true;
        }

        return false;

    }

    // ticket:1784 - Make "system" group contents system types.
    public boolean isInSystemGroup(Long groupId) {
        Long systemGroupId = Long.valueOf(roles.getSystemGroupId());
        if (systemGroupId.equals(groupId)) {
            return true;
        }
        return false;
    }

    public boolean isInSystemGroup(Details d) {
        if (d == null || d.getGroup() == null) {
            return false;
        }
        Long groupId = d.getGroup().getId();
        return isInSystemGroup(groupId);
    }

    // ticket:1791
    public boolean isInUserGroup(Long groupId) {
        Long userGroupId = Long.valueOf(roles.getUserGroupId());
        if (userGroupId.equals(groupId)) {
            return true;
        }
        return false;
    }

    public boolean isInUserGroup(Details d) {
        if (d == null || d.getGroup() == null) {
            return false;
        }
        Long groupId = d.getGroup().getId();
        return isInUserGroup(groupId);
    }
}
