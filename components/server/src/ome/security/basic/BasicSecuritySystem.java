/*
 * ome.security.basic.BasicSecuritySystem
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.security.basic;

// Java imports
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.Assert;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IEnum;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.GraphHolder;
import ome.model.internal.Permissions;
import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;
import ome.model.internal.Token;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.ExternalInfo;
import ome.model.meta.GroupExperimenterMap;
import ome.security.ACLVoter;
import ome.security.AdminAction;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.security.basic.BasicACLVoter;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.HibernateUtils;
import ome.tools.hibernate.SecurityFilter;
import ome.tools.spring.PostProcessInjector;
import ome.util.IdBlock;

/**
 * simplest implementation of {@link SecuritySystem}. Uses an ctor-injected
 * {@link EventContext} and the {@link ThreadLocal ThreadLocal-}based
 * {@link CurrentDetails} to provide the security infrastructure.
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
public class BasicSecuritySystem implements SecuritySystem {
	private final static Log log = LogFactory.getLog(BasicSecuritySystem.class);

	/**
	 * private token for identifying loose "ownership" of certain objects.
	 * 
	 * @see IObject#getGraphHolder()
	 * @see GraphHolder#hasToken()
	 */
	private Token token = new Token();

	/** {@link ServiceFactory} for accessing all available services */
	private ServiceFactory sf;

	/** metadata for calculating certain walks */
	protected ExtendedMetadata em;

	/** active principal */
	protected ThreadLocal<Principal> principalHolder =
		new ThreadLocal<Principal>();
	
	// Configured Elements
	
	protected CurrentDetails cd = new CurrentDetails();
	
	protected Roles roles = new Roles();
		
	protected ACLVoter acl = new BasicACLVoter(this); // FIXME dangerous
	
	/**
	 * only public constructor for this {@link SecuritySystem} implementation.
	 * 
	 * @param factory
	 *            Not null.
	 */
	public BasicSecuritySystem(ServiceFactory factory) {
		Assert.notNull(factory);
		this.sf = factory;
	}

	/**
	 * injector for {@link ExtendedMetadata}. Needed to overcome a cyclical
	 * dependency in the Hibernate beans. Used by {@link PostProcessInjector} to
	 * fulfill this requirement.
	 */
	public final void setExtendedMetadata(ExtendedMetadata metadata) {
		if (this.em != null)
			throw new InternalException("Cannot reset metadata.");

		this.em = metadata;
	}

	// ~ Login/logout
	// =========================================================================

	public void login(Principal principal) {
		principalHolder.set(principal);
	}

	public void logout() {
		principalHolder.remove();
	}

	// ~ Checks
	// =========================================================================
	/**
	 * implements {@link SecuritySystem#isReady()}. Simply checks for null
	 * values in all the relevant fields of {@link CurrentDetails}
	 */
	public boolean isReady() {
		// TODO could check for open session.
		if (cd.getCreationEvent() != null
				&& cd.getGroup() != null
				&& cd.getOwner() != null) {
			return true;
		}
		return false;
	}

	/**
	 * implements {@link SecuritySystem#isGlobal()} classes without owners and
	 * events
	 */
	public boolean isGlobal(Class<? extends IObject> klass) {
		if (klass == null)
			return false;
		if (Experimenter.class.isAssignableFrom(klass))
			return true;
		if (Event.class.isAssignableFrom(klass))
			return true;
		if (EventLog.class.isAssignableFrom(klass))
			return true;
		return false;
	}

	/**
	 * classes which cannot be created by regular users.
	 * 
	 * @see <a
	 *      href="https://trac.openmicroscopy.org.uk/omero/ticket/156">ticket156</a>
	 */
	public boolean isSystemType(Class<? extends IObject> klass) {
		if (klass == null)
			return false;
		if (Experimenter.class.isAssignableFrom(klass))
			return true;
		if (ExperimenterGroup.class.isAssignableFrom(klass))
			return true;
		if (GroupExperimenterMap.class.isAssignableFrom(klass))
			return true;
		if (Event.class.isAssignableFrom(klass))
			return true;
		if (EventLog.class.isAssignableFrom(klass))
			return true;
		if (IEnum.class.isAssignableFrom(klass))
			return true;
		return false;
	}

	/** 
	 * tests whether or not the current user is either the owner of this entity,
	 * or the superivsor of this entity, for example as root or as group
	 * owner.
	 * 
	 * @param iObject Non-null managed entity. 
	 * @return true if the current user is owner or supervisor of this entity
	 */
	public boolean isOwnerOrSupervisor( IObject iObject )
	{
		if ( iObject == null ) throw new ApiUsageException("Object can't be null");
		final Long o = HibernateUtils.nullSafeOwnerId(iObject);
		final Long g = HibernateUtils.nullSafeGroupId(iObject);
		
		final EventContext ec = cd.getCurrentEventContext();
		final boolean isAdmin = ec.isCurrentUserAdmin();
		final boolean isPI    = ec.getLeaderOfGroupsList().contains(g);
		final boolean isOwner = ec.getCurrentUserId().equals(o);

		if (isAdmin || isPI || isOwner)
		{
			return true;
		}
		return false;
	}
	
	// ~ Read security
	// =========================================================================
	/** enables the read filter such that graph queries will have non-visible
	 * entities silently removed from the return value. This filter does <em>
	 * not</em> apply to single value loads from the database. See 
	 * {@link #allowLoad(Class, Details)} for more.
	 * 
	 * Note: this filter must be disabled on logout, otherwise the necessary
	 * parameters (current user, current group, etc.) for building the filters 
	 * will not be available. Similarly, while enabling this filter, no calls 
	 * should be made on the given session object.
	 * 
	 * @param session a generic session object which can be used to enable
	 *   this filter. Each {@link SecuritySystem} implementation will require
	 *   a specific session type.
	 * @see EventHandler#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public void enableReadFilter(Object session) {
		if (session == null || !(session instanceof Session)) {
			throw new ApiUsageException(
					"The Object argument to enableReadFilter"
							+ " in the BasicSystemSecurity implementation must be a "
							+ " non-null org.hibernate.Session.");
		}

		checkReady("enableReadFilter");
		// beware
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-1932
		Session sess = (Session) session;
		sess.enableFilter(SecurityFilter.filterName).setParameter(
				SecurityFilter.is_admin, currentUserIsAdmin()).setParameter(
				SecurityFilter.current_user, currentUserId()).setParameterList(
				SecurityFilter.current_groups, memberOfGroups())
				.setParameterList(SecurityFilter.leader_of_groups,
						leaderOfGroups());
	}

	/** disable this filer. All future queries will have no security context
	 * associated with them and all items will be visible. 
	 * 
	 * @param session a generic session object which can be used to disable
	 * 		this filter. Each {@link SecuritySystem} implementation will require
	 * 		a specifc session type.
	 * @see EventHandler#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public void disableReadFilter(Object session) {
		checkReady("disableReadFilter");

		Session sess = (Session) session;
		sess.disableFilter(SecurityFilter.filterName);
	}

	// ~ Subsystem disabling
	// =========================================================================

	public void disable(String... ids) {
		if (ids == null || ids.length == 0)
			throw new ApiUsageException("Ids should not be empty.");
		cd.addAllDisabled(ids);
	}

	public void enable(String... ids) {
		if (ids == null || ids.length == 0)
			cd.clearDisabled();
		cd.removeAllDisabled(ids);
	}

	public boolean isDisabled(String id) {
		if (id == null)
			throw new ApiUsageException("Id should not be null.");
		return cd.isDisabled(id);
	}

	// ~ Details (for OmeroInterceptor)
	// =========================================================================

	/** checks, and if necessary, stores argument and entities attached to the 
	 * argument entity in the current context for later modification 
	 * (see {@link #lockMarked()} 
	 * 
	 * These modifications cannot be done during save and update because not just
	 * the entity itself but entities 1-step down the graph are to be edited, 
	 * and it cannot be guaranteed that the graph walk will not subsequently
	 * re-write the changes. Instead, changes are all made during the flush
	 * procedure of {@link FlushEntityEventListener}. This also prevents 
	 * accidental changes by administrative users by making the locking of an
	 * element the very last action.
	 * 
	 * This method is called during 
	 * {@link OmeroInterceptor#onSave(Object, java.io.Serializable, Object[], String[], org.hibernate.type.Type[]) save}
	 * and {@link OmeroInterceptor#onFlushDirty(Object, java.io.Serializable, Object[], Object[], String[], org.hibernate.type.Type[]) update}
	 * since this is the only time that new entity references can be created.  
	 * 
	 * @param iObject new or updated entity which may reference other entities
	 * 		which then require locking. Nulls are tolerated but do nothing.
	 */
	public void markLockedIfNecessary(IObject iObject) {
		if (iObject == null || isSystemType( iObject.getClass() ))
			return;

		Set<IObject> s = new HashSet<IObject>();

		IObject[] candidates = em.getLockCandidates(iObject);
		for (IObject object : candidates) {
			// omitting system types since they don't have permissions
			// which can be locked.
			if (isSystemType(object.getClass()))
			{
				// do nothing.
			}
			else 
			{
				s.add(object);
			}
			// TODO NEED TO CHECK FOR OWNERSHIP etc. etc.
		}

		cd.appendLockCandidates(s);
	}

	/** sets the {@link Flag#LOCKED LOCKED flag} on the entities stored in the 
	 * context from the {@link #markLockedIfNecessary(IObject)} method. Called
	 * from {@link FlushEntityEventListener#onFlushEntity(org.hibernate.event.FlushEntityEvent)}
	 */
	public void lockMarked() {
		Set<IObject> c = cd.getLockCandidates();

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

		checkReady("transientDetails");

		if (obj == null)
			throw new ApiUsageException("Argument cannot be null.");
		
		if (hasPrivilegedToken(obj))
			return obj.getDetails(); // EARLY EXIT

		Details source = obj.getDetails();
		Details newDetails = cd.createDetails();

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
					&& !source.getOwner().getId().equals(
							newDetails.getOwner().getId())) {
				// but this is root
				if (currentUserIsAdmin()) {
					newDetails.setOwner(source.getOwner());
				} else {
					throw new SecurityViolation(String.format(
							"You are not authorized to set the ExperimenterID"
									+ " for %s to %d", obj, source.getOwner()
									.getId()));
				}

			}

			// GROUP
			// users are only allowed to set to another of their groups
			if (source.getGroup() != null && source.getGroup().getId() != null) {
				// users can change to their own group
				if (memberOfGroups().contains(source.getGroup().getId())) {
					newDetails.setGroup(source.getGroup());
				}

				// and admin can change it too
				else if (currentUserIsAdmin()) {
					newDetails.setGroup(source.getGroup());
				}

				// oops. boom!
				else {
					throw new SecurityViolation(String.format(
							"You are not authorized to set the ExperimenterGroupID"
									+ " for %s to %d", obj, source.getGroup()
									.getId()));
				}
			}

			// EXTERNALINFO
			// useres _are_ allowed to set the external info on a new object.
			// subsequent operations, however, will not be able to edit this
			// value.
			newDetails.setExternalInfo( source.getExternalInfo() );

			// CREATION/UPDATEVENT : currently ignore what users do
			

		}

		return newDetails;

	}

	/**
	 * @see SecuritySystem#checkManagedDetails(IObject, Details)
	 */
	public Details checkManagedDetails(final IObject iobj,
			final Details previousDetails) 
	{
		checkReady("managedDetails");

		if (iobj == null)
			throw new ApiUsageException("Argument cannot be null.");
		
		if (iobj.getId() == null)
			throw new ValidationException(
					"Id required on all detached instances.");

		// Note: privileged check moved into the if statement below.

		// done first as validation.
		if (iobj instanceof IMutable) {
			Integer version = ((IMutable) iobj).getVersion();
			if (version == null || version.intValue() < 0)
				;
			// throw new ValidationException(
			// "Version must properly be set on managed objects :\n"+
			// obj.toString()
			// );
			// TODO
		}
		
		// check if the newDetails variable has been reset or if the instance
		// has been changed.
		boolean altered = false;

		final Details currentDetails = iobj.getDetails();
		/* not final! */Details newDetails = cd.createDetails();

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
			newDetails = new Details(previousDetails);
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

			if (previousDetails.getPermissions().isSet(Flag.LOCKED))
				locked = true;

			if (hasPrivilegedToken(iobj))
				privileged = true;

			// isGlobal implies nothing (currently) about external info
			// see mapping.vm for more.
			altered |= managedExternalInfo(locked, privileged, iobj,
					previousDetails, currentDetails, newDetails);
			
			if (!isGlobal(iobj.getClass())) // implies that Permissions dosn't matter
			{
				altered |= managedPermissions(locked, privileged, iobj,
						previousDetails, currentDetails, newDetails);
			}
			
			if (!isGlobal(iobj.getClass())) // implies that owner doesn't matter
			{
				altered |= managedOwner(locked, privileged, iobj,
						previousDetails, currentDetails, newDetails);
			}

			if (!isGlobal(iobj.getClass())) // implies that group doesn't matter
			{
				altered |= managedGroup(locked, privileged, iobj,
						previousDetails, currentDetails, newDetails);
			}
			
			// the event check needs to be last, because we need to test
			// whether or not it is necessary to change the updateEvent 
			// (i.e. last modification)
			if (!isGlobal(iobj.getClass())) // implies that event doesn't matter
			{
				altered |= managedEvent(locked, privileged, iobj,
						previousDetails, currentDetails, newDetails);
			}

		}

		return altered ? newDetails : previousDetails;

	}

	/**
	 * responsible for guaranteeing that external info is not modified by 
	 * any users, including rot.
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

		ExternalInfo current = currentDetails == null ? null 
				: currentDetails.getExternalInfo();
		
		if (previous == null)
		{
			// do we allow a change?
			newDetails.setExternalInfo( current );
			altered |= 
				newDetails.getExternalInfo() != currentDetails.getExternalInfo();
		} 
		
		// The ExternalInfo was previously set. We do not allow it to be changed,
		// similar to not allowing the Event for an entity to be changed.
		else
		{
			if (!HibernateUtils.idEqual(previous, current))
			{
				throw new SecurityViolation(String.format(
						"Cannot update ExternalInfo for %s from %s to %s",
						obj,previous,current));
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
			if (!currentP.identical(previousP)) 
			{
				if ( ! isOwnerOrSupervisor(obj))
					throw new SecurityViolation(String.format(
							"You are not authorized to change "
									+ "the permissions for %s from %s to %s", obj,
							previousP, currentP));
			
				altered = true;
			}
		}

		// now we've calculated the desired permissions, throw
		// a security violation if this instance was locked AND
		// the read permissions have been lowered or if the lock
		// was removed.
		if (locked) {

			if (previousP == null) // if null it can't have been locked.
				throw new InternalException("Null permissions cannot be locked");

			Permissions calculatedP = newDetails.getPermissions();

			if (calculatedP != null) {

				// can't override
				if ( ! calculatedP.isSet( Flag.LOCKED ))
				{
					calculatedP.set(Flag.LOCKED);
					altered = true;
				}
				
				if ((previousP.isGranted(USER, READ) && !calculatedP.isGranted(USER,
						READ))
						|| (previousP.isGranted(GROUP, READ) && !calculatedP
								.isGranted(GROUP, READ))
						|| (previousP.isGranted(WORLD, READ) && !calculatedP
								.isGranted(WORLD, READ)))
					throw new SecurityViolation(
							"Cannot remove READ from locked entity:" + obj);
			}
		}

		// privileged plays no role since everyone can alter their permissions
		// (within bounds)

		return altered;

	}

	protected boolean managedOwner(boolean locked, boolean privileged,
			IObject obj, Details previousDetails, Details currentDetails,
			Details newDetails) {

		if (!HibernateUtils.idEqual(previousDetails.getOwner(), 
				currentDetails.getOwner())) {

			// !idEquals implies that they aren't both null; if current_owner is
			// null, then it was *probably* not intended, so just fix it and 
			// move on. this goes for root and admins as well.
			if ( currentDetails.getOwner() == null ) {
				newDetails.setOwner(previousDetails.getOwner());
				return true;
			}
			
			// Locked items cannot have their owner altered, unless they
			// are world-readable. In that case, the owner will play no 
			// real role. The WORLD-READ is also not removable. The check for 
			// this is in managedPermissions()
			if (locked && 
					! currentDetails.getPermissions().isGranted( WORLD, READ )) 
			{	
				throw new SecurityViolation("Object locked! " +
						"Cannot change owner for:" + obj);
			}

			// if the current user is an admin or if the entity has been
			// marked privileged, then use the current owner.
			else if (currentUserIsAdmin() || privileged) {
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
		if (!HibernateUtils.idEqual(previousDetails.getGroup(), 
				currentDetails.getGroup())) {

			// !idEquals implies that they aren't both null; if current_group is
			// null, then it was *probably* not intended, so just fix it and 
			// move on. this goes for root and admins as well.
			if ( currentDetails.getGroup() == null ) {
				newDetails.setGroup(previousDetails.getGroup());
				return true;
			}
			
			if (locked) {
				throw new SecurityViolation("Object locked! " +
						"Cannot change group for entity:" + obj);
			}

			// if user is a member of the group or the current user is an admin
			// or if the entity has been marked as privileged, then use the
			// current group.
			else if (memberOfGroups().contains(
					currentDetails.getGroup().getId())
					|| currentUserIsAdmin() || privileged) {
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
			if ( currentDetails.getCreationEvent() == null ) 
			{
				newDetails.setCreationEvent( previousDetails.getCreationEvent() );
				altered = true;
			} 
			// otherwise throw an exception, because as seen in ticket:346, 
			// it can lead to confusion otherwise. See:
			// https://trac.openmicroscopy.org.uk/omero/ticket/346
			else 
			{
			
				// no one change them.
				throw new SecurityViolation(String.format(
						"You are not authorized to change "
								+ "the creation event for %s from %s to %s", obj,
						previousDetails.getCreationEvent(), currentDetails
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
			if ( currentDetails.getUpdateEvent() == null ) 
			{
				newDetails.setUpdateEvent( previousDetails.getUpdateEvent() );
				altered = true;
			} 
			// otherwise throw an exception, because as seen in ticket:346, 
			// it can lead to confusion otherwise. See:
			// https://trac.openmicroscopy.org.uk/omero/ticket/346
			else 
			{
			
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

	// ~ CurrentDetails delegation (ensures proper settings of Tokens)
	// =========================================================================

	public void loadEventContext(boolean isReadyOnly) {

		// needed services
		LocalAdmin localAdmin = (LocalAdmin) sf.getAdminService();
		ITypes iTypes = sf.getTypesService();
		IUpdate iUpdate = sf.getUpdateService();

		final Principal p = clearAndCheckPrincipal();

		// start refilling current details
		cd.setReadOnly(isReadyOnly);
		
		// Experimenter

		final Experimenter exp = localAdmin.userProxy(p.getName());
		exp.getGraphHolder().setToken(token, token);
		cd.setOwner(exp);

		// Member of Groups
		List<Long> memberOfGroupsIds = localAdmin.getMemberOfGroupIds(exp);
		cd.setMemberOfGroups(memberOfGroupsIds);

		// Leader of Groups
		List<Long> leaderOfGroupsIds = localAdmin.getLeaderOfGroupIds(exp);
		cd.setLeaderOfGroups(leaderOfGroupsIds);

		// Active group

		ExperimenterGroup grp = localAdmin.groupProxy(p.getGroup());
		grp.getGraphHolder().setToken(token, token);
		cd.setGroup(grp);

		// isAdmin

		if (roles.isSystemGroup(grp)) {
			cd.setAdmin(true);
		}

		// Event

		EventType type = iTypes.getEnumeration(EventType.class, p
				.getEventType());
		type.getGraphHolder().setToken(token, token);
		cd.newEvent(type, token);

		Event event = getCurrentEvent();
		event.getGraphHolder().setToken(token, token);
		
		// If this event is not read only, then lets save this event to prevent
		// flushing issues later.
		if ( ! isReadyOnly )
		{
			setCurrentEvent(iUpdate.saveAndReturnObject(event));
		}

	}
	
	/**
	 * @see SecuritySystem#setEventContext(EventContext)
	 */
	public void setEventContext(EventContext context) 
	{
		final Principal p = clearAndCheckPrincipal();
		throw new UnsupportedOperationException("not implemented.");
	}

	private Principal clearAndCheckPrincipal() {
		// clear even if this fails. (make SecuritySystem unusable)
		cd.clear();
		
		final Principal p = principalHolder.get();
		
		if (p == null)
			throw new SecurityViolation(
					"Principal is null. Not logged in to SecuritySystem.");

		if (p.getName() == null)
			throw new InternalException(
					"Principal.name is null. Security system failure.");

		if (p.getGroup() == null)
			throw new InternalException(
					"Principal.group is null in EventContext. Security system failure.");
		
		if (p.getEventType() == null)
			throw new InternalException(
					"Principal.eventType is null in EventContext. Security system failure.");
		return p;
	}
	
	// TODO should possible set all or nothing.
	
	public Details createDetails() 
	{
		return cd.createDetails();
	}
	
	public void newEvent(EventType type) {
		cd.newEvent(type, token);
	}
	
	public void setCurrentEvent(Event event) {
		cd.setCreationEvent(event);
	}

	public void addLog(String action, Class klass, Long id) 
	{

		Assert.notNull(action);
		Assert.notNull(klass);
		Assert.notNull(id);

		if (Event.class.isAssignableFrom(klass)
				|| EventLog.class.isAssignableFrom(klass)) {
			log.debug("Not logging creation of logging type:" + klass);
		}

		else {
			checkReady("addLog");

			log.info("Adding log:" + action + "," + klass + "," + id);

//			CurrentDetails.getCreationEvent().addEventLog(l);
			cd.addLog( action, klass, id );
		}
	}
	
	public List<EventLog> getLogs( )
	{
		return cd.getLogs();
	}

	public void clearEventContext() {
		cd.clear();
	}

	// read-only ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * @see SecuritySystem#isEmptyEventContext()
	 */
	public boolean isEmptyEventContext() {
		EventContext ctx = cd.getCurrentEventContext();
		// These are the only values which can be null checked in
		// EventContext. Others (like leaderOfGroups) are never null.
		return ctx.getCurrentEventId() == null &&
			ctx.getCurrentGroupId() == null &&
			ctx.getCurrentUserId() == null;
	}

	public Long currentUserId() {
		checkReady("currentUserId");
		return cd.getOwner().getId();
	}

	public Long currentGroupId() {
		checkReady("currentGroupId");
		return cd.getGroup().getId();
	}

	public Collection<Long> leaderOfGroups() {
		checkReady("leaderOfGroups");
		return cd.getLeaderOfGroups();
	}

	public Collection<Long> memberOfGroups() {
		checkReady("memberOfGroups");
		return cd.getMemberOfGroups();
	}

	public Experimenter currentUser() {
		checkReady("currentUser");
		return cd.getOwner();
	}

	public ExperimenterGroup currentGroup() {
		checkReady("currentGroup");
		return cd.getGroup();
	}

	public Event currentEvent() {
		checkReady("currentEvent");
		return cd.getCreationEvent();
	}

	public Event getCurrentEvent() {
		checkReady("getCurrentEvent");
		return cd.getCreationEvent();
	}

	public boolean currentUserIsAdmin() {
		checkReady("currentUserIsAdmin");
		return cd.isAdmin();
	}

	// ~ Tokens & Actions
	// =========================================================================

	/**
	 * 
	 * It would be better to catch the
	 * {@link SecureAction#updateObject(IObject)} method in a try/finally block,
	 * but since flush can be so poorly controlled that's not possible. instead,
	 * we use the one time token which is removed this Object is checked for
	 * {@link #hasPrivilegedToken(IObject) privileges}.
	 * 
	 * @param obj A managed (non-detached) entity. Not null.
	 * @param action A code-block that will be given the entity argument with
	 * 		a {@link #hasPrivilegedToken(IObject)} privileged token}. 
	 */
	public <T extends IObject> T doAction(T obj, SecureAction action) {
		Assert.notNull(obj);
		Assert.notNull(action);
		
		// TODO inject
		if (obj.getId() != null && 
				!((LocalQuery)sf.getQueryService()).contains(obj))
			throw new SecurityViolation("Services are not allowed to call " +
					"doAction() on non-Session-managed entities.");

		// FIXME
		// Token oneTimeToken = new Token();
		// oneTimeTokens.put(oneTimeToken);
		obj.getGraphHolder().setToken(token, token);// oneTimeToken);

		T retVal;
		try {
			retVal = action.updateObject(obj);
		} finally {
			obj.getGraphHolder().setToken(token, null);
		}
		return retVal;
	}

	/** merge event is disabled for {@link #runAsAdmin(AdminAction)} because
	 * passing detached (client-side) entities to this method is particularly
	 * dangerous. 
	 */
	public void runAsAdmin(AdminAction action) {
		Assert.notNull(action);
		disable(MergeEventListener.MERGE_EVENT);
		cd.setAdmin(true);
		try {
			action.runAsAdmin();
		} finally {
			cd.setAdmin(false);
			enable(MergeEventListener.MERGE_EVENT);
		}
	}

	/**
	 * copy a token from one {@link IObject} to another. This is currently
	 * insecure and should take a third token implying the rights to copy.
	 * Should only be called by {@link MergeEventListener}
	 */
	public void copyToken(IObject source, IObject copy) {

		if (source == null || copy == null || source == copy)
			return;

		GraphHolder gh1 = source.getGraphHolder();
		GraphHolder gh2 = copy.getGraphHolder();

		// try our token first
		if (gh1.tokenMatches(token)) {
			gh2.setToken(token, token);
		}

	}

	public boolean hasPrivilegedToken(IObject obj) {
		
		if (obj == null) return false;
		
		GraphHolder gh = obj.getGraphHolder();

		// most objects will not have a token
		if (gh.hasToken()) {
			// check if truly secure.
			if (gh.tokenMatches(token))
				return true;
		}
		return false;
	}
	
	// ~ Configured Elements
	// =========================================================================

	public Roles getSecurityRoles() {
		return roles;
	}

	public EventContext getEventContext() {
		return cd.getCurrentEventContext();
	}
	
	public ACLVoter getACLVoter() {
		return acl;
	}
	
	// ~ Helpers
	// =========================================================================

	/**
	 * calls {@link #isReady()} and if not throws an {@link ApiUsageException}.
	 * The {@link SecuritySystem} must be in a valid state to perform several
	 * functions.
	 */
	protected void checkReady(String method) {
		if (!isReady()) {
			throw new ApiUsageException("The security system is not ready.\n"
					+ "Cannot execute: " + method);
		}

	}

	// ~ Details checks. Used by to examine transient and managed Details.
	// =========================================================================

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
	 * transient details should be have the umask applied to them if soft.
	 */
	void applyUmaskIfNecessary(Details d) {
		Permissions p = d.getPermissions();
		if (p.isSet(Flag.SOFT)) {
			if (principalHolder.get().hasUmask()) {
				p.grantAll(principalHolder.get().getUmask());
				p.revokeAll(principalHolder.get().getUmask());
			}
			// don't store it in the DB.
			p.unSet(Flag.SOFT);
		}
	}

}
