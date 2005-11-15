/*
 * ome.api.ReturnLogger
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
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

//Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.caucho.burlap.io.BurlapOutput;

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
 */
public class ReturnLogger implements MethodInterceptor {

	private static Log log = LogFactory.getLog(ReturnLogger.class);
	private boolean printXML = false;

	public void setPrintXML(boolean value){
		this.printXML = value;
	}
	
	/**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable {
        log.info("Meth:\t"+arg0.getMethod().getName());
        log.info("Args:\t"+Arrays.asList(arg0.getArguments()));
        Object o;
        try {
            o = arg0.proceed();
            log.info("Rslt:\t"+o);
        } catch (Throwable t) {
            log.debug("Excp:\t"+t);
            throw t;
        }
        log(o);
        return o;
    }
    
    public void log(Object o) throws Throwable{
    	if (printXML){
    		OutputStream os = new ByteArrayOutputStream();
    		BurlapOutput out = new BurlapOutput(os);
    		out.writeObject(o);
    		byte[] b = ((ByteArrayOutputStream)os).toByteArray();
    		os.close();
    		log.info(new String(b));
    	}
    }
    
}
