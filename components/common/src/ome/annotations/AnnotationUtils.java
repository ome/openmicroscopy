/*
 * ome.annotations.AnnotationUtils
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import ome.conditions.InternalException;

/**
 * Checks metadata constraints on API calls.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * @since 1.0
 */
public class AnnotationUtils {

    /**
     * finds all parameter {@link Annotation annotations} for the given class
     * including on all implemented interfaces.
     */
    @SuppressWarnings("unchecked")
    public static Object[] findMethodAnnotations(Class implClass, Method mthd)
            throws InternalException {

        Class[] interfaces = implClass.getInterfaces();
        Object[] annotations = new Object[interfaces.length + 1];

        for (int i = 0; i < interfaces.length; i++) {
            Method m = findMethod(interfaces[i], mthd);
            annotations[i] = m == null ? null : m.getDeclaredAnnotations();
        }
        annotations[interfaces.length] = mthd.getDeclaredAnnotations();

        return annotations;

    }

    /**
     * finds all parameter {@link Annotation annotations} for the given class
     * including on all implemented interfaces.
     */
    @SuppressWarnings("unchecked")
    public static Object[] findParameterAnnotations(Class implClass, Method mthd)
            throws InternalException {

        Class[] interfaces = implClass.getInterfaces();
        Object[] annotations = new Object[interfaces.length + 1];

        for (int i = 0; i < interfaces.length; i++) {
            Method m = findMethod(interfaces[i], mthd);
            annotations[i] = m == null ? null : m.getParameterAnnotations();
        }
        annotations[interfaces.length] = mthd.getParameterAnnotations();

        return annotations;

    }

    /**
     * finds methods on interfaces based on the {@link Class} and
     * {@link Method#getName() method name}.
     */
    @SuppressWarnings("unchecked")
    private static Method findMethod(Class implClass, Method mthd)
            throws InternalException {

        // Get the method.
        Method implMethod;
        try {
            implMethod = implClass.getMethod(mthd.getName(), mthd
                    .getParameterTypes());

        } catch (SecurityException e) {
            throw new InternalException(
                    "Not allowed to perform reflection for testing API.\n"
                            + String.format("Class:%s Method:%s", implClass
                                    .getName(), mthd));
        } catch (NoSuchMethodException e) {
            implMethod = null; // TODO No method == no violation.
        }
        return implMethod;
    }
}
