/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import static ome.model.internal.Permissions.Right.READ;
import static ome.model.internal.Permissions.Role.GROUP;
import static ome.model.internal.Permissions.Role.USER;
import static ome.model.internal.Permissions.Role.WORLD;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.ApiUsageException;
import ome.conditions.GroupSecurityViolation;
import ome.conditions.InternalException;
import ome.conditions.OptimisticLockException;
import ome.conditions.PermissionMismatchGroupSecurityViolation;
import ome.conditions.ReadOnlyGroupSecurityViolation;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.ExternalInfo;
import ome.security.SecuritySystem;
import ome.security.SystemTypes;
import ome.services.sessions.stats.SessionStats;
import ome.system.Principal;
import ome.system.Roles;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.HibernateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.event.FlushEntityEventListener;
import org.hibernate.type.Type;
import org.springframework.util.Assert;

/**
 * implements {@link org.hibernate.Interceptor} for controlling various aspects
 * of the Hibernate runtime. Where no special requirements exist, methods
 * delegate to {@link EmptyInterceptor}
 *
 * Current responsibilities include the proper (re-)setting of {@link Details}
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see EmptyInterceptor
 * @see Interceptor
 * @since 3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class OmeroInterceptor implements Interceptor {

    static volatile String last = null;

    static volatile int count = 1;

    private static Log log = LogFactory.getLog(OmeroInterceptor.class);

    private final Interceptor EMPTY = EmptyInterceptor.INSTANCE;

    private final SystemTypes sysTypes;

    private final CurrentDetails currentUser;

    private final TokenHolder tokenHolder;

    private final ExtendedMetadata em;

    private final SessionStats stats;

    private final Roles roles;

    public OmeroInterceptor(Roles roles, SystemTypes sysTypes, ExtendedMetadata em,
            CurrentDetails cd, TokenHolder tokenHolder, SessionStats stats) {
        Assert.notNull(tokenHolder);
        Assert.notNull(sysTypes);
        // Assert.notNull(em); Permitting null for testing
        // Assert.notNull(cd); Permitting null for testing
        Assert.notNull(stats);
        Assert.notNull(roles);
        this.tokenHolder = tokenHolder;
        this.currentUser = cd;
        this.sysTypes = sysTypes;
        this.stats = stats;
        this.roles = roles;
        this.em = em;
    }

    /**
     * default logic, but we may want to use them eventually for
     * dependency-injection.
     */
    public Object instantiate(String entityName, EntityMode entityMode,
            Serializable id) throws CallbackException {

        debug("Intercepted instantiate.");
        return EMPTY.instantiate(entityName, entityMode, id);

    }

    /** default logic. */
    public boolean onLoad(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) throws CallbackException {

        debug("Intercepted load.");
        this.stats.loadedObjects(1);
        return EMPTY.onLoad(entity, id, state, propertyNames, types);

    }

    /** default logic */
    public int[] findDirty(Object entity, Serializable id,
            Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types) {
        debug("Intercepted dirty check.");
        return EMPTY.findDirty(entity, id, currentState, previousState,
                propertyNames, types);
    }

    /**
     * callsback to {@link BasicSecuritySystem#newTransientDetails(IObject)} for
     * properly setting {@link IObject#getDetails() Details}
     */
    public boolean onSave(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) {
        debug("Intercepted save.");
        this.stats.updatedObjects(1);
        if (entity instanceof IObject) {
            IObject iobj = (IObject) entity;
            int idx = HibernateUtils.detailsIndex(propertyNames);

            evaluateLinkages(iobj);

            // Get a new details based on the current context
            Details d = newTransientDetails(iobj);
            state[idx] = d;
        }

        return true; // transferDetails ALWAYS edits the new entity.
    }

    /**
     * callsback to
     * {@link BasicSecuritySystem#checkManagedDetails(IObject, Details)} for
     * properly setting {@link IObject#getDetails() Details}.
     */
    public boolean onFlushDirty(Object entity, Serializable id,
            Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types) {
        debug("Intercepted update.");
        this.stats.updatedObjects(1);
        boolean altered = false;
        if (entity instanceof IObject) {
            IObject iobj = (IObject) entity;
            int idx = HibernateUtils.detailsIndex(propertyNames);

            evaluateLinkages(iobj);

            altered |= resetDetails(iobj, currentState, previousState, idx);

        }
        return altered;
    }

    /** default logic */
    public void onDelete(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) throws CallbackException {
        debug("Intercepted delete.");
        EMPTY.onDelete(entity, id, state, propertyNames, types);
    }

    // ~ Collections (of interest)
    // =========================================================================
    public void onCollectionRecreate(Object collection, Serializable key)
            throws CallbackException {
        debug("Intercepted collection recreate.");
    }

    public void onCollectionRemove(Object collection, Serializable key)
            throws CallbackException {
        debug("Intercepted collection remove.");
    }

    public void onCollectionUpdate(Object collection, Serializable key)
            throws CallbackException {
        debug("Intercepted collection update.");
    }

    // ~ Flush (currently unclear semantics)
    // =========================================================================
    public void preFlush(Iterator entities) throws CallbackException {
        debug("Intercepted preFlush.");
        EMPTY.preFlush(entities);
    }

    public void postFlush(Iterator entities) throws CallbackException {
        debug("Intercepted postFlush.");
        EMPTY.postFlush(entities);
    }

    // ~ Serialization
    // =========================================================================

    private static final long serialVersionUID = 7616611615023614920L;

    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
    }

    // ~ Unused interface methods
    // =========================================================================

    public void afterTransactionBegin(Transaction tx) {
    }

    public void afterTransactionCompletion(Transaction tx) {
    }

    public void beforeTransactionCompletion(Transaction tx) {
    }

    public Object getEntity(String entityName, Serializable id)
            throws CallbackException {
        return EMPTY.getEntity(entityName, id);
    }

    public String getEntityName(Object object) throws CallbackException {
        return EMPTY.getEntityName(object);
    }

    public Boolean isTransient(Object entity) {
        return EMPTY.isTransient(entity);
    }

    public String onPrepareStatement(String sql) {
        // start
        if (!log.isDebugEnabled()) {
            return sql;
        }

        // from
        StringBuilder sb = new StringBuilder();
        String[] first = sql.split("\\sfrom\\s");
        sb.append(first[0]);

        for (int i = 1; i < first.length; i++) {
            sb.append("\n from ");
            sb.append(first[i]);
        }

        // where
        String[] second = sb.toString().split("\\swhere\\s");
        sb = new StringBuilder();
        sb.append(second[0]);

        for (int j = 1; j < second.length; j++) {
            sb.append("\n where ");
            sb.append(second[j]);
        }

        return sb.toString();

    }

    // ~ Helpers
    // =========================================================================

    /**
     * asks {@link BasicSecuritySystem} to create a new managed {@link Details}
     * based on the previous state of this entity. If the previous state is null
     * (see ticket:3929) then throw an exception.
     *
     * @param entity
     *            IObject to be updated
     * @param currentState
     *            the possibly changed field data for this entity
     * @param previousState
     *            the field data as seen in the db
     * @param idx
     *            the index of Details in the state arrays.
     */
    protected boolean resetDetails(IObject entity, Object[] currentState,
            Object[] previousState, int idx) {

        if (previousState == null) {
            log.warn(String.format("Null previousState for %s(loaded=%s). Details=%s",
                entity, entity.isLoaded(), currentState[idx]));
            throw new InternalException("Previous state is null. Possibly caused by evict. See ticket:3929");
        }

        final Details previous = (Details) previousState[idx];
        final Details result = checkManagedDetails(entity, previous);

        if (previous != result) {
            currentState[idx] = result;
            return true;
        }

        return false;
    }

    protected void log(String msg) {
        if (msg.equals(last)) {
            count++;
        }

        else if (log.isDebugEnabled()) {
            String times = " ( " + count + " times )";
            log.debug(msg + times);
            last = msg;
            count = 1;
        }
    }

    private void debug(String msg) {
        if (log.isDebugEnabled()) {
            log(msg);
        }
    }

    // Methods moved from BasicSecuritySystem
    // =========================================================================

    /**
     * Checks the details of the objects which the given object links to in
     * order to guarantee that linkages are valid.
     *
     * This method is called during
     * {@link OmeroInterceptor#onSave(Object, java.io.Serializable, Object[], String[], org.hibernate.type.Type[])
     * save} and
     * {@link OmeroInterceptor#onFlushDirty(Object, java.io.Serializable, Object[], Object[], String[], org.hibernate.type.Type[])
     * update} since this is the only time that new entity references can be
     * created.
     *
     * @param iObject
     *            new or updated entity which may reference other entities which
     *            then require locking. Nulls are tolerated but do nothing.
     */
    public void evaluateLinkages(IObject iObject) {

        if (iObject == null ||
                sysTypes.isSystemType(iObject.getClass()) ||
                sysTypes.isInSystemGroup(iObject.getDetails())
                ) {
            return;
        }

        IObject[] candidates = em.getLockCandidates(iObject);
        for (IObject object : candidates) {

            if (!sysTypes.isSystemType(object.getClass()) &&
                    !sysTypes.isInSystemGroup(object.getDetails()) &&
                    !sysTypes.isInUserGroup(object.getDetails())) {

                Details d = object.getDetails();
                if (d == null) {
                    // ticket:2575. Previously, the details of the candidates
                    // were never null. the addition of the reagent linkages
                    // *somehow* led to NPEs here. for the moment, we're assuming
                    // if null, then the object can't be mis-linked. (i.e. it's
                    // probably new)
                    continue;
                }

                if (d != null && d.getGroup() != null &&
                        !HibernateUtils.idEqual(d.getGroup(),
                        currentUser.getGroup())) {
                    throw new GroupSecurityViolation(String.format(
                            "MIXED GROUP: " +
                            "%s(group=%s) and %s(group=%s) cannot be linked.",
                            iObject, currentUser.getGroup(),
                            object, d.getGroup()));
                }

                // Rather than as in <=4.1 in which objects were scheduled
                // for locking which prevented later actions, now we check
                // whether or not we're graph critical and if so, and if
                // the objects do not belong the current user, then we abort.

                Experimenter owner = object.getDetails().getOwner();
                if (owner == null) {
                    continue;
                }
                Long oid = owner.getId();
                Long uid = currentUser.getOwner().getId();
                if (oid != null && !uid.equals(oid)) {
                    if (currentUser.isGraphCritical()) {  // ticket:1769
                    String gname = currentUser.getGroup().getName();
                    String oname = currentUser.getOwner().getOmeName();
                    Permissions p = currentUser.getCurrentEventContext()
                        .getCurrentGroupPermissions();
                    throw new ReadOnlyGroupSecurityViolation(String.format(
                            "Cannot link to %s\n" +
                            "Current user (%s) is an admin or the owner of\n" +
                            "the private group (%s=%s). It is not allowed to\n" +
                            "link to users' data.", object, oname, gname, p));
                    } else if (!currentUser.getCurrentEventContext()
                            .getCurrentGroupPermissions()
                            .isGranted(Role.GROUP, Right.WRITE)) {// ticket:1992
                        throw new ReadOnlyGroupSecurityViolation("Group is READ-ONLY. " +
					"Cannot link to object: " + object);
                    }
                }
            }
        }

    }

    // TODO is this natural? perhaps permissions don't belong in details
    // details are the only thing that users can change the rest is
    // read only...
    /**
     * @see SecuritySystem#newTransientDetails(IObject)
     */
    public Details newTransientDetails(IObject obj) {

        if (obj == null) {
            throw new ApiUsageException("Argument cannot be null.");
        }

        if (tokenHolder.hasPrivilegedToken(obj)) {
            return obj.getDetails(); // EARLY EXIT
        }

        final Details source = obj.getDetails();
        final Details newDetails = source.newInstance();
        final BasicEventContext bec = currentUser.current();

        newDetails.copy(currentUser.createDetails());

        if (source != null) {

            // OWNER
            // users *aren't* allowed to set the owner of an item.
            if (source.getOwner() != null
                    && !newDetails.getOwner().getId().equals(
                            source.getOwner().getId())) {
                // but this is root
                if (bec.isCurrentUserAdmin()) {
                    newDetails.setOwner(source.getOwner());
                } else {
                    throw new SecurityViolation(String.format(
                            "You are not authorized to set the Experimenter"
                                    + " for %s to %s", obj, source.getOwner()));
                }

            }

            // GROUP
            // users are only allowed to set to the current group
            if (source.getGroup() != null && source.getGroup().getId() != null) {

                // ticket:1434
                if (bec.getCurrentGroupId().equals(source.getGroup().getId())) {
                    newDetails.setGroup(source.getGroup());
                }

                // ticket:1794
                else if (bec.isCurrentUserAdmin() &&
                        Long.valueOf(roles.getUserGroupId())
                        .equals(source.getGroup().getId())) {
                    newDetails.setGroup(source.getGroup());
                }

                // oops. boom!
                else {
                    throw new SecurityViolation(String.format(
                            "You are not authorized to set the ExperimenterGroup"
                                    + " for %s to %s", obj, source.getGroup()));
                }
            }


            // PERMISSIONS: ticket:1434 and #1731 and #1779 (systypes)
            // before 4.2, users were allowed to manually set the permissions
            // on an object, and even set a umask to be applied. for the initial
            // 4.2 version, however, we are disallowing manually setting
            // permissions so that all objects will match group permissions.
            // Doing this after the setting of newDetails.group in case the
            // user is logged into user or system.
            if (source.getPermissions() != null) {

                Permissions groupPerms = currentUser.getCurrentEventContext()
                    .getCurrentGroupPermissions();

                boolean isInSysGrp = sysTypes.isInSystemGroup(newDetails);
                boolean isInUsrGrp = sysTypes.isInUserGroup(newDetails);
                if (groupPerms.identical(source.getPermissions())) {
                    // ok. weird that they're set. probably an instance
                    // of a managed object being passed in as with
                    // ticket:2055
                } else if (!sysTypes.isSystemType(obj.getClass())) {
                    if (isInSysGrp) {
                        // allow admin to do what they want. is this right?
                    } else if (isInUsrGrp) {
                        // similarly, allow whatever in user group for the moment.
                    } else {
                        throw new PermissionMismatchGroupSecurityViolation(
                        "Manually setting permissions currently disallowed");
                    }
                }
                // Above didn't throw, so set permissions.
                newDetails.setPermissions(source.getPermissions());
            }

            // EXTERNALINFO
            // useres _are_ allowed to set the external info on a new object.
            // subsequent operations, however, will not be able to edit this
            // value.
            newDetails.setExternalInfo(source.getExternalInfo());

            // CREATION/UPDATEVENT : currently ignore what users do

        }

        return newDetails;

    }

    /**
     * @see SecuritySystem#checkManagedDetails(IObject, Details)
     */
    public Details checkManagedDetails(final IObject iobj,
            final Details previousDetails) {

        if (iobj == null) {
            throw new ApiUsageException("Argument cannot be null.");
        }

        if (iobj.getId() == null) {
            throw new ValidationException(
                    "Id required on all detached instances.");
        }

        // Note: privileged check moved into the if statement below.

        // done first as validation.
        if (iobj instanceof IMutable) {
            Integer version = ((IMutable) iobj).getVersion();
            if (version == null || version.intValue() < 0) {
                ;
                // throw new ValidationException(
                // "Version must properly be set on managed objects :\n"+
                // obj.toString()
                // );
                // TODO
            }
        }

        // check if the newDetails variable has been reset or if the instance
        // has been changed.
        boolean altered = false;

        final Details currentDetails = iobj.getDetails();
        /* not final! */Details newDetails = currentDetails.newInstance();
        newDetails.copy(currentUser.createDetails());

        // This happens if all fields of details are null (which can't happen)
        // And is so uninteresting for all of our checks. The object can't be
        // locked and nothing can be edited. Just return null.
        if (previousDetails == null) {
            newDetails = null;
            altered = true;
            if (log.isDebugEnabled()) {
                log.debug("Setting details on " + iobj
                        + " to null like original");
            }
        }

        // Also uninteresting. If the users say nothing, then the originals.
        // Probably common since users don't worry about this information.
        else if (currentDetails == null) {
            newDetails = previousDetails.copy();
            altered = true;
            if (log.isDebugEnabled()) {
                log.debug("Setting details on " + iobj
                        + " to copy of original details.");
            }

            // Now we have to make sure certain things do not happen. The
            // following
            // take into account whether or not the entity is privileged (has a
            // token),
            // is locked in the database, and who the current user and group
            // are.
        } else {

            boolean privileged = false;

            if (tokenHolder.hasPrivilegedToken(iobj)) {
                privileged = true;
            }

            // Acquiring the context here to prevent multiple
            // accesses to the threadlocal
            final BasicEventContext bec = currentUser.current();

            // ticket:1784 - NOTE: here we are NOT including a check
            // for sysTypes.isInSystemGroup(), since that implies that
            // the object doesn't have owner/group
            final boolean sysType = sysTypes.isSystemType(iobj.getClass());

            // isGlobal implies nothing (currently) about external info
            // see mapping.vm for more.
            altered |= managedExternalInfo(privileged, iobj,
                    previousDetails, currentDetails, newDetails);

            // implies that owner doesn't matter
            if (!sysType) {
                altered |= managedOwner(privileged, iobj,
                        previousDetails, currentDetails, newDetails, bec);
            }

            // implies that group doesn't matter
            if (!sysType) {
                altered |= managedGroup(privileged, iobj,
                        previousDetails, currentDetails, newDetails, bec);
            }

            // implies that Permissions dosn't matter
            //if (!IGlobal.class.isAssignableFrom(iobj.getClass())) {
            // ticket:1434 re-activating permission mgmt for globals.
            // ticket:1791 moving after managedGroup to re-use newDetails
                altered |= managedPermissions(privileged, iobj,
                        previousDetails, currentDetails, newDetails, sysType);
            //}

            // the event check needs to be last, because we need to test
            // whether or not it is necessary to change the updateEvent
            // (i.e. last modification)
            // implies that event doesn't matter
            if (!sysType) {
                altered |= managedEvent(privileged, iobj,
                        previousDetails, currentDetails, newDetails);
            }

        }

        return altered ? newDetails : previousDetails;

    }

    /**
     * responsible for guaranteeing that external info is not modified by any
     * users, including rot.
     *
     * @param locked
     * @param privileged
     * @param obj
     * @param previousDetails
     *            details representing the known DB state
     * @param currentDetails
     *            details representing the user request (UNTRUSTED)
     * @param newDetails
     *            details from the current context. Holder for the merged
     *            {@link Permissions}
     * @return true if the {@link Permissions} of newDetails are changed.
     */
    protected boolean managedExternalInfo(boolean privileged,
            IObject obj, Details previousDetails, Details currentDetails,
            Details newDetails) {

        boolean altered = false;

        ExternalInfo previous = previousDetails == null ? null
                : previousDetails.getExternalInfo();

        ExternalInfo current = currentDetails == null ? null : currentDetails
                .getExternalInfo();

        if (previous == null) {
            // do we allow a change?
            newDetails.setExternalInfo(current);
            altered |= newDetails.getExternalInfo() != current;
        }

        // The ExternalInfo was previously set. We do not allow it to be
        // changed,
        // similar to not allowing the Event for an entity to be changed.
        else {
            if (!HibernateUtils.idEqual(previous, current)) {
                throw new SecurityViolation(String.format(
                        "Cannot update ExternalInfo for %s from %s to %s", obj,
                        previous, current));
            }
        }

        return altered;
    }

    /**
     * responsible for properly copying user-requested permissions taking into
     * account the {@link Flag#LOCKED} status. This method does not need to
     * (like {@link #newTransientDetails(IObject)} take into account the session
     * umask available from {@link CurrentDetails#createDetails()}
     *
     * @param locked
     * @param privileged
     * @param obj
     * @param previousDetails
     *            details representing the known DB state
     * @param currentDetails
     *            details representing the user request (UNTRUSTED)
     * @param newDetails
     *            details from the current context. Holder for the merged
     *            {@link Permissions}
     * @return true if the {@link Permissions} of newDetails are changed.
     */
    protected boolean managedPermissions(boolean privileged,
            IObject obj, Details previousDetails, Details currentDetails,
            Details newDetails, boolean sysType) {

        // setup

        boolean altered = false;

        Permissions previousP = previousDetails == null ? null
                : previousDetails.getPermissions();

        Permissions currentP = currentDetails == null ? null : currentDetails
                .getPermissions();

        // ignore newDetails permissions.

        // If the stored perms are null, then we can't validate anything
        // TODO : is this alright. Should only happen for system types.
        // Then can silently ignore ??
        if (previousP == null) {
            if (currentP == null) {
                newDetails.setPermissions(null);
                altered |= false; // don't need to update
            } else {
                newDetails.setPermissions(currentP);
                altered = true;
            }
        }

        // Users did not enter permission (normal case) so is null.
        else if (currentP == null) {
            newDetails.setPermissions(previousP);
            altered = true;
        }

        // if the user has set the permissions (currentDetails), then we should
        // try to allow that. if it's identical to the current, then there
        // is no reason to hit the DB.
        else {

            // if we need to filter any permissions, do it here!

            newDetails.setPermissions(currentP);

            // see https://trac.openmicroscopy.org.uk/omero/ticket/1434
            // and https://trac.openmicroscopy.org.uk/omero/ticket/1731
            if (!currentP.identical(previousP) &&
                    obj instanceof ExperimenterGroup) {
                throw new PermissionMismatchGroupSecurityViolation(
                        "Group permissions must be changed via IAdmin");
            }

            // see https://trac.openmicroscopy.org.uk/omero/ticket/1776
            Permissions groupPerms = currentUser.getCurrentEventContext()
                .getCurrentGroupPermissions();
            if ( !(sysType || sysTypes.isInUserGroup(newDetails)) // ticket:1791
                && !groupPerms.sameRights(currentP)) { // ticket:1779
                // ticket:2204. After work on permissions upgrade, it was
                // decide to just ignore all incorrect permissions for the
                // moment.
                newDetails.setPermissions(previousP);
                altered = true;
            }

            // finally, check isOwnerOrSupervisor.
            if (!currentP.identical(previousP)) {
                if (!currentUser.isOwnerOrSupervisor(obj)) {
                    // remove from below??
                    throw new SecurityViolation(String.format(
                            "You are not authorized to change "
                                    + "the permissions for %s from %s to %s",
                            obj, previousP, currentP));
                }
            }
        }

        // privileged plays no role since everyone can alter their permissions
        // (within bounds)

        return altered;

    }

    protected boolean managedOwner(boolean privileged,
            IObject obj, Details previousDetails, Details currentDetails,
            Details newDetails, final BasicEventContext bec) {

        if (!HibernateUtils.idEqual(previousDetails.getOwner(), currentDetails
                .getOwner())) {

            // !idEquals implies that they aren't both null; if current_owner is
            // null, then it was *probably* not intended, so just fix it and
            // move on. this goes for root and admins as well.
            if (currentDetails.getOwner() == null) {
                newDetails.setOwner(previousDetails.getOwner());
                return true;
            }

            // if the current user is an admin or if the entity has been
            // marked privileged, then use the current owner.
            else if (bec.isCurrentUserAdmin() || privileged) {
                // ok
            }

            // everyone else can't change them at all.
            else {
                throw new SecurityViolation(String.format(
                        "You are not authorized to change "
                                + "the owner for %s from %s to %s", obj,
                        previousDetails.getOwner(), currentDetails.getOwner()));
            }
        }

        else {

            // values are the same. ensure they are the same for
            // newDetails as well
            newDetails.setOwner(previousDetails.getOwner());
        }
        return false;
    }

    protected boolean managedGroup(boolean privileged,
            IObject obj, Details previousDetails, Details currentDetails,
            Details newDetails, final BasicEventContext bec) {

        if (null != previousDetails.getGroup()) {
            long objGroupId = previousDetails.getGroup().getId();
            long sessGroupId = currentUser.getGroup().getId();
            long userGroupId = roles.getUserGroupId();
            if (sessGroupId != objGroupId && objGroupId != userGroupId) { // ticket:1794 & ticket:2058
                throw new SecurityViolation(String.format(
                        "Currently logged into group %s. Cannot alter object in group %s",
                        sessGroupId, objGroupId));
            }
        }

        // previous and current have different ids. either change it and return
        // true if permitted, or throw an exception.
        if (!HibernateUtils.idEqual(previousDetails.getGroup(), currentDetails
                .getGroup())) {

            // !idEquals implies that they aren't both null; if current_group is
            // null, then it was *probably* not intended, so just fix it and
            // move on. this goes for root and admins as well.
            if (currentDetails.getGroup() == null) {
                newDetails.setGroup(previousDetails.getGroup());
                return true;
            }

            // if user is a member of the group or the current user is an admin
            // or if the entity has been marked as privileged, then use the
            // current group.
            // TODO refactor
            else if ((!currentDetails.getGroup().getId().equals(
                         roles.getUserGroupId()) &&
                       bec.getMemberOfGroupsList().contains(
                         currentDetails.getGroup().getId())) // ticket:1794
                    || bec.isCurrentUserAdmin() || privileged) {
                newDetails.setGroup(currentDetails.getGroup());
                return true;
            }

            // everyone else can't change them at all.
            else {
                throw new SecurityViolation(String.format(
                        "You are not authorized to change "
                                + "the group for %s from %s to %s", obj,
                        previousDetails.getGroup(), currentDetails.getGroup()));
            }

        }

        // previous and current are the same, but we need to set
        // that value on newDetails.
        else {

            // This doesn't need to return true, because it'll only
            // be used if something else was changed.
            newDetails.setGroup(previousDetails.getGroup());

        }
        return false;
    }

    protected boolean managedEvent(boolean privileged,
            IObject obj, Details previousDetails, Details currentDetails,
            Details newDetails) {

        // TODO no longer need to keep track of alteration boolean. like
        // transient with update event, managedDetails will now ALWAYS return
        // an updated details.
        // -------------------

        boolean altered = false;

        // creation event~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        if (!HibernateUtils.idEqual(previousDetails.getCreationEvent(),
                currentDetails.getCreationEvent())) {

            // !idEquals implies that they aren't both null; if current_event is
            // null, then it was *probably* not intended, so just fix it and
            // move on. this goes for root and admins as well.
            if (currentDetails.getCreationEvent() == null) {
                newDetails.setCreationEvent(previousDetails.getCreationEvent());
                altered = true;
            }
            // otherwise throw an exception, because as seen in ticket:346,
            // it can lead to confusion otherwise. See:
            // https://trac.openmicroscopy.org.uk/omero/ticket/346
            else {

                // no one change them.
                throw new SecurityViolation(String.format(
                        "You are not authorized to change "
                                + "the creation event for %s from %s to %s",
                        obj, previousDetails.getCreationEvent(), currentDetails
                                .getCreationEvent()));
            }

        }

        // they are equal meaning no change was intended but in case other
        // changes took place, we have to make sure newDetails has the correct
        // value
        else {
            newDetails.setCreationEvent(previousDetails.getCreationEvent());
        }

        // update event ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        if (!HibernateUtils.idEqual(previousDetails.getUpdateEvent(),
                currentDetails.getUpdateEvent())) {

            // !idEquals implies that they aren't both null; if current_event is
            // null, then it was *probably* not intended, so just fix it and
            // move on. this goes for root and admins as well.
            if (currentDetails.getUpdateEvent() == null) {
                newDetails.setUpdateEvent(previousDetails.getUpdateEvent());
                altered = true;
            }
            // otherwise throw an exception, because as seen in ticket:346,
            // it can lead to confusion otherwise. See:
            // https://trac.openmicroscopy.org.uk/omero/ticket/346
            else {

                // no one change them, but this is less likely intentional
                // and more likely an optimistic lock issue. ticket:2162
                throw new OptimisticLockException(String.format(
                        "You are not authorized to change "
                                + "the update event for %s from %s to %s\n"
                                + "You may need to reload the object before continuing.", obj,
                        previousDetails.getUpdateEvent(), currentDetails
                                .getUpdateEvent()));
            }

        }

        // they are equal meaning no change was intended but in case other
        // changes took place, we have to make sure newDetails has the correct
        // value
        else {
            newDetails.setUpdateEvent(previousDetails.getUpdateEvent());
        }

        // QUATSCH
        // update event : newDetails keeps its update event, which is by
        // necessity different then what was in currentDetails and there-
        // fore we now return true;

        return altered;
    }

    // ~ Details checks. Used by to examine transient and managed Details.
    // =========================================================================
    // Also copied from BasicSecuritySystem

    /**
     * everyone is allowed to set the umask if desired. if the user does not set
     * a permissions, then the DEFAULT value as defined in the Permissions class
     * is used. if there's a umask for this session then that will be AND'd
     * against the given permissions.
     */
    boolean copyNonNullPermissions(Details target, Permissions p) {
        if (p != null) {
            target.setPermissions(p);
            return true;
        }
        return false;
    }

}
