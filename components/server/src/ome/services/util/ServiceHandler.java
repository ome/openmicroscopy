/*
 * ome.services.util.ServiceHandler 
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

package ome.services.util;

//Java imports
import java.lang.reflect.Method;
import java.util.Arrays;

//Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;

//import com.caucho.burlap.io.BurlapOutput;

//Application-internal dependencies
import ome.annotations.ApiConstraintChecker;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.OptimisticLockException;
import ome.conditions.RootException;

/** 
 *   
 */
public class ServiceHandler implements MethodInterceptor {

    private static Log log = LogFactory.getLog(ServiceHandler.class);

    private boolean printXML = false;

    public void setPrintXML(boolean value){
        this.printXML = value;
    }

    
    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable
    {
        if ( arg0 == null )
            throw new InternalException(
                    "Cannot act on null MethodInvocation. Stopping."
                    );
        
        Class implClass = arg0.getThis().getClass();
        Method mthd = arg0.getMethod();
        Object[] args = arg0.getArguments();
        
        ApiConstraintChecker.errorOnViolation( implClass, mthd, args );

        if ( log.isInfoEnabled() )
        {
            // Method
            log.info("Meth:\t"+arg0.getMethod().getName());
        
            // Arguments
            String arguments;
            if (arg0.getArguments() == null || arg0.getArguments().length < 1) 
                arguments = "()";
            else
                arguments = Arrays.asList(arg0.getArguments()).toString(); 
            log.info("Args:\t"+arguments);
        }

        
        // Results and/or Exceptions
        Object o;
        String finalOutput = "";
        
        try {
            o = arg0.proceed();
            finalOutput = "Rslt:\t"+o;

            // Extended output and return.
            log(o);
            return o;
            
        } catch (Throwable t) {
            finalOutput = "Excp:\t"+t; 
            throw getAndLogException(t);
        } finally {
            if ( log.isInfoEnabled() )
                log.info( finalOutput );
        }
        
    }
    
    protected void log(Object o) throws Throwable{
        if (printXML){
//            OutputStream os = new ByteArrayOutputStream();
//            BurlapOutput out = new BurlapOutput(os);
//            out.writeObject(o);
//            byte[] b = ((ByteArrayOutputStream)os).toByteArray();
//            os.close();
//            log.info(new String(b));
              log.warn("PrintXML is disabled");
        }
    }
    
    protected Throwable getAndLogException(Throwable t){
        if (null == t)
        {
            log.error("Exception thrown. (null)");
            return new InternalException("Exception thrown with null message");
        } 
        else {
            String msg = " Wrapped Exception: ("
                + t.getClass().getName()+"):\n"
                + t.getMessage();
            
            log.error("Exception thrown: "+msg);
            
            if ( RootException.class
                    .isAssignableFrom( t.getClass() ) )
                return t;

            else if ( OptimisticLockingFailureException.class
                    .isAssignableFrom( t.getClass() ))
            {
                OptimisticLockException ole = new OptimisticLockException( t.getMessage() );
                ole.setStackTrace( t.getStackTrace() );
                return ole;
            }
            
            else if ( IllegalArgumentException.class
                    .isAssignableFrom( t.getClass() ))
            {
                ApiUsageException aue = new ApiUsageException( t.getMessage() );
                aue.setStackTrace( t.getStackTrace() );
                log.warn("IllegalArgumentException thrown:\n"+aue.getStackTrace());
                return aue;
            }
            
            else if ( InvalidDataAccessResourceUsageException.class
                    .isAssignableFrom( t.getClass() ) )
            {
                ApiUsageException aue = new ApiUsageException( t.getMessage() );
                aue.setStackTrace( t.getStackTrace() );
                log.warn("InvalidDataAccessResourceUsageException thrown:\n"+aue.getStackTrace());
                return aue;
            }
            
            else 
            {
                // Wrap all other exceptions in InternalException
                InternalException re = new InternalException(msg);
                re.setStackTrace(t.getStackTrace());
                return re;
            }
           
            
        }
            
    }

}
