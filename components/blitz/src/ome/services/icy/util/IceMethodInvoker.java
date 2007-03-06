package ome.services.icy.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.ModelBased;
import ome.api.ServiceInterface;
import ome.model.IObject;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.system.EventContext;
import ome.system.Roles;
import ome.util.Filterable;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.data.PlaneDef;
import omero.RType;
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
 *  <li>Method names exact</li>
 *  <li>Collections of the same type only (no arrays)</li>
 *  <li>Primitivies use Ice primitives (long, int, bool,...)</li>
 *  <li>Primitive wrapeprs all use RTypes (RLong, RInt, RBool,...)</li>
 * </ul>
 * 
 * Future:
 * <ul>
 *  <li>Currently ignoring @NotNull</li>
 * </ul>
 * 
 * @author josh
 * 
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

    /**
     * Create an {@link IceMethodInvoker} instance using the {@link Class} of
     * the passed argument to call
     * {@link IceMethodInvoker#IceMethodInvoker(Class)}.
     * 
     * @param srv
     *            A Non-null {@link ServiceInterface} instance.
     */
    public IceMethodInvoker(ServiceInterface srv) {
        this(srv.getClass());
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
    public <S extends ServiceInterface> IceMethodInvoker(Class<S> c) {
        Method[] ms = c.getMethods();
        for (Method m : ms) {
            Info i = new Info();
            i.method = m;
            i.params = m.getParameterTypes();
            i.retType = m.getReturnType();
            map.put(m.getName(), i);
        }
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
            objs[i] = handleInput(mapper, p, arg);
            if (null != objs[i] && !isPrimitive(p) && // FIXME need way to check autoboxing.
                    !p.isAssignableFrom(objs[i].getClass())) {
                throw new IllegalStateException(String.format(
                        "Cannot assign %s to %s",objs[i],p));
            }
        }

        Class retType = info.retType;
        Object retVal;
        try {
            retVal = info.method.invoke(obj, objs);
        } catch (Throwable t) {
            return handleException(t);
        }

        return handleOutput(mapper, retType, retVal);
    }

    /** For testing the cached method. */
    public Method getMethod(String name) {
        return map.get(name).method;
    }

    // ~ Helpers
    // =========================================================================

    protected boolean isPrimitive(Class p) {
        if (p.equals(byte.class) || p.equals(byte[].class)
                || p.equals(int.class) || p.equals(int[].class)
                || p.equals(long.class) || p.equals(long[].class)
                || p.equals(double.class) || p.equals(double[].class)
                || p.equals(float.class) || p.equals(float[].class)
                || p.equals(boolean.class) || p.equals(boolean[].class)) {
            return true;
        }
        return false;
    }

    protected Object handleInput(IceMapper mapper, Class p, Object arg) 
    throws ServerError {
        if (arg instanceof RType) {
            RType rt = (RType) arg;
            return mapper.convert(rt);
        } else if (isPrimitive(p)) { // FIXME use findTarget for Immutable.
            return arg;
        } else if (p.equals(Long.class)) {
            return arg;
        } else if (p.equals(Integer.class)) {
            return arg;
        } else if (p.equals(Double.class)) {
            return arg;
        } else if (p.equals(Float.class)) {
            return arg;
        } else if (p.equals(String.class)) {
            return arg;
        } else if (p.equals(Class.class)) {
            return mapper.omeroClass((String) arg, true);
        } else if (ome.model.IObject.class.isAssignableFrom(p)) {
            return mapper.reverse((ModelBased) arg);
        } else if (p.equals(Filter.class)) {
            return mapper.convert((omero.sys.Filter) arg);
        } else if (p.equals(Parameters.class)) {
            return mapper.convert((omero.sys.Parameters) arg);
        } else if (List.class.isAssignableFrom(p)) {
            return mapper.reverse(new HashSet((List) arg)); // Necessary since Ice doesn't support Sets.
        } else if (Set.class.isAssignableFrom(p)) {
            return mapper.reverse((Collection) arg);
        } else if (Map.class.isAssignableFrom(p)) {
            return mapper.reverse((Map) arg);
        } else if (PlaneDef.class.isAssignableFrom(p)) {
            return mapper.convert((omero.romio.PlaneDef) arg);
        } else {
            throw new IllegalStateException("Can't handle input " + p);
        }
    }

    protected Object handleOutput(IceMapper mapper, Class type, Object o) {
        if (void.class.isAssignableFrom(type)) {
            assert o == null;
            return o;
        } else if (isPrimitive(type)) {
            return o;
        } else if (RGBBuffer.class.isAssignableFrom(type)) {
            return mapper.convert((RGBBuffer)o);
        } else if (Roles.class.isAssignableFrom(type)) {
            return mapper.convert((Roles)o);
        } else if (Date.class.isAssignableFrom(type)) {
            return mapper.convert((Date)o);
        } else if (EventContext.class.isAssignableFrom(type)) {
            return mapper.convert((EventContext)o);
        } else if (Collection.class.isAssignableFrom(type)) {
            return mapper.map((Collection) o);
        } else if (IObject.class.isAssignableFrom(type)) {
            return mapper.map((Filterable) o);
        } else if (Map.class.isAssignableFrom(type)) {
            return mapper.map((Map) o);
        } else {
            throw new IllegalStateException("Can't handle output " + type);
        }
    }

    protected Ice.UserException handleException(Throwable t) {

        if (log.isInfoEnabled()) {
            log.info("Handling:", t);
        }

        Class c = t.getClass();
        if (ome.conditions.ValidationException.class.isAssignableFrom(c)) {
            omero.ValidationException ve = new omero.ValidationException();
            return fillServerError(ve, t);
        } else if (ome.conditions.ApiUsageException.class.isAssignableFrom(c)) {
            omero.ApiUsageException aue = new omero.ApiUsageException();
            return fillServerError(aue, t);
        } else if (ome.conditions.SecurityViolation.class.isAssignableFrom(c)) {
            omero.SecurityViolation sv = new omero.SecurityViolation();
            return fillServerError(sv, t);
        } else if (ome.conditions.OptimisticLockException.class
                .isAssignableFrom(c)) {
            omero.OptimisticLockException ole = new omero.OptimisticLockException();
            return fillServerError(ole, t);
        } else if (ome.conditions.ResourceError.class.isAssignableFrom(c)) {
            omero.ResourceError re = new omero.ResourceError();
            return fillServerError(re, t);
        } else {
            omero.InternalException ie = new omero.InternalException();
            return fillServerError(ie, t);
        }
    }

    protected ServerError fillServerError(ServerError se, Throwable t) {
        se.message = t.getMessage();
        se.serverExceptionClass = t.getClass().getName();
        se.serverStackTrace = stackAsString(t);
        return se;
    }

    protected String stackAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        pw.close();
        
        return sw.getBuffer().toString();
    }

}