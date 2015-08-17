/*
 * ome.util.Filter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.util.Collection;
import java.util.Map;

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
public interface Filter {

    public Filterable filter(String fieldId, Filterable f);

    public Collection filter(String fieldId, Collection c);

    public Map filter(String fieldId, Map m);

    public Object filter(String fieldId, Object o);

}
