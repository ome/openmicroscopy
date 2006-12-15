/*
 * ome.api.local.LocalAdmin
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;

// Java imports
import java.util.List;

import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

// Third-party libraries

// Application-internal dependencies

/**
 * Provides local (internal) extensions for administration
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$ $Date$)
 *          </small>
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
     * Finds the ids for all groups for which the given {@link Experimenter} is
     * owner/leader.
     * 
     * @param e
     *            Non-null, managed (i.e. with id) {@link Experimenter}
     * @see ExperimenterGroup#getDetails()
     * @see Details#getOwner()
     */
    List<Long> getLeaderOfGroupIds(Experimenter e);

    /**
     * Finds the ids for all groups for which the given {@link Experimenter} is
     * a member.
     * 
     * @param e
     *            Non-null, managed (i.e. with id) {@link Experimenter}
     * @see ExperimenterGroup#getDetails()
     * @see Details#getOwner()
     */
    List<Long> getMemberOfGroupIds(Experimenter e);
}
