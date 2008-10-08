/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.throttling;

import java.lang.reflect.Method;

import omero.InternalException;
import omero.util.IceMapper;

/**
 * Simple base task which contains logic for routing calls reflectively to
 * ice_response and ice_exception of any AMD callback.
 * 
 * @since Beta4
 */
public abstract class Task implements Runnable {

    protected final Object cb;

    protected final boolean isVoid;

    protected final Ice.Current current;

    protected final Method response;

    protected final Method exception;

    public Task(Object callback, Ice.Current current, boolean isVoid) {
        this.current = current;
        this.isVoid = isVoid;
        this.cb = callback;
        if (callback != null) {
            response = getMethod(callback, "ice_response");
            exception = getMethod(callback, "ice_exception");
        } else {
            response = null;
            exception = null;
        }
    }

    public abstract void run();

    /**
     * Calls the response method
     */
    protected void response(Object rv) {
        try {
            if (isVoid) {
                response.invoke(cb);
            } else {
                response.invoke(cb, rv);
            }
        } catch (Exception e) {
            try {
                InternalException ie = new InternalException();
                IceMapper.fillServerError(ie, e);
                ie.message = "Failed to invoke: " + this.toString();
                exception(ie);
            } catch (Exception e2) {
                throw new RuntimeException(
                        "Failed to invoke exception() after failed response()",
                        e2);
            }
        }
    }

    protected void exception(Exception ex) {
        try {
            exception.invoke(cb, ex);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke exception()", e);
        }
    }

    // Helpers
    // =========================================================================

    Method getMethod(Object o, String methodName) {
        Class c = getPublicInterface(o.getClass());
        Method[] methods = c.getMethods();
        Method rv = null;
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (methodName.equals(m.getName())) {
                if (rv != null) {
                    throw new RuntimeException(methodName + " exists twice!");
                } else {
                    rv = m;
                }
            }
        }
        return rv;
    }

    /**
     * The Ice AMD-implementations are package-private and so cannot be executed
     * on. Instead, we have to find the public interface and use its methods.
     */
    private Class getPublicInterface(Class c) {
        if (!c.getName().startsWith("AMD_")) {
            while (!c.equals(Object.class)) {
                Class[] ifaces = c.getInterfaces();
                for (Class c2 : ifaces) {
                    if (c2.getSimpleName().startsWith("AMD_")) {
                        return c2;
                    }
                }
                // Ok. We didn't find anything so recurse into the superclass
                c = c.getSuperclass();
            }
            throw new RuntimeException("No public AMD_ interface found.");
        } else {
            return c;
        }
    }
}