/*
 * ome.security.SecuritySystem
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

package ome.security;

//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Token;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Principal;
import ome.tools.hibernate.EventHandler;
import ome.tools.hibernate.FlushEntityEventListener;
import ome.tools.hibernate.MergeEventListener;
import ome.tools.hibernate.OmeroInterceptor;


/** 
 * central security interface. All queries and actions that deal with a secure
 * context should pass through an implementation of this interface.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see 	Token
 * @see     Details
 * @see     Permissions
 * @see		ACLEventListener
 * @see 	MergeEventListener
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public interface SecuritySystem
{

	// ~ Login/logout
	// =========================================================================
	
	/** stores this {@link Principal} instance in the current thread context 
	 * for authenticating and authorizing all actions. This method does 
	 * <em>not</em> make any queries.
	 */
	void login( Principal principal );
	
	/** clears any {@link Principal} instances from the current thread context.
	 */
	void logout( );
	
	// ~ Checks
	// =========================================================================
	/** checks if this {@link SecuritySystem} instance is in a valid state. This
	 * includes that a user is properly logged in and that a connection is 
	 * available to all necessary resources, e.g. database handle and mapping
	 * session.
	 * 
	 * Not all methods require that the instance is ready. 
	 * 
	 * @return true if all methods on this interface are ready to be called.
	 */
	boolean isReady( );
	
	/** checks if instances of the given type are "System-Types". Security 
	 * logic for all system types is significantly different. In general, 
	 * system types cannot be created, updated, or deleted by regular users, and
	 * are visible to all users.
	 * 
	 * @param klass A class which extends from {@link IObject}
	 * @return true if instances of the class argument can be considered 
	 * 		system types.
	 */
	boolean isSystemType( Class<? extends IObject> klass );	
	
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
	void enableReadFilter( Object session );
	
	/** disable this filer. All future queries will have no security context
	 * associated with them and all items will be visible. 
	 * 
	 * @param session a generic session object which can be used to disable
	 * 		this filter. Each {@link SecuritySystem} implementation will require
	 * 		a specifc session type.
	 * @see EventHandler#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	void disableReadFilter( Object session );
	
	// ~ Write security
	// =========================================================================
	/** test whether the object of the given {@link Class} with the given 
	 * {@link Details} should be loadable in the current security context. 
	 * 
	 * This method does not take an actual object because that will not be 
	 * generated until after loading is permitted. 
	 * 
	 * The {@link SecuritySystem} implementors will usually call 
	 * {@link #throwLoadViolation(IObject)} if this method returns false.
	 * 
	 * @param klass a non-null class to test for loading
	 * @param d the non-null trusted details (usually from the db) 
	 * 		for this instance
	 * @return true if loading of this object can proceed
	 * @see ACLEventListener#onPostLoad(org.hibernate.event.PostLoadEvent)
	 */
	boolean allowLoad( Class<? extends IObject> klass, Details trustedDetails );
	
	/** test whether the given object should be insertable into the DB. 
	 * 
	 * No trusted {@link Details details} is passed to this method, since
	 * for transient entities there are no trusted values.
	 * 
	 * The {@link SecuritySystem} implementors will usually call 
	 * {@link #throwCreationViolation(IObject)} if this method returns false.
	 * 
	 * @param iObject a non-null entity to test for creation.
	 * @return true if creation of this object can proceed
	 * @see ACLEventListener#onPreInsert(org.hibernate.event.PreInsertEvent)
	 */
	boolean allowCreation( IObject iObject );
	
	/** test whether the given object should be updateable given the 
	 * trusted {@link Details details}. The details will usually be retrieved
	 * from the current state array coming from the database.
	 * 
	 * The {@link SecuritySystem} implementors will usually call 
	 * {@link #throwUpdateViolation(IObject)} if this method returns false.
	 * 
	 * @param iObject a non-null entity to test for update.
	 * @param trustedDetails a {@link Details} instance that is known to be valid.
	 * @return true if update of this object can proceed
	 * @see ACLEventListener#onPreUpdate(org.hibernate.event.PreUpdateEvent)
	 */
	boolean allowUpdate( IObject iObject, Details trustedDetails );
	
	/** test whether the given object should be deleteable given the 
	 * trusted {@link Details details}. The details will usually be retrieved
	 * from the current state array coming from the database.
	 * 
	 * The {@link SecuritySystem} implementors will usually call 
	 * {@link #throwDeleteViolation(IObject)} if this method returns false.
	 * 
	 * @param iObject a non-null entity to test for deletion.
	 * @param trustedDetails a {@link Details} instance that is known to be valid.
	 * @return true if deletion of this object can proceed
	 * @see ACLEventListener#onPreDelete(org.hibernate.event.PreDeleteEvent)
	 */
	boolean allowDelete( IObject iObject, Details trustedDetails );

	/** throws a {@link SecurityViolation} based on the given {@link IObject}
	 * and the context of the current user.
	 * 
	 * @param iObject Non-null object which caused this violation
	 * @throws SecurityViolation
	 * @see {@link ACLEventListener#onPostLoad(org.hibernate.event.PostLoadEvent)}
	 */
	void throwLoadViolation( IObject iObject ) throws SecurityViolation;

	/** throws a {@link SecurityViolation} based on the given {@link IObject}
	 * and the context of the current user.
	 * 
	 * @param iObject Non-null object which caused this violation
	 * @throws SecurityViolation
	 * @see {@link ACLEventListener#onPreInsert(org.hibernate.event.PreInsertEvent)}
	 */
	void throwCreationViolation( IObject iObject ) throws SecurityViolation;
	
	/** throws a {@link SecurityViolation} based on the given {@link IObject}
	 * and the context of the current user.
	 * 
	 * @param iObject Non-null object which caused this violation
	 * @throws SecurityViolation
	 * @see ACLEventListener#onPreUpdate(org.hibernate.event.PreUpdateEvent)
	 */
	void throwUpdateViolation( IObject iObject ) throws SecurityViolation;
	
	/** throws a {@link SecurityViolation} based on the given {@link IObject}
	 * and the context of the current user.
	 * 
	 * @param iObject Non-null object which caused this violation
	 * @throws SecurityViolation
	 * @see ACLEventListener#onPreDelete(org.hibernate.event.PreDeleteEvent)
	 */
	void throwDeleteViolation( IObject iObject ) throws SecurityViolation;
	
	// ~ Subsystem disabling
	// =========================================================================
	
	/** disables components of the backend for the current Thread. Further checks
	 * to {@link #isDisabled(String)} will return false. It is the responsibility 
	 * of various security system components to then throw exceptions.
	 * 
	 * @param ids Non-null, non-empty array of String ids to disable.
	 */
	void disable( String... ids );
	
	/** enables components of the backend for the current Thread. Further checks
	 * to {@link #isDisabled(String)} will return true.
	 * 
	 * @param ids possibly null array of String ids. A null array specifies
	 *   that all subsystems are to be enabled. Otherwise, only those subsystems
	 *   specified by the ids.
	 */
	void enable( String... ids );
	
	/** checks if the listed id is disabled for the current Thread.
	 * 
	 * @param id non-null String representing a backend subsystem. 
	 * @return true if the backend subsystem has been previously disabled by 
	 *   calls to {@link #disable(String[])}
	 */
	boolean isDisabled( String id );
	
	// ~ Privileged accounts
	// =========================================================================
	long getRootId();
	long getSystemGroupId();
	long getUserGroupId();
	String getRootName();
	String getSystemGroupName();
	String getUserGroupName();
	boolean isSystemGroup(ExperimenterGroup g);
	
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
	 * @param trustedDetails {@link Details} for this entity which are known
	 * 		to be valid. Most likely represent database state. If null, then
	 * 		nothing is known about this new entity.
	 */
	void markLockedIfNecessary( IObject iObject, Details trustedDetails );
	
	/** sets the {@link Flag#LOCKED LOCKED flag} on the entities stored in the 
	 * context from the {@link #markLockedIfNecessary(IObject)} method. Called
	 * from {@link FlushEntityEventListener#onFlushEntity(org.hibernate.event.FlushEntityEvent)}
	 */
	void lockMarked( );
	
	Details transientDetails( IObject iObject );
	Details managedDetails( IObject iObject, Details previousDetails );
	
	// ~ CurrentDetails delegation
	// =========================================================================

	Long currentUserId();
	Long currentGroupId();
	Collection<Long> leaderOfGroups();
	
	Experimenter currentUser();
	ExperimenterGroup currentGroup();
	Event currentEvent();
	
	boolean emptyDetails( );
	void addLog( String action, Class klass, Long id );
	void newEvent( EventType type );
	Event getCurrentEvent();
	void setCurrentEvent( Event event );
	void clearCurrentDetails();
	void setCurrentDetails();
	boolean currentUserIsAdmin();
	
	// ~ Actions
	// =========================================================================
	void runAsAdmin( AdminAction action );
	<T extends IObject> T doAction( T obj, SecureAction action );
	void copyToken( IObject source, IObject copy );
}
