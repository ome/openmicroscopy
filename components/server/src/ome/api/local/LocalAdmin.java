/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;

import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;

/**
 * Provides local (internal) extensions for administration
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0
 * @since OMERO3.0
 */
public interface LocalAdmin extends ome.api.IAdmin {

    /**
     * returns a possibly uninitialized proxy for the given
     * {@link Experimenter#getOmeName() user name}. Use of the
     * {@link Experimenter} instance will initialize its values.
     * @param omeName the name of a user
     * @return the user (may be uninitialized)
     */
    Experimenter userProxy(String omeName);

    /**
     * returns a possibly uninitialized proxy for the given
     * {@link Experimenter#getId() user id}. Use of the {@link Experimenter}
     * instance will initialize its values.
     * @param userId the ID of a user
     * @return the user (may be uninitialized)
     */
    Experimenter userProxy(Long userId);

    /**
     * returns a possibly uninitialized proxy for the given
     * {@link ExperimenterGroup#getId() group id}. Use of the
     * {@link Experimenter} instance will initialize its values.
     * @param groupId the ID of a group
     * @return the group (may be uninitialized)
     */
    ExperimenterGroup groupProxy(Long groupId);

    /**
     * returns a possibly uninitialized proxy for the given
     * {@link ExperimenterGroup#getName() group name}. Use of the
     * {@link Experimenter} instance will initialize its values.
     * @param groupName the name of a group
     * @return the group (may be uninitialized)
     */
    ExperimenterGroup groupProxy(String groupName);

    /**
     * Finds the group names for all groups for which the given {@link Experimenter} is
     * a member.
     * 
     * @param e
     *            Non-null, managed (i.e. with id) {@link Experimenter}
     * @return the groups of which the user is a member
     * @see ExperimenterGroup#getDetails()
     * @see Details#getOwner()
     */
    List<String> getUserRoles(Experimenter e);
    
    /**
     * Checks password for given user. ReadOnly determines if a actions can be
     * taken to create the given user, for example in the case of LDAP.
     * @param user the name of a user
     * @param password the user's password
     * @param readOnly if the password check should be transactionally read-only
     * @return if the user's password is correct
     * @see <a href="http://trac.openmicroscopy.org/ome/ticket/4626">Trac ticket #4626</a>
     */
    boolean checkPassword(String user, String password, boolean readOnly);

    //TODO The following method will eventually return a list of ids
    /**
     * Returns a map from {@link Class} (as string) to a count for all entities
     * which point to the given {@link IObject}. The String "*" is mapped to
     * the sum of all the locks.
     *
     * @param klass the name of a model class
     * @param id the ID of an instance of {@code klass}
     * @param groupId the ID of a group to omit from the results, may be {@code null}
     * @return the classes and counts of the objects that point to the given object
     */
    Map<String, Long> getLockingIds(Class<IObject> klass, long id, Long groupId);

    /**
     * Like {@link #getEventContext()} but will not reload the context.
     * This also has the result that values from the current call context
     * will be applied as simply the session context.
     * @return the current event context
     */
    EventContext getEventContextQuiet();

    /**
     * Companion to {@link ome.api.IAdmin#canUpdate(IObject)} but not yet remotely
     * accessible.
     * @param obj Not null.
     * @return if the object can be annotated
     */
    boolean canAnnotate(IObject obj);

    /**
     * Unconditionally move an object into the user group (usually id=1).
     * Here, it will be readable from any group context.
     */
    void internalMoveToCommonSpace(IObject obj);
}
