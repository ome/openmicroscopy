/*
 * ome.tools.hibernate.ProxySafeFilter
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
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
 * extension to {@link ome.util.ContextFilter} to check for uninitialized Hibernate
 * proxies before stepping into an entity or collection.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class ProxySafeFilter extends ContextFilter
{

    @Override
    public Filterable filter(String fieldId, Filterable f)
    {
        if ( ! Hibernate.isInitialized( f ))
        {
            return f;
        }
        return super.filter(fieldId, f);
    }

    @Override
    public Collection filter(String fieldId, Collection c)
    {
        if ( ! Hibernate.isInitialized(c))
        {
            return c;
        }
        return super.filter(fieldId, c);
    }
    
}
