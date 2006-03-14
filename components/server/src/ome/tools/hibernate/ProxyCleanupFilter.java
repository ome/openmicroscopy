/*
 * ome.tools.hibernate.ProxyCleanupFilter
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
import java.util.IdentityHashMap;
import java.util.Map;

// Third-party libraries
import ome.model.IObject;
import ome.model.internal.Details;
import ome.util.ContextFilter;
import ome.util.Filterable;
import ome.util.Utils;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Hibernate;

// Application-internal dependencies

/**
 * removes all proxies from a return graph to prevent ClassCastExceptions and
 * Session Closed exceptions. You need to be careful with printing. Calling
 * toString() on an unitialized object will break before filtering is complete.
 * 
 * Note: we aren't setting the filtered collections here because it's 
 * "either null/unloaded or filtered". We will definitiely filter here, so
 * it would just increase bandwidth.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class ProxyCleanupFilter extends ContextFilter
{
    
    protected Map unloadedObjectCache = new IdentityHashMap();

    @Override
    public Filterable filter(String fieldId, Filterable f)
    {
        if ( f == null ) return null;

        if ( unloadedObjectCache.containsKey( f ))
            return (IObject) unloadedObjectCache.get( f );
        
        // A proxy; send over the wire in altered form.
        if ( ! Hibernate.isInitialized(f) )
        {
            
            if (f instanceof IObject)
            {
                IObject proxy = (IObject) f;
                IObject unloaded = (IObject) Utils.trueInstance(f.getClass());
                unloaded.setId(proxy.getId()); // TODO is this causing a DB hit?
                unloaded.unload();
                unloadedObjectCache.put( f, unloaded );
                return unloaded;
            } else if ( f instanceof Details) {
                // Currently Details is only "known" non-IObject Filterable
                return super.filter(fieldId,new Details((Details)f));
            } else {
                // TODO Here there's not much we can do. copy constructor?
                throw new RuntimeException(
                        "Bailing out. Don't want to set to a value to null.");
            }
            
        // Not a proxy; it will be serialized and sent over the wire.
        } else {
            
            // Any clean up here.
            return super.filter(fieldId, f);
            
        }

    }

    @Override
    public Collection filter(String fieldId, Collection c)
    {
        if (null == c || !Hibernate.isInitialized(c))
        {
            return null;
        }
        return super.filter(fieldId, c);
    }

    /** wraps a filter for each invocation */
    public static class Interceptor implements MethodInterceptor
    {
        
        public Object invoke(MethodInvocation arg0) throws Throwable
        {
            return new ProxyCleanupFilter().filter(null, arg0.proceed());
        }
    }
    
}
