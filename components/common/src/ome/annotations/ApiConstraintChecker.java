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
import java.util.Arrays;
import java.util.Collection;

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
        
        if (log.isDebugEnabled())
        {
        	log.debug("Checking: " + mthd);
        }
        
        /* get arrays of arguments with parameters */
        if ( args == null )
            args = new Object[]{};

        boolean[] validated = new boolean[args.length];
        
        Object[] allAnnotations = AnnotationUtils
        	.findParameterAnnotations(implClass, mthd);
        
        for (int j = 0; j < allAnnotations.length; j++)
        {
            Annotation[][] anns = (Annotation[][]) allAnnotations[j];            
            if (anns == null) continue;
            
            for (int i = 0; i < args.length; i++)
            {
                Object arg = args[i];
                Annotation[] annotations = anns[i];
    
                validated[i] |= false;
    
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
                        validated[i] = true;
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
            }
            
            for (int i = 0; i < validated.length; i++) {
                /* warn if someone's forgotten to annotate a method */
                if (args[i] instanceof Collection && !validated[i])
                    throw new ValidationException(mthd
                            + " is missing a required @" + Validate.class.getName()
                            + " annotation. This should be added to one of the " 
                            + " implemented interfaces. Refusing to proceed...");

			}
        }

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
