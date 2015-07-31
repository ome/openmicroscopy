/*
 * ome.security.BasicACLVoter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import static ome.model.internal.Permissions.Role.GROUP;
import static ome.model.internal.Permissions.Role.USER;
import static ome.model.internal.Permissions.Role.WORLD;

import java.util.Set;

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
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.ACLVoter;
import ome.security.SecurityFilter;
import ome.security.SecuritySystem;
import ome.security.SystemTypes;
import ome.security.policy.PolicyService;
import ome.system.EventContext;
import ome.system.Roles;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * Simple enum to represent the interpretation of "WRITE" permissions.
     */
    private static enum Scope {
        ANNOTATE(Right.ANNOTATE),
        DELETE(Right.WRITE),
        EDIT(Right.WRITE),
        LINK(Right.WRITE);

        final Right right;
        Scope(Right right) {
            this.right = right;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(BasicACLVoter.class);

    protected final CurrentDetails currentUser;

    protected final SystemTypes sysTypes;

    protected final TokenHolder tokenHolder;

    protected final SecurityFilter securityFilter;

    protected final PolicyService policyService;

    protected final Roles roles;

    public BasicACLVoter(CurrentDetails cd, SystemTypes sysTypes,
        TokenHolder tokenHolder, SecurityFilter securityFilter,
        PolicyService policyService) {
        this(cd, sysTypes, tokenHolder, securityFilter, policyService,
                new Roles());
    }

    public BasicACLVoter(CurrentDetails cd, SystemTypes sysTypes,
        TokenHolder tokenHolder, SecurityFilter securityFilter,
        PolicyService policyService,
        Roles roles) {
        this.currentUser = cd;
        this.sysTypes = sysTypes;
        this.securityFilter = securityFilter;
        this.tokenHolder = tokenHolder;
        this.roles = roles;
        this.policyService = policyService;
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

        if (d == null || sysTypes.isSystemType(klass)) {
            // Here we're returning true because there
            // will be no group value that we can use
            // to store any permissions and don't want
            // WARNS in the log.

            return true; // EARLY EXIT!
        }

        boolean rv = false;
        if (sysTypes.isInSystemGroup(d) ||
                sysTypes.isInUserGroup(d)) {
            rv = true;
        }
        else {
            rv = securityFilter.passesFilter(session, d, currentUser.current());
        }

        // Misusing this location to store the loaded objects perms for later.
        if (this.currentUser.getCurrentEventContext().getCurrentGroupId() < 0) {
            // For every object that gets loaded when omero.group = -1, we
            // cache it's permissions in the session context so that when the
            // session is over we can re-apply all the permissions.
            ExperimenterGroup g = d.getGroup();
            if (g == null) {
                log.warn(String.format("Group null while loading %s:%s",
                        klass.getName(), id));
            }
            if (g != null) { // Null for system types
                Long gid = g.getId();
                Permissions p = g.getDetails().getPermissions();
                if (p == null) {
                    log.warn(String.format("Permissions null for group %s " +
                            "while loading %s:%s", gid, klass.getName(), id));
                } else {
                    this.currentUser.current().setPermissionsForGroup(gid, p);
                }
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

        // Note: removed restriction from #1769 that admins can only
        // create objects belonging to the current user. Instead,
        // OmeroInterceptor checks whether or not objects are only
        // LINKED to one's own objects which is the actual intent.

        if (tokenHolder.hasPrivilegedToken(iObject)
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

        if (!sysType && currentUser.isGraphCritical(iObject.getDetails())) { // ticket:1769
            throw new GroupSecurityViolation(iObject + "-insertion violates " +
                    "group-security.");
        }

        throw new SecurityViolation(iObject
                + " is a System-type, and may only be "
                + "created through privileged APIs.");
    }

    public boolean allowAnnotate(IObject iObject, Details trustedDetails) {
        BasicEventContext c = currentUser.current();
        return 1 == allowUpdateOrDelete(c, iObject, trustedDetails, Scope.ANNOTATE);
    }

    public boolean allowUpdate(IObject iObject, Details trustedDetails) {
        BasicEventContext c = currentUser.current();
        return 1 == allowUpdateOrDelete(c, iObject, trustedDetails, Scope.EDIT);
    }

    public void throwUpdateViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);

        boolean sysType = sysTypes.isSystemType(iObject.getClass()) ||
            sysTypes.isInSystemGroup(iObject.getDetails());

        if (!sysType && currentUser.isGraphCritical(iObject.getDetails())) { // ticket:1769
            throw new GroupSecurityViolation(iObject +"-modification violates " +
                    "group-security.");
        }

        throw new SecurityViolation("Updating " + iObject + " not allowed.");
    }

    public boolean allowDelete(IObject iObject, Details trustedDetails) {
        BasicEventContext c = currentUser.current();
        return 1 == allowUpdateOrDelete(c, iObject, trustedDetails, Scope.DELETE);
    }

    public void throwDeleteViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation("Deleting " + iObject + " not allowed.");
    }

    boolean owner(Long o, EventContext c) {
        return (o != null && o.equals(c.getCurrentUserId()));
    }

    boolean owner(Details d, EventContext c) {
        Long o = d.getOwner() == null ? null : d.getOwner().getId();
        return (o != null && o.equals(c.getCurrentUserId()));
    }

    boolean member(Long g, EventContext c) {
        return (g != null && c.getMemberOfGroupsList().contains(g));
    }

    boolean member(Details d, EventContext c) {
        Long g = d.getGroup() == null ? null : d.getGroup().getId();
        return member(g, c);
    }

    boolean leader(Long g, EventContext c) {
        return (g != null && c.getLeaderOfGroupsList().contains(g));
    }

    boolean leader(Details d, EventContext c) {
        Long g = d.getGroup() == null ? null : d.getGroup().getId();
        return leader(g, c);
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
     * @return an int with the bit turned on for each {@link Scope} element
     *     which should be allowed.
     */
    private int allowUpdateOrDelete(BasicEventContext c, IObject iObject,
        Details trustedDetails, Scope...scopes) {

        int rv = 0;

        if (iObject == null) {
            throw new IllegalArgumentException("null object");
        }

        // Do not take the details directly from iObject
        // as it is in a critical state. Values such as
        // Permissions, owner, and group may have been changed.
        final Details d = trustedDetails;

        // this can now only happen if a table doesn't have permissions
        // and there aren't any of those. so let it be updated.
        if (d == null) {
            throw new InternalException("trustedDetails are null!");
        }

        final boolean sysType = sysTypes.isSystemType(iObject.getClass()) ||
            sysTypes.isInSystemGroup(d);
        final boolean sysTypeOrUsrGroup = sysType ||
            sysTypes.isInUserGroup(d);

        // needs no details info
        if (tokenHolder.hasPrivilegedToken(iObject)) {
            return 1; // ticket:1794, allow move to "user
        } else if (!sysTypeOrUsrGroup && currentUser.isGraphCritical(d)) { //ticket:1769
            Boolean belongs = null;
            final Long uid = c.getCurrentUserId();
            for (int i = 0; i < scopes.length; i++) {
                if (scopes[i].equals(Scope.LINK) || scopes[i].equals(Scope.ANNOTATE)) {
                    if (belongs == null) {
                        belongs = objectBelongsToUser(iObject, uid);
                    }
                    // Cancel processing of this scope. rv is already 0
                    if (!belongs) {
                        scopes[i] = null;
                    }
                }
            }
            // Don't return. Need further processing for delete.
        }

        if (c.isCurrentUserAdmin()) {
            for (int i = 0; i < scopes.length; i++) {
                if (scopes[i] != null) {
                    rv |= (1<<i);
                }
            }
            return rv; // EARLY EXIT!
        } else if (sysType) {
            return 0;
        }

        Permissions grpPermissions = c.getCurrentGroupPermissions();
        if (grpPermissions == null || grpPermissions == Permissions.DUMMY) {
            if (d.getGroup() != null) {
                Long gid = d.getGroup().getId();
                grpPermissions = c.getPermissionsForGroup(gid);
                if (grpPermissions == null && gid.equals(roles.getUserGroupId())) {
                    grpPermissions = new Permissions(Permissions.EMPTY);
                }
            }
            if (grpPermissions == null) {
                throw new InternalException(
                    "Permissions are null! Security system "
                            + "failure -- refusing to continue. The Permissions should "
                            + "be set to a default value.");
            }
        }

        final boolean owner = owner(d, c);
        final boolean leader = leader(d, c);
        final boolean member = member(d, c);

        for (int i = 0; i < scopes.length; i++) {
            Scope scope = scopes[i];
            if (scope == null) continue;

            if (leader) {
                rv |= (1<<i);
            }

            // standard
            else if (grpPermissions.isGranted(WORLD, scope.right)) {
                rv |= (1<<i);
            }

            else if (owner && grpPermissions.isGranted(USER, scope.right)) {
                // Using cuId rather than getOwner since postProcess is also
                // post-login!
                rv |= (1<<i);
            }
            // Previously restricted by ticket:1992
            // As of ticket:8562 this is handled by
            // the separation of ANNOTATE and WRITE
            else if (member && grpPermissions.isGranted(GROUP, scope.right) ) {
                rv |= (1<<i);
            }
        }
        return rv; // default was off, i.e. false

    }

    @Override
    public Set<String> restrictions(IObject object) {
        return policyService.listActiveRestrictions(object);
    }

    public void postProcess(IObject object) {
        if (object.isLoaded()) {
            Details details = object.getDetails();
            // Sets context values.s
            this.currentUser.applyContext(details,
                    !(object instanceof ExperimenterGroup));

            final BasicEventContext c = currentUser.current();
            final Permissions p = details.getPermissions();
            final int allow = allowUpdateOrDelete(c, object, details,
                // This order must match the ordered of restrictions[]
                // expected by p.copyRestrictions
                Scope.LINK, Scope.EDIT, Scope.DELETE, Scope.ANNOTATE);

            // #9635 - This is not the most efficient solution
            // But since it's unclear why Permission objects
            // are currently being shared, the safest solution
            // is to always produce a copy.
            Permissions copy = new Permissions(p);
            copy.copyRestrictions(allow, restrictions(object));
            details.setPermissions(copy); // #9635
        }
    }

    /**
     * @param iObject
     * @param uid
     * @return
     * @DEV.TODO this is less problematic than linking.
     */
    private boolean objectBelongsToUser(IObject iObject, Long uid) {
        final Experimenter e = iObject.getDetails().getOwner();
        if (e == null) {
            if (iObject.getId() == null) {
                // ticket:8818 if this object does not yet have an ID
                // then we'll assume it's a newly created instance
                // which will eventually be saved with owner==uid
                return true;
            }

            throw new NullPointerException("Null owner for " + iObject);
        }
        Long oid = e.getId();
        return uid.equals(oid); // Only allow own objects!
    }

}
