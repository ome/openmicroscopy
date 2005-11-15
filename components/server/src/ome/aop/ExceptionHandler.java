/*
 * ome.aop.ExceptionHandler
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
package ome.aop;

//Java imports

//Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.conditions.Policy;
import ome.conditions.RootException;

/** 
 * ExceptionHandler which maps all server-side exceptions to something 
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
	
    private final static String MESSAGE = "Internal server error";
    
    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable {
    	try {
    		Object o = arg0.proceed();
    		return o;
    	} catch (Throwable t) {
    	    throw getAndLogException(t);
        }
    }
    
    Throwable getAndLogException(Throwable t){
        if (null == t)
        {
            log.error("Exception thrown. (null)");
            return new RootException(MESSAGE+"(null)");
        } 
        else {
            String msg = " ("+t.getClass().getName()+"):"+t.getMessage();
            log.error("Exception thrown "+msg);
            
            if ( Policy.thrownByServer(t) ) return t;
            RootException re = new RootException(MESSAGE+msg);
            re.setStackTrace(t.getStackTrace());
            return re;
        }
            
    }


    
}
