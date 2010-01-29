/*
 * ome.security.BasicACLVoter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports

// Third-party libraries
import static ome.model.internal.Permissions.Right.WRITE;
import static ome.model.internal.Permissions.Role.GROUP;
import static ome.model.internal.Permissions.Role.USER;
import static ome.model.internal.Permissions.Role.WORLD;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Token;
import ome.security.ACLVoter;
import ome.security.SecuritySystem;
import ome.security.SystemTypes;
import ome.tools.hibernate.SecurityFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see Token
 * @see SecuritySystem
 * @see Details
 * @see Permissions
 * @since 3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class BasicACLVoter implements ACLVoter {

    private final static Log log = LogFactory.getLog(BasicACLVoter.class);

    protected final CurrentDetails currentUser;

    protected final SystemTypes sysTypes;

    protected final TokenHolder tokenHolder;

    public BasicACLVoter(CurrentDetails cd, SystemTypes sysTypes,
            TokenHolder tokenHolder) {
        this.currentUser = cd;
        this.sysTypes = sysTypes;
        this.tokenHolder = tokenHolder;
    }

    // ~ Interface methods
    // =========================================================================

    /**
     * 
     */
    public boolean allowChmod(IObject iObject) {
        return currentUser.isOwnerOrSupervisor(iObject);
    }

    /**
     * delegates to SecurityFilter because that is where the logic is defined
     * for the {@link #enableReadFilter(Object) read filter}
     * 
     * Ignores the id for the moment.
     * 
     * Though we pass in whether or not a share is active for completeness, a
     * different {@link ACLVoter} implementation will almost certainly be active
     * for share use.
     */
    public boolean allowLoad(Class<? extends IObject> klass, Details d, long id) {
        Assert.notNull(klass);
        // Assert.notNull(d);
        if (d == null || sysTypes.isSystemType(klass)) {
            return true;
        }
        BasicEventContext c = currentUser.current();
        return SecurityFilter.passesFilter(d, c.getOwner().getId(), c
                .getMemberOfGroupsList(), c.getLeaderOfGroupsList(), c
                .isCurrentUserAdmin(), c.getCurrentShareId() != null);
    }

    public void throwLoadViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation("Cannot read " + iObject);
    }

    public boolean allowCreation(IObject iObject) {
        Assert.notNull(iObject);
        Class cls = iObject.getClass();

        if (tokenHolder.hasPrivilegedToken(iObject)
                || currentUser.getCurrentEventContext().isCurrentUserAdmin()) {
            return true;
        }

        else if (sysTypes.isSystemType(cls)) {
            return false;
        }

        return true;
    }

    public void throwCreationViolation(IObject iObject)
            throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation(iObject
                + " is a System-type, and may only be "
                + "created through privileged APIs.");
    }

    public boolean allowUpdate(IObject iObject, Details trustedDetails) {
        return allowUpdateOrDelete(iObject, trustedDetails);
    }

    public void throwUpdateViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation("Updating " + iObject + " not allowed.");
    }

    public boolean allowDelete(IObject iObject, Details trustedDetails) {
        return allowUpdateOrDelete(iObject, trustedDetails);
    }

    public void throwDeleteViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation("Deleting " + iObject + " not allowed.");
    }

    private boolean allowUpdateOrDelete(IObject iObject, Details trustedDetails) {
        Assert.notNull(iObject);

        BasicEventContext c = currentUser.current();

        // needs no details info
        if (tokenHolder.hasPrivilegedToken(iObject) || c.isCurrentUserAdmin()) {
            return true;
        } else if (sysTypes.isSystemType(iObject.getClass())) {
            return false;
        }

        // previously we were taking the details directly from iObject
        // iObject, however, is in a critical state. Values such as
        // Permissions, owner, and group may have been changed.
        Details d = trustedDetails;

        // this can now only happen if a table doesn't have permissions
        // and there aren't any of those. so let it be updated.
        if (d == null) {
            return true;
        }

        // the owner and group information might be null if the type
        // is intended to be a system-type but isn't marked as one
        // via SecuritySystem.isSystemType(). A NPE here might imply
        // that that information is out of sync.
        Long o = d.getOwner() == null ? null : d.getOwner().getId();
        Long g = d.getGroup() == null ? null : d.getGroup().getId();

        // needs no permissions info
        if (g != null && c.getLeaderOfGroupsList().contains(g)) {
            return true;
        }

        Permissions p = d.getPermissions();

        // this should never occur.
        if (p == null) {
            throw new InternalException(
                    "Permissions null! Security system "
                            + "failure -- refusing to continue. The Permissions should "
                            + "be set to a default value.");
        }

        // standard
        if (p.isGranted(WORLD, WRITE)) {
            return true;
        }
        if (p.isGranted(USER, WRITE) && o != null
                && o.equals(c.getOwner().getId())) {
            return true;
        }
        if (p.isGranted(GROUP, WRITE) && g != null
                && c.getMemberOfGroupsList().contains(g)) {
            return true;
        }

        return false;
    }

}
