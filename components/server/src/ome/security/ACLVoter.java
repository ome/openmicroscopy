/*
 * ome.security.ACLVoter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

// Java imports

// Third-party libraries

// Application-internal dependencies
import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.system.EventContext;

import org.hibernate.Session;

/**
 * helper security interface for all decisions on access control
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see SecuritySystem
 * @see ACLEventListener
 * @since 3.0-M3
 */
public interface ACLVoter {

    /**
     * test whether the given object can have its
     * {@link Details#getPermissions() Permissions} changed within the current
     * {@link EventContext security context}.
     * @param iObject a model object
     * @return if the object's permissions may be changed
     */
    boolean allowChmod(IObject iObject);

    /**
     * test whether the object of the given {@link Class} with the given
     * {@link Details} should be loadable in the current security context.
     * 
     * This method does not take an actual object because that will not be
     * generated until after loading is permitted.
     * 
     * The {@link SecuritySystem} implementors will usually call
     * {@link #throwLoadViolation(IObject)} if this method returns false.

     * @param session the Hibernate session to use for the query
     * @param klass
     *            a non-null class to test for loading
     * @param trustedDetails
     *            the non-null trusted details (usually from the db) for this
     *            instance
     * @param id
     *            the id of the object which will be loaded. As opposed to the
     *            rest of the object, this must be known.
     * @return true if loading of this object can proceed
     * @see ACLEventListener#onPostLoad(org.hibernate.event.PostLoadEvent)
     */
    boolean allowLoad(Session session,
    		Class<? extends IObject> klass, Details trustedDetails,
            long id);

    /**
     * test whether the given object should be insertable into the DB.
     * 
     * No trusted {@link Details details} is passed to this method, since for
     * transient entities there are no trusted values.
     * 
     * The {@link SecuritySystem} implementors will usually call
     * {@link #throwCreationViolation(IObject)} if this method returns false.
     * 
     * @param iObject
     *            a non-null entity to test for creation.
     * @return true if creation of this object can proceed
     * @see ACLEventListener#onPreInsert(org.hibernate.event.PreInsertEvent)
     */
    boolean allowCreation(IObject iObject);

    /**
     * test whether the given object should be annotatable given the trusted
     * {@link Details details}. The details will usually be retrieved from the
     * current state array coming from the database.
     *
     * @param iObject
     *            a non-null entity to test for update.
     * @param trustedDetails
     *            a {@link Details} instance that is known to be valid.
     * @return true if annotation of this object can proceed
     */
    boolean allowAnnotate(IObject iObject, Details trustedDetails);

    /**
     * test whether the given object should be updateable given the trusted
     * {@link Details details}. The details will usually be retrieved from the
     * current state array coming from the database.
     * 
     * The {@link SecuritySystem} implementors will usually call
     * {@link #throwUpdateViolation(IObject)} if this method returns false.
     * 
     * @param iObject
     *            a non-null entity to test for update.
     * @param trustedDetails
     *            a {@link Details} instance that is known to be valid.
     * @return true if update of this object can proceed
     * @see ACLEventListener#onPreUpdate(org.hibernate.event.PreUpdateEvent)
     */
    boolean allowUpdate(IObject iObject, Details trustedDetails);

    /**
     * test whether the given object should be deleteable given the trusted
     * {@link Details details}. The details will usually be retrieved from the
     * current state array coming from the database.
     * 
     * The {@link SecuritySystem} implementors will usually call
     * {@link #throwDeleteViolation(IObject)} if this method returns false.
     * 
     * @param iObject
     *            a non-null entity to test for deletion.
     * @param trustedDetails
     *            a {@link Details} instance that is known to be valid.
     * @return true if deletion of this object can proceed
     * @see ACLEventListener#onPreDelete(org.hibernate.event.PreDeleteEvent)
     */
    boolean allowDelete(IObject iObject, Details trustedDetails);

    /**
     * throws a {@link SecurityViolation} based on the given {@link IObject} and
     * the context of the current user.
     * 
     * @param iObject
     *            Non-null object which caused this violation
     * @throws SecurityViolation
     * @see ACLEventListener#onPostLoad(org.hibernate.event.PostLoadEvent)
     */
    void throwLoadViolation(IObject iObject) throws SecurityViolation;

    /**
     * throws a {@link SecurityViolation} based on the given {@link IObject} and
     * the context of the current user.
     * 
     * @param iObject
     *            Non-null object which caused this violation
     * @throws SecurityViolation
     * @see ACLEventListener#onPreInsert(org.hibernate.event.PreInsertEvent)
     */
    void throwCreationViolation(IObject iObject) throws SecurityViolation;

    /**
     * throws a {@link SecurityViolation} based on the given {@link IObject} and
     * the context of the current user.
     * 
     * @param iObject
     *            Non-null object which caused this violation
     * @throws SecurityViolation
     * @see ACLEventListener#onPreUpdate(org.hibernate.event.PreUpdateEvent)
     */
    void throwUpdateViolation(IObject iObject) throws SecurityViolation;

    /**
     * throws a {@link SecurityViolation} based on the given {@link IObject} and
     * the context of the current user.
     * 
     * @param iObject
     *            Non-null object which caused this violation
     * @throws SecurityViolation
     * @see ACLEventListener#onPreDelete(org.hibernate.event.PreDeleteEvent)
     */
    void throwDeleteViolation(IObject iObject) throws SecurityViolation;

    /**
     * Provide the active restrictions for this {@link IObject}.
     *
     * See {@link ome.security.policy.PolicyService} for further details.
     * @param object a model object
     * @return the restrictions applying for the object
     */
    Set<String> restrictions(IObject object);

    /**
     * Gives the {@link ACLVoter} instance a chance to act on the {@link IObject}
     * <em>after</em> the transaction but before finishing the AOP stack.
     * @param obj a model object
     */
    void postProcess(IObject obj);
}
