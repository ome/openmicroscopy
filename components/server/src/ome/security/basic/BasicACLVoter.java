/*
 * ome.security.BasicACLVoter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports

// Third-party libraries
import static ome.model.internal.Permissions.Right.ANNOTATE;
import static ome.model.internal.Permissions.Right.WRITE;
import static ome.model.internal.Permissions.Role.USER;
import static ome.model.internal.Permissions.Role.GROUP;
import static ome.model.internal.Permissions.Role.WORLD;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.util.Assert;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.GroupSecurityViolation;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Token;
import ome.model.meta.ExperimenterGroup;
import ome.security.ACLVoter;
import ome.security.SecurityFilter;
import ome.security.SecuritySystem;
import ome.security.SystemTypes;
import ome.system.EventContext;

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

    protected final SecurityFilter securityFilter;

    public BasicACLVoter(CurrentDetails cd, SystemTypes sysTypes,
            TokenHolder tokenHolder, SecurityFilter securityFilter) {
        this.currentUser = cd;
        this.sysTypes = sysTypes;
        this.securityFilter = securityFilter;
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
    public boolean allowLoad(Session session, Class<? extends IObject> klass, Details d, long id) {
        Assert.notNull(klass);

        if (d == null ||
                sysTypes.isSystemType(klass) ||
                sysTypes.isInSystemGroup(d) ||
                sysTypes.isInUserGroup(d)) {
            return true;
        }

        boolean rv = securityFilter.passesFilter(session, d, currentUser.current());

        // Misusing this location to store the loaded objects perms for later.
        if (this.currentUser.getCurrentEventContext().getCurrentGroupId() < 1) {
            // For every object that gets loaded when omero.group = -1, we
            // cache it's permissions in the session context so that when the
            // session is over we can re-apply all the permissions.
            ExperimenterGroup g = d.getGroup();
            if (g != null) { // Null for system types
                Long gid = g.getId();
                Permissions p = g.getDetails().getPermissions();
                this.currentUser.current().setPermissionsForGroup(gid, p);
            }
        }

        return rv;
    }

    public void throwLoadViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation("Cannot read " + iObject);
    }

    public boolean allowCreation(IObject iObject) {
        Assert.notNull(iObject);
        Class<?> cls = iObject.getClass();

        boolean sysType = sysTypes.isSystemType(cls)
            || sysTypes.isInSystemGroup(iObject.getDetails());

        if (!sysType && currentUser.isGraphCritical()) { // ticket:1769
            Long uid = currentUser.getOwner().getId();
            return objectBelongsToUser(iObject, uid);
        }

        else if (tokenHolder.hasPrivilegedToken(iObject)
                || currentUser.getCurrentEventContext().isCurrentUserAdmin()) {
            return true;
        }

        else if (sysType) {
            return false;
        }

        return true;
    }

    public void throwCreationViolation(IObject iObject)
            throws SecurityViolation {
        Assert.notNull(iObject);

        boolean sysType = sysTypes.isSystemType(iObject.getClass()) ||
            sysTypes.isInSystemGroup(iObject.getDetails());

        if (!sysType && currentUser.isGraphCritical()) { // ticket:1769
            throw new GroupSecurityViolation(iObject + "-insertion violates " +
                    "group-security.");
        }

        throw new SecurityViolation(iObject
                + " is a System-type, and may only be "
                + "created through privileged APIs.");
    }

    public boolean allowUpdate(IObject iObject, Details trustedDetails) {
        EventContext c = currentUser.current();
        return allowUpdateOrDelete(c, iObject, trustedDetails, true, WRITE);
    }

    public void throwUpdateViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);

        boolean sysType = sysTypes.isSystemType(iObject.getClass()) ||
            sysTypes.isInSystemGroup(iObject.getDetails());

        if (!sysType && currentUser.isGraphCritical()) { // ticket:1769
            throw new GroupSecurityViolation(iObject +"-modification violates " +
                    "group-security.");
        }

        throw new SecurityViolation("Updating " + iObject + " not allowed.");
    }

    public boolean allowDelete(IObject iObject, Details trustedDetails) {
        EventContext c = currentUser.current();
        return allowUpdateOrDelete(c, iObject, trustedDetails, false, WRITE);
    }

    public void throwDeleteViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation("Deleting " + iObject + " not allowed.");
    }

    /**
     * Determines whether or not the {@link Right} is available on this object
     * based on the ownership, group-membership, and group-permissions.
     *
     * Note: group leaders are automatically granted all rights.
     *
     * @param iObject
     * @param trustedDetails
     * @param update
     * @param right
     * @return
     */
    private boolean allowUpdateOrDelete(EventContext c, IObject iObject,
            Details trustedDetails, boolean update, Right right) {

        Long uid = c.getCurrentUserId();

        boolean sysType = sysTypes.isSystemType(iObject.getClass()) ||
            sysTypes.isInSystemGroup(iObject.getDetails());
        boolean sysTypeOrUsrGroup = sysType ||
            sysTypes.isInUserGroup(iObject.getDetails());

        // needs no details info
        if (tokenHolder.hasPrivilegedToken(iObject)) {
            return true; // ticket:1794, allow move to "user
        } else if (update && !sysTypeOrUsrGroup && currentUser.isGraphCritical()) { //ticket:1769
            return objectBelongsToUser(iObject, uid);
        } else if (c.isCurrentUserAdmin()) {
            return true;
        } else if (sysType) {
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

        Permissions p = c.getCurrentGroupPermissions(); // From Group!

        // this should never occur.
        if (p == null) {
            throw new InternalException(
                    "Permissions null! Security system "
                            + "failure -- refusing to continue. The Permissions should "
                            + "be set to a default value.");
        }

        // standard
        if (p.isGranted(WORLD, right)) {
            return true;
        }
        if (p.isGranted(USER, right) && o != null
                && o.equals(c.getCurrentUserId())) {
            // Using cuId rather than getOwner since postProcess is also
            // post-login!
            return true;
        }
        // Previously restricted by ticket:1992
        // As of ticket:8562 this is handled by
        // the separation of ANNOTATE and WRITE
        if (p.isGranted(GROUP, right) && g != null
                && c.getMemberOfGroupsList().contains(g)) {
            return true;
        }

        return false;
    }

    public EventContext getEventContext() {
        return this.currentUser.getCurrentEventContext();
    }

    public void postProcess(IObject object) {
        if (object.isLoaded()) {
            Details details = object.getDetails();
            // Sets context values.s
            this.currentUser.applyContext(details,
                    !(object instanceof ExperimenterGroup));

            final BasicEventContext c = currentUser.current();
            final Permissions p = details.getPermissions();
            boolean disallowAnnotate = !allowUpdateOrDelete(c, object, details, true, ANNOTATE);
            boolean disallowEdit = !allowUpdateOrDelete(c, object, details, true, WRITE);

            boolean[] restrictions = new boolean[4];
            restrictions[Permissions.ANNOTATERESTRICTION] = disallowAnnotate;
            restrictions[Permissions.DELETERESTRICTION] = disallowEdit;
            restrictions[Permissions.EDITRESTRICTION] = disallowEdit;
            restrictions[Permissions.LINKRESTRICTION] = disallowEdit;
            if (currentUser.isGraphCritical()) {
                // If we're in the graph critical situation, then we open back
                // up the permissions for delete.
                restrictions[Permissions.DELETERESTRICTION] = false;
            }
            p.copyRestrictions(restrictions);
        }
    }

    /**
     * @param iObject
     * @param uid
     * @return
     * @DEV.TODO this is less problematic than linking.
     */
    private boolean objectBelongsToUser(IObject iObject, Long uid) {
        Long oid = iObject.getDetails().getOwner().getId();
        return uid.equals(oid); // Only allow own objects!
    }

    private Long group(Details d) {
        if (d == null || d.getGroup() == null) {
            return null;
        }
        return d.getGroup().getId();
    }

}
