/*
 * ome.security.BasicACLVoter
 *
 *   Copyright 2006-2017 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import static ome.model.internal.Permissions.Role.GROUP;
import static ome.model.internal.Permissions.Role.USER;
import static ome.model.internal.Permissions.Role.WORLD;

import java.util.HashSet;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.conditions.GroupSecurityViolation;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.core.OriginalFile;
import ome.model.enums.AdminPrivilege;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Token;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.security.ACLVoter;
import ome.security.SecurityFilter;
import ome.security.SecuritySystem;
import ome.security.SystemTypes;
import ome.security.policy.PolicyService;
import ome.system.EventContext;
import ome.system.Roles;
import ome.tools.hibernate.HibernateUtils;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see Token
 * @see SecuritySystem
 * @see Details
 * @see Permissions
 * @since 3.0-M3
 */
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

    private final LightAdminPrivileges adminPrivileges;

    /* thread-safe */
    private final Set<String> managedRepoUuids, scriptRepoUuids;

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
        this(cd, sysTypes, tokenHolder, securityFilter, policyService,
                roles, new LightAdminPrivileges(roles), new HashSet<String>(), new HashSet<String>());
    }

    public BasicACLVoter(CurrentDetails cd, SystemTypes sysTypes,
        TokenHolder tokenHolder, SecurityFilter securityFilter,
        PolicyService policyService,
        Roles roles, LightAdminPrivileges adminPrivileges, Set<String> managedRepoUuids, Set<String> scriptRepoUuids) {
        this.currentUser = cd;
        this.sysTypes = sysTypes;
        this.securityFilter = securityFilter;
        this.tokenHolder = tokenHolder;
        this.roles = roles;
        this.policyService = policyService;
        this.adminPrivileges = adminPrivileges;
        this.managedRepoUuids = managedRepoUuids;
        this.scriptRepoUuids = scriptRepoUuids;
    }

    // ~ Interface methods
    // =========================================================================

    public boolean allowChmod(IObject iObject) {
        if (iObject == null) {
            throw new ApiUsageException("Object can't be null");
        }
        final Long ownerId = HibernateUtils.nullSafeOwnerId(iObject);
        final Long groupId; // see 2874 and chmod
        if (iObject instanceof ExperimenterGroup) {
            groupId = iObject.getId();
        } else {
            groupId = HibernateUtils.nullSafeGroupId(iObject);
        }
        final EventContext ec = currentUser.getCurrentEventContext();
        if (ec.getCurrentUserId().equals(ownerId) || ec.getLeaderOfGroupsList().contains(groupId)) {
            return true;   // object owner or group owner
        } else if (!ec.isCurrentUserAdmin()) {
            return false;  // not an admin
        }
        final Event event = currentUser.getEvent();
        if (event == null || !event.isLoaded()) {
            return true;   // no session to limit privileges
        }
        final Set<AdminPrivilege> privileges = adminPrivileges.getSessionPrivileges(event.getSession());
        if (sysTypes.isSystemType(iObject.getClass())) {
            if (iObject instanceof Experimenter) {
                return privileges.contains(adminPrivileges.getPrivilege("ModifyUser"));
            } else if (iObject instanceof ExperimenterGroup) {
                return privileges.contains(adminPrivileges.getPrivilege("ModifyGroup"));
            } else if (iObject instanceof GroupExperimenterMap) {
                return privileges.contains(adminPrivileges.getPrivilege("ModifyGroupMembership"));
            } else {
                return true;
            }
        } else {
            if (iObject instanceof OriginalFile) {
                final String repo = ((OriginalFile) iObject).getRepo();
                if (repo != null) {
                    if (managedRepoUuids.contains(repo)) {
                        return privileges.contains(adminPrivileges.getPrivilege("WriteManagedRepo"));
                    } else if (scriptRepoUuids.contains(repo)) {
                        return privileges.contains(adminPrivileges.getPrivilege("WriteScriptRepo"));
                    }
                }
                return privileges.contains(adminPrivileges.getPrivilege("WriteFile"));
            } else {
                return privileges.contains(adminPrivileges.getPrivilege("WriteOwned"));
            }
        }
    }

    /**
     * delegates to SecurityFilter because that is where the logic is defined
     * for the {@link BasicSecuritySystem#enableReadFilter(Object) read filter}
     * 
     * Ignores the id for the moment.
     * 
     * Though we pass in whether or not a share is active for completeness, a
     * different {@link ACLVoter} implementation will almost certainly be active
     * for share use.
     */
    public boolean allowLoad(Session session, Class<? extends IObject> klass, Details d, long id) {
        Assert.notNull(klass);

        final BasicEventContext ec = currentUser.current();

        if (klass == ome.model.meta.Session.class) {
            /* determine "real" owner of current and queried session, i.e. taking sudo into account */
            final ome.model.meta.Session currentSession = (ome.model.meta.Session)
                    session.get(ome.model.meta.Session.class, ec.getCurrentSessionId());
            Experimenter sessionOwnerCurrent = currentSession.getSudoer();
            if (sessionOwnerCurrent == null) {
                sessionOwnerCurrent = currentSession.getOwner();
            }
            final ome.model.meta.Session queriedSession = (ome.model.meta.Session)
                    session.get(ome.model.meta.Session.class, id);
            Experimenter sessionOwnerQueried = queriedSession.getSudoer();
            if (sessionOwnerQueried == null) {
                sessionOwnerQueried = queriedSession.getOwner();
            }
            /* determine if the "real" owner is an administrator */
            final Object systemMembership = session.createQuery(
                    "FROM GroupExperimenterMap WHERE parent.id = :group AND child.id = :user")
                    .setLong("group", roles.getSystemGroupId()).setLong("user", sessionOwnerCurrent.getId()).uniqueResult();
            if (systemMembership != null) {
                /* cannot yet cache privileges because Sessions may be loaded while still being constructed by the session bean */
                final Set<AdminPrivilege> privileges = adminPrivileges.getSessionPrivileges(currentSession, false);
                if (privileges.contains(adminPrivileges.getPrivilege("ReadSession"))) {
                    /* only a full administrator may read all sessions */
                    return true;
                }
            }
            /* without ReadSession privilege may read only those sessions for which "real" owner matches */
            return sessionOwnerCurrent.getId() == sessionOwnerQueried.getId();
        }

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
            rv = securityFilter.passesFilter(session, d, ec);
        }

        // Misusing this location to store the loaded objects perms for later.
        if (ec.getCurrentGroupId() < 0) {
            // For every object that gets loaded when omero.group = -1, we
            // cache its permissions in the session context so that when the
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
                    ec.setPermissionsForGroup(gid, p);
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

        if (tokenHolder.hasPrivilegedToken(iObject)) {
            return true;
        } else if (!sysType) {
            if (iObject instanceof OriginalFile && ((OriginalFile) iObject).getRepo() != null) {
                /* Cannot yet set OriginalFile.repo except via SQL.
                 * TODO: Need to first work through implications before permitting this. */
                return false;
            }
            /* also checked by OmeroInterceptor.newTransientDetails */
            return true;
        } else if (currentUser.getCurrentEventContext().isCurrentUserAdmin()) {
            final Set<AdminPrivilege> privileges;
            final Event event = currentUser.current().getEvent();
            if (event == null || !event.isLoaded()) {
                privileges = adminPrivileges.getAllPrivileges();
            } else {
                privileges = adminPrivileges.getSessionPrivileges(event.getSession());
            }
            /* see trac ticket 10691 re. enum values */
            if (iObject instanceof Experimenter) {
                return privileges.contains(adminPrivileges.getPrivilege("ModifyUser"));
            } else if (iObject instanceof ExperimenterGroup) {
                return privileges.contains(adminPrivileges.getPrivilege("ModifyGroup"));
            } else if (iObject instanceof GroupExperimenterMap) {
                return privileges.contains(adminPrivileges.getPrivilege("ModifyGroupMembership"));
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public void throwCreationViolation(IObject iObject)
            throws SecurityViolation {
        Assert.notNull(iObject);

        boolean sysType = sysTypes.isSystemType(iObject.getClass()) ||
            sysTypes.isInSystemGroup(iObject.getDetails());

        if (sysType) {
            throw new SecurityViolation(iObject + " is a System-type, and may be created only through privileged APIs.");
        } else if (currentUser.isGraphCritical(iObject.getDetails())) { // ticket:1769
            throw new GroupSecurityViolation(iObject + "-insertion violates " +
                    "group-security.");
        } else if (iObject instanceof OriginalFile) {
            /* Cannot yet set OriginalFile.repo except via SQL. */
            throw new SecurityViolation("cannot set repo property of " + iObject + " via ORM");
        } else {
            throw new SecurityViolation("not permitted to create " + iObject);
        }
    }

    public boolean allowAnnotate(IObject iObject, Details trustedDetails) {
        BasicEventContext c = currentUser.current();
        return 1 == allowUpdateOrDelete(c, iObject, trustedDetails, true, Scope.ANNOTATE);
    }

    public boolean allowUpdate(IObject iObject, Details trustedDetails) {
        BasicEventContext c = currentUser.current();
        return 1 == allowUpdateOrDelete(c, iObject, trustedDetails, true, Scope.EDIT);
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
        return 1 == allowUpdateOrDelete(c, iObject, trustedDetails, false, Scope.DELETE);
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
        Details trustedDetails, boolean isUpdate, Scope...scopes) {

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

        final Set<AdminPrivilege> privileges;
        final Event event = c.getEvent();
        if (event == null || !event.isLoaded()) {
            privileges = adminPrivileges.getAllPrivileges();
        } else {
            privileges = adminPrivileges.getSessionPrivileges(event.getSession());
        }
        if (c.isCurrentUserAdmin()) {
            /* see trac ticket 10691 re. enum values */
            boolean isLightAdminRestricted = false;
            final String prefix = isUpdate ? "Write" : "Delete";
            if (!sysType) {
                if (iObject instanceof OriginalFile) {
                    final String repo = ((OriginalFile) iObject).getRepo();
                    if (repo != null) {
                        if (managedRepoUuids.contains(repo)) {
                            if (!privileges.contains(adminPrivileges.getPrivilege(prefix + "ManagedRepo"))) {
                                isLightAdminRestricted = true;
                            }
                        } else if (scriptRepoUuids.contains(repo)) {
                            if (!privileges.contains(adminPrivileges.getPrivilege(prefix + "ScriptRepo"))) {
                                isLightAdminRestricted = true;
                            }
                        } else {
                            /* other repository */
                            if (!privileges.contains(adminPrivileges.getPrivilege(prefix + "File"))) {
                                isLightAdminRestricted = true;
                            }
                        }
                    } else {
                        /* not in repository */
                        if (!privileges.contains(adminPrivileges.getPrivilege(prefix + "File"))) {
                            isLightAdminRestricted = true;
                        }
                    }
                } else {
                    if (!privileges.contains(adminPrivileges.getPrivilege(prefix + "Owned"))) {
                        isLightAdminRestricted = true;
                    }
                }
            } else if (iObject instanceof Experimenter) {
                if (!privileges.contains(adminPrivileges.getPrivilege("ModifyUser"))) {
                    isLightAdminRestricted = true;
                }
            } else if (iObject instanceof ExperimenterGroup) {
                if (!privileges.contains(adminPrivileges.getPrivilege("ModifyGroup"))) {
                    isLightAdminRestricted = true;
                }
            } else if (iObject instanceof GroupExperimenterMap) {
                if (!privileges.contains(adminPrivileges.getPrivilege("ModifyGroupMembership"))) {
                    isLightAdminRestricted = true;
                }
            }
            if (!isLightAdminRestricted) {
                for (int i = 0; i < scopes.length; i++) {
                    if (scopes[i] != null) {
                        rv |= (1<<i);
                    }
                }
                return rv; // EARLY EXIT!
            }
        }
        if (sysType) {
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
            final int allow = allowUpdateOrDelete(c, object, details, true,
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
     * Check if the given object is owned by the given user.
     * @param iObject a model object
     * @param uid the ID of a user
     * @return if the object is owned by the user, or is not yet persisted
     */
    // TODO this is less problematic than linking
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
