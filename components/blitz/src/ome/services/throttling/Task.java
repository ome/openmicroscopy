/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.throttling;

import java.lang.reflect.Method;

import ome.system.OmeroContext;
import omero.InternalException;
import omero.ServerError;
import omero.util.IceMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple base task which contains logic for routing calls reflectively to
 * ice_response and ice_exception of any AMD callback.
 * 
 * @since Beta4
 */
public abstract class Task {

    private final static Logger log = LoggerFactory.getLogger(Task.class);

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

    public abstract void run(OmeroContext ctx);

    /**
     * Calls the response method
     */
    protected void response(Object rv, OmeroContext ctx) {
        try {
            if (isVoid) {
                response.invoke(cb);
            } else {
                response.invoke(cb, postProcess(rv));
            }
        } catch (Exception e) {
            InternalException ie = new InternalException();
            IceMapper.fillServerError(ie, e);
            ie.message = "Failed to invoke: " + this.toString();
            log.error(ie.message, e);
            exception(ie, ctx);
        }
    }

    /**
     * Can be overridden to transform the return value from the async method.
     * This implementation leaves the return value unchanged.
     * @param rv a return value
     * @return the return value transformed
     * @throws ServerError if the transformation failed
     */
    protected Object postProcess(Object rv) throws ServerError {
        return rv;
    }

    protected void exception(Throwable ex, OmeroContext ctx) {
        try {
            if (!(ex instanceof Exception)) {
                log.error("Throwable thrown!", ex);
            }
            IceMapper mapper = new IceMapper();
            ex = mapper.handleException(ex, ctx);
            exception.invoke(cb, ex);
        } catch (Exception e2) {
            String msg = "Failed to invoke exception()";
            log.error(msg, e2);
            throw new RuntimeException("Failed to invoke exception()", e2);
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" (");
        sb.append(cb);
        sb.append(" )");
        return sb.toString();
    }

}
