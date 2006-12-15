/*
 * ome.tools.hibernate.ProxySafeFilter
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.hibernate;

// Java imports
import java.util.Collection;

// Third-party libraries
import org.hibernate.Hibernate;

// Application-internal dependencies
import ome.util.ContextFilter;
import ome.util.Filterable;

/**
 * extension to {@link ome.util.ContextFilter} to check for uninitialized
 * Hibernate proxies before stepping into an entity or collection.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class ProxySafeFilter extends ContextFilter {

    @Override
    protected void doFilter(String fieldId, Filterable f) {
        if (Hibernate.isInitialized(f)) {
            super.doFilter(fieldId, f);
        }

    }

    @Override
    protected void doFilter(String fieldId, Collection c) {
        if (Hibernate.isInitialized(c)) {
            super.doFilter(fieldId, c);
        }

    }

}
