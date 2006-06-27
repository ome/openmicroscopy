/*
 * ome.annotations.ApiConstraintChecker
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
package ome.annotations;

// Java imports
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.ValidationException;

/**
 * Checks metadata constraints on API calls.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class ApiConstraintChecker
{

    private static Log log = LogFactory.getLog(ApiConstraintChecker.class);

    public static void errorOnViolation(Class implClass, Method mthd, Object[] args) 
    throws ValidationException
    {

        if ( implClass == null || mthd == null )
        { 
            throw new ApiUsageException(
                    "ApiConstraintChecker expects non null class and method " +
                    "arguments.");
        }
        
        log.info("Checking: " + mthd);
        
        /* get arrays of arguments with parameters */
        if ( args == null )
            args = new Object[]{};

        Object[] allAnnotations = findAnnotations(implClass, mthd);
        for (int j = 0; j < allAnnotations.length; j++)
        {
            Annotation[][] anns = (Annotation[][]) allAnnotations[j];            
            if (anns == null) continue;
            
            for (int i = 0; i < args.length; i++)
            {
                Object arg = args[i];
                Annotation[] annotations = anns[i];
    
                boolean validated = false;
    
                for (Annotation annotation : annotations)
                {
                    if (NotNull.class.equals(annotation.annotationType()))
                    {
                        if (null == arg)
                        {
                            String msg = "Argument " + i + " to " + mthd
                                    + " may not be null.";
                            log.warn(msg);
                            throw new ApiUsageException(msg);
    
                        }
                    }
    
                    else if (Validate.class.equals(annotation.annotationType()))
                    {
                        validated = true;
                        Validate validator = (Validate) annotation;
                        Class[] validClasses = validator.value();
                        ValidSet validSet = new ValidSet( validClasses );
    
                        String msg = "Argument " + i + " must be of a type in:"
                                + validSet;
    
                        if (null == arg) {
                            // handled by NotNull
                        }
                        
                        else if (arg instanceof Collection)
                        {
                            Collection coll = (Collection) arg;
                            for (Object object : coll)
                            {
                                if ( ! validSet.isValid( object.getClass() ))
                                    throw new ApiUsageException(msg);
                            }
    
                        }
    
                        else
                        {
                            if ( ! validSet.isValid( arg.getClass() ))
                            {
                                throw new ApiUsageException(msg);
                            }
                        }
                    }
                }
    
                /* warn if someone's forgotten to annotate a method */
                if (arg instanceof Collection && !validated)
                    throw new ValidationException(mthd
                            + " is missing a required @" + Validate.class.getName()
                            + " annotation. This should be added to one of the " 
                            + " implemented interfaces. Refusing to proceed...");
    
            }
        }

    }

    /** finds the concrete method to use for checking. Because of the inverted 
     * AOP done on EJBs, the actual implClass used may need to be the EJB 
     * delegate. 
     */
    @SuppressWarnings("unchecked")
    private static Object[] findAnnotations(
            Class implClass, Method mthd) 
    throws InternalException
    {
        
        Class[] interfaces = implClass.getInterfaces();
        Object[] annotations = new Object[interfaces.length];
        
        for (int i = 0; i < annotations.length; i++)
        {
            Method m = findMethod(interfaces[i],mthd);
            annotations[i] = m == null ? null : m.getParameterAnnotations();
        }
        
        return annotations;
        
    }

    /* find concrete method */
    @SuppressWarnings("unchecked")
    private static Method findMethod(
            Class implClass, Method mthd) 
    throws InternalException
    {

        // Get the method.
        Method implMethod;
        try
        {
            implMethod = implClass.getMethod(mthd.getName(), (Class[]) mthd
                    .getParameterTypes());
        } catch (SecurityException e)
        {
            throw new InternalException(
                    "Not allowed to perform reflection for testing API.\n" +
                    String.format("Class:%s Method:%s",implClass.getName(),mthd));
        } catch (NoSuchMethodException e)
        {
            implMethod = null; // TODO No method == no violation. 
        }
        return implMethod;
    }
}

class ValidSet {
 
    Class[] classes;
    
    public ValidSet( Class[] validClasses )
    {
        classes = validClasses;
    }
    
    public boolean isValid( Class target )
    {
        if ( classes == null )
            return false;
        
        for (int i = 0; i < classes.length; i++)
        {
            Class klass = classes[i];
            if ( klass == null)
                continue;
            
            if ( klass.isAssignableFrom( target ))
                return true;
        }
        return false;
    }
    
    public String toString()
    {
        return Arrays.asList(classes).toString();
    }
}
