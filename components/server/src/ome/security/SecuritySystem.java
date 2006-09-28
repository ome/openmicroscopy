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
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Token;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;


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
	 * <em>not</em> make any queries and is only a conduit for login information
	 * from the outer-most levels. Session bean
	 * implementations and other in-JVM clients can fill the
	 * {@link Principal}. Note, however, a call must first be made to 
	 * {@link #loadEventContext(boolean)} or {@link #setEventContext(EventContext)}
	 * for some calls to be made to the {@link SecuritySystem}. In general, this
	 * means that execution must pass through the
	 * {@link ome.security.basic.EventHandler} 
	 */
	void login( Principal principal );
	
	/** clears any {@link Principal} instances from the current thread context.
	 */
	void logout( );
	
	void loadEventContext(boolean isReadyOnly);
	void setEventContext(EventContext context);
	void clearEventContext();

	boolean isEmptyEventContext( );

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
	
	/**
	 * checks that the {@link IObject} argument has been granted a {@link Token}
	 * by the {@link SecuritySystem}.
	 */
	boolean hasPrivilegedToken(IObject obj);

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
	
	// ~ Details checking (prime responsibility)
	// =========================================================================
	
	/**
	 * creates a new secure {@link IObject#getDetails() details} for transient
	 * entities. Non-privileged users can only edit the
	 * {@link Details#getPermissions() Permissions} field. Privileged users can
	 * use the {@link Details} object as a single-step <code>chmod</code> and
	 * <code>chgrp</code>.
	 * 
	 * {@link #newTransientDetails(IObject) newTransientDetails} always returns a
	 * non-null Details that is not equivalent (==) to the Details argument.
	 * 
	 * This method can be used from anywhere in the codebase to obtain a 
	 * valid {@link Details}, but passing in an {@link IObject} instance with
	 * a null {@link Details}. However, if the {@link Details} is non-null,
	 * there is the possibility that this method will throw an exception.
	 * 
	 * @throws ApiUsageException if {@link SecuritySystem} is not 
	 * 		{@link #isReady() ready}
	 * @throws SecurityViolation if {@link Details} instance contains 
	 * 		illegal values.
	 */
	Details newTransientDetails( IObject iObject )
	throws ApiUsageException, SecurityViolation;
	
	/**
	 * checks that a non-privileged user has not attempted to edit the entity's
	 * {@link IObject#getDetails() security details}. Privileged users can set
	 * fields on {@link Details} as a single-step <code>chmod</code> and
	 * <code>chgrp</code>.
	 * 
	 * {@link #checkManagedDetails(IObject, Details) managedDetails} may create
	 * a new Details instance and return that if needed. If the returned Details
	 * is not equivalent (==) to the argument Details, then values have been
	 * changed.
	 * 
	 * @param iObject non-null {@link IObject} instance. {@link Details} for 
	 * 		that instance can be null.
	 * @param trustedDetails possibly null {@link Details} instance. These
	 * 		{@link Details} are trusted in the sense that they have already
	 * 		once passed through the {@link SecuritySystem}.
	 * @throws ApiUsageException if {@link SecuritySystem} is not 
	 * 		{@link #isReady() ready}
	 * @throws SecurityViolation if {@link Details} instance contains 
	 * 		illegal values.
	 */
	Details checkManagedDetails( IObject iObject, Details trustedDetails )
	throws ApiUsageException, SecurityViolation;
	
	// ~ Actions
	// =========================================================================
	void runAsAdmin( AdminAction action );
	<T extends IObject> T doAction( T obj, SecureAction action );
	// TODO do these need checks to isReady()?
	
	// ~ Configured Elements
	// =========================================================================
	Roles getSecurityRoles();
	EventContext getEventContext();
	ACLVoter getACLVoter();
}
