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

//Third-party libraries

//Application-internal dependencies
import java.util.Collection;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Token;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/** 
 * central security interface. All queries and actions that deal with a secure
 * context should pass through an implementation of this interface.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see 	Token
 * @see     Details
 * @see     Permissions
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public interface SecuritySystem
{

	// ~ Checks
	// =========================================================================
	/** checks if this {@link SecuritySystem} instance is in a valid state. This
	 * includes that a user is properly logged in and that a connection is 
	 * available to all necessary resources, e.g. database handle and mapping
	 * session.
	 * 
	 * Not all methods require that the instance is ready. 
	 * 
	 * @param true if all methods on this interface are ready to be called.
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
	 * {@lnk {@link #allowLoad(Class, Details)} for more.
	 * 
	 * Note: this filter must be disabled on logout, otherwise the necessary
	 * parameters (current user, current group, etc.) for building the filters 
	 * will not be available. Similarly, while enabling this filter, no calls 
	 * should be made on the given session object.
	 * 
	 * @param session a generic session object which can be used to enable
	 *   this filter. Each {@link SecuritySystem} implementation will require
	 *   a specific session type.
	 */
	void enableReadFilter( Object session );
	
	/** disable this filer. All future queries will have no security context
	 * associated with them and all items will be visible. 
	 * 
	 * @param session a generic session object which can be used to disable
	 * 		this filter. Each {@link SecuritySystem} implementation will require
	 * 		a specifc session type.
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
	 */
	boolean allowDelete( IObject iObject, Details trustedDetails );

	void throwLoadViolation( IObject iObject ) throws SecurityViolation;
	void throwCreationViolation( IObject iObject ) throws SecurityViolation;
	void throwUpdateViolation( IObject iObject ) throws SecurityViolation;
	void throwDeleteViolation( IObject iObject ) throws SecurityViolation;
	
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
	<T extends IObject> T doAction( T obj, SecureAction action );
	void copyToken( IObject source, IObject copy );
}
