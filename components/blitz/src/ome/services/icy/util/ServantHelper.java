package ome.services.icy.util;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import ome.api.ServiceInterface;
import ome.system.OmeroContext;
import ome.util.Filterable;
import omero.InternalException;
import omero.RType;
import omero.ServerError;
import omero.model.IObject;
import omero.util.IceMapper;

/**
 * Provides helper methods so that servant implementations need not extend a
 * particular {@link Class}.
 * 
 * @author josh
 * 
 */
public class ServantHelper {

    OmeroContext ctx;

    Ehcache cache;

    public ServantHelper(OmeroContext context, Ehcache cache) {
        this.ctx = context;
        this.cache = cache;
    }

    public ServiceInterface getService(String key, Ice.Current current)
            throws ServerError {
        Element elt = cache.get(key);
        if (elt == null) {
            ctx.publishEvent(new UnregisterServantMessage(this, key, current));
            ServerError se = new ServerError(); // FIXME
            se.message = "This service has been removed. All further calls to this proxy will fail.";
            throw se;
        }
        return (ServiceInterface) elt.getObjectValue();
    }

    /**
     * If a method has a void return type, then it is necessary to directly call
     * {@link #throwIfNecessary(Object)} on the value return by
     * {@link IceMethodInvoker#invoke(Object, Ice.Current, IceMapper, Object[])}
     * otherwise the appropriate exception will not get thrown.
     * 
     * @param obj
     * @throws ServerError
     */
    public static void throwIfNecessary(Object obj) throws ServerError {
        if (obj instanceof ServerError) {
            throw (ServerError) obj;
        }
    }

    /**
     * Ensures that the return value is null for a void call, throwing
     * {@link InternalException} otherwise.
     * 
     * Calls {@link #throwIfNecessary(Object)} on the passed in {@link Object}
     * argument, so that the result of a call to
     * {@link IceMethodInvoker#invoke()} can be passed directly in.
     * 
     * @param obj
     * @param mapper
     * @return
     */
    public void checkVoid(Object obj) throws ServerError {
        throwIfNecessary(obj);
        if (obj != null) {
            InternalException ie = new InternalException();
            ie.message = "Non-null value returned by void method.";
            throw ie;
        }
    }

    /**
     * Casts an object to the given type, or throws a {@link ClassCastException}
     * if not possible.
     * 
     * Calls {@link #throwIfNecessary(Object)} on the passed in {@link Object}
     * argument, so that the result of a call to
     * {@link IceMethodInvoker#invoke()} can be passed directly in.
     * 
     * @param obj
     * @param mapper
     * @return
     */
    public <T> T returnValue(Class<T> c, Object obj) throws ServerError {
        throwIfNecessary(obj);
        return c.cast(obj);
    }

}