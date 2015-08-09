/*
 * ome.model.IEnum
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.model;

/**
 * interface for all domain enumarations. Mostly a marker interface, but does
 * provide access to the single value associated with this instance (See
 * {@link #getValue()})
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 * @author josh
 * 
 */
public interface IEnum extends IObject {

    /**
     * the single String value associated with this instance. This value is a
     * surrogate (business) key for this type of enumeration. Equality between
     * two instances for {@link #getValue() value} implies that there will also
     * be equality for {@link IObject#getId() id}, at least in an controlled
     * environment.
     */
    public String getValue();

}
