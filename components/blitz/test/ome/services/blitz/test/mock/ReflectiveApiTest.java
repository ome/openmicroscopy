/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.services.blitz.test.mock.MockFixture;
import ome.system.Roles;
import omero.api.IScriptPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.ArgumentsMatchBuilder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Ice.ObjectPrxHelperBase;

/**
 * Reflectively calls all interface methods defined in omero/API.ice and checks
 * for exceptions. Generally called "fuzz-testing".
 */
public class ReflectiveApiTest extends MockObjectTestCase {

    MockFixture fixture;

    @AfterMethod(groups = "integration")
    public void shutdownFixture() {
        fixture.tearDown();
    }

    @Test(groups = "integration")
    public void testByReflection() throws Exception {

        fixture = new MockFixture(this);
        ServiceFactoryPrx sf = fixture.createServiceFactory();

        List<Method> factoryMethods = factoryMethods();
        for (Method factoryMethod : factoryMethods) {

            Object service = factoryMethod.invoke(sf);

            // Filtering the blitz-only services for now
            if (service instanceof IScriptPrx) {
                continue;
            }

            List<Method> serviceMethods = serviceMethods(service.getClass());
            for (Method method : serviceMethods) {
                callWithFuzz(service, method);
            }

        }

        System.out.println("boo");

    }

    // Helpers
    // =========================================================================

    /**
     * Returns all methods on the {@link ServiceFactoryPrx} which will return a
     * concrete service implementation.
     */
    List<Method> factoryMethods() {
        List<Method> rv = new ArrayList<Method>();
        Method[] methods = ServiceFactoryPrx.class.getMethods();
        for (Method method : methods) {
            String name = method.getName();

            // These requirement a string argument
            if (method.getParameterTypes().length > 0) {
                continue;
            }

            if (name.startsWith("get") || name.startsWith("create")) {
                rv.add(method);
            }
        }
        return rv;
    }

    /**
     * Returns all service methods which are intended for client use. Filters
     * async methods as well as private and Ice-only methods.
     */
    List<Method> serviceMethods(Class serviceClass) {
        Map<String, Method> rv = new HashMap<String, Method>();
        Method[] methods = serviceClass.getMethods();
        for (Method current : methods) {
            String name = current.getName();
            Class[] types = current.getParameterTypes();

            if (name.startsWith("_") || name.startsWith("ice_")
                    || name.endsWith("_async") || name.endsWith("checkedCast")) {
                continue;
            }

            try {
                // If method exists, then we don't want it.
                Object.class.getMethod(current.getName(), types);
                continue;
            } catch (Exception e) {
                // Good
            }

            try {
                // Same as for Object.class
                ObjectPrxHelperBase.class.getMethod(current.getName(), types);
                continue;
            } catch (Exception e) {
                // Good.
            }

            Method already = rv.get(name);
            if (already == null) {
                rv.put(name, current);
            } else {
                // Filter out methods with Ice context parameters
                int currentLength = current.getParameterTypes().length;
                int alreadyLength = already.getParameterTypes().length;
                if (alreadyLength < currentLength) {
                    rv.put(name, current);
                }
            }
        }
        return new ArrayList(rv.values());
    }

    /**
     * Determines the necessary arguments for the given method and calls with
     * random values to test the Ice mapping code.
     */
    void callWithFuzz(Object service, Method method) throws Exception {
        Type[] parameterTypes = method.getGenericParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            Type t = parameterTypes[i];
            parameters[i] = makeFuzz(method, t);
        }
        Mock mock = fixture.blitzMock(service.getClass());
        ArgumentsMatchBuilder builder = mock.expects(once()).method(
                method.getName());
        Class<?> returnClass = method.getReturnType();
        if (void.class.isAssignableFrom(returnClass)) {
            // nothing
        } else if (long.class.isAssignableFrom(returnClass)) {
            builder.will(returnValue(1L));
        } else if (int.class.isAssignableFrom(returnClass)) {
            builder.will(returnValue(1));
        } else if (double.class.isAssignableFrom(returnClass)) {
            builder.will(returnValue(0.0));
        } else if (float.class.isAssignableFrom(returnClass)) {
            builder.will(returnValue(0.0f));
        } else if (boolean.class.isAssignableFrom(returnClass)) {
            builder.will(returnValue(false));
        } else {
            builder.will(returnValue(null));
        }

        String msg = "Error running " + method + " with parameters "
                + Arrays.deepToString(parameters);
        try {
            method.invoke(service, parameters);
        } catch (InvocationTargetException ite) {
            Exception t = (Exception) ite.getCause();
            if (t instanceof omero.ApiUsageException) {
                // Ok. This means our fuzz was bad, but we can try to improve it
                omero.ApiUsageException aue = (omero.ApiUsageException) t;
                if (aue.message.contains("does not specify a valid class")) {
                    for (int i = 0; i < parameters.length; i++) {
                        if (parameters[i] instanceof String) {
                            parameters[i] = "Image";
                        }
                    }
                    // TODO
                }
            } else {
                throw new RuntimeException(msg, t);
            }
        } catch (IllegalArgumentException iae) {
            throw new RuntimeException(msg, iae);
        }

    }

    private Object makeFuzz(Method method, Type type)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {

        Class<?> t = null;
        ParameterizedType pt = null;
        GenericArrayType at = null;
        if (type instanceof ParameterizedType) {
            pt = (ParameterizedType) type;
            Type raw = pt.getRawType();
            if (raw instanceof GenericArrayType) {
                throw new RuntimeException(method.toString());
            } else {
                t = (Class<?>) raw;
            }
        } else if (type instanceof GenericArrayType) {
            at = (GenericArrayType) type;
        } else {
            t = (Class<?>) type;
        }

        try {

            Object v;
            if (at != null) {
                // Handle arrays
                Type componentType = at.getGenericComponentType();
                Class<?> componentClass = (Class<?>) componentType;
                v = Array.newInstance(componentClass, 0);
            } else if (long.class.isAssignableFrom(t)) {
                v = new Long(0);
            } else if (int.class.isAssignableFrom(t)) {
                v = new Integer(0);
            } else if (double.class.isAssignableFrom(t)) {
                v = new Double(0.0);
            } else if (float.class.isAssignableFrom(t)) {
                v = new Float(0.0);
            } else if (boolean.class.isAssignableFrom(t)) {
                v = Boolean.FALSE;
            } else if (Integer.class.isAssignableFrom(t)) {
                v = new Integer(0);
            } else if (Long.class.isAssignableFrom(t)) {
                v = new Long(0);
            } else if (List.class.isAssignableFrom(t)) {
                List l = new ArrayList<Object>();
                if (pt != null) {
                    java.lang.reflect.Type[] types = pt
                            .getActualTypeArguments();
                    java.lang.reflect.Type listType = types[0];
                    Class<?> typeClass = (Class<?>) listType;
                    try {
                        l.add(makeFuzz(method, listType));
                    } catch (Exception e) {
                        throw new RuntimeException("Error instantiating "
                                + typeClass, e);
                    }
                }
                v = l;
            } else if (Map.class.isAssignableFrom(t)) {
                v = new HashMap();
            } else if (omero.model.IObject.class.equals(t)) {
                // Picking a random IObjectI since IObject is abstract.
                v = new omero.model.FormatI();
            } else if (omero.model.Annotation.class.equals(t)) {
                v = new omero.model.BooleanAnnotationI();
            } else if (omero.model.Job.class.equals(t)) {
                v = new omero.model.ImportJobI();
            } else if (t.getName().startsWith("omero.model.")) {
                Class t2 = Class.forName(t.getName() + "I");
                v = t2.newInstance();
            } else {
                v = t.newInstance();
            }
            return v;
        } catch (InstantiationException ie) {
            throw new RuntimeException("Failed to instantiate a " + t.getName()
                    + " for method " + method, ie);
        }
    }
}
