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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IGlobal;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.ExternalInfo;
import ome.security.SecuritySystem;
import ome.security.SystemTypes;
import ome.system.Principal;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.HibernateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
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

    private final PrincipalHolder principalHolder;

    private final TokenHolder tokenHolder;

    private final ExtendedMetadata em;

    public OmeroInterceptor(SystemTypes sysTypes, ExtendedMetadata em,
            CurrentDetails cd, TokenHolder tokenHolder,
            PrincipalHolder principalHolder) {
        Assert.notNull(principalHolder);
        Assert.notNull(tokenHolder);
        Assert.notNull(sysTypes);
        Assert.notNull(em);
        Assert.notNull(cd);
        this.principalHolder = principalHolder;
        this.tokenHolder = tokenHolder;
        this.sysTypes = sysTypes;
        this.currentUser = cd;
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

        if (entity instanceof IObject) {
            IObject iobj = (IObject) entity;
            int idx = HibernateUtils.detailsIndex(propertyNames);

            markLockedIfNecessary(iobj);

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

        boolean altered = false;
        if (entity instanceof IObject) {
            IObject iobj = (IObject) entity;
            int idx = HibernateUtils.detailsIndex(propertyNames);

            markLockedIfNecessary(iobj);

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
     * based on the previous state of this entity.
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
        Details previous = (Details) previousState[idx];
        Details result = checkManagedDetails(entity, previous);

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
     * checks, and if necessary, stores argument and entities attached to the
     * argument entity in the current context for later modification (see
     * {@link #lockMarked()}
     * 
     * These modifications cannot be done during save and update because not
     * just the entity itself but entities 1-step down the graph are to be
     * edited, and it cannot be guaranteed that the graph walk will not
     * subsequently re-write the changes. Instead, changes are all made during
     * the flush procedure of {@link FlushEntityEventListener}. This also
     * prevents accidental changes by administrative users by making the locking
     * of an element the very last action.
     * 
     * This method is called during
     * {@link OmeroInterceptor#onSave(Object, java.io.Serializable, Object[], String[], org.hibernate.type.Type[]) save}
     * and
     * {@link OmeroInterceptor#onFlushDirty(Object, java.io.Serializable, Object[], Object[], String[], org.hibernate.type.Type[]) update}
     * since this is the only time that new entity references can be created.
     * 
     * @param iObject
     *            new or updated entity which may reference other entities which
     *            then require locking. Nulls are tolerated but do nothing.
     */
    public void markLockedIfNecessary(IObject iObject) {
        if (iObject == null || sysTypes.isSystemType(iObject.getClass())) {
            return;
        }

        Set<IObject> s = new HashSet<IObject>();

        IObject[] candidates = em.getLockCandidates(iObject);
        for (IObject object : candidates) {
            // omitting system types since they don't have permissions
            // which can be locked.
            if (sysTypes.isSystemType(object.getClass())) {
                // do nothing.
            } else {
                s.add(object);
            }
            // TODO NEED TO CHECK FOR OWNERSHIP etc. etc.
        }

        currentUser.appendLockCandidates(s);
    }

    /**
     * sets the {@link Flag#LOCKED LOCKED flag} on the entities stored in the
     * context from the {@link #markLockedIfNecessary(IObject)} method. Called
     * from
     * {@link FlushEntityEventListener#onFlushEntity(org.hibernate.event.FlushEntityEvent)}
     */
    public void lockMarked() {
        Set<IObject> c = currentUser.getLockCandidates();

        for (IObject i : c) {
            Details d = i.getDetails();
            Permissions p = new Permissions(d.getPermissions());
            p.set(Flag.LOCKED);
            d.setPermissions(p);
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
        newDetails.copy(currentUser.createDetails());

        if (source != null) {

            // PERMISSIONS
            // users _are_ allowed to alter the permissions of new objects.
            // this entity may be locked in the same operation, in which case
            // the READ permissions which are set will not be removable.
            copyNonNullPermissions(newDetails, source.getPermissions());
            applyUmaskIfNecessary(newDetails);

            // OWNER
            // users *aren't* allowed to set the owner of an item.
            if (source.getOwner() != null
                    && !newDetails.getOwner().getId().equals(
                            source.getOwner().getId())) {
                // but this is root
                if (currentUser.isAdmin()) {
                    newDetails.setOwner(source.getOwner());
                } else {
                    throw new SecurityViolation(String.format(
                            "You are not authorized to set the Experimenter"
                                    + " for %s to %s", obj, source.getOwner()));
                }

            }

            // GROUP
            // users are only allowed to set to another of their groups
            if (source.getGroup() != null && source.getGroup().getId() != null) {
                // users can change to their own group
                if (currentUser.getMemberOfGroups().contains(
                        source.getGroup().getId())) {
                    newDetails.setGroup(source.getGroup());
                }

                // and admin can change it too
                else if (currentUser.isAdmin()) {
                    newDetails.setGroup(source.getGroup());
                }

                // oops. boom!
                else {
                    throw new SecurityViolation(String.format(
                            "You are not authorized to set the ExperimenterGroup"
                                    + " for %s to %s", obj, source.getGroup()));
                }
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
            if (currentDetails != null) {
                newDetails = null;
                altered = true;
                if (log.isDebugEnabled()) {
                    log.debug("Setting details on " + iobj
                            + " to null like original");
                }
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

            boolean locked = false;
            boolean privileged = false;

            if (previousDetails.getPermissions().isSet(Flag.LOCKED)) {
                locked = true;
            }

            if (tokenHolder.hasPrivilegedToken(iobj)) {
                privileged = true;
            }

            // isGlobal implies nothing (currently) about external info
            // see mapping.vm for more.
            altered |= managedExternalInfo(locked, privileged, iobj,
                    previousDetails, currentDetails, newDetails);

            // implies that Permissions dosn't matter
            if (!IGlobal.class.isAssignableFrom(iobj.getClass())) {
                altered |= managedPermissions(locked, privileged, iobj,
                        previousDetails, currentDetails, newDetails);
            }

            // implies that owner doesn't matter
            if (!IGlobal.class.isAssignableFrom(iobj.getClass())) {
                altered |= managedOwner(locked, privileged, iobj,
                        previousDetails, currentDetails, newDetails);
            }

            // implies that group doesn't matter
            if (!IGlobal.class.isAssignableFrom(iobj.getClass())) {
                altered |= managedGroup(locked, privileged, iobj,
                        previousDetails, currentDetails, newDetails);
            }

            // the event check needs to be last, because we need to test
            // whether or not it is necessary to change the updateEvent
            // (i.e. last modification)
            // implies that event doesn't matter
            if (!IGlobal.class.isAssignableFrom(iobj.getClass())) {
                altered |= managedEvent(locked, privileged, iobj,
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
    protected boolean managedExternalInfo(boolean locked, boolean privileged,
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
    protected boolean managedPermissions(boolean locked, boolean privileged,
            IObject obj, Details previousDetails, Details currentDetails,
            Details newDetails) {

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

        // WORKAROUND for ticket:307 by checking for SOFT below
        // see https://trac.openmicroscopy.org.uk/omero/ticket/307
        // see
        // http://opensource.atlassian.com/projects/hibernate/browse/HHH-2027

        // Users did not enter permission (normal case) so is null OR
        // in the workaround permissions is SOFT, then
        // need to copy whole sale those from database.
        else if (currentP == null || currentP.isSet(Flag.SOFT)) {
            newDetails.setPermissions(previousP);
            altered = true;
        }

        // if the user has set the permissions (currentDetails), then we should
        // try to allow that. if it's identical to the current, then there
        // is no reason to hit the DB.
        else {

            // if we need to filter any permissions, do it here!

            newDetails.setPermissions(currentP);
            Permissions tmpDetails = new Permissions(previousP);
            // see https://trac.openmicroscopy.org.uk/omero/ticket/553
            if (currentP.isSet(Flag.LOCKED)) {
                tmpDetails.set(Flag.LOCKED);
            } else {
                tmpDetails.unSet(Flag.LOCKED);
            }
            if (!currentP.identical(tmpDetails)) {
                if (!currentUser.isOwnerOrSupervisor(obj)) {
                    // remove from below??
                    throw new SecurityViolation(String.format(
                            "You are not authorized to change "
                                    + "the permissions for %s from %s to %s",
                            obj, previousP, currentP));
                }

                altered = true;
            }
        }

        // now we've calculated the desired permissions, throw
        // a security violation if this instance was locked AND
        // the read permissions have been lowered or if the lock
        // was removed.
        if (locked) {

            if (previousP == null) {
                throw new InternalException("Null permissions cannot be locked");
            }

            Permissions calculatedP = newDetails.getPermissions();

            if (calculatedP != null) {

                // can't override
                if (!calculatedP.isSet(Flag.LOCKED)) {
                    calculatedP.set(Flag.LOCKED);
                    altered = true;
                }

                if (previousP.isGranted(USER, READ)
                        && !calculatedP.isGranted(USER, READ)
                        || previousP.isGranted(GROUP, READ)
                        && !calculatedP.isGranted(GROUP, READ)
                        || previousP.isGranted(WORLD, READ)
                        && !calculatedP.isGranted(WORLD, READ)) {
                    throw new SecurityViolation(
                            "Cannot remove READ from locked entity:" + obj);
                }
            }
        }

        // privileged plays no role since everyone can alter their permissions
        // (within bounds)

        return altered;

    }

    protected boolean managedOwner(boolean locked, boolean privileged,
            IObject obj, Details previousDetails, Details currentDetails,
            Details newDetails) {

        if (!HibernateUtils.idEqual(previousDetails.getOwner(), currentDetails
                .getOwner())) {

            // !idEquals implies that they aren't both null; if current_owner is
            // null, then it was *probably* not intended, so just fix it and
            // move on. this goes for root and admins as well.
            if (currentDetails.getOwner() == null) {
                newDetails.setOwner(previousDetails.getOwner());
                return true;
            }

            // Locked items cannot have their owner altered, unless they
            // are world-readable. In that case, the owner will play no
            // real role. The WORLD-READ is also not removable. The check for
            // this is in managedPermissions()
            if (locked
                    && !currentDetails.getPermissions().isGranted(WORLD, READ)) {
                throw new SecurityViolation("Object locked! "
                        + "Cannot change owner for:" + obj);
            }

            // if the current user is an admin or if the entity has been
            // marked privileged, then use the current owner.
            else if (currentUser.isAdmin() || privileged) {
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

    protected boolean managedGroup(boolean locked, boolean privileged,
            IObject obj, Details previousDetails, Details currentDetails,
            Details newDetails) {
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

            if (locked) {
                throw new SecurityViolation("Object locked! "
                        + "Cannot change group for entity:" + obj);
            }

            // if user is a member of the group or the current user is an admin
            // or if the entity has been marked as privileged, then use the
            // current group.
            // TODO refactor
            else if (currentUser.getMemberOfGroups().contains(
                    currentDetails.getGroup().getId())
                    || currentUser.isAdmin() || privileged) {
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

    protected boolean managedEvent(boolean locked, boolean privileged,
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

                // no one change them.
                throw new SecurityViolation(String.format(
                        "You are not authorized to change "
                                + "the update event for %s from %s to %s", obj,
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

    /**
     * transient details should have the umask applied to them if soft.
     */
    void applyUmaskIfNecessary(Details d) {
        Principal pr = principalHolder.getLast();
        Permissions p = d.getPermissions();
        if (p.isSet(Flag.SOFT)) {
            if (pr.hasUmask()) {
                p.grantAll(pr.getUmask());
                p.revokeAll(pr.getUmask());
            }
            // don't store it in the DB.
            p.unSet(Flag.SOFT);
        }
    }
}
