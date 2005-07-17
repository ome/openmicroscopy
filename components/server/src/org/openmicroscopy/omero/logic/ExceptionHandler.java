/*
 * org.openmicroscopy.omero.logic.DaoCleanUpHibernate
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
package org.openmicroscopy.omero.logic;

//Java imports

//Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.exception.NestableException;

import com.caucho.hessian.io.HessianProtocolException;

//Application-internal dependencies

/** 
 * method interceptor to log all result objects. 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 * @DEV.TODO should possibly move to common!org.ome.omero.aop
 */
public class ExceptionHandler implements MethodInterceptor {

	private static Log log = LogFactory.getLog(ExceptionHandler.class);
	
    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable {
    	try {
    		Object o = arg0.proceed();
    		return o;
    	} catch (Throwable t) {
    		if (filter_p(t)){
    			throw new RuntimeException("Internal server error.");
    		}
    		throw t;
    	}
    }

    protected boolean filter_p(Throwable t){
    	if (t == null) {
    		return true;
    	} else if (t instanceof HibernateException) {
			return true;
    	} else {
    		return false;
		}
    }
    
    protected Throwable makeException(String msg, Throwable t){
    	Throwable newT = new RuntimeException(msg);
    	Throwable cause = t.getCause();
    	if (cause!=null){
    		newT.initCause(makeException(t.getMessage(),t.getCause()));	
    	}
    	return newT;
    }
    
}
