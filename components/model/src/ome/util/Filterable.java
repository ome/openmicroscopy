/*
 * ome.util.Filterable
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

/**
 * marks objects which can be filtered using {see FIXME }.
 * 
 * altered visitor pattern. model only calls visit on each of its fields. the
 * rest of the navigation is done in visitor.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * @since 1.0
 */
public interface Filterable {

    public boolean acceptFilter(Filter filter);
    // public Filterable newInstance();

}
