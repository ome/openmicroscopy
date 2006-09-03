/*
 * ome.annotations.AnnotationUtils
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

// Third-party libraries

// Application-internal dependencies
import ome.conditions.InternalException;

/**
 * Checks metadata constraints on API calls.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class AnnotationUtils
{

    /** finds all parameter {@link Annotation annotations} for the given class
     * including on all implemented interfaces.
     */
    @SuppressWarnings("unchecked")
    public static Object[] findMethodAnnotations(
            Class implClass, Method mthd) 
    throws InternalException
    {
        
        Class[] interfaces = implClass.getInterfaces();
        Object[] annotations = new Object[interfaces.length+1];
        
        for (int i = 0; i < interfaces.length; i++)
        {
            Method m = findMethod(interfaces[i],mthd);
            annotations[i] = m == null ? null : m.getDeclaredAnnotations();
        }
        annotations[interfaces.length] = mthd.getDeclaredAnnotations();
        
        return annotations;
        
    }

    /** finds all parameter {@link Annotation annotations} for the given class
     * including on all implemented interfaces.
     */
    @SuppressWarnings("unchecked")
    public static Object[] findParameterAnnotations(
            Class implClass, Method mthd) 
    throws InternalException
    {
        
        Class[] interfaces = implClass.getInterfaces();
        Object[] annotations = new Object[interfaces.length+1];
        
        for (int i = 0; i < interfaces.length; i++)
        {
            Method m = findMethod(interfaces[i],mthd);
            annotations[i] = m == null ? null : m.getParameterAnnotations();
        }
        annotations[interfaces.length] = mthd.getParameterAnnotations();
        
        return annotations;
        
    }

    
    /** finds methods on interfaces based on the {@link Class} and 
     * {@link Method#getName() method name}.
     */
    @SuppressWarnings("unchecked")
    private static Method findMethod(
            Class implClass, Method mthd) 
    throws InternalException
    {

        // Get the method.
        Method implMethod;
        try
        {
            implMethod = implClass.getMethod(mthd.getName(), 
            		mthd.getParameterTypes());
            
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
