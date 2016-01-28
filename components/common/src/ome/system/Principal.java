/*
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.io.Serializable;

import ome.model.enums.EventType;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;

/**
 * implementation of {@link java.security.Principal}. Specialized for Omero to
 * carry a {@link ExperimenterGroup group}, an {@link EventType event type} and
 * a {@link Permissions umask}.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see EventType
 * @see ExperimenterGroup
 * @see Permissions
 * @since 3.0
 */
public class Principal implements java.security.Principal, Serializable {

    private static final long serialVersionUID = 3761954018296933086L;

    protected String name;

    protected String group;

    protected String type;

    private final PreferenceContext preferences = new PreferenceContext();

    /**
     * Creates a Principal with null group and event type. These must be taken
     * from the session.
     * 
     * @param name
     */
    public Principal(String name) {
        this(name, null, null);
    }

    public Principal(String name, String group, String eventType) {
        if (Boolean.parseBoolean(preferences
                .getProperty("omero.security.ignore_case"))) {
            name = name.toLowerCase();
        }
        this.name = name;
        this.group = group;
        this.type = eventType;
    }

    // IMMUTABLE

    public String getName() {
        return this.name;
    }

    public String getGroup() {
        return this.group;
    }

    public String getEventType() {
        return this.type;
    }

    /**
     * returns only the name of the instance because that is the expected
     * behavior of {@link java.security.Principal} implementations
     * 
     * @return value of {@link #name}
     */
    @Override
    public String toString() {
        return this.name;
    }

}
