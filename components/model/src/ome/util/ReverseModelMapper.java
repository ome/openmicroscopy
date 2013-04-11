/*
 * ome.util.ReverseModelMapper
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

// Java imports
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

// Third-party libraries
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Application-internal dependencies
import ome.model.ModelBased;
import ome.model.IObject;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public interface ReverseModelMapper { // extends ContextFilter {

    public Filterable reverse(ModelBased source);
    public Collection reverse(Collection source);
    
}
