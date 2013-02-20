/*
 * ome.api.local.LocalAdmin
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;

// Java imports
import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;

// Third-party libraries

// Application-internal dependencies

/**
 * Provides local (internal) extensions for administration
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OMERO3.0
 */
public interface LocalAdmin extends ome.api.IAdmin {

    /**
     * returns a possibly uninitialized proxy for the given
     * {@link Experimenter#getOmeName() user name}. Use of the
     * {@link Experimenter} instance will initial its values.
     */
    Experimenter userProxy(String omeName);

    /**
     * returns a possibly uninitialized proxy for the given
     * {@link Experimenter#getId() user id}. Use of the {@link Experimenter}
     * instance will initial its values.
     */
    Experimenter userProxy(Long userId);

    /**
     * returns a possibly uninitialized proxy for the given
     * {@link ExperimenterGroup#getId() group id}. Use of the
     * {@link Experimenter} instance will initial its values.
     */
    ExperimenterGroup groupProxy(Long groupId);

    /**
     * returns a possibly uninitialized proxy for the given
     * {@link ExperimenterGroup#getName() group name}. Use of the
     * {@link Experimenter} instance will initial its values.
     */
    ExperimenterGroup groupProxy(String groupName);

    /**
     * Finds the group names for all groups for which the given {@link Experimenter} is
     * a member.
     * 
     * @param e
     *            Non-null, managed (i.e. with id) {@link Experimenter}
     * @see ExperimenterGroup#getDetails()
     * @see Details#getOwner()
     */
    List<String> getUserRoles(Experimenter e);
    
    /**
     * Checks password for given user. ReadOnly determines if a actions can be
     * taken to create the given user, for example in the case of LDAP.
     *
     * @see ticket:4626
     */
    boolean checkPassword(String user, String password, boolean readOnly);

    /**
     * Returns a map from {@link Class} (as string) to a count for all entities
     * which point to the given {@link IObject}. The String "*" is mapped to
     * the sum of all the locks.
     * 
     * TODO This will eventually return a list of ids
     */
    Map<String, Long> getLockingIds(Class<IObject> klass, long id, Long groupId);

    /**
     * Like {@link #getEventContext()} but will not reload the context.
     * This also has the result that values from the current call context
     * will be applied as simply the session context.
     */
    EventContext getEventContextQuiet();

    /**
     * Companion to {@link IAdmin#canUpdate(IObject)} but not yet remotely
     * accessible.
     * @param obj Not null.
     */
    boolean canAnnotate(IObject obj);

    /**
     * Unconditionally move an object into the user group (usually id=1).
     * Here, it will be readable from any group context.
     */
    void internalMoveToCommonSpace(IObject obj);
}
