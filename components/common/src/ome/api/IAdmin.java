/*
 * ome.api.IAdmin
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

package ome.api;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.annotations.Hidden;
import ome.annotations.NotNull;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;
import ome.system.Roles;

/**
 *  Administration interface providing access to admin-only functionality as 
 *  well as JMX-based server access and selected user functions. Most methods 
 *  require membership in privileged {@link ExperimenterGroup groups}.
 * 
 *  Methods which return {@link ome.model.meta.Experimenter} or 
 *  {@link ome.model.meta.ExperimenterGroup} instances fetch and load all 
 *  related instances of {@link ome.model.meta.ExperimenterGroup} or
 *  {@link ome.model.meta.Experimenter}, respectively.
 *  
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OME3.0
 */
public interface IAdmin extends ServiceInterface{
    
    // ~ Getting users and groups
    // =========================================================================
    
    /** fetch an {@link Experimenter} and all related 
     * {@link ExperimenterGroup groups}. 
     * @param id id of the Experimenter
     * @return an Experimenter. Never null.
     * @throws ome.conditions.ApiUsageException if id does not exist.
     */
    Experimenter getExperimenter( @NotNull Long id );

    /** look up an {@link Experimenter} and all related 
     * {@link ExperimenterGroup groups} by name.
     * @param omeName Name of the Experimenter 
     * @return an Experimenter. Never null.
     * @throws ome.conditions.ApiUsageException if omeName does not exist.
     */
    Experimenter lookupExperimenter( @NotNull String omeName );

    /** fetch an {@link ExperimenterGroup} and all contained
     * {@link Experimenter users}.
     * @param id id of the ExperimenterGroup 
     * @return an ExperimenterGroup. Never null.
     * @throws ome.conditions.ApiUsageException if id does not exist.
     */
    ExperimenterGroup getGroup( @NotNull Long id );
    
    /** look up an {@link ExperimenterGroup} and all contained 
     * {@link Experimenter users} by name.
     * @param groupName Name of the ExperimenterGroup 
     * @return an ExperimenterGroup. Never null.
     * @throws ome.conditions.ApiUsageException if groupName does not exist.
     */
    ExperimenterGroup lookupGroup( @NotNull String groupName );
    
    /** fetch all {@link Experimenter users} contained in this group.
     * 
     * @param groupId id of the ExperimenterGroup
     * @return non-null array of all {@link Experimenter users} in this group.
     */
    Experimenter[] containedExperimenters( @NotNull Long groupId );
    
    /** fetch all {@link ExperimenterGroup groups} of which the given user
     * is a member.
     * 
     * @param experimenterId id of the Experimenter. Not null.
     * @return non-null array of all {@link ExperimenterGroup groups} for
     *  this user.
     */
    ExperimenterGroup[] containedGroups( @NotNull Long experimenterId );
    
    /** retrieve the default {@link ExperimenterGroup group} for the given
     * user id.
     * 
     * @param experimenterId of the Experimenter. Not null.
     * @return non-null {@link ExperimenterGroup}. If no default group is found,
     * 		an exception will be thrown. 
     */
    ExperimenterGroup getDefaultGroup( @NotNull Long experimenterId );
    
    // ~ Creating users in groups
    // =========================================================================
    
    /** create and return a new user. This user will be created with the default
     * "User" group.
     * @param newUser a new {@link Experimenter} instance 
     * @return id of the newly created {@link Experimenter}
     */
    long createUser( @NotNull Experimenter newUser );
    
    /** create and return a new system user. This user will be created with the 
     * "System" (administration) group and will also be in the "user" group.
     * @param newUser a new {@link Experimenter} instance 
     * @return id of the newly created {@link Experimenter}
     */
    long createSystemUser( @NotNull Experimenter newSystemUser );

    /** create and return a new user in the given groups. 
     * @param experimenter. A new {@link Experimenter} instance. Not null.
     * @param defaultGroup. Instance of {@link ExperimenterGroup. Not null.
     * @param otherGroups. Array of {@link ExperimenterGroup} instances. Can be null.     
     * @return id of the newly created {@link Experimenter}
     *  Not null.
     */
    long createExperimenter( 
            @NotNull Experimenter experimenter, 
            @NotNull ExperimenterGroup defaultGroup,
            ExperimenterGroup[] otherGroups );
    
    /** create and return a new group. 
     * @param newGroup a new {@link ExperimenterGroup} instance. Not null. 
     * @return id of the newly created {@link ExperimenterGroup}
     */
    long createGroup( ExperimenterGroup group );

    /** adds a user to the given groups. 
     * @param user. A currently managed entity. Not null.
     * @param groups. Groups to which the user will be added. Not null. 
     */
    void addGroups( 
    		@NotNull Experimenter user, 
    		@NotNull ExperimenterGroup...groups );
    
    /** removes a user from the given groups. 
     * @param user. A currently managed entity. Not null.
     * @param groups. Groups from which the user will be removed. Not null. 
     */
    void removeGroups( 
    		@NotNull Experimenter user, 
    		@NotNull ExperimenterGroup...groups );
    
    /** sets the default group for a given user. 
     * @param user. A currently managed entity. Not null.
     * @param group. The group which should be set as default group for this 
     *  user. Not null. 
     */
    void setDefaultGroup( 
    		@NotNull Experimenter user, 
    		@NotNull ExperimenterGroup group );
    
    /** removes a user after removing the password information for that user. 
     * This prevents constraint violations for DB-based login modules. 
     * @param user. Experimenter to be deleted. Not null.
     */
    void deleteExperimenter( @NotNull Experimenter user );
    
    // ~ Permissions and Ownership
    // =========================================================================

    /** call 
     * {@link ome.model.internal.Details#setOwner(Experimenter) details.setOwner()}
     * on this instance. It is valid for the instance to be 
     * {@link IObject#unload() unloaded} (or constructed with an 
     * unloading-constructor.)
     * @param iObject. An entity or an unloaded reference to an entity. Not null.
     * @param omeName. The user name who should gain ownership of this entity. Not null.
     */
    void changeOwner( @NotNull IObject iObject, @NotNull String omeName );

    /** call 
     * {@link ome.model.internal.Details#setGroup(ExperimenterGroup) details.setGroup()}
     * on this instance. It is valid for the instance to be 
     * {@link IObject#unload() unloaded} (or constructed with an 
     * unloading-constructor.)
     * @param iObject. An entity or an unloaded reference to an entity. Not null.
     * @param groupName. The group name who should gain ownership of this entity. Not null.
     */
    void changeGroup( @NotNull IObject iObject, @NotNull String groupName );

    /** call 
     * {@link ome.model.internal.Details#setPermissions(Permissions) defaults.setPermissions()}
     * on this instance. It is valid for the instance to be 
     * {@link IObject#unload() unloaded} (or constructed with an 
     * unloading-constructor.)
     * @param iObject. An entity or an unloaded reference to an entity. Not null.
     * @param perms. The permissions value for this entity. Not null.
     */
    void changePermissions( @NotNull IObject iObject, @NotNull Permissions perms );
    
    /** checks an entity for any in-bound references and if none are present,
     * will remove the {@link Flag#LOCKED} status. This method is backend-
     * intensive and should not be used in a tight loop. Returns an array with 
     * length equal to the number of instances passed in. A true value means
     * that the object is now unlocked.
     * @param iObjects a variable array argument of objects to be unlocked
     * @return an array of equal length to iObjects where a true value 
     * 		asserts that the instance is now unlocked in the database. 
     */
    boolean[] unlock( IObject...iObjects );
    
    // ~ Authentication and Authorization
    // =========================================================================
    
    /** change the password for the current user
     * @param newPassword. Not-null. 
     * Must pass validation in the security sub-system.
     * @throws ome.conditions.SecurityViolation if the new password is too weak. 
     */
    void changePassword( @Hidden String newPassword );
    
    /** change the password for the a given user.
     * @param newPassword. Not-null. 
     * Might must pass validation in the security sub-system.
     * @throws ome.conditions.SecurityViolation if the new password is too weak. 
     */
    void changeUserPassword( @NotNull String omeName, @Hidden String newPassword );
    
    /** uses JMX to refresh the login cache <em>if supported</em>. Some backends
     * may not provide refreshing. This may be called internally during some
     * other administrative tasks. The exact implementation of this depends on
     * the application server and the authentication/authorization backend.
     */
    void synchronizeLoginCache();

    // ~ Security context
	// =========================================================================

    /** returns the active {@link Roles} in use by the server. 
     * @return Non-null, immutable {@link Roles} instance.
     */
    Roles getSecurityRoles();
    
    /** returns an implementation of {@link EventContext} loaded with 
     * the security for the current user and thread. If called remotely, 
     * not all values of {@link EventContext} will be sensible.
     * @return Non-null, immutable {@link EventContext} instance
     */
    EventContext getEventContext();
}
