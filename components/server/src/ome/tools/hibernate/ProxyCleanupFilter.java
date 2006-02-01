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

//Java imports
import java.util.Collection;


//Third-party libraries
import ome.util.ContextFilter;
import ome.util.Filterable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Hibernate;


//Application-internal dependencies



/** removes all proxies from a return graph to prevent ClassCastExceptions and Session Closed exceptions.
 *  you need to be careful with printing. calling toString() on an unitialized object will break before filtering
 *  is complete.
 *   
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 * 
 */
public class ProxyCleanupFilter extends ContextFilter implements MethodInterceptor {

	@Override
	public Filterable filter(String fieldId, Filterable f) {
	    if (null==f || !Hibernate.isInitialized(f)){
	    	return null;
	    }
	    return super.filter(fieldId, f);
	}
	
	@Override
	public Collection filter(String fieldId, Collection c) {
	    if (null==c || !Hibernate.isInitialized(c)){
	    	return null;
	    }
	    return super.filter(fieldId, c);
	}

	public Object invoke(MethodInvocation arg0) throws Throwable {
		return filter(null,arg0.proceed());
	}
	
}
