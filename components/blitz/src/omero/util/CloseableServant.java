/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.util;

/**
 * Marker interface which distinguishes a servant which wants to have
 * {@link #close(Ice.Current)} called on
 * {@link ome.services.blitz.impl.ServiceFactoryI#doDestroy()}
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 */
public interface CloseableServant {

    void close(Ice.Current current) throws Exception;

}
