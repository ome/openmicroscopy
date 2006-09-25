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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

//Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.PropertyValueException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.hibernate3.HibernateSystemException;

//import com.caucho.burlap.io.BurlapOutput;

//Application-internal dependencies
import ome.annotations.AnnotationUtils;
import ome.annotations.ApiConstraintChecker;
import ome.annotations.Hidden;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.OptimisticLockException;
import ome.conditions.RootException;
import ome.conditions.ValidationException;

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
            // Method and arguments
            log.info("Meth:\t"+arg0.getMethod().getName());
            log.info("Args:\t"+getArgumentsString(arg0));
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
            
            if ( RootException.class
                    .isAssignableFrom( t.getClass() ) )
                return t;

            else if ( OptimisticLockingFailureException.class
                    .isAssignableFrom( t.getClass() ))
            {
                OptimisticLockException ole = new OptimisticLockException( t.getMessage() );
                ole.setStackTrace( t.getStackTrace() );
                printException("OptimisticLockingFailureException thrown.", t);
                return ole;
            }
            
            else if ( IllegalArgumentException.class
                    .isAssignableFrom( t.getClass() ))
            {
                ApiUsageException aue = new ApiUsageException( t.getMessage() );
                aue.setStackTrace( t.getStackTrace() );
                printException("IllegalArgumentException thrown.",t);
                return aue;
            }
            
            else if ( InvalidDataAccessResourceUsageException.class
                    .isAssignableFrom( t.getClass() ) )
            {
                ApiUsageException aue = new ApiUsageException( t.getMessage() );
                aue.setStackTrace( t.getStackTrace() );
                printException("InvalidDataAccessResourceUsageException thrown.",t);
                return aue;
            }
            
            else if ( DataIntegrityViolationException.class
            		.isAssignableFrom( t.getClass() ) )
            {
            	ValidationException ve = new ValidationException( t.getMessage() );
            	ve.setStackTrace( t.getStackTrace() );
            	printException("DataIntegrityViolationException thrown.",t);
            	return ve;
            }
            
            else if ( HibernateSystemException.class
            		.isAssignableFrom( t.getClass() ) )
            {
            	Throwable cause = t.getCause();
            	if ( cause == null || cause == t )
            	{
            		return wrapUnknown(t, msg);
            	} else if ( PropertyValueException.class
            			.isAssignableFrom( cause.getClass() ) )
            	{
            		ValidationException ve = new ValidationException( cause.getMessage() );
            		ve.setStackTrace( cause.getStackTrace() );
            		printException("PropertyValueException thrown.", cause);
            		return ve;
            	} else {
            		return wrapUnknown(t, msg);
            	}
            }
            
            else 
            {
                return wrapUnknown(t, msg);
            }
           
            
        }
            
    }


	private Throwable wrapUnknown(Throwable t, String msg) {
		// Wrap all other exceptions in InternalException
		InternalException re = new InternalException(msg);
		re.setStackTrace(t.getStackTrace());
		printException("Unknown exception thrown.", t);
		return re;
	}
    
    /** produces a String from the arguments array. Argument parameters
     * marked as {@link Hidden} will be replaced by "*******". 
     */
    private String getArgumentsString(MethodInvocation mi)
    {
        String arguments;
        Object[] args = mi.getArguments();
        
        if (args == null || args.length < 1)
        {
            return "()";
        }

        String[] prnt = new String[args.length];
        for (int i = 0; i < prnt.length; i++) {
			prnt[i] = args[i] == null ? "null" : args[i].toString();
		}
        
    	Object[] allAnnotations = 
    	AnnotationUtils.findParameterAnnotations(
    			mi.getThis().getClass(), mi.getMethod());
    	
        for (int j = 0; j < allAnnotations.length; j++)
        {
            Annotation[][] anns = (Annotation[][]) allAnnotations[j];            
            if (anns == null) continue;
            
            for (int i = 0; i < args.length; i++)
            {
                Annotation[] annotations = anns[i];
    
                for (Annotation annotation : annotations)
                {
                    if (Hidden.class.equals(annotation.annotationType()))
                    {
                    	prnt[i] = "********";
                    }
                }
            }
        }
    	
    	arguments = Arrays.asList(prnt).toString(); 
        return arguments;
    }

    private void printException(String msg, Throwable ex)
    {
    	if (log.isWarnEnabled())
    	{
    		StringWriter sw = null;
    		PrintWriter pw = null;
   
    		try {
    			sw = new StringWriter();
    			pw = new PrintWriter(sw);
    			ex.printStackTrace(pw);
    			pw.flush();
    			sw.flush();
    			log.warn(msg+"\n"+sw.toString());
    		} finally {
    			if ( pw != null )
    			{
    				pw.close();
    			}
    			if (sw != null)
    			{
    				try {
    					sw.close();
    				} catch (IOException ioe) {
    					// ignore.
    				}
    			}
    		}
    	}
    }
}

