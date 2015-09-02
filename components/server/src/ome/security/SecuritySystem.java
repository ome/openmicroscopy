/*
 * ome.security.SecuritySystem
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Token;
import ome.model.meta.ExperimenterGroup;
import ome.security.policy.Policy;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;

/**
 * central security interface. All queries and actions that deal with a secure
 * context should pass through an implementation of this interface.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see Token
 * @see Details
 * @see Permissions
 * @see ACLEventListener
 * @since 3.0-M3
 */
public interface SecuritySystem {

    // ~ Login/logout
    // =========================================================================

    /**
     * stores this {@link Principal} instance in the current thread context for
     * authenticating and authorizing all actions. This method does <em>not</em>
     * make any queries and is only a conduit for login information from the
     * outer-most levels. Session bean implementations and other in-JVM clients
     * can fill the {@link Principal}. Note, however, a call must first be made
     * to {@link #loadEventContext(boolean)} or
     * {@link #setEventContext(EventContext)} for some calls to be made to the
     * {@link SecuritySystem}. In general, this means that execution must pass
     * through the {@link ome.security.basic.EventHandler}
     */
    void login(Principal principal);

    /**
     * clears the top {@link Principal} instance from the current thread
     * context.
     * 
     * @return the number of remaining instances.
     */
    int logout();

    /**
     * Calls {@link #getEventContext(boolean)} with a false as "refresh".
     * This is the previous, safer logic of the method since consumers
     * are not expecting a long-method run.
     * 
     * @return
     */
    EventContext getEventContext();

    /**
     * Returns UID based on whether a share is active, etc. This is the UID
     * value that should be used for writing data.
     *
     * The return value <em>may be</em> null if the user is currently querying
     * across multiple contents. In this case another method for
     * choosing the UID must be chosen, for example by taking the UID of
     * another element under consideration.
     *
     * For example,
     * <pre>
     * Annotation toSave = ...;
     * if (toSave.getDetails().getOwner() == null) // No owner need to find one.
     * {
     *     Long uid = sec.getEffectiveUID();
     *     if (uid != null)
     *     {
     *         toSave.getDetails().setOwner(new Experimenter(uid, false));
     *     }
     *     else
     *     {
     *         toSave.getDetails().setOwner(
     *            image.getDetails().getOwner()); // may be null.
     *     }
     * }
     * image.linkAnnotation(toSave);
     * etc.
     * <pre>
     */
    Long getEffectiveUID();

    /**
     * If refresh is false, returns the current {@link EventContext} stored
     * in the session. Otherwise, reloads the context to have the most
     * up-to-date information.
     *
     * @see ticket:4011
     * @return
     */
    EventContext getEventContext(boolean refresh);

    /**
     * Prepares the current {@link EventContext} instance with the current
     * {@link Principal}. An exception is thrown if there is none.
     * 
     * @param isReadOnly
     */
    void loadEventContext(boolean isReadOnly);

    /**
     * Clears the content of the {@link EventContext}so that the
     * {@link SecuritySystem} will no longer return true for {@link #isReady()}.
     * The {@link Principal} set during {@link #login(Principal)} is retained.
     */
    void invalidateEventContext();

    // ~ Checks
    // =========================================================================
    /**
     * checks if this {@link SecuritySystem} instance is in a valid state. This
     * includes that a user is properly logged in and that a connection is
     * available to all necessary resources, e.g. database handle and mapping
     * session.
     * 
     * Not all methods require that the instance is ready.
     * 
     * @return true if all methods on this interface are ready to be called.
     */
    boolean isReady();

    /**
     * checks if instances of the given type are "System-Types". Security logic
     * for all system types is significantly different. In general, system types
     * cannot be created, updated, or deleted by regular users, and are visible
     * to all users.
     * 
     * @param klass
     *            A class which extends from {@link IObject}
     * @return true if instances of the class argument can be considered system
     *         types.
     */
    boolean isSystemType(Class<? extends IObject> klass);

    /**
     * checks that the {@link IObject} argument has been granted a {@link Token}
     * by the {@link SecuritySystem}.
     */
    boolean hasPrivilegedToken(IObject obj);

    /**
     * Checks whether or not a {@link ome.sercurity.Policy} instance of matching
     * name has been registered, considers itself active, <em>and</em>
     * considers the passed context object to be restricted.
     * 
     * @param name A non-null unique name for a class of policies.
     * @param obj An instance which is to be checked against matching policies.
     * @throws a {@link SecurityViolation} if the given {@link Policy} is
     *      considered to be restricted.
     */
    void checkRestriction(String name, IObject obj) throws SecurityViolation;

    // ~ Subsystem disabling
    // =========================================================================

    /**
     * disables components of the backend for the current Thread. Further checks
     * to {@link #isDisabled(String)} will return false. It is the
     * responsibility of various security system components to then throw
     * exceptions.
     * 
     * @param ids
     *            Non-null, non-empty array of String ids to disable.
     */
    void disable(String... ids);

    /**
     * enables components of the backend for the current Thread. Further checks
     * to {@link #isDisabled(String)} will return true.
     * 
     * @param ids
     *            possibly null array of String ids. A null array specifies that
     *            all subsystems are to be enabled. Otherwise, only those
     *            subsystems specified by the ids.
     */
    void enable(String... ids);

    /**
     * checks if the listed id is disabled for the current Thread.
     * 
     * @param id
     *            non-null String representing a backend subsystem.
     * @return true if the backend subsystem has been previously disabled by
     *         calls to {@link #disable(String[])}
     */
    boolean isDisabled(String id);

    // ~ Details checking (prime responsibility)
    // =========================================================================

    /**
     * Determines if the current security context has the possibility of
     * corrupting consistent graphs. Consistent graphs are enforced by the
     * security context to make sure that all READ actions work smoothly. If an
     * administrator or PI is logged into a private group, or otherwise may
     * create an object linked to an object with lower READ rights, then
     * corruption could occur.
     *
     * Starting with 4.4.2, a trusted details object should be passed in order
     * to handle the situation where the current group id is -1. Possibles
     * cases that can occur:
     *
     * <pre>
     *  The current group is non-negative, then use the previous logic;
     *  else the current group is negative,
     *     and the object is in a non-"user" group: USE THAT GROUP;
     *     else the object is in the "user" group: UNCLEAR
     *     (for the moment we're throwing an exception)
     * </pre>
     *
     * If no {@link Details} instance is passed or a {@link Details} without
     * a {@link ExperimenterGroup} value, then throw as well.
     *
     * @see <a
     *      href="http://trac.openmicroscopy.org.uk/ome/ticket/1434>1434</a>
     * @see <a
     *      href="http://trac.openmicroscopy.org.uk/ome/ticket/1769>1769</a>
     * @see <a
     *      href="http://trac.openmicroscopy.org.uk/ome/ticket/9474>9474</a>
     * @return
     */
    boolean isGraphCritical(Details details);

    /**
     * creates a new secure {@link IObject#getDetails() details} for transient
     * entities. Non-privileged users can only edit the
     * {@link Details#getPermissions() Permissions} field. Privileged users can
     * use the {@link Details} object as a single-step <code>chmod</code> and
     * <code>chgrp</code>.
     * 
     * {@link #newTransientDetails(IObject) newTransientDetails} always returns
     * a non-null Details that is not equivalent (==) to the Details argument.
     * 
     * This method can be used from anywhere in the codebase to obtain a valid
     * {@link Details}, but passing in an {@link IObject} instance with a null
     * {@link Details}. However, if the {@link Details} is non-null, there is
     * the possibility that this method will throw an exception.
     * 
     * @throws ApiUsageException
     *             if {@link SecuritySystem} is not {@link #isReady() ready}
     * @throws SecurityViolation
     *             if {@link Details} instance contains illegal values.
     */
    Details newTransientDetails(IObject iObject) throws ApiUsageException,
            SecurityViolation;

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
     * @param iObject
     *            non-null {@link IObject} instance. {@link Details} for that
     *            instance can be null.
     * @param trustedDetails
     *            possibly null {@link Details} instance. These {@link Details}
     *            are trusted in the sense that they have already once passed
     *            through the {@link SecuritySystem}.
     * @throws ApiUsageException
     *             if {@link SecuritySystem} is not {@link #isReady() ready}
     * @throws SecurityViolation
     *             if {@link Details} instance contains illegal values.
     */
    Details checkManagedDetails(IObject iObject, Details trustedDetails)
            throws ApiUsageException, SecurityViolation;

    // ~ Actions
    // =========================================================================
    /**
     * Allows actions to be performed with the
     * {@link EventContext#isCurrentUserAdmin()} flag enabled but
     * <em>without</em> changing the value of
     * {@link EventContext#getCurrentUserId()}, so that ownerships are properly
     * handled. The merging of detached entity graphs should be disabled for the
     * extent of the execution.
     * 
     * Due to the addition of the group permission system, we also permit
     * setting the group on the call so that the administrator can work within
     * all groups. A value of null will not change the current group.
     *
     * Note: the {@link ome.api.IUpdate} save methods should not be used, since
     * they also accept detached entities, which could pose security risks.
     * Instead load an entity from the database via {@link ome.api.IQuery},
     * make changes, and save the changes with {@link ome.api.IUpdate#flush()}.
     */
    void runAsAdmin(ExperimenterGroup group, AdminAction action);

    /**
     * Calls {@link #runAsAdmin(ExperimenterGroup, AdminAction)} with a
     * null group.
     */
    void runAsAdmin(AdminAction action);

    <T extends IObject> T doAction(SecureAction action, T... objs);

    // TODO do these need checks to isReady()?

    // ~ Configured Elements
    // =========================================================================
    Roles getSecurityRoles();

}
