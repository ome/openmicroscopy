/*
 * ome.annotations.ApiConstraintChecker
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;

/**
 * Checks metadata constraints on API calls.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * @since 1.0
 */
public class ApiConstraintChecker {

    private static Logger log = LoggerFactory.getLogger(ApiConstraintChecker.class);

    public static void errorOnViolation(Class implClass, Method mthd,
            Object[] args) throws ValidationException {

        if (implClass == null || mthd == null) {
            throw new ApiUsageException(
                    "ApiConstraintChecker expects non null class and method "
                            + "arguments.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Checking: " + mthd);
        }

        /* get arrays of arguments with parameters */
        if (args == null) {
            args = new Object[] {};
        }

        Class<?>[] paramTypes = mthd.getParameterTypes();
        boolean[] validated = new boolean[args.length];

        Object[] allAnnotations = AnnotationUtils.findParameterAnnotations(
                implClass, mthd);

        for (int j = 0; j < allAnnotations.length; j++) {
            Annotation[][] anns = (Annotation[][]) allAnnotations[j];
            if (anns == null) {
                continue;
            }

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                Annotation[] annotations = anns[i];

                validated[i] |= false;

                for (Annotation annotation : annotations) {
                    if (NotNull.class.equals(annotation.annotationType())) {
                        if (null == arg) {
                            String msg = "Argument " + i + " to " + mthd
                                    + " may not be null.";
                            log.warn(msg);
                            throw new ApiUsageException(msg);

                        }
                    }

                    else if (Validate.class.equals(annotation.annotationType())) {
                        validated[i] = true;
                        Validate validator = (Validate) annotation;
                        Class[] validClasses = validator.value();
                        ValidSet validSet = new ValidSet(validClasses);

                        String msg = "Argument " + i + " must be of a type in:"
                                + validSet;

                        if (null == arg) {
                            // handled by NotNull
                        }

                        else if (arg instanceof Collection) {
                            Collection coll = (Collection) arg;
                            for (Object object : coll) {
                                if (object == null) {
                                    continue; // ticket:2513. Perhaps should throw AUE
                                }
                                if (!validSet.isValid(object.getClass())) {
                                    throw new ApiUsageException(msg);
                                }
                            }

                        }

                        else {
                            if (!validSet.isValid(arg.getClass())) {
                                throw new ApiUsageException(msg);
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < validated.length; i++) {
                /* warn if someone's forgotten to annotate a method */
                if (!paramTypes[i].equals(Object.class) &&
                        args[i] instanceof Collection && !validated[i]) {
                    throw new ValidationException(
                            mthd
                                    + " is missing a required @"
                                    + Validate.class.getName()
                                    + " annotation. This should be added to one of the "
                                    + " implemented interfaces. Refusing to proceed...");
                }

            }
        }

    }

}

class ValidSet {

    Class[] classes;

    public ValidSet(Class[] validClasses) {
        classes = validClasses;
    }

    public boolean isValid(Class target) {
        if (classes == null) {
            return false;
        }

        for (int i = 0; i < classes.length; i++) {
            Class klass = classes[i];
            if (klass == null) {
                continue;
            }

            if (klass.isAssignableFrom(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return Arrays.asList(classes).toString();
    }
}
