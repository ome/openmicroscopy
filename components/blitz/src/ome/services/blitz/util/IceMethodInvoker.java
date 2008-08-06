/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.blitz.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ome.api.ServiceInterface;
import ome.system.OmeroContext;
import omero.ServerError;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link Method}-cache primed either with an {@link ServiceInterface} instance
 * or with a {@link Class} with generic type {@link ServiceInterface}. Actual
 * invocation happens via
 * {@link #invoke(Object, Ice.Current, IceMapper, Object[])}
 * 
 * No reference is held to the initial priming argument.
 * 
 * MAPPING RULES:
 * <ul>
 * <li>Method names exact</li>
 * <li>Collections of the same type only (no arrays)</li>
 * <li>Primitivies use Ice primitives (long, int, bool,...)</li>
 * <li>Primitive wrapeprs all use RTypes (RLong, RInt, RBool,...)</li>
 * </ul>
 * 
 * Future:
 * <ul>
 * <li>Currently ignoring
 * 
 * @NotNull</li>
 *          </ul>
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class IceMethodInvoker {

    private static Log log = LogFactory.getLog(IceMethodInvoker.class);

    static class Info {
        Method method;

        Class[] params;

        Class retType;

        int[] switches;
    }

    private final Map<String, Info> map = new HashMap<String, Info>();

    private OmeroContext ctx;

    /**
     * Create an {@link IceMethodInvoker} instance using the {@link Class} of
     * the passed argument to call
     * {@link IceMethodInvoker#IceMethodInvoker(Class)}.
     * 
     * @param srv
     *            A Non-null {@link ServiceInterface} instance.
     * @param context
     *            The active {@link OmeroContext} instance.
     */
    public IceMethodInvoker(ServiceInterface srv, OmeroContext context) {
        this(srv.getClass(), context);
    }

    /**
     * Creates an {@link IceMethodInvoker} instance by using reflection on the
     * {@link Class} argument. All information is cached internally.
     * 
     * @param <S>
     *            A type which subclasses {@link ServiceInterface}
     * @param c
     *            A non-null {@link ServiceInterface} {@link Class}
     */
    public <S extends ServiceInterface> IceMethodInvoker(Class<S> c,
            OmeroContext context) {
        Method[] ms = c.getMethods();
        for (Method m : ms) {
            Info i = new Info();
            i.method = m;
            i.params = m.getParameterTypes();
            i.retType = m.getReturnType();
            map.put(m.getName(), i);
        }
        this.ctx = context;
    }

    /**
     * Calls the method named in {@link Ice.Current#operation} with the
     * arguments provided mapped via the {@link IceMapper} instance. The return
     * value or any method which is thrown is equally mapped and returned.
     * Clients of this method must check the return value for exceptions.
     * {@link ServantHelper#throwIfNecessary(Object)} does just this, but is
     * also called internally by {@link ServantHelper#checkVoid(Object)} and
     * {@link ServantHelper#returnValue(Class, Object)}.
     * 
     * @param obj
     *            Instance for the call to
     *            {@link Method#invoke(Object, Object[])}. Can be null if this
     *            is a static call.
     * @param current
     *            The current Ice operation. Non-null.
     * @param mapper
     *            A non-null mapper.
     * @param args
     *            The proper number of arguments for the method specified in
     *            current.
     * @return Either the return value of the invocation, or the exception if
     *         one was thrown.
     */
    public Object invoke(Object obj, Ice.Current current, IceMapper mapper,
            Object... args) throws ServerError {

        Info info = map.get(current.operation);
        if (info == null) {
            throw new IllegalArgumentException("Unknown method:"
                    + current.operation);
        }

        // Alias
        Class[] params = info.params;

        if (params.length != args.length) {
            throw new IllegalArgumentException("Must provide " + params.length
                    + " arguments for " + current.operation);
        }

        // The Mapped argument parameters to be passed to the
        // ServiceInterface instance.
        Object[] objs = new Object[params.length];

        // be sure to use our own types
        for (int i = 0; i < params.length; i++) {
            Class p = params[i];
            Object arg = args[i];
            objs[i] = mapper.handleInput(p, arg);
            // This check duplicates what should be in handleInput
            // if (null != objs[i] && !isPrimitive(p) && // FIXME need way to
            // check autoboxing.
            // !p.isAssignableFrom(objs[i].getClass())) {
            // throw new IllegalStateException(String.format(
            // "Cannot assign %s to %s",objs[i],p));
            // }
        }

        Class retType = info.retType;
        Object retVal = null;
        try {

            // To replicate the lifecycle logic of the application server,
            // it's necessary to catch all calls to "close()" (which is also
            // done within the Hibernate SessionHandler), and ALSO call the
            // "destroy()" method if present. TODO This could be much better
            // placed, but this location is sufficient, since no call will
            // be made on the delegation targets without going through this
            // method.
            //
            // Unfortunately, however, the destroy method is not on the
            // interface and so must be checked directly.
            if ("close".equals(info.method.getName())) {
                Method destroy = null;
                try {
                    destroy = obj.getClass().getMethod("destroy");
                } catch (Exception e) {
                    // No problems. Can't call method then.
                }
                if (destroy != null) {
                    try {
                        destroy.invoke(obj);
                    } catch (Exception ex) {
                        log.error("Exception on service.destroy()", ex);
                    }
                }
                UnregisterServantMessage usm = new UnregisterServantMessage(
                        this, Ice.Util.identityToString(current.id), current);
                ctx.publishMessage(usm);
            } else {
                // Here we are skipping the close() since there is currently
                // no logic in any of them. This is also essentially a HACK
                // and should be re
                retVal = info.method.invoke(obj, objs);
            }
        } catch (Throwable t) {
            return handleException(t);
        }

        // Handling case of generics (.e.g Search.next())
        // in which case we cannot properly handle the mapping.
        if (retType == Object.class && retVal != null) {
            retType = retVal.getClass();
        }
        return mapper.handleOutput(retType, retVal);
    }

    /** For testing the cached method. */
    public Method getMethod(String name) {
        return map.get(name).method;
    }

    // ~ Helpers
    // =========================================================================

    public Ice.UserException handleException(Throwable t) {

        // Getting rid of the reflection wrapper.
        if (InvocationTargetException.class.isAssignableFrom(t.getClass())) {
            t = t.getCause();
        }

        if (log.isInfoEnabled()) {
            log.info("Handling:", t);
        }

        // First we give registered handlers a chance to convert the message,
        // if that doesn't succeed, then we try either manually, or just
        // wrap the exception in an InternalException
        try {
            ConvertToBlitzExceptionMessage ctbem = new ConvertToBlitzExceptionMessage(
                    this, t);
            ctx.publishMessage(ctbem);
            if (ctbem.to != null) {
                t = ctbem.to;
            }
        } catch (Throwable handlerT) {
            // Logging the output, but we shouldn't worry the user
            // with a failing handler
            log.error("Exception handler failure", handlerT);
        }

        Class c = t.getClass();
        if (Ice.UserException.class.isAssignableFrom(c)) {
            return (Ice.UserException) t;
        } else if (ome.conditions.ValidationException.class.isAssignableFrom(c)) {
            omero.ValidationException ve = new omero.ValidationException();
            return IceMapper.fillServerError(ve, t);
        } else if (ome.conditions.ApiUsageException.class.isAssignableFrom(c)) {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            return IceMapper.fillServerError(aue, t);
        } else if (ome.conditions.SecurityViolation.class.isAssignableFrom(c)) {
            omero.SecurityViolation sv = new omero.SecurityViolation();
            return IceMapper.fillServerError(sv, t);
        } else if (ome.conditions.OptimisticLockException.class
                .isAssignableFrom(c)) {
            omero.OptimisticLockException ole = new omero.OptimisticLockException();
            return IceMapper.fillServerError(ole, t);
        } else if (ome.conditions.ResourceError.class.isAssignableFrom(c)) {
            omero.ResourceError re = new omero.ResourceError();
            return IceMapper.fillServerError(re, t);
        } else if (ome.conditions.RemovedSessionException.class.isAssignableFrom(c)) {
            omero.RemovedSessionException rse = new omero.RemovedSessionException();
            return IceMapper.fillServerError(rse, t);
        } else if (ome.conditions.SessionTimeoutException.class.isAssignableFrom(c)) {
            omero.SessionTimeoutException ste = new omero.SessionTimeoutException();
            return IceMapper.fillServerError(ste, t);
        } else if (ome.conditions.InternalException.class.isAssignableFrom(c)) {
            omero.InternalException ie = new omero.InternalException();
            return IceMapper.fillServerError(ie, t);
        } else if (ome.conditions.AuthenticationException.class.isAssignableFrom(c)) {
            // not an omero.ServerError()
            omero.AuthenticationException ae = new omero.AuthenticationException(t.getMessage());
            return ae;
        } else if (ome.conditions.ExpiredCredentialException.class.isAssignableFrom(c)) {
            // not an omero.ServerError()
            omero.ExpiredCredentialException ece = new omero.ExpiredCredentialException(t.getMessage());
            return ece;
        } else if (ome.conditions.RootException.class.isAssignableFrom(c)) {
            // Not returning but logging error message.
            log
                    .error("RootException thrown which is an unknown subclasss.\n"
                            + "This most likely means that an exception was added to the\n"
                            + "ome.conditions hierarchy, without being accountd for in blitz:\n"
                            + c.getName());
        }

        // Catch all in case above did not return
        omero.InternalException ie = new omero.InternalException();
        return IceMapper.fillServerError(ie, t);

    }
}
