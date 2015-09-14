/*
 * ome.util.ReverseModelMapper
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.util.Collection;

import ome.model.ModelBased;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 1.0
 */
public interface ReverseModelMapper { // extends ContextFilter {

    public Filterable reverse(ModelBased source);
    public Collection reverse(Collection source);
    
}
