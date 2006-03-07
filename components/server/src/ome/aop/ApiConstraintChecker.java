/*
 * ome.api.ApiConstraintChecker
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

// Java imports
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// Third-party libraries
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.Validate;
import ome.conditions.RootException;

/**
 * method interceptor to check metadata constraints on API calls.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class ApiConstraintChecker implements MethodInterceptor
{

    private static Log log = LogFactory.getLog(ApiConstraintChecker.class);

    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation mi) throws Throwable
    {

        /* find concrete method */
        Method mthd = mi.getMethod();
        Class implClass = mi.getThis().getClass();
        Method implMethod = implClass.getMethod(mthd.getName(), (Class[]) mthd
                .getParameterTypes());

        log.info("Checking: " + implMethod);

        /* get arrays of arguments with parameters */
        Object[] args = mi.getArguments() == null ? new Object[]{} : mi.getArguments();
        Annotation[][] anns = implMethod.getParameterAnnotations();

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
                        String msg = "Argument " + i + " to " + implMethod
                                + " may not be null.";
                        log.warn(msg);
                        throw new IllegalArgumentException(msg);

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
                                throw new IllegalArgumentException(msg);
                        }

                    }

                    else
                    {
                        if ( ! validSet.isValid( arg.getClass() ))
                        {
                            throw new IllegalArgumentException(msg);
                        }
                    }
                }
            }

            /* warn if someone's forgotten to annotate a method */
            if (arg instanceof Collection && !validated)
                throw new RootException("Internal server error: " + implMethod
                        + " is missing a required @" + Validate.class.getName()
                        + " annotation. Refusing to proceed...");

        }

        return mi.proceed();
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
}
