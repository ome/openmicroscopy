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
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import org.openmicroscopy.omero.OMEModel;

import com.caucho.burlap.io.BurlapOutput;

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
        Object o = arg0.proceed();
        log.info("Meth:\t"+arg0.getMethod().getName());
        log.info("Args:\t"+arg0.getArguments());
        log.info("Rslt:\t"+o);
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
