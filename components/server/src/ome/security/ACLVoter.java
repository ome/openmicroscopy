/*
 * ome.security.ACLVoter
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
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;




/** 
 * helper security interface for all decisions on access control
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see 	SecuritySystem
 * @see		ACLEventListener
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public interface ACLVoter
{

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
	
}
