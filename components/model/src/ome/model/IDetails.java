/*
 * ome.model.IDetails
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model;

import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

/**
 * simple interface for {@link ome.model.internal.Details}; currently unused.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * @since 3.0
 */
public interface IDetails {

    public Permissions getPermissions();

    public Experimenter getOwner();

    public Event getCreationEvent();

    public void setPermissions(Permissions perms);

    public void setOwner(Experimenter exp);

    public void setCreationEvent(Event e);

}
