/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.blitz.util;

import net.sf.ehcache.Ehcache;
import ome.system.OmeroContext;
import omero.InternalException;
import omero.ServerError;
import omero.util.IceMapper;

/**
 * Provides helper methods so that servant implementations need not extend a
 * particular {@link Class}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ServantHelper {

    /**
     * If a method has a void return type, then it is necessary to directly call
     * {@link #throwIfNecessary(Object)} on the value return by
     * {@link IceMethodInvoker#invoke(Object, Ice.Current, IceMapper, Object[])}
     * otherwise the appropriate exception will not get thrown.
     * 
     * @param obj
     * @throws Ice.UserException
     */
    public static void throwIfNecessary(Object obj) throws Ice.UserException {
        if (obj instanceof Ice.UserException) {
            throw (Ice.UserException) obj;
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
    public void checkVoid(Object obj) throws Ice.UserException {
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
    public <T> T returnValue(Class<T> c, Object obj) throws Ice.UserException {
        throwIfNecessary(obj);
        return c.cast(obj);
    }

}
