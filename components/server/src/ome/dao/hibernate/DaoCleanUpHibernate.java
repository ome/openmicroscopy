/*
 * ome.logic.DaoCleanUpHibernate
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
package ome.dao.hibernate;

//Java imports
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

//Application-internal dependencies
import ome.api.OMEModel;
import ome.dao.DaoUtils;

/** 
 * method interceptor to clean objects of all Hibernate references, 
 * specifically lazy-loading proxies
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class DaoCleanUpHibernate implements MethodInterceptor {

    DaoUtils daoUtils;

    public DaoCleanUpHibernate(DaoUtils daoUtils) {
        this.daoUtils = daoUtils;
    }
    
    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable {
        return clean(arg0.proceed());
    }
    
    public Object clean(Object obj) {
        //TODO push OMEModel down into all calls
        if (null != obj) {
            if (obj instanceof OMEModel) {
                daoUtils.clean((OMEModel) obj);
            } else if (obj instanceof Set) {
                daoUtils.clean((Set) obj);
            } else if (obj instanceof Map) {
                //daoUtils.clean(((Map) obj).keySet());TODO here only integers, but...
                daoUtils.clean(new HashSet(((Map) obj).values()));                
            } else {
                String msg = "Instances of " + obj.getClass().getName()
                + " not supported.";
                throw new IllegalArgumentException(msg);
            }
        }
        return obj;
    }

}
